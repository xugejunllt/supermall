package com.lanf.order.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum OrderProcessStepEnum {

    ORDER_CREATED(0, "订单创建成功"),
    TRADE_CREATED(1, "交易单创建成功"),
    STOCK_DEDUCTED(2, "库存扣减成功"),
    STOCK_DEDUCT_FAILED(3, "库存扣减失败");

    @EnumValue
    @JsonValue
    private final Integer code;
    private final String name;

    OrderProcessStepEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    @JsonValue
    public Integer getCode() {
        return code;
    }

    @JsonCreator
    public static OrderProcessStepEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (OrderProcessStepEnum stepEnum : OrderProcessStepEnum.values()) {
            if (code.equals(stepEnum.getCode())) {
                return stepEnum;
            }
        }
        return null;
    }

    public static OrderProcessStepEnum getStepEnum(Integer code) {
        for (OrderProcessStepEnum e : OrderProcessStepEnum.values()) {
            if (e.code.equals(code)) {
                return e;
            }
        }
        return ORDER_CREATED;
    }
}
