package com.lanf.pay.model.enums;


import lombok.Getter;

@Getter
public enum TradeOrderStatusEnum {

    PENDING(0, "待支付"),
    COMPLETED(1, "支付完成");

    private  Integer code;

    private String name;

    TradeOrderStatusEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    /**
     * 根据 code 获取枚举
     *
     * @param code 状态码
     * @return 对应的枚举值，如果未找到返回 null
     */
    public static TradeOrderStatusEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (TradeOrderStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
}
