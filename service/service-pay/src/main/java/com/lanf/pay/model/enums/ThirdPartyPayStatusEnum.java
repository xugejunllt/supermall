package com.lanf.pay.model.enums;

import lombok.Getter;

/**
 * 三方支付订单状态枚举
 */
@Getter
public enum ThirdPartyPayStatusEnum {

    NOT_INITIATED(0, "未发起交易"),
    WAIT_PAY(1, "待支付"),
    PAID_REFUNDING(2, "已支付，进行退款");

    private final Integer code;
    private final String description;

    ThirdPartyPayStatusEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }



}
