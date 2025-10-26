package com.lanf.sms.model.enums;


import lombok.Getter;

@Getter
public enum ChannelEnum {

    ALIYUN("1001", "阿里云"),
    HUAWEI("1002", "华为云");
    private String code;
    private String name;


    ChannelEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }




}
