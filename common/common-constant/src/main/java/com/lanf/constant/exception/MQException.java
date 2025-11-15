package com.lanf.constant.exception;

import lombok.Data;

/**
 * 自定义全局异常类
 *
 */
@Data
public class MQException extends RuntimeException {


    private String message;


    public MQException(String message) {
        this.message = message;
    }


}
