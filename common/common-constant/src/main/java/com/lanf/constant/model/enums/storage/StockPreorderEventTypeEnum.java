package com.lanf.constant.model.enums.storage;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum StockPreorderEventTypeEnum {

    PUBLISH(0, "发布"),
    RECYCLE(1, "回收");

    @EnumValue
    @JsonValue
    private final Integer code;
    private final String name;

    StockPreorderEventTypeEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    @JsonCreator
    public static StockPreorderEventTypeEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (StockPreorderEventTypeEnum typeEnum : StockPreorderEventTypeEnum.values()) {
            if (code.equals(typeEnum.getCode())) {
                return typeEnum;
            }
        }
        return null;
    }
}
