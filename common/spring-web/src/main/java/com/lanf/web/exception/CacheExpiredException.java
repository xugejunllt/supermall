package com.lanf.web.exception;
import lombok.Data;

/**
 * 自定义全局异常类
 *
 */
@Data
public class CacheExpiredException extends RuntimeException {

    private Integer code;

    private String message;

    /**
     * 通过状态码和错误消息创建异常对象
     * @param code
     * @param message
     */
    public CacheExpiredException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }



}
