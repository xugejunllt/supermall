package com.lanf.rocketmq.model.enums;


import lombok.Getter;

@Getter
public enum EventCodeEnum {

    USER_REGISTER("1001", "用户注册"),
    PURCHASE_ORDER_IN_STOCK("1002", "采购入库单入库");


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
