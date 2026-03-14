package com.lanf.pay.model.enums;


import lombok.Getter;

@Getter
public enum PayTypeEnum {

    ALI_PAY(0, "支付宝");

    private  Integer code;

    private String name;

    PayTypeEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }
}
