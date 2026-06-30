package com.lanf.api.pay.model.vo;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum RecipientTypeEnum {

    MERCHANT(0, "商家");

    @EnumValue
    private final Integer code;
    private final String name;

    RecipientTypeEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    @JsonValue
    public Integer getCode() {
        return code;
    }

    @JsonCreator
    public static RecipientTypeEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (RecipientTypeEnum typeEnum : RecipientTypeEnum.values()) {
            if (code.equals(typeEnum.getCode())) {
                return typeEnum;
            }
        }
        return null;
    }
}
