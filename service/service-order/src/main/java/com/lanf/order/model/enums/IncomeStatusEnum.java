package com.lanf.order.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 退款金额到账状态枚举
 */
@Getter
public enum IncomeStatusEnum {

    /**
     * 未到账
     */
    NOT_RECEIVED(0, "未到账", 0),
    /**
     * 已到账
     */
    RECEIVED(1, "已到账", 1);

    @EnumValue
    private final Integer code;
    private final String desc;
    private final Integer sort;

    IncomeStatusEnum(Integer code, String desc, Integer sort) {
        this.code = code;
        this.desc = desc;
        this.sort = sort;
    }

    @JsonValue
    public Integer getCode() {
        return code;
    }

    /**
     * 根据code获取枚举
     *
     * @param code 状态码
     * @return 对应的枚举值，如果未找到返回null
     */
    @JsonCreator
    public static IncomeStatusEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (IncomeStatusEnum typeEnum : IncomeStatusEnum.values()) {
            if (code.equals(typeEnum.getCode())) {
                return typeEnum;
            }
        }
        return null;
    }
}
