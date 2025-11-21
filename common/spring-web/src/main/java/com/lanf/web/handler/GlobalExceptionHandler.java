package com.lanf.web.handler;

import com.lanf.common.utils.StackTraceUtil;
import com.lanf.constant.exception.IRedisException;
import com.lanf.constant.exception.MQException;
import com.lanf.constant.exception.UtilException;
import com.lanf.web.code.CommonResultCodeEnum;
import com.lanf.web.exception.BizException;
import com.lanf.web.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;


/**
 * 全局异常处理类
 */
@Slf4j
@ControllerAdvice
@Order(2)
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public Result error(Exception e) {

        log.error("请求异常[{}]", StackTraceUtil.getStackTrace(e));

        return Result.fail();
    }
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseBody
    public Result error(AccessDeniedException e) {
        //权限异常  没有访问权限 -spring security
        return Result.fail(CommonResultCodeEnum.SERVICE_ERROR.getCode(), e.getMessage());

    }
    @ExceptionHandler(BizException.class)
    @ResponseBody
    public Result error(BizException e) {

        return Result.fail(e.getCode(), e.getMessage());
    }


    @ExceptionHandler(IRedisException.class)
    @ResponseBody
    public Result error(IRedisException e) {

        return Result.fail(CommonResultCodeEnum.SERVICE_ERROR.getCode(), CommonResultCodeEnum.SERVICE_ERROR.getMessage());
    }

    @ExceptionHandler(MQException.class)
    @ResponseBody
    public Result error(MQException e) {

        return Result.fail(CommonResultCodeEnum.SERVICE_ERROR.getCode(), CommonResultCodeEnum.SERVICE_ERROR.getMessage());
    }

    @ExceptionHandler(UtilException.class)
    @ResponseBody
    public Result error(UtilException e) {

        return Result.fail(CommonResultCodeEnum.SERVICE_ERROR.getCode(), CommonResultCodeEnum.SERVICE_ERROR.getMessage());
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    public Result error(MethodArgumentNotValidException e) {

        List<ObjectError> allErrors = e.getAllErrors();
        if (!allErrors.isEmpty()) {
            ObjectError objectError = allErrors.get(0);
            String defaultMessage = objectError.getDefaultMessage();
            return Result.fail(CommonResultCodeEnum.FAIL.getCode(), defaultMessage);
        }
        return Result.fail(CommonResultCodeEnum.FAIL.getCode(), CommonResultCodeEnum.FAIL.getMessage());
    }


}
