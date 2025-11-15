package com.lanf.constant.exception;

import lombok.Data;

/**
 * 自定义全局异常类
 *
 */
@Data
public class UtilException extends RuntimeException {

    private Integer code;

    private String message;


    public UtilException(String message) {
        this.code = 1003;
        this.message = message;
    }


}
