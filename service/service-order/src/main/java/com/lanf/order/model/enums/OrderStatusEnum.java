package com.lanf.order.model.enums;


import lombok.Getter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Getter
public enum OrderStatusEnum {

    WAIT_PAY(0, "待付款"),
    PAID_RECONCILING(1, "已付款,对账中"),
    WAIT_OUTBOUND(2, "待出库"),
    OUTBOUNDED(3, "已出库"),
    SHIPPED(4, "已发货"),
    COMPLETED(5, "已完成"),
    CLOSED(6, "已关闭"),
    CANCELLED(7, "已取消");

    private Integer code;
    private String name;

    private static final Set<Integer> CANCELABLE_STATUS_SET = new HashSet<>(Arrays.asList(
            WAIT_PAY.code,
            PAID_RECONCILING.code,
            WAIT_OUTBOUND.code
    ));


    OrderStatusEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    public static boolean isCancelable(Integer code) {
        return CANCELABLE_STATUS_SET.contains(code);
    }

}
