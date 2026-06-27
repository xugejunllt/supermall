package com.lanf.order.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum OrderAutoCloseStatusEnum {

    PENDING(0, "待完成"),
    COMPLETED(1, "已完成");

    @EnumValue
    private final Integer code;
    private final String name;

    OrderAutoCloseStatusEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    @JsonValue
    public Integer getCode() {
        return code;
    }

    @JsonCreator
    public static OrderAutoCloseStatusEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (OrderAutoCloseStatusEnum typeEnum : OrderAutoCloseStatusEnum.values()) {
            if (code.equals(typeEnum.getCode())) {
                return typeEnum;
            }
        }
        return null;
    }
}
