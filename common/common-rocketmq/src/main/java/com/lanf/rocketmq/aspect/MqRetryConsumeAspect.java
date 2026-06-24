package com.lanf.rocketmq.aspect;

import com.lanf.common.utils.JsonUtils;
import com.lanf.constant.mq.base.BaseMessage;
import com.lanf.constant.utils.MessageLevelUtils;
import com.lanf.constant.utils.TraceIdUtils;
import com.lanf.rocketmq.annotation.MqRetryConsume;
import com.lanf.rocketmq.exception.MessageRetryConsumeException;
import com.lanf.rocketmq.model.entity.MqConsumeMessageDO;
import com.lanf.rocketmq.sevice.IMqConsumeMessageService;
import com.lanf.rocketmq.sevice.MqConsumeRetryService;
import com.lanf.rocketmq.sevice.MqRetryReflectExecutor;
import com.lanf.rocketmq.sevice.MqRetryStrategy;
import io.netty.util.HashedWheelTimer;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * MQ重试消费AOP切面
 * <p>扫描 @MqRetryConsume 注解，实现幂等控制、状态管理和失败重试</p>
 * <p>核心流程：</p>
 * <p>1. 拦截带有 @MqRetryConsume 注解的MQ消费方法</p>
 * <p>2. 通过SpEL解析消息ID，并拼接group确保跨消费组唯一性</p>
 * <p>3. 基于Redisson分布式锁实现幂等控制，同一消息同一时间只允许一个实例消费</p>
 * <p>4. 记录消费状态到mq_consume_message表，实现本地事务消息管理</p>
 * <p>5. 消费成功更新状态，消费失败则判断异常类型</p>
 * <p>6. 如果是MessageRetryConsumeException，则加入HashedWheelTimer延迟重试队列</p>
 * <p>7. 重试时通过反射重新执行原方法，绕过AOP代理避免循环</p>
 * <p>设计亮点：分布式锁+去重容器双重保障；18级阶梯延迟策略；反射绕过代理避免递归；独立线程池隔离</p>
 */
@Slf4j
@Aspect
@Component
public class MqRetryConsumeAspect {

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private IMqConsumeMessageService mqLocalTransactionMessageService;


    @Autowired
    private MqRetryReflectExecutor mqRetryReflectExecutor;

    @Autowired
    private MqConsumeRetryService mqConsumeRetryService;

    @Autowired
    @Qualifier("mqConsumeRetrySendExecutor")
    private Executor mqRetrySendExecutor;

    @Autowired
    private ApplicationContext applicationContext;

    private final SpelExpressionParser spelExpressionParser = new SpelExpressionParser();
    private final ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();
    private final HashedWheelTimer timer = new HashedWheelTimer(100, TimeUnit.MILLISECONDS, 512);

    /**
     * 最大重试任务数（队列中同时存在的最大任务数）
     */
    private static final int MAX_RETRY_TASK_COUNT = 10000;

    /**
     * 当前重试任务数统计
     */
    private final AtomicInteger retryTaskCount = new AtomicInteger(0);

    /**
     * 去重容器：已加入重试队列的消息ID（MqConsumeMessageDO.id）
     */
    private final Set<String> retryMessageIdSet = ConcurrentHashMap.newKeySet();

    /**
     * SpEL表达式前缀
     */
    private static final String SPEL_PREFIX = "#";

    /**
     * 分布式锁前缀
     */
    private static final String LOCK_PREFIX = "mq:consume:";

