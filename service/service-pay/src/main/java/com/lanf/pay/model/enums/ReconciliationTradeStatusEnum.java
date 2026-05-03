package com.lanf.pay.model.enums;


import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum ReconciliationTradeStatusEnum {

    SUCCESS(0, "交易成功"),
    FAILED(1, "交易失败");

    @EnumValue
    @JsonValue
    private final Integer code;
    private final String name;

    ReconciliationTradeStatusEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    @JsonCreator
    public static ReconciliationTradeStatusEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (ReconciliationTradeStatusEnum statusEnum : ReconciliationTradeStatusEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum;
            }
        }
        return null;
    }
}
