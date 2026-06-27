package com.lanf.constant.model.enums.order;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum OrderSubStatus {

    INITIAL(0, "初始状态"),
    WAIT_EVALUATE(1, "待评价"),
    EVALUATED(2, "评价完成");

    @EnumValue
    private final Integer code;
    private final String name;

    OrderSubStatus(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    @JsonValue
    public Integer getCode() {
        return code;
    }

    @JsonCreator
    public static OrderSubStatus getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (OrderSubStatus typeEnum : OrderSubStatus.values()) {
            if (code.equals(typeEnum.getCode())) {
                return typeEnum;
            }
        }
        return null;
    }
}
