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
    COMPLETED(4, "已完成"),
    CANCELLED(5, "已取消"),
    CLOSED(5, "已关闭"),

    ;

    private Integer code;
    private String name;

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

}
