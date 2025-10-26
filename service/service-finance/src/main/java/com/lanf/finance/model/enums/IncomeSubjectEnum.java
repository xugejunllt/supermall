package com.lanf.finance.model.enums;

import lombok.Getter;

@Getter
public enum IncomeSubjectEnum {

    CODE0(0, "用户下单，平台收入全部金额"),
    CODE1(1, "用户下单，支付给支付平台费用"),
    CODE3(2, "履约完成，平台转账给商户"),
    CODE4(3, "履约完成，商家收到平台转账"),
    CODE5(4, "用户订单退款，平台支出");

    private final Integer code;
    private final String name;


    IncomeSubjectEnum(Integer code, String name) {
        this.code = code;
        this.name = name;

    }

    public static IncomeSubjectEnum getByCode(Integer code) {

        for (IncomeSubjectEnum subjectEnum : IncomeSubjectEnum.values()) {
            if (code.equals(subjectEnum.code)) {
                return subjectEnum;
            }
        }
        return null;
    }


}
