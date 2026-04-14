package com.lanf.pay.model.enums;



import lombok.Getter;

@Getter
public enum TradeOrderFrozenEnum {

    NORMAL(0, "正常"),
    FROZEN(1, "冻结状态");

    private Integer code;

    private String name;

    TradeOrderFrozenEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }
}
