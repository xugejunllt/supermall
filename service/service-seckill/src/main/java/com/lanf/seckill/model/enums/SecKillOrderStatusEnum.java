package com.lanf.seckill.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum SecKillOrderStatusEnum {

    CREATING(0, "订单创建中"),
    CREATED(1, "创建成功"),
    CREATE_FAILED(2, "创建失败");

    @EnumValue
    @JsonValue
    private final Integer code;
    private final String name;

    SecKillOrderStatusEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    @JsonValue
    public Integer getCode() {
        return code;
    }

    @JsonCreator
    public static SecKillOrderStatusEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (SecKillOrderStatusEnum statusEnum : SecKillOrderStatusEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum;
            }
        }
        return null;
    }

    public static SecKillOrderStatusEnum getStatusEnum(Integer code) {
        for (SecKillOrderStatusEnum e : SecKillOrderStatusEnum.values()) {
            if (e.code.equals(code)) {
                return e;
            }
        }
        return CREATING;
    }
}
