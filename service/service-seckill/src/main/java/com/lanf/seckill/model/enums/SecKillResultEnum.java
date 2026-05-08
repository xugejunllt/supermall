package com.lanf.seckill.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum SecKillResultEnum {

    SUCCESS_ORDER_CREATING(0, "秒杀成功，订单生成中"),
    SUCCESS_ORDER_CREATED(1, "订单生成完成"),
    FAILED(2, "秒杀失败"),
    SOLD_OUT(3, "商品已售罄");
    @EnumValue
    @JsonValue
    private final Integer code;
    private final String name;

    SecKillResultEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    @JsonValue
    public Integer getCode() {
        return code;
    }

    @JsonCreator
    public static SecKillResultEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (SecKillResultEnum resultEnum : SecKillResultEnum.values()) {
            if (code.equals(resultEnum.getCode())) {
                return resultEnum;
            }
        }
        return null;
    }
}
