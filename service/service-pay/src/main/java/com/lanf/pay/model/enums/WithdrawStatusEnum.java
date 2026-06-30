package com.lanf.pay.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum WithdrawStatusEnum {

    PENDING(0, "待处理"),
    PROCESSING(1, "处理中"),
    SUCCESS(2, "成功"),
    FAILED(3, "失败"),
    CANCELLED(4, "已取消");

    @EnumValue
    private final Integer code;
    private final String name;

    WithdrawStatusEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    @JsonValue
    public Integer getCode() {
        return code;
    }

    @JsonCreator
    public static WithdrawStatusEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (WithdrawStatusEnum statusEnum : WithdrawStatusEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum;
            }
        }
        return null;
    }
}
