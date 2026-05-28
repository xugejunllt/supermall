package com.lanf.constant.model.enums.order;


import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.lanf.constant.mq.OrderTopicWithTag;
import lombok.Getter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Getter
public enum OrderStatusEnum {


    WAIT_PAY(0, "待付款", OrderTopicWithTag.TAG_WAIT_PAY),
    PAID(1, "已付款", OrderTopicWithTag.TAG_PAID),
    /**
     * 商家审核通过后 已 1-->2 生成销售出库单
     */
    WAIT_OUTBOUND(2, "待出库", OrderTopicWithTag.TAG_WAIT_OUTBOUND),
    OUTBOUNDED(3, "已出库", OrderTopicWithTag.TAG_OUTBOUNDED),
    SHIPPED(4, "已发货", OrderTopicWithTag.TAG_SHIPPED),
    WAIT_COMMENT(5, "待评价", OrderTopicWithTag.TAG_WAIT_COMMENT),
    /**
     * 1.主动评价
     * 2.如果超过7天没有评价，系统自动评价
     *
     */
    COMPLETED(6, "已评价(已完成)", OrderTopicWithTag.TAG_COMPLETED),

    CANCELLED(7, "已取消", OrderTopicWithTag.TAG_CANCELLED),
    /**
     *
     *  1.已取消订单 3天之后 转成 已关闭
     *  2.主动售后完成
     *  3.超过售后完成时间
     *
     */
    CLOSED(8, "已关闭,订单已终结", OrderTopicWithTag.TAG_CLOSED),

    ;
    @EnumValue
    private final Integer code;
    private final String name;
    
    /**
     * 对应的 MQ Tag 标识
     */
    private final String tag;

    private static final Set<Integer> CANCELABLE_STATUS_SET = new HashSet<>(Arrays.asList(
            WAIT_PAY.code,
            PAID.code

    ));


    OrderStatusEnum(Integer code, String name, String tag) {
        this.code = code;
        this.name = name;
        this.tag = tag;
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
    @JsonValue
    public Integer getCode() {
        return code;
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

    public static OrderStatusEnum getByTag(String tag) {
        if (tag == null) {
            return null;
        }
        for (OrderStatusEnum typeEnum : OrderStatusEnum.values()) {
            if (tag.equals(typeEnum.getTag())) {
                return typeEnum;
            }
        }
        return null;
    }
}
