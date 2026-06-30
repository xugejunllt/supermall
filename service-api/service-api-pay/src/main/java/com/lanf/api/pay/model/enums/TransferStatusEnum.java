package com.lanf.api.pay.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum TransferStatusEnum {

    REFUNDING(0, "退款中"),
    SUCCESS(1, "退款成功"),
    FAILED(2, "退款失败");

    @EnumValue
    @JsonValue
    private final Integer code;
    private final String name;

    TransferStatusEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    @JsonCreator
    public static TransferStatusEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (TransferStatusEnum statusEnum : TransferStatusEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum;
            }
        }
        return null;
    }
}
