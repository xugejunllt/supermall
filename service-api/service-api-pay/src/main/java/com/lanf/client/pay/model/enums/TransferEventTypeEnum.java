package com.lanf.client.pay.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum TransferEventTypeEnum {

    ORDER_SETTLEMENT(0, "订单结算给商家"),
    WALLET_WITHDRAW(1, "用户钱包提现");

    @EnumValue
    @JsonValue
    private final Integer code;
    private final String name;

    TransferEventTypeEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    @JsonValue
    public Integer getCode() {
        return code;
    }

    @JsonCreator
    public static TransferEventTypeEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (TransferEventTypeEnum typeEnum : TransferEventTypeEnum.values()) {
            if (code.equals(typeEnum.getCode())) {
                return typeEnum;
            }
        }
        return null;
    }
}
