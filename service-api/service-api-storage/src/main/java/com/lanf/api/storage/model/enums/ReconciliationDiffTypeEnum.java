package com.lanf.api.storage.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 对账差异类型枚举
 */
@Getter
public enum ReconciliationDiffTypeEnum {
    /**
     * 长库存
     */
    LONG_STOCK(0, "长库存"),

    /**
     * 短库存
     */
    SHORT_STOCK(1, "短库存"),

    /**
     * 订单项目与用户库存流水数量不一致
     */
    ORDER_ITEM_STOCK_FLOW_MISMATCH(2, "订单项目与用户库存流水数量不一致"),

    /**
     * 仓储流水数量与用户库存流水数量不一致
     */
    WAREHOUSE_STOCK_FLOW_MISMATCH(3, "仓储流水数量与用户库存流水数量不一致"),

    /**
     * 用户库存流水缺失（订单项目有，流水没有）
     */
    USER_STOCK_FLOW_MISSING(4, "用户库存流水缺失");

    @EnumValue
    @JsonValue
    private final Integer code;
    private final String name;

    ReconciliationDiffTypeEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    /**
     * 根据code获取枚举
     */
    public static ReconciliationDiffTypeEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (ReconciliationDiffTypeEnum typeEnum : ReconciliationDiffTypeEnum.values()) {
            if (code.equals(typeEnum.getCode())) {
                return typeEnum;
            }
        }
        return null;
    }
}
