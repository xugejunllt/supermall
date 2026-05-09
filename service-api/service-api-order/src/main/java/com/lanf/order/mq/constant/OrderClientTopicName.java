package com.lanf.order.mq.constant;

public class OrderClientTopicName {

    /**
     * 订单事件总 Topic
     */
    public static final String ORDER_EVENT_TOPIC = "ORDER_EVENT_TOPIC";

    // --- 消息 Tag 定义 (用于过滤不同状态的订单事件) ---

    /**
     * Tag: 待确认 (秒杀场景)
     */
    public static final String TAG_WAIT_CONFIRM = "WAIT_CONFIRM";

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
     * Tag: 待评价
     */
    public static final String TAG_WAIT_COMMENT = "WAIT_COMMENT";

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
    
    /**
     * 秒杀下单 订单创建成功事件
     */
    public static final String SEC_KILL_PLANE_CREATE_ORDER_SUCCESS_EVENT_TOPIC = "SEC_KILL_PLANE_CREATE_ORDER_SUCCESS_EVENT_TOPIC";
    
    /**
     * 秒杀订单确认成功
     */
    public static final String SEC_KILL_ORDER_CONFIRM_TOPIC = "SEC_KILL_ORDER_CONFIRM_TOPIC";
    
    /**
     * 秒杀订单创建成功通知 秒杀服务
     */
    public static final String SEC_KILL_ORDER_CREATED_TOPIC = "SEC_KILL_ORDER_CREATED_TOPIC";
}
