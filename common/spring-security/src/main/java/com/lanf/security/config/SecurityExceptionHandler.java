package com.lanf.security.config;

import com.lanf.security.code.SystemResultCodeEnum;
import com.lanf.web.result.Result;
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

        return Result.fail(SystemResultCodeEnum.PERMISSION.getCode(),SystemResultCodeEnum.PERMISSION.getMessage());
    }
}
