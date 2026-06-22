package com.lanf.rocketmq.aspect;

import com.lanf.rocketmq.model.entity.MqSendMessageDO;
import com.lanf.rocketmq.sevice.MqMessageSendService;
import com.lanf.rocketmq.util.MqSendMessageUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;
import java.util.concurrent.Executor;

/**
 * MQ消息发送AOP切面
 * <p>扫描Spring事务注解，在事务提交成功后，从内存队列中获取消息并发送到消息队列</p>
 *
 * @author jarven
 * @since 2026-06-20
 */
@Slf4j
@Aspect
@Component
public class MqSendMessageAop {

    @Autowired
    @Qualifier("mqSendExecutor")
    private Executor mqSendExecutor;

    @Autowired
    private MqMessageSendService mqMessageSendService;

    /**
     * 拦截带有@Transactional注解的方法
     *
     * @param joinPoint 连接点
     * @return 方法返回值
     * @throws Throwable 异常
     */
    @Around("@annotation(org.springframework.transaction.annotation.Transactional)")
    public Object aroundTransactional(ProceedingJoinPoint joinPoint) throws Throwable {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    doSendMessages();
                }

                @Override
                public void afterCompletion(int status) {
                    if (status != TransactionSynchronization.STATUS_COMMITTED) {
                        MqSendMessageUtils.clear();
                    }
                }
            });
        }

        return joinPoint.proceed();
    }

    /**
     * 从内存队列中获取消息并发送到MQ
     */
    private void doSendMessages() {
        List<MqSendMessageDO> messages = MqSendMessageUtils.getAndClearMessages();
        if (messages.isEmpty()) {
            return;
        }

        log.info("事务提交成功，开始发送MQ消息，共{}条", messages.size());

        for (MqSendMessageDO messageDO : messages) {
            mqSendExecutor.execute(() -> mqMessageSendService.sendMessage(messageDO));
        }
    }


}
