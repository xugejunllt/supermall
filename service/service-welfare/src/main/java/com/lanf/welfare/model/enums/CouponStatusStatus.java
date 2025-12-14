package com.lanf.welfare.model.enums;

import lombok.Getter;

/**
 * 优惠卷模板状态
 */
@Getter
public enum CouponStatusStatus {


    WAIT(0, "待使用"),
    USE(1, "已使用"),
    REVOKE(2, "作废");

    private Integer code;

    private String name;


    CouponStatusStatus(Integer code, String name) {
        this.code = code;
        this.name = name;
    }
    public  static boolean include(Integer code){
        for (CouponStatusStatus value : CouponStatusStatus.values()) {
            if(value.code.equals(code)){
                return true;
            }
        }
        return false;
    }

    public static CouponStatusStatus getByCode(Integer code){

        for (CouponStatusStatus value : CouponStatusStatus.values()) {

            if(value.code.equals(code)){
                return value;
            }
        }
        return null;
    }
}
