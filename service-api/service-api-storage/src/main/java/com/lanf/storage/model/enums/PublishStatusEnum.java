package com.lanf.storage.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 发布状态枚举
 */
@Getter
public enum PublishStatusEnum {

    SUCCESS(0, "成功"),
    FAILED(1, "失败");

    @EnumValue
    @JsonValue
    private final Integer code;
    private final String name;

    PublishStatusEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    @JsonValue
    public Integer getCode() {
        return code;
    }

    @JsonCreator
    public static PublishStatusEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (PublishStatusEnum typeEnum : PublishStatusEnum.values()) {
            if (code.equals(typeEnum.getCode())) {
                return typeEnum;
            }
        }
        return null;
    }
}
