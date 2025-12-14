package com.lanf.welfare.model.enums;

import lombok.Getter;

/**
 * 优惠卷模板状态
 */
@Getter
public enum CouponTemplateStatus {



    WAIT(0, "待发布"),
    PUSH(1, "已发布"),
    REVOKE(2, "作废");





    private Integer code;
    private String name;


    CouponTemplateStatus(Integer code, String name) {
        this.code = code;
        this.name = name;
    }
    public  static boolean include(Integer code){
        for (CouponTemplateStatus value : CouponTemplateStatus.values()) {
            if(value.code.equals(code)){
                return true;
            }
        }
        return false;
    }

    public static CouponTemplateStatus getByCode(Integer code){

        for (CouponTemplateStatus value : CouponTemplateStatus.values()) {

            if(value.code.equals(code)){
                return value;
            }
        }
        return null;
    }
}
