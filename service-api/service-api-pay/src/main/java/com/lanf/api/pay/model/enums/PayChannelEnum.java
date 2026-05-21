package com.lanf.api.pay.model.enums;


import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
public enum PayChannelEnum {

    ALI_PAY(0, "支付宝"),
    
    WECHAT_PAY(1, "微信支付"),
    
    UNION_PAY(2, "银联支付");
    
    @EnumValue
    private final Integer code;

    private final String name;

    /**
     * 可用支付渠道集合
     * 用于快速判断某个支付渠道是否可用
     */
    /**
     * 可用支付渠道集合
     * 用于快速判断某个支付渠道是否可用
     */
    public static final Set<PayChannelEnum> AVAILABLE_CHANNELS = Arrays.stream(values())
            .collect(Collectors.toSet());

    @JsonValue
    public Integer getCode() {
        return code;
    }
    
    PayChannelEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    @JsonCreator
    public static PayChannelEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (PayChannelEnum typeEnum : PayChannelEnum.values()) {
            if (code.equals(typeEnum.getCode())) {
                return typeEnum;
            }
        }
        return null;
    }
    

}
