package com.lanf.pay.model.enums;

import lombok.Getter;

@Getter
public enum TradeStatusEnum {

    UNKNOWN(0, "未知状态"),
    TRADE_SUCCESS(1, "交易支付成功"),
    /**
     * 交易结束 不支持查询
     */
    TRADE_FINISHED(3, "交易结束");
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

        if (alipayStatus.equals("TRADE_SUCCESS")) {
            return TRADE_SUCCESS;
        }
        return UNKNOWN;
    }

}
