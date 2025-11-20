package com.lanf.constant.enums;

/**
 * 权益发放事件
 */

import lombok.Getter;

@Getter
public enum BenefitGrantEventEnum {

    USER_REGISTER("1001", "用户注册事件", 50),
    CREATE_ORDER("1002", "用户下单事件", 50);

    private String code;
    private String name;
    //发放的成长值 这里写死 实际上DB存储 后台页面里配置
    private Integer value;

    BenefitGrantEventEnum(String code, String name, Integer value) {
        this.code = code;
        this.name = name;
        this.value = value;
    }

    public static BenefitGrantEventEnum getByCode(String code) {

        for (BenefitGrantEventEnum codeEnum : BenefitGrantEventEnum.values()) {
            if (codeEnum.getCode().equals(code)) {

                return codeEnum;
            }
        }
        return null;
    }

    public static boolean includeCode(String code) {

        for (BenefitGrantEventEnum codeEnum : BenefitGrantEventEnum.values()) {
            if (codeEnum.getCode().equals(code)) {

                return true;
            }
        }
        return false;
    }


}
