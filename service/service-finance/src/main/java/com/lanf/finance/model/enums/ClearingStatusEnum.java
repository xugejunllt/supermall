package com.lanf.finance.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum ClearingStatusEnum {

    WAIT_CLEARING(0, "待结算"),
    CLEARING(1, "结算中"),
    CLEARING_COMPLETED(2, "结算完成"),
    CANCELLED(3, "已取消"),
    EXCEPTION(4, "异常");

    @EnumValue
    @JsonValue
    private final Integer code;
    private final String name;

    ClearingStatusEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    @JsonValue
    public Integer getCode() {
        return code;
    }

    @JsonCreator
    public static ClearingStatusEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (ClearingStatusEnum statusEnum : ClearingStatusEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum;
            }
        }
        return null;
    }
}
