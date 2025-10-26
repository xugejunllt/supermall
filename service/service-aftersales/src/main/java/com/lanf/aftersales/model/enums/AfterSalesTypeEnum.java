package com.lanf.aftersales.model.enums;

import lombok.Getter;

/**
 * 与快递100状态码一致 其他状态码都视为异常
 */
@Getter
public enum AfterSalesTypeEnum {
    //Return and refund
    RETURN_REFUND(0, "退货退款");


    private final Integer code;
    private final String name;

    //收入支出 0:收入 1:支出
    AfterSalesTypeEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    public static AfterSalesTypeEnum getAfterSalesTypeEnum(Integer code) {

        for (AfterSalesTypeEnum e : AfterSalesTypeEnum.values()) {
            if (e.code.equals(code)) {

                return e;
            }
        }
        return RETURN_REFUND;
    }


}
