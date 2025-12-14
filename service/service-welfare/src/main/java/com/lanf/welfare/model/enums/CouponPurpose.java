package com.lanf.welfare.model.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;

@Getter
public enum CouponPurpose {



    SHOP(0, "店铺优惠卷"),
    USER_REGISTER(1, "用户注册-全店铺使用"),
    MEMBER_LEVEL_UPGRADE(2, "会员等级升级-全店铺使用");

    //非平台租户优惠卷类型code
    public static final List<CouponPurpose> notPlatformCouponPurpose = Arrays.asList(SHOP);



    private Integer code;
    private String name;


    CouponPurpose(Integer code, String name) {
        this.code = code;
        this.name = name;
    }
    public  static boolean include(Integer code){
        for (CouponPurpose value : CouponPurpose.values()) {
            if(value.code.equals(code)){
                return true;
            }
        }
        return false;
    }

    public static CouponPurpose getByCode(Integer code){

        for (CouponPurpose value : CouponPurpose.values()) {

            if(value.code.equals(code)){
                return value;
            }
        }
        return null;
    }
}
