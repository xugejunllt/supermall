package com.lanf.goods.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 用户库存流水事件类型枚举
 */
@Getter
public enum UserStockFlowEventTypeEnum {
    /**
     * 下单出库
     */
    ORDER_OUTBOUND(0, "下单出库"),
    
    /**
     * 取消订单入库
     */
    CANCEL_ORDER_INBOUND(1, "取消订单入库");

    @EnumValue
    @JsonValue
    private final Integer code;
    private final String name;

    UserStockFlowEventTypeEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    @JsonValue
    public Integer getCode() {
        return code;
    }

    /**
     * 根据code获取枚举
     */
    public static UserStockFlowEventTypeEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (UserStockFlowEventTypeEnum typeEnum : UserStockFlowEventTypeEnum.values()) {
            if (code.equals(typeEnum.getCode())) {
                return typeEnum;
            }
        }
        return null;
    }
}
