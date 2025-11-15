package com.lanf.constant.exception;

import lombok.Data;

/**
 * 自定义全局异常类
 *
 */
@Data
public class IRedisException extends RuntimeException {


    private String message;


    public IRedisException(String message) {
        this.message = message;
    }
    public IRedisException() {

    }

}