    /**
     * AOP环绕通知核心方法
     * <p>拦截所有带有 @MqRetryConsume 注解的方法，实现幂等控制与失败重试</p>
     *
     * @param joinPoint       AOP连接点，包含目标方法、参数、类等信息
     * @param mqRetryConsume  注解对象，包含messageId表达式和重试策略
     * @return 目标方法执行结果，失败返回null
     * @throws Throwable 执行过程中的异常
     */
    @Around("@annotation(mqRetryConsume)")
    public Object around(ProceedingJoinPoint joinPoint, MqRetryConsume mqRetryConsume) throws Throwable {
        //1.解析消息ID（支持SpEL表达式，如"#message.id"）
        String messageId = parseMessageId(joinPoint, mqRetryConsume.messageId());
        if (messageId == null || messageId.isEmpty()) {
            log.error("消息ID解析失败，跳过消费");
            return null;
        }

        //2.从方法参数中提取traceId和messageLevel，用于链路追踪
        String traceId = null;
        Integer messageLevel = null;
        Object[] args = joinPoint.getArgs();
        if (args != null && args.length > 0) {
            Object arg = args[0];
            if (arg instanceof BaseMessage) {
                traceId = ((BaseMessage) arg).getTraceId();
                messageLevel = ((BaseMessage) arg).getLevel();
            }
        }
        TraceIdUtils.setTraceId(traceId);
        if (messageLevel != null) {
            MessageLevelUtils.setLevel(messageLevel + 1);
        }

        //3.从目标类上的@RocketMQMessageListener注解反射获取topic和group
        String topic = null;
        String group = null;
        Class<?> targetClass = joinPoint.getTarget().getClass();
        org.apache.rocketmq.spring.annotation.RocketMQMessageListener rocketMQMessageListener =
                targetClass.getAnnotation(org.apache.rocketmq.spring.annotation.RocketMQMessageListener.class);
        if (rocketMQMessageListener != null) {
            topic = rocketMQMessageListener.topic();
            group = rocketMQMessageListener.consumerGroup();
        }

        //4.拼接messageId = messageId + ":" + group，确保跨消费组的消息唯一性
        if (group != null && !group.isEmpty()) {
            messageId = messageId +":"+ group;
        }

        String lockKey = LOCK_PREFIX + messageId;
        RLock lock = redissonClient.getLock(lockKey);

        //5.获取Redisson分布式锁，失败直接return（幂等拦截，同一消息同一时间只允许一个实例消费）
        if (!lock.tryLock()) {
            log.warn("获取分布式锁失败，跳过消费，messageId:{}", messageId);
            return null;
        }

        try {
            //6.查询或创建消费记录，实现本地事务消息管理
            MqConsumeMessageDO messageDO = mqLocalTransactionMessageService.getByMessageId(messageId);
            if (messageDO == null) {
                //6.1 消息首次消费，创建消费记录并持久化到DB
                messageDO = createMessageRecord(joinPoint, mqRetryConsume, messageId, topic, group);
                mqLocalTransactionMessageService.save(messageDO);
            } else if (messageDO.getStatus() != null && messageDO.getStatus() == 1) {
                //6.2 消息已消费成功，幂等跳过
                log.info("消息已消费成功，跳过，messageId:{}", messageId);
                return null;
            }

            //7.执行目标业务方法
            try {
                Object result = joinPoint.proceed();

                //8.执行完成，更新消息状态为消费成功（status=1）
                messageDO.setStatus(1);
                messageDO.setErrorMsg(null);
                mqLocalTransactionMessageService.updateById(messageDO);

                log.info("MQ消息消费成功，messageId:{}", messageId);
                return result;
            } catch (Exception e) {
                //9.消费失败，根据异常类型决定处理方式
                log.error("MQ消息消费失败，准备延迟重试，messageId:{}", messageId, e);

                //9.1 如果是MessageRetryConsumeException，加入延迟重试队列
                if (isRetryException(e)) {
                    mqConsumeRetryService.addToRetryQueue(messageDO);
                } else {
                    //9.2 非预期异常，直接钉钉告警，不再重试
                    log.error("【钉钉告警】MQ消息消费超过最大重试次数，messageId:{}",
                            messageDO.getMessageId());
                }
                return null;
            }
        } finally {
            //10.释放分布式锁并清理链路追踪上下文
            lock.unlock();
            TraceIdUtils.clearAll();
            MessageLevelUtils.clear();
        }
    }

    /**
     * 解析消息ID（支持SpEL表达式）
     * <p>当messageId以"#"开头时，使用Spring SpEL引擎解析方法参数中的变量值</p>
     * <p>例如："#message.id" 表示从第一个参数message对象中获取id属性</p>
     *
     * @param joinPoint           AOP连接点，用于获取方法签名和参数
     * @param messageIdExpression 消息ID表达式，支持SpEL（如"#message.id"）或常量字符串
     * @return 解析后的消息ID字符串，解析失败返回null
     */
    private String parseMessageId(ProceedingJoinPoint joinPoint, String messageIdExpression) {
        //1.空值校验
        if (messageIdExpression == null || messageIdExpression.isEmpty()) {
            return null;
        }

        //2.非SpEL表达式（不以"#"开头），直接返回常量值
        if (!messageIdExpression.startsWith(SPEL_PREFIX)) {
            return messageIdExpression;
        }

        //3.使用Spring SpEL解析表达式
        try {
            MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
            Method method = methodSignature.getMethod();
            String[] paramNames = parameterNameDiscoverer.getParameterNames(method);
            Object[] args = joinPoint.getArgs();

            //4.构建SpEL执行上下文，注入方法参数
            StandardEvaluationContext context = new StandardEvaluationContext();
            if (paramNames != null) {
                for (int i = 0; i < paramNames.length; i++) {
                    context.setVariable(paramNames[i], args[i]);
                }
            }

            //5.解析并返回结果
            return spelExpressionParser.parseExpression(messageIdExpression).getValue(context, String.class);
        } catch (Exception e) {
            log.error("解析消息ID失败，expression:{}", messageIdExpression, e);
            return null;
        }
    }

