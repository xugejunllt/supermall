package com.lanf.security.config;

import com.lanf.common.utils.StackTraceUtil;
import com.lanf.security.code.SystemResultCodeEnum;
import com.lanf.web.code.CommonResultCodeEnum;
import com.lanf.web.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.nio.file.AccessDeniedException;

/**
 * @author tanlingfei
 * @version 1.0
 * @description TODO
 * @date 2023/4/3 8:45
 */
@Slf4j
@ControllerAdvice
@Order(1)
public class SecurityExceptionHandler {
    /**
     * spring security异常
     * @param e
     * @return
     */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseBody
    public Result error(AccessDeniedException e){

        log.error("没有权限，异常堆栈[{}]", StackTraceUtil.getStackTrace(e));

        return Result.fail(CommonResultCodeEnum.FAIL.getCode(),CommonResultCodeEnum.FAIL.getMessage());
    }
}
