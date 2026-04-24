package com.lanf.aftersales.model.enums;


import lombok.Getter;

/**
 * 售后订单主状态枚举
 */
@Getter
public enum MainStatusEnum {

    WAIT_SELLER_AGREE(0, "待审核"),
    WAIT_BUYER_RETURN(1, "待买家退货"),
    WAIT_SELLER_RECEIVE(1, "待收货"),
    WAIT_CONFIRM(3, "待退款/换货"),
    SUCCESS(4, "已完成"),
    CLOSED(5, "已关闭");

    private final Integer code;
    private final String desc;



    MainStatusEnum(Integer code, String name) {
        this.code = code;
        this.desc = name;
    }



}
