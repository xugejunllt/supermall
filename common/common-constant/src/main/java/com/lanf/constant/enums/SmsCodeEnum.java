package com.lanf.constant.enums;


import lombok.Getter;

@Getter
public enum SmsCodeEnum {

    SMS_CODE_1001("SMS_1001", "注册");

    private String code;
    private String name;


    SmsCodeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }




}
