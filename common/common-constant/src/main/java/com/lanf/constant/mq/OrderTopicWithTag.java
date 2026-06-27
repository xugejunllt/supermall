package com.lanf.constant.mq;

public class OrderTopicWithTag {

    /**
     * 订单事件总 Topic
     */
    public static final String ORDER_EVENT_TOPIC = "ORDER_EVENT_TOPIC";

    /**
     * Tag: 待付款
     */
    public static final String TAG_WAIT_PAY = "WAIT_PAY";

    /**
     * Tag: 已付款 (支付成功事件)
     */
    public static final String TAG_PAID = "PAID";

    /**
     * Tag: 待出库
     */
    public static final String TAG_WAIT_OUTBOUND = "WAIT_OUTBOUND";

    /**
     * Tag: 已出库
     */
    public static final String TAG_OUTBOUNDED = "OUTBOUNDED";

    /**
     * Tag: 已发货
     */
    public static final String TAG_SHIPPED = "SHIPPED";

    /**
     * Tag: 已签收
     */
    public static final String TAG_RECEIVED = "TAG_RECEIVED";

    /**
     * Tag: 已完成
     */
    public static final String TAG_COMPLETED = "COMPLETED";

    /**
     * Tag: 已取消
     */
    public static final String TAG_CANCELLED = "CANCELLED";

    /**
     * Tag: 已关闭
     */
    public static final String TAG_CLOSED = "CLOSED";

}
