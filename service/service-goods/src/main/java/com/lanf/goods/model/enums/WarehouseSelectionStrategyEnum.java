package com.lanf.goods.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 仓库选择策略枚举
 * 通常关联商品的配送时间
 * 如 同区域附近仓库、同区域库存最多的仓库 次日达
 *
 */
@Getter
public enum WarehouseSelectionStrategyEnum {

    SAME_REGION_NEARBY(0, "同区域附近仓库"),
    NATIONAL_MOST_STOCK(1, "全国范围内附近仓库");
    @EnumValue
    @JsonValue
    private final Integer code;
    private final String name;

    WarehouseSelectionStrategyEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    @JsonCreator
    public static WarehouseSelectionStrategyEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (WarehouseSelectionStrategyEnum strategy : WarehouseSelectionStrategyEnum.values()) {
            if (code.equals(strategy.getCode())) {
                return strategy;
            }
        }
        return null;
    }
}