    /**
     * 创建消息消费记录
     * <p>将消息元数据、方法信息、参数信息持久化到mq_consume_message表</p>
     * <p>便于后续重试时通过反射重新调用原方法</p>
     *
     * @param joinPoint      AOP连接点，用于获取方法签名和参数
     * @param mqRetryConsume 注解对象，包含重试策略配置
     * @param messageId      消息唯一标识（已拼接group）
     * @param topic          RocketMQ Topic名称
     * @param group          消费组名称
     * @return 初始化后的消息消费记录实体
     */
    private MqConsumeMessageDO createMessageRecord(ProceedingJoinPoint joinPoint, MqRetryConsume mqRetryConsume, String messageId, String topic, String group) {
        //1.获取方法签名和重试策略
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();
        Class<? extends MqRetryStrategy> aClass = mqRetryConsume.retryStrategy();
        MqRetryStrategy retryStrategy = applicationContext.getBean(aClass);

        //2.计算首次预计完成时间（当前时间 + 第一次重试延迟）
        Date nextEstimatedCompletionAt = new Date(System.currentTimeMillis() +
                retryStrategy.getDelayMillis(1));

        //3.构建消费记录实体
        MqConsumeMessageDO messageDO = new MqConsumeMessageDO();
        messageDO.setMessageId(messageId);
        messageDO.setTopic(topic);
        messageDO.setGroup(group);
        messageDO.setStatus(0); //0=待消费
        messageDO.setRetryCount(0);
        messageDO.setMaxRetryCount(retryStrategy.maxRetryCount());
        messageDO.setClassName(method.getDeclaringClass().getName());
        messageDO.setMethodName(method.getName());
        messageDO.setRetryStrategyBeanClass(aClass.getName());
        messageDO.setNextEstimatedCompletionAt(nextEstimatedCompletionAt);

        //4.记录参数类型JSON，用于反射时解析参数类型
        Class<?>[] parameterTypes = method.getParameterTypes();
        String[] paramTypeNames = new String[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            paramTypeNames[i] = parameterTypes[i].getName();
        }
        messageDO.setParamTypes(JsonUtils.toJsonString(paramTypeNames));

        //5.记录参数值JSON，用于反射时恢复参数对象
        Object[] args = joinPoint.getArgs();
        messageDO.setParamValues(JsonUtils.toJsonString(args));

        return messageDO;
    }

    /**
     * 发送到重试队列（HashedWheelTimer延迟执行）
     * <p>将失败的消息加入内存延迟队列，按策略指定的延迟时间后执行重试</p>
     * <p>设计亮点：去重容器+任务计数器双重保障，防止重复入队和内存溢出</p>
     *
     * @param messageDO 消息消费记录实体
     */
    private void sendToRetryQueue(MqConsumeMessageDO messageDO) {
        String messageId = messageDO.getMessageId();

        //1.去重检查：防止同一消息在短时间内重复入队
        if (!retryMessageIdSet.add(messageId)) {
            log.warn("消息已在重试队列中，跳过重复添加，mqConsumeMessageId:{}", messageId);
            return;
        }

        //2.任务数上限检查：防止内存溢出，保护系统稳定性
        int currentCount = retryTaskCount.incrementAndGet();
        if (currentCount > MAX_RETRY_TASK_COUNT) {
            retryTaskCount.decrementAndGet();
            retryMessageIdSet.remove(messageId);
            log.warn("重试任务数已达上限{}/{}，跳过添加，mqConsumeMessageId:{}", currentCount, MAX_RETRY_TASK_COUNT, messageId);
            return;
        }

        //3.从Spring容器中获取重试策略
        String retryStrategyBeanClass = messageDO.getRetryStrategyBeanClass();
        Class<?> aClass = null;
        try {
            aClass = Class.forName(retryStrategyBeanClass);
        } catch (ClassNotFoundException e) {
            log.error("【钉钉告警】重试策略类加载失败，messageId:{}",
                    messageDO.getMessageId());
            return;
        }

        MqRetryStrategy mqRetryStrategy = (MqRetryStrategy) applicationContext.getBean(aClass);
        int retryCount = messageDO.getRetryCount() + 1;

        //4.校验是否超过最大重试次数
        if (retryCount > mqRetryStrategy.maxRetryCount()) {
            log.error("【钉钉告警】MQ消息消费超过最大重试次数，messageId:{}, retryCount:{}",
                    messageDO.getMessageId(), retryCount);
            // TODO: 调用钉钉Webhook API 发送告警
            return;
        }

        //5.计算延迟时间并加入HashedWheelTimer延迟队列
        long delayMillis = mqRetryStrategy.getDelayMillis(retryCount);
        log.info("消息加入重试队列，messageId:{}, mqConsumeMessageId:{}, retryCount:{}, delay:{}ms",
                messageDO.getMessageId(), messageId, retryCount, delayMillis);
        timer.newTimeout(timeout -> {
            mqRetrySendExecutor.execute(() -> doRetry(messageDO, retryCount, mqRetryStrategy));
        }, delayMillis, TimeUnit.MILLISECONDS);
    }

