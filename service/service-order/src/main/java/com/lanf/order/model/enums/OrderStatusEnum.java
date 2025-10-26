package com.lanf.order.model.enums;


import lombok.Getter;

@Getter
public enum OrderStatusEnum {

    SMS_CODE_1001("SMS_1001", "注册");

    private String code;
    private String name;


    OrderStatusEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }




}
