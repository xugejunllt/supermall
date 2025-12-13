package com.lanf.lock.aop;

import com.lanf.common.utils.IStringUtils;
import com.lanf.common.utils.StackTraceUtil;
import com.lanf.constant.exception.IRedisException;
import com.lanf.lock.service.DistributedLocker;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.client.RedisException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindException;

import java.lang.reflect.Method;


@Aspect
@Component
@Slf4j
public class DistributedLockAspect {


    @Autowired
    private DistributedLocker distributedLocker;
    /**
     * SpEL表达式解析器
     */
    private final ExpressionParser parser = new SpelExpressionParser();

    /**
     * 参数名发现器
     */
    private final LocalVariableTableParameterNameDiscoverer discoverer =
            new LocalVariableTableParameterNameDiscoverer();

    @Around("@annotation(distributedLock)")
    public Object around(ProceedingJoinPoint joinPoint, DistributedLock distributedLock) throws Throwable {

        String lockKey = buildLockKey(joinPoint, distributedLock);

        try {
            // 尝试获取锁
            boolean locked = distributedLocker.getLock(lockKey);

            if (locked) {
                return joinPoint.proceed();
            } else {

                throw new IRedisException("正在执行，请稍后");
            }
        } finally {
            distributedLocker.unlock(lockKey);
        }
    }

    /**
     * 构建锁的key
     */
    private String buildLockKey(ProceedingJoinPoint joinPoint, DistributedLock distributedLock) {

        String keyExpression = distributedLock.key();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();

        String prefix = signature.getDeclaringType().getSimpleName() + ":" + signature.getName();

        // 如果前缀为空，使用类名+方法名
        if (IStringUtils.isEmpty(prefix)) {
            log.error("key前缀为空");
            throw new RedisException("key前缀为空");
        }

        // 解析SpEL表达式获取实际的key值
        String actualKey = parseSpel(keyExpression, joinPoint);

        return String.format("%s:%s", prefix, actualKey);

    }

    /**
     * 解析SpEL表达式
     */
    private String parseSpel(String expression, ProceedingJoinPoint joinPoint) {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();

        // 获取参数名
        String[] paramNames = discoverer.getParameterNames(method);

        if (paramNames == null) {
            log.error("无法获取方法参数名，请编译时添加-parameters参数");
            throw new IRedisException();
        }

        // 创建评估上下文
        EvaluationContext context = new StandardEvaluationContext();
        Object[] args = joinPoint.getArgs();

        // 设置参数到上下文
        for (int i = 0; i < paramNames.length; i++) {
            context.setVariable(paramNames[i], args[i]);
        }

        // 解析表达式
        try {
            Object value = parser.parseExpression(expression).getValue(context);
            if (value == null) {
                log.error("锁key表达式解析结果为null");
                throw new IRedisException();
            }
            return value.toString();
        } catch (Exception e) {
            log.error("解析锁key表达式失败[{}]", StackTraceUtil.getStackTrace(e));

            throw new IRedisException();
        }
    }
}