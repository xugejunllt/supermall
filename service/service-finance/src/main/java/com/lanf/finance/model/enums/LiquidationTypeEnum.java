package com.lanf.finance.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 清分单类型枚举
 */
@Getter
public enum LiquidationTypeEnum {

    MERCHANT_INCOME(0, "商家收入"),
    PLATFORM_INCOME(1, "平台收入"),
    PLATFORM_EXPENSE(2, "平台支出");

    @EnumValue
    @JsonValue
    private final Integer code;

    private final String name;

    LiquidationTypeEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    /**
     * 根据code获取枚举
     * @param code 清分单类型代码
     * @return 对应的枚举值，如果未找到返回null
     */
    @JsonCreator
    public static LiquidationTypeEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (LiquidationTypeEnum typeEnum : LiquidationTypeEnum.values()) {
            if (code.equals(typeEnum.getCode())) {
                return typeEnum;
            }
        }
        return null;
    }
}
