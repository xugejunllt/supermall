package com.lanf.aftersales.model.enums;

import lombok.Getter;

/**
 * 与快递100状态码一致 其他状态码都视为异常
 */
@Getter
public enum ReturnsAndRefundsStatusEnum {


    PUBLISHED(0, "已发布，商家处理中"),
    AGREES_TO_APPLY(1, "商家同意申请，买家处理中"),
    REFUSE_TO_APPLY(2, "商家拒绝申请"),
    SHIPPED(3, "买家已发货，待商家收货"),
    FINISH(4, "商家收货，售后完成"),
    REFUSE_FINISH(5, "商家拒绝收货(拒绝退款)"),
    CLOSE(6, "售后关闭"),
    //cancel
    CANCEL(7, "已撤销");


    private final Integer code;
    private final String name;

    //收入支出 0:收入 1:支出
    ReturnsAndRefundsStatusEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    public static ReturnsAndRefundsStatusEnum getReturnsAndRefundsStatusEnum(Integer code) {

        for (ReturnsAndRefundsStatusEnum e : ReturnsAndRefundsStatusEnum.values()) {
            if (e.code.equals(code)) {

                return e;
            }
        }
        return PUBLISHED;
    }


}
