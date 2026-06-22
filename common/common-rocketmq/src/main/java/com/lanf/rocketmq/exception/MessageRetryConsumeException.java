package com.lanf.rocketmq.exception;

/**
 * 消息重试消费异常
 * <p>抛出此异常表示需要延迟重试</p>
 */
public class MessageRetryConsumeException extends RuntimeException {

    public MessageRetryConsumeException(String message) {
        super(message);
    }

    public MessageRetryConsumeException(String message, Throwable cause) {
        super(message, cause);
    }
}
