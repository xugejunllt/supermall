package com.lanf.pay.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum TradeTypeEnum {

    REALTIME_ORDER(0, "实时下单"),
    WALLET_RECHARGE(1, "钱包充值");

    @EnumValue
    @JsonValue
    private final Integer code;
    private final String name;

    TradeTypeEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    @JsonValue
    public Integer getCode() {
        return code;
    }

    @JsonCreator
    public static TradeTypeEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (TradeTypeEnum typeEnum : TradeTypeEnum.values()) {
            if (code.equals(typeEnum.getCode())) {
                return typeEnum;
            }
        }
        return null;
    }
}
