package com.lanf.order.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum OrderTypeEnum {

    NORMAL(0, "普通订单"),
    SEC_KILL(1, "秒杀单");

    @EnumValue
    @JsonValue
    private final Integer code;
    private final String name;

    OrderTypeEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    @JsonValue
    public Integer getCode() {
        return code;
    }

    @JsonCreator
    public static OrderTypeEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (OrderTypeEnum typeEnum : OrderTypeEnum.values()) {
            if (code.equals(typeEnum.getCode())) {
                return typeEnum;
            }
        }
        return null;
    }

    public static OrderTypeEnum getOrderTypeEnum(Integer code) {
        for (OrderTypeEnum e : OrderTypeEnum.values()) {
            if (e.code.equals(code)) {
                return e;
            }
        }
        return NORMAL;
    }
}
