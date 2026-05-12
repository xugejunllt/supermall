package com.lanf.security.handler;

import com.lanf.constant.code.CommonCodeEnum;
import com.lanf.constant.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;


/**
 * 全局异常处理类
 */
@Slf4j
@ControllerAdvice
@Order(2)
public class SpringSecurityExceptionHandler {


    @ExceptionHandler(AccessDeniedException.class)
    @ResponseBody
    public Result error(AccessDeniedException e) {
        //权限异常  没有访问权限 -spring security
        return Result.fail(CommonCodeEnum.FAIL.getCode(), e.getMessage());

    }



}
