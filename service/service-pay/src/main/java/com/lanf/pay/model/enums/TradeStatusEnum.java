package com.lanf.pay.model.enums;

import lombok.Getter;

@Getter
public enum TradeStatusEnum {

    NOT_EXIST(-1, "交易不存在"),
    UNKNOWN(0, "未知状态"),
    WAIT_BUYER_PAY(1, "交易创建，等待买家付款"),
    TRADE_SUCCESS(2, "交易支付成功"),
    TRADE_FINISHED(3, "交易结束，不可退款"),
    TRADE_CLOSED(4, "未付款交易超时关闭，或支付完成后全额退款");

    private final Integer code;
    private final String description;

    TradeStatusEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    public static TradeStatusEnum fromCode(Integer code) {
        if (code == null) {
            return UNKNOWN;
        }
        
        for (TradeStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        
        return UNKNOWN;
    }

    public static TradeStatusEnum fromAlipayStatus(String alipayStatus) {
        if (alipayStatus == null) {
            return UNKNOWN;
        }
        
        switch (alipayStatus) {

            case "WAIT_BUYER_PAY":
                return WAIT_BUYER_PAY;
            case "TRADE_SUCCESS":
                return TRADE_SUCCESS;
            case "TRADE_FINISHED":
                return TRADE_FINISHED;
            case "TRADE_CLOSED":
                return TRADE_CLOSED;
            default:
                return UNKNOWN;
        }
    }

}
