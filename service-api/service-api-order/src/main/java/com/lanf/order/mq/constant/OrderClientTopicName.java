package com.lanf.order.mq.constant;

public class OrderClientTopicName {
    /**
     * 订单签收事件
     */
    public static final String SIGN_ORDER_EVENT_TOPIC = "SIGN_ORDER_EVENT_TOPIC";
    /**
     * 添加销售出库单
     */
    public static final String ADD_SALES_OUT_STOCK_ORDER_TOPIC = "ADD_SALES_OUT_STOCK_ORDER_TOPIC";
    /**
     * 订单出库成功事件
     */
    public static final String ORDER_OUT_BOUNDED_EVENT_TOPIC = "ORDER_OUT_BOUNDED_EVENT_TOPIC";
    /**
     * 取消订单事件
     */
    public static final String ORDER_CANCEL_EVENT_TOPIC = "ORDER_CANCEL_EVENT_TOPIC";
}
