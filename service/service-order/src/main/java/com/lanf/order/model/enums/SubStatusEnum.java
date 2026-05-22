package com.lanf.order.model.enums;


import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 物流订阅状态枚举
 */
@Getter
public enum SubStatusEnum {

    /**
     * 0：待订阅
     */
    PENDING(0, "待订阅"),

    /**
     * 1：订阅成功
     */
    SUBSCRIBED(1, "订阅成功");
    @EnumValue
    private final Integer code;
    private final String desc;

    SubStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
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
    public static SubStatusEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (SubStatusEnum e : SubStatusEnum.values()) {
            if (e.code.equals(code)) {
                return e;
            }
        }
        return null;
    }
}
