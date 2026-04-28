package com.lanf.client.pay.model.enums;


import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum PayTypeEnum {

    ALI_PAY(0, "支付宝");
    @EnumValue
    @JsonValue
    private  final Integer code;

    private final String name;

    @JsonValue
    public Integer getCode() {
        return code;
    }
    PayTypeEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    @JsonCreator
    public static PayTypeEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (PayTypeEnum typeEnum : PayTypeEnum.values()) {
            if (code.equals(typeEnum.getCode())) {
                return typeEnum;
            }
        }
        return null;
    }
}