    /**
     * 执行重试
     * <p>通过反射重新调用原消费方法，并更新消费状态</p>
     * <p>重试失败时会递归加入延迟队列，实现自动重试</p>
     *
     * @param messageDO        消息消费记录实体
     * @param retryCount       当前重试次数
     * @param mqRetryStrategy  重试策略，用于计算下次延迟时间
     */
    private void doRetry(MqConsumeMessageDO messageDO, int retryCount, MqRetryStrategy mqRetryStrategy) {
        //1.以messageId作为分布式锁key，防止同一消息并发重试
        String messageId = messageDO.getMessageId();
        String lockKey = LOCK_PREFIX + messageId;
        RLock lock = redissonClient.getLock(lockKey);
        boolean locked = false;
        try {
            locked = lock.tryLock();
            if (!locked) {
                log.warn("获取分布式锁失败，跳过本次重试，messageId:{}", messageDO.getMessageId());
                return;
            }

            //2.重新从DB查询最新状态，防止脏数据
            messageDO = mqLocalTransactionMessageService.getByMessageId(messageDO.getMessageId());
            if (messageDO.getRetryCount() > mqRetryStrategy.maxRetryCount()){
               log.error("钉钉告警");
               return;
            }

            //3.更新重试次数和预计完成时间
            Date nextEstimatedCompletionAt = new Date(System.currentTimeMillis() +
                    mqRetryStrategy.getDelayMillis(retryCount+1));
            messageDO.setRetryCount(retryCount);
            messageDO.setNextEstimatedCompletionAt(nextEstimatedCompletionAt);
            if (messageDO.getMaxRetryCount().equals(retryCount)){
                messageDO.setStatus(2); //2=消费失败
            }
            mqLocalTransactionMessageService.updateById(messageDO);

            //4.通过反射重新执行原方法（绕过AOP代理，避免循环递归）
            mqRetryReflectExecutor.execute(messageDO);

            //5.反射执行成功，更新状态为消费成功
            messageDO.setStatus(1);
            messageDO.setErrorMsg(null);
            mqLocalTransactionMessageService.updateById(messageDO);

            log.info("MQ消息重试消费成功，messageId:{}, , retryCount:{}",
                    messageDO.getMessageId(),  retryCount);
        } catch (Exception e) {
            //6.重试失败，递归加入重试队列
            log.error("MQ消息第{}次重试失败，messageId:{},", retryCount, messageDO.getMessageId(), e);
            // 第一次执行时如果发生业务异常，不会进入重试队列，所以重试阶段接受任意异常
            MqConsumeMessageDO freshMessage = mqLocalTransactionMessageService.getByMessageId(messageDO.getMessageId());
            if (freshMessage != null) {
                //6.1 先从去重容器中移除，否则sendToRetryQueue会判定为重复
                retryMessageIdSet.remove(messageId);
                int remain = retryTaskCount.decrementAndGet();
                log.info("准备再次入队，先清除当前去重标识，messageId:{}, 剩余任务数:{}", messageId, remain);
                //6.2 递归加入延迟重试队列
                sendToRetryQueue(freshMessage);
            }
        } finally {
            //7.任务执行完成后，清理去重标识、减少任务数、释放分布式锁
            retryMessageIdSet.remove(messageId);
            int remain = retryTaskCount.decrementAndGet();
            log.info("消费重试任务执行完成，mqConsumeMessageId:{}, 剩余任务数:{}", messageId, remain);
            if (locked) {
                lock.unlock();
            }
        }
    }

    /**
     * 判断异常是否属于可重试异常
     * <p>只有抛出MessageRetryConsumeException时才允许进入重试队列</p>
     * <p>其他异常（如NPE、业务校验异常等）直接告警不再重试，避免无效重试</p>
     *
     * @param e 捕获到的异常对象
     * @return true=允许重试，false=不允许重试
     */
    private boolean isRetryException(Exception e) {
        if (e instanceof MessageRetryConsumeException) {
            return true;
        }
        return false;
    }
}
