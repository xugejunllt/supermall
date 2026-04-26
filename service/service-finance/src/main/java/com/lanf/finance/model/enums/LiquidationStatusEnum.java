package com.lanf.finance.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 清算单结算状态枚举
 */
@Getter
public enum LiquidationStatusEnum {

    WAIT_SETTLEMENT(0, "待结算"),
    SETTLED(1, "已结算"),
    CANCELLED(2, "已取消");

    @EnumValue
    @JsonValue
    private final Integer code;
    

    private final String name;
    @JsonValue
    public Integer getCode() {
        return code;
    }


    LiquidationStatusEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    /**
     * 根据code获取枚举
     * @param code 状态代码
     * @return 对应的枚举值，如果未找到返回null
     */
    @JsonCreator
    public static LiquidationStatusEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (LiquidationStatusEnum statusEnum : LiquidationStatusEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum;
            }
        }
        return null;
    }
}
