package com.lanf.pay.model.enums;


import lombok.Getter;

@Getter
public enum BathTradeOrderStatusEnum {

    PENDING(0, "待支付"),
    COMPLETED(1, "支付完成"),
    MERGE_TRANSFER_SINGLE(2, "合并转单笔"),
    ;

    private  Integer code;

    private String name;

    BathTradeOrderStatusEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }
}
