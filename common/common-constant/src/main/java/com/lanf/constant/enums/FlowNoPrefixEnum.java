package com.lanf.constant.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 流水号前缀枚举
 * 用于生成各类业务流水号的前缀标识
 */
@Getter
@AllArgsConstructor
public enum FlowNoPrefixEnum {

    /**
     * 交易订单流水号
     */
    TRADE_ORDER("TO", "交易订单流水号"),


    /**
     * 退款单流水号
     */
    REFUND_ORDER("RO", "退款单流水号"),

    /**
     * 转账单流水号
     */
    TRANSFER_ORDER("TO", "转账单流水号"),

    /**
     * 钱包流水号
     */
    WALLET_FLOW("WF", "钱包流水号"),

    /**
     * 资金流水号
     */
    MONEY_FLOW("MF", "资金流水号"),
    /**
     * 库存流水号
     */
    STOCK_FLOW("SF", "库存流水号"),
    /**
     * 发布预售库存流水号
     */
    PUBLISH_PREORDER_STOCK_FLOW("PSF", "发布预售库存流水号");
    /**
     * 前缀字符串
     */
    private final String prefix;

    /**
     * 描述
     */
    private final String desc;

    /**
     * 根据code获取枚举
     *
     * @param prefix 前缀字符串
     * @return 枚举值
     */
    public static FlowNoPrefixEnum getByPrefix(String prefix) {
        for (FlowNoPrefixEnum flowNoPrefixEnum : values()) {
            if (flowNoPrefixEnum.getPrefix().equals(prefix)) {
                return flowNoPrefixEnum;
            }
        }
        throw new IllegalArgumentException("未找到对应的流水号前缀: " + prefix);
    }
}
