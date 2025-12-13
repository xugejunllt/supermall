package com.lanf.welfare.model.enums;

import lombok.Getter;

@Getter
public enum CouponType {



    FULL(0, "满减"),
    DISCOUNT(1, "折扣"),
    FIXED(2, "无门槛");

    private Integer code;
    private String name;


    CouponType(Integer code, String name) {
        this.code = code;
        this.name = name;
    }
    public  static boolean include(Integer code){
        for (CouponType value : CouponType.values()) {
            if(value.code.equals(code)){
                return true;
            }
        }
        return false;
    }
    public static CouponType getByCode(Integer code){
        for (CouponType value : CouponType.values()) {
            if(value.code.equals(code)){
                return value;
            }
        }
        return null;
    }

}
