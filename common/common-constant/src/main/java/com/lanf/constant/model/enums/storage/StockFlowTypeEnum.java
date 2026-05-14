package com.lanf.constant.model.enums.storage;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 库存流水类型枚举
 */
@Getter
public enum StockFlowTypeEnum {

    PURCHASE_INBOUND(0, "采购入库单入库"),
    SALES_OUTBOUND(1, "销售出库单出库"),
    AFTERSALES_RETURN_INBOUND(2, "售后退货单入库");

    @EnumValue
    @JsonValue
    private final Integer code;
    private final String name;

    StockFlowTypeEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }



    @JsonCreator
    public static StockFlowTypeEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (StockFlowTypeEnum typeEnum : StockFlowTypeEnum.values()) {
            if (code.equals(typeEnum.getCode())) {
                return typeEnum;
            }
        }
        return null;
    }
}
