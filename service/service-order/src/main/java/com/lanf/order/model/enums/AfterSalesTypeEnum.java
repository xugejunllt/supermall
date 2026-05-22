package com.lanf.order.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 与快递100状态码一致 其他状态码都视为异常
 */
@Getter
public enum AfterSalesTypeEnum {
    RETURN_REFUND(0, "退货退款");

    @EnumValue
    private final Integer code;
    private final String name;

    //收入支出 0:收入 1:支出
    AfterSalesTypeEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }
    @JsonValue
    public Integer getCode() {
        return code;
    }

    @JsonCreator
    public static AfterSalesTypeEnum getAfterSalesTypeEnum(Integer code) {

        for (AfterSalesTypeEnum e : AfterSalesTypeEnum.values()) {
            if (e.code.equals(code)) {

                return e;
            }
        }
        return RETURN_REFUND;
    }


}
