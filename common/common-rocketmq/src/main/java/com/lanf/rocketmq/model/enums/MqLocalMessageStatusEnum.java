package com.lanf.rocketmq.model.enums;

import lombok.Getter;

/**
 * MQ本地事务消息状态枚举
 */
@Getter
public enum MqLocalMessageStatusEnum {

    PENDING(0, "待消费"),
    SUCCESS(1, "消费成功"),
    FAIL(2, "消费失败");

    private final int code;
    private final String desc;

    MqLocalMessageStatusEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
