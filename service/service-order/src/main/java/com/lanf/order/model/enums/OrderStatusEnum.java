package com.lanf.order.model.enums;


import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Getter
public enum OrderStatusEnum {

    WAIT_PAY(0, "待付款"),
    PAID(1, "已付款"),
    /**
     * 商家审核通过后 已 1-->2 生成销售出库单
     */
    WAIT_OUTBOUND(2, "待出库"),
    //OUTBOUNDED(3, "已出库"),
    SHIPPED(4, "已发货"),
    WAIT_COMMENT(5, "待评价"),
    /**
     * 1.主动评价
     * 2.如果超过7天没有评价，系统自动评价
     *
     */
    COMPLETED(6, "已评价(已完成)"),

    CANCELLED(7, "已取消"),
    /**
     *
     *  1.已取消订单 3天之后 转成 已关闭
     *  失败终态（取消、退款）
     */
    CLOSED(8, "已关闭,订单已终结"),

    ;
    @EnumValue
    @JsonValue
    private final Integer code;
    private final String name;

    private static final Set<Integer> CANCELABLE_STATUS_SET = new HashSet<>(Arrays.asList(
            WAIT_PAY.code,
            PAID.code
    ));


    OrderStatusEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }
    @JsonValue
    public Integer getCode() {
        return code;
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
    @JsonCreator
    public static OrderStatusEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (OrderStatusEnum typeEnum : OrderStatusEnum.values()) {
            if (code.equals(typeEnum.getCode())) {
                return typeEnum;
            }
        }
        return null;
    }
}
