package com.lanf.constant.model.enums.pay;


import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 退款事件类型枚举
 */
@Getter
public enum RefundEventTypeEnum {

    CANCEL_PAID_ORDER(0, "取消已支付的订单"),
    AFTER_SALES_REFUND(1, "售后单退款");
    @EnumValue
    private final Integer code;
    private final String name;

    RefundEventTypeEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    public static RefundEventTypeEnum getRefundEventTypeEnum(Integer code) {
        for (RefundEventTypeEnum e : RefundEventTypeEnum.values()) {
            if (e.code.equals(code)) {
                return e;
            }
        }
        return CANCEL_PAID_ORDER;
    }

    @JsonValue
    public Integer getCode() {
        return code;
    }

    @JsonCreator
    public static RefundEventTypeEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (RefundEventTypeEnum e : RefundEventTypeEnum.values()) {
            if (e.code.equals(code)) {
                return e;
            }
        }
        return CANCEL_PAID_ORDER;
    }
}
