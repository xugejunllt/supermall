package com.lanf.finance.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 资金流水记录类型枚举
 *
 * 平台支付收入账号 资金 发生了变更
 * 就需要记录类型
 * 0: 下单,  关联交易单id
 * 1: 售后退款,  关联售后单
 * 2: 取消订单退款, 关联订单
 * 3: 商家结算收入 关联结算单
 * 4: 用户钱包提现 关联提现单id
 * 5: 用户钱包充值  关联交易单id
 *
 *
 */
@Getter
public enum RecordTypeEnum {

    ORDER(0, "下单"),
    AFTER_SALES_REFUND(1, "售后退款"),
    CANCEL_ORDER_REFUND(2, "取消订单退款"),
    MERCHANT_SETTLEMENT_INCOME(3, "商家结算收入"),
    WALLET_WITHDRAW(4, "用户钱包提现"),
    WALLET_RECHARGE(5, "用户钱包充值"),
    ;

    public static final Set<Integer> INCOME_TYPE_SET = new HashSet<>(Arrays.asList(
            RecordTypeEnum.ORDER.getCode(),
            RecordTypeEnum.MERCHANT_SETTLEMENT_INCOME.getCode(),
            RecordTypeEnum.WALLET_RECHARGE.getCode()
    ));

    public static final Set<Integer> EXPENSE_TYPE_SET = new HashSet<>(Arrays.asList(
            RecordTypeEnum.AFTER_SALES_REFUND.getCode(),
            RecordTypeEnum.CANCEL_ORDER_REFUND.getCode(),
            RecordTypeEnum.WALLET_WITHDRAW.getCode()));


    @EnumValue
    private final Integer code;
    

    private final String name;

    RecordTypeEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }
    @JsonValue
    public Integer getCode() {
        return code;
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
