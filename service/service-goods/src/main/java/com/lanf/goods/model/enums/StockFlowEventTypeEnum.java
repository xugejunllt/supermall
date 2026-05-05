package com.lanf.goods.model.enums;


import lombok.Getter;

@Getter
public enum StockFlowEventTypeEnum {

    ORDER_OUTBOUND(0, "下单出库"),
    CANCEL_ORDER_INBOUND(1, "取消订单入库"),

    ;

    private Integer code;
    private String name;

    StockFlowEventTypeEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

}
