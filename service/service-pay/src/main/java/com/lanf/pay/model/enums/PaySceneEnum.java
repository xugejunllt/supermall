package com.lanf.pay.model.enums;


import lombok.Getter;

@Getter
public enum PaySceneEnum {

    SINGLE_ORDER_SINGLE_PAY(1, "单笔下单单笔付款"),
    COMBINED_PAY(2, "组合付款"),
    COMBINED_TO_SINGLE_PAY(3, "组合转单笔付款"),


    UNKNOWN(4, "未知"),
    ;

    private Integer code;

    private String name;

    PaySceneEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }
}
