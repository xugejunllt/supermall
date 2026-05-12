package com.lanf.web.handler;

import com.lanf.constant.code.CommonCodeEnum;
import com.lanf.constant.exception.BizException;
import com.lanf.constant.exception.IRedisException;
import com.lanf.constant.exception.MQException;
import com.lanf.constant.exception.UtilException;
import com.lanf.constant.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
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

        log.error("请求异常[{}]", e);

        return Result.fail();
    }

    @ExceptionHandler(BizException.class)
    @ResponseBody
    public Result error(BizException e) {

        return Result.fail(e.getCode(), e.getMessage());
    }


    @ExceptionHandler(IRedisException.class)
    @ResponseBody
    public Result error(IRedisException e) {

        return Result.fail(CommonCodeEnum.FAIL.getCode(), CommonCodeEnum.FAIL.getMessage());
    }

    @ExceptionHandler(MQException.class)
    @ResponseBody
    public Result error(MQException e) {

        return Result.fail(CommonCodeEnum.FAIL.getCode(), CommonCodeEnum.FAIL.getMessage());
    }

    @ExceptionHandler(UtilException.class)
    @ResponseBody
    public Result error(UtilException e) {

        return Result.fail(CommonCodeEnum.FAIL.getCode(), CommonCodeEnum.FAIL.getMessage());
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    public Result error(MethodArgumentNotValidException e) {

        List<ObjectError> allErrors = e.getAllErrors();
        if (!allErrors.isEmpty()) {
            ObjectError objectError = allErrors.get(0);
            String defaultMessage = objectError.getDefaultMessage();
            return Result.fail(CommonCodeEnum.FAIL.getCode(), defaultMessage);
        }
        return Result.fail(CommonCodeEnum.FAIL.getCode(), CommonCodeEnum.FAIL.getMessage());
    }


}
