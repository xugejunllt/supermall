package com.lanf.order.model.enums;


import lombok.Getter;

@Getter
public enum PayStatusEnum {

    WAIT_PAY(0, "待支付"),
    PAID(1, "已支付"),

    ;

    private Integer code;
    private String name;

    PayStatusEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

}
