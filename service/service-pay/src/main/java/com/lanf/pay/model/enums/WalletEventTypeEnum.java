package com.lanf.pay.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum WalletEventTypeEnum {

    RECHARGE(0, "充值"),
    WITHDRAW(1, "提现"),
    ORDER(2, "下单"),
    CANCEL_ORDER_ROLLBACK(3, "取消订单回滚");

    @EnumValue
    @JsonValue
    private final Integer code;
    private final String name;

    WalletEventTypeEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    @JsonValue
    public Integer getCode() {
        return code;
    }

    @JsonCreator
    public static WalletEventTypeEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (WalletEventTypeEnum typeEnum : WalletEventTypeEnum.values()) {
            if (code.equals(typeEnum.getCode())) {
                return typeEnum;
            }
        }
        return null;
    }
}
