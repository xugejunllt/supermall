package com.lanf.api.pay.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 下单付款方式
 */
@Getter
public enum PayMethodEnum {

    THIRD_PARTY_PAY(0, "三方支付"),
    WALLET_BALANCE(1, "钱包余额");

    @EnumValue
    @JsonValue
    private final Integer code;
    private final String name;

    PayMethodEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    @JsonValue
    public Integer getCode() {
        return code;
    }

    @JsonCreator
    public static PayMethodEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (PayMethodEnum typeEnum : PayMethodEnum.values()) {
            if (code.equals(typeEnum.getCode())) {
                return typeEnum;
            }
        }
        return null;
    }
}
