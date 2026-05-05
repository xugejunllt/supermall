package com.lanf.storage.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 发布平台枚举
 */
@Getter
public enum PublishPlatformEnum {

    MAIL_MALL(0, "mail商城");

    @EnumValue
    @JsonValue
    private final Integer code;
    private final String name;

    PublishPlatformEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    @JsonValue
    public Integer getCode() {
        return code;
    }

    @JsonCreator
    public static PublishPlatformEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (PublishPlatformEnum typeEnum : PublishPlatformEnum.values()) {
            if (code.equals(typeEnum.getCode())) {
                return typeEnum;
            }
        }
        return null;
    }
}
