package com.lanf.rocketmq.model.enums;

import lombok.Getter;

/**
 * MQ消费异常类型枚举
 * <p>用于区分消费异常是否需要重试</p>
 */
@Getter
public enum MqConsumeExceptionTypeEnum {

    /**
     * 需要重试的异常
     */
    NEED_RETRY(0, "需要重试的异常"),

    /**
     * 不需要重试的异常
     */
    NO_NEED_RETRY(1, "不需要重试的异常");

    private final int code;
    private final String desc;

    MqConsumeExceptionTypeEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
