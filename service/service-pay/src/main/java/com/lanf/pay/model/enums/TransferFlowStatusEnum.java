package com.lanf.pay.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum TransferFlowStatusEnum {

    SUCCESS(0, "退款成功"),
    FAILED(1, "退款失败");

    @EnumValue
    @JsonValue
    private final Integer code;
    private final String name;

    TransferFlowStatusEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    @JsonCreator
    public static TransferFlowStatusEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (TransferFlowStatusEnum statusEnum : TransferFlowStatusEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum;
            }
        }
        return null;
    }
}
