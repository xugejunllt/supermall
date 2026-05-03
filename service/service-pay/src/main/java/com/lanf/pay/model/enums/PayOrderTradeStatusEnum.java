package com.lanf.pay.model.enums;


import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum PayOrderTradeStatusEnum {

    WAIT_BUYER_PAY("WAIT_BUYER_PAY", "交易创建，等待买家付款"),
    TRADE_CLOSED("TRADE_CLOSED", "未付款交易超时关闭，或支付完成后全额退款"),
    TRADE_SUCCESS("TRADE_SUCCESS", "交易支付成功"),
    TRADE_FINISHED("TRADE_FINISHED", "交易结束，不可退款");

    @EnumValue
    @JsonValue
    private final String code;
    private final String name;

    PayOrderTradeStatusEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    @JsonCreator
    public static PayOrderTradeStatusEnum getByCode(String code) {
        if (code == null) {
            return null;
        }
        for (PayOrderTradeStatusEnum statusEnum : PayOrderTradeStatusEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum;
            }
        }
        return null;
    }
}
