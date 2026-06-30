package com.lanf.api.pay.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum WalletEventTypeEnum {

    RECHARGE(0, "充值", 1),
    WITHDRAW(1, "提现", -1),
    ORDER(2, "下单", -1),
    CANCEL_ORDER_ROLLBACK(3, "取消订单回滚", 1);

    @EnumValue
    private final Integer code;
    private final String name;
    /**
     * 收支方向：1 收入，-1 支出
     */
    private final Integer inOut;

    WalletEventTypeEnum(Integer code, String name, Integer inOut) {
        this.code = code;
        this.name = name;
        this.inOut = inOut;
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
