package com.lanf.web.handler;

import com.lanf.web.code.CommonResultCodeEnum;
import com.lanf.web.exception.BizException;
import com.lanf.web.result.Result;
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
@ControllerAdvice
@Order(2)
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public Result error(Exception e) {
        e.printStackTrace();
        return Result.fail();
    }


    @ExceptionHandler(BizException.class)
    @ResponseBody
    public Result error(BizException e) {
        e.printStackTrace();
        return Result.fail(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    public Result error(MethodArgumentNotValidException e) {

        List<ObjectError> allErrors = e.getAllErrors();
        if (!allErrors.isEmpty()) {
            ObjectError objectError = allErrors.get(0);
            String defaultMessage = objectError.getDefaultMessage();
            return Result.fail(CommonResultCodeEnum.ARGUMENT_VALID_ERROR.getCode(), defaultMessage);
        }
        return Result.fail(CommonResultCodeEnum.ARGUMENT_VALID_ERROR.getCode(), CommonResultCodeEnum.ARGUMENT_VALID_ERROR.getMessage());
    }


}
