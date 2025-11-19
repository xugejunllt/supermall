package com.lanf.user.model.enums;

/**
 * 权益code
 */

import lombok.Getter;

@Getter
public enum BenefitCodeEnum {

    EMPTY("9999","空权益"),
    GRANT_COUPON("1001", "赠送优惠卷"),
    GRANT_WALLET_BALANCE("1002", "赠送钱包余额");

    private String code;
    private String name;


    BenefitCodeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static boolean includeCode(String code){

        for (BenefitCodeEnum codeEnum : BenefitCodeEnum.values()){
            if (codeEnum.getCode().equals(code)){

                return true;
            }
        }
        return false;
    }



}
