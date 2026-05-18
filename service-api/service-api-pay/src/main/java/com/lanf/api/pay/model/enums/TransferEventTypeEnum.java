package com.lanf.api.pay.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.lanf.api.pay.mq.constant.TransferEventTagConstant;
import lombok.Getter;

@Getter
public enum TransferEventTypeEnum {

    ORDER_SETTLEMENT(0, "订单结算给商家", TransferEventTagConstant.ORDER_SETTLEMENT),
    WALLET_WITHDRAW(1, "用户钱包提现", TransferEventTagConstant.WALLET_WITHDRAW);

    @EnumValue
    @JsonValue
    private final Integer code;
    private final String name;
    private final String tag;

    TransferEventTypeEnum(Integer code, String name, String tag) {
        this.code = code;
        this.name = name;
        this.tag = tag;
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

    public static TransferEventTypeEnum getByTag(String tag) {
        if (tag == null) {
            return null;
        }
        for (TransferEventTypeEnum typeEnum : TransferEventTypeEnum.values()) {
            if (tag.equals(typeEnum.getTag())) {
                return typeEnum;
            }
        }
        return null;
    }
}
