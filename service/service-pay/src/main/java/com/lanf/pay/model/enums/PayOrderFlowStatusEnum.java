package com.lanf.pay.model.enums;


import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum PayOrderFlowStatusEnum {

    SUCCESS(0, "交易成功"),
    FAILED(1, "交易失败");

    @EnumValue
    @JsonValue
    private final Integer code;
    private final String name;

    PayOrderFlowStatusEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    @JsonCreator
    public static PayOrderFlowStatusEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (PayOrderFlowStatusEnum statusEnum : PayOrderFlowStatusEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum;
            }
        }
        return null;
    }
}
