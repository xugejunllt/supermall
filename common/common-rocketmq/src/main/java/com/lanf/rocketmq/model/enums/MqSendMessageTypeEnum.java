package com.lanf.rocketmq.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * MQ发送消息类型枚举
 */
@Getter
public enum MqSendMessageTypeEnum {

    /**
     * 0: 普通消息
     */
    NORMAL(0, "普通消息"),

    /**
     * 1: 顺序消息
     */
    ORDERED(1, "顺序消息"),

    /**
     * 2: 普通tag消息
     */
    NORMAL_TAG(2, "普通tag消息"),

    /**
     * 3: 顺序tag消息
     */
    ORDERED_TAG(3, "顺序tag消息"),

    /**
     * 4: 延迟普通消息
     */
    DELAY_NORMAL(4, "延迟普通消息"),

    /**
     * 5: 延迟顺序消息
     */
    DELAY_ORDERED(5, "延迟顺序消息"),

    /**
     * 6: 延迟普通tag消息
     */
    DELAY_NORMAL_TAG(6, "延迟普通tag消息"),

    /**
     * 7: 延迟顺序tag消息
     */
    DELAY_ORDERED_TAG(7, "延迟顺序tag消息");

    @EnumValue
    private final Integer code;

    private final String name;

    MqSendMessageTypeEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    @JsonValue
    public Integer getCode() {
        return code;
    }

    /**
     * 根据code获取枚举
     *
     * @param code 消息类型代码
     * @return 对应的枚举值，如果未找到返回null
     */
    @JsonCreator
    public static MqSendMessageTypeEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (MqSendMessageTypeEnum typeEnum : MqSendMessageTypeEnum.values()) {
            if (code.equals(typeEnum.getCode())) {
                return typeEnum;
            }
        }
        return null;
    }
}
