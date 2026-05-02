package com.lanf.pay.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum RefundStatusEnum {

    REFUNDING(0, "退款中"),
    SUCCESS(1, "退款成功"),
    FAILED(2, "退款失败");

    @EnumValue
    @JsonValue
    private final Integer code;
    private final String name;

    RefundStatusEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    @JsonCreator
    public static RefundStatusEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (RefundStatusEnum statusEnum : RefundStatusEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum;
            }
        }
        return null;
    }
}
