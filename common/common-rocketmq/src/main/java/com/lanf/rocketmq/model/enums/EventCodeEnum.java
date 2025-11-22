package com.lanf.rocketmq.model.enums;


import lombok.Getter;

@Getter
public enum EventCodeEnum {

    USER_REGISTER("1001", "用户注册");

    private String code;
    private String name;

    EventCodeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public  static  String buildBizKey(String key,String code){

        return key+":"+code;
    }


}
