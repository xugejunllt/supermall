package com.lanf.web.sentinel;

import com.alibaba.csp.sentinel.Tracer;
import com.lanf.constant.exception.BizException;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.MethodArgumentNotValidException;

@Slf4j
@Aspect
@Component
public class SentinelDegradeAspect {

    @Around("@annotation(com.alibaba.csp.sentinel.annotation.SentinelResource)")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        try {
            return point.proceed();
        } catch (Throwable e) {
            // 判断是否需要统计
            if (shouldTrace(e)) {
                // ✅ 手动记录异常，Sentinel 会统计
                Tracer.trace(e);
            }
            // ❌ 不统计的异常不调用 。往上抛，最终被全局异常处理器捕获
            throw e;
        }
    }

    /**
     * 判断异常是否需要被 Sentinel 统计
     */
    private boolean shouldTrace(Throwable e) {
        // 不统计业务异常
        if (e instanceof BizException) {
            return false;
        }
        // 不统计参数校验异常
        if (e instanceof IllegalArgumentException) {
            return false;
        }
        // 不统计状态异常
        if (e instanceof MethodArgumentNotValidException) {
            return false;
        }
        // 其他异常都统计
        return true;
    }
}
