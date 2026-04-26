package com.lanf.finance.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 资金流水记录类型枚举
 *
 * 平台支付收入账号 资金 发生了变更
 * 就需要记录类型
 */
@Getter
public enum RecordTypeEnum {

    ORDER(0, "下单"),
    AFTER_SALES_REFUND(1, "售后退款"),
    CANCEL_ORDER_REFUND(2, "取消订单退款"),
    PLATFORM_SETTLEMENT_EXPENSE(3, "平台结算支出"),
    MERCHANT_SETTLEMENT_INCOME(4, "商家结算收入");

    @EnumValue
    private final Integer code;
    
    @JsonValue
    private final String name;

    RecordTypeEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    /**
     * 根据code获取枚举
     * @param code 记录类型代码
     * @return 对应的枚举值，如果未找到返回null
     */
    @JsonCreator
    public static RecordTypeEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (RecordTypeEnum typeEnum : RecordTypeEnum.values()) {
            if (code.equals(typeEnum.getCode())) {
                return typeEnum;
            }
        }
        return null;
    }
}
