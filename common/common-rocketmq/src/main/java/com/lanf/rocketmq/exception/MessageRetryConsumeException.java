package com.lanf.rocketmq.exception;

import com.lanf.constant.code.CommonResultCodeEnum;
import lombok.Data;

@Data
public class MessageRetryConsumeException extends Exception{

    private Integer code;

    private String message;

    /**
     * 通过状态码和错误消息创建异常对象
     * @param code
     * @param message
     */
    public MessageRetryConsumeException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }
    public MessageRetryConsumeException( String message) {
        super(message);
        this.code = CommonResultCodeEnum.FAIL.getCode();
        this.message = message;
    }
}
