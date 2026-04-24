package com.lanf.order.model.enums;


import lombok.Getter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Getter
public enum OrderStatusEnum {

    WAIT_PAY(0, "待付款"),
    WAIT_OUTBOUND(1, "待出库"),
    OUTBOUNDED(2, "已出库"),
    SHIPPED(3, "已发货"),
    WAIT_COMMENT(4, "待评价"),
    COMPLETED(5, "已完成,表示履约完成"),
    CANCELLED(6, "已取消"),
    CLOSED(7, "已关闭,订单已终结，不支持售后"),

    ;

    private final Integer code;
    private final String name;

    private static final Set<Integer> CANCELABLE_STATUS_SET = new HashSet<>(Arrays.asList(
            WAIT_PAY.code,
            WAIT_OUTBOUND.code
    ));


    OrderStatusEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    public static boolean isCancelable(Integer code) {
        return CANCELABLE_STATUS_SET.contains(code);
    }

    public static OrderStatusEnum getOrderStatusEnum(Integer code) {
        for (OrderStatusEnum e : OrderStatusEnum.values()) {
            if (e.code.equals(code)) {
                return e;
            }
        }
        return WAIT_PAY;
    }

}
