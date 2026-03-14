package com.lanf.pay.model.enums;


import lombok.Getter;

@Getter
public enum TradeOrderStatusEnum {

    PENDING(0, "待支付"),
    COMPLETED(1, "支付完成"),
    CANCELLED(3, "已取消");

    private  Integer code;

    private String name;

    TradeOrderStatusEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }
}
