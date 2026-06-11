package com.lanf.web.handler;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.lanf.constant.code.CommonCodeEnum;
import com.lanf.constant.exception.BizException;
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


    @ExceptionHandler(BlockException.class)
    @ResponseBody
    public Result error(BlockException e) {
        log.warn("触发Sentinel异常");
        return Result.fail(CommonCodeEnum.FAIL.getCode(), "触发Sentinel异常");
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public Result error(Exception e) {

        log.error("请求异常", e);

        return Result.fail(CommonCodeEnum.FEIGN_DEGRADE.getCode(), CommonCodeEnum.FEIGN_DEGRADE.getMessage());
    }

    @ExceptionHandler(BizException.class)
    @ResponseBody
    public Result error(BizException e) {

        return Result.fail(e.getCode(), e.getMessage());
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
