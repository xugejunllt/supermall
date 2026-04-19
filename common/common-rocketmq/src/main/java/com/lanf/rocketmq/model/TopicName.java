package com.lanf.rocketmq.model;

public class TopicName {


    public static final int MAX_RECONSUME_TIMES = 2;

    /**
     * 用户库存新增
     */
    public static final String USER_STOCK_ADD_TOPIC = "USER_STOCK_ADD_TOPIC";
    /**
     * 扣减优惠卷模板数量
     */
    public static final String DEDUCT_COUPON_TEMPLATE_COUNT_TOPIC = "DEDUCT_COUPON_TEMPLATE_COUNT_TOPIC";
    /**
     * 订单状态变更
     */
    public static  final String ORDER_STATUS_CHANGE_TOPIC = "ORDER_STATUS_CHANGE_TOPIC";
    /**
     * 销售单状态变更
     */
    public static  final String AFTER_SALES_ORDER_STATUS_CHANGE_TOPIC = "AFTER_SALES_ORDER_STATUS_CHANGE_TOPIC";
    /**
     * 退款
     */
    public static  final String REFUND_TOPIC = "REFUND_TOPIC";

    /**
     * 清算
     */

    public static  final String LIQUIDATION_TOPIC = "LIQUIDATION_TOPIC";
    /**
     * 结算
     */
    public static  final String SETTLEMENT_TOPIC = "SETTLEMENT_TOPIC";
    /**
     * 资金流水 财务结算topic
     */
    public static  final String MONEY_FLOW_TOPIC = "MONEY_FLOW_TOPIC";

    /**
     * 对账任务
     */
    public static  final String CONTRAST_BILL_TASK_TOPIC = "CONTRAST_BILL_TASK_TOPIC";
    /**
     * 添加物流轨迹
     */
    public static  final String ADD_LOGISTICS_TRACK_TOPIC = "ADD_LOGISTICS_TRACK_TOPIC";
    /**
     * 批量写入物流轨迹
     */
    public static  final String BATH_ADD_LOGISTICS_TRACK_TOPIC = "BATH_ADD_LOGISTICS_TRACK_TOPIC";
    /**
     * 单挑短信发送
     */
    public static  final String SEND_SMS_TOPIC = "SEND_SMS_TOPIC";

    /**
     * 取消订单
     */
    public static  final String CANCEL_ORDER_TOPIC = "CANCEL_ORDER_TOPIC";
    /**
     * 进行预支付后关闭订单
     */
    public static  final String PRE_PAY_CLOSE_ORDER_TOPIC = "PRE_PAY_CLOSE_ORDER_TOPIC";
    /**
     * 履约单退款
     */
    public static  final String PROMISE_ORDER_RETURN_MONEY_TOPIC = "PROMISE_ORDER_RETURN_MONEY_TOPIC";

    /**
     * 履约单履约完成进行结算
     *
     */
    public static  final String PROMISE_ORDER_LIQUIDATION_TOPIC = "PROMISE_ORDER_RETURN_MONEY_TOPIC";

    /**
     * 支付成功事件
     */
    public static  final String PAY_SUCCESS_EVENT_TOPIC = "PAY_SUCCESS_EVENT_TOPIC";
    /**
     * 出库完成事件
     */
    public static  final String OUT_STOCK_FINISH_EVENT_TOPIC = "OUT_STOCK_FINISH_EVENT_TOPIC";
    /**
     *保存商品到ES
     */
    public static  final String SAVE_GOODS_ES_TOPIC = "SAVE_GOODS_ES_TOPIC";

    /**
     * 订单创建成功
     */
    public static final String ORDER_CREATE_SUCCESS_TOPIC = "ORDER_CREATE_SUCCESS_TOPIC";
    /**
     * 补投支付订单
     */

    public static final String COMPENSATE_PAYMENT_TOPIC = "COMPENSATE_PAYMENT_TOPIC";

    /**
     * 支付流水插入成功通知
     */
    public static final String PAY_ORDER_FLOW_INSERT_SUCCESS_TOPIC = "PAY_ORDER_FLOW_INSERT_SUCCESS_TOPIC";

    /**
     * 交易成功事件
     */
    public static final String TRADE_SUCCESS_EVENT_TOPIC = "TRADE_SUCCESS_EVENT_TOPIC";
    /**
     * 取消订单事件
     */
    public static final String CANCEL_ORDER_EVENT_TOPIC = "CANCEL_ORDER_EVENT_TOPIC";
    /**
     * 取消三方待支付的订单
     */
    public static final String CANCEL_WAIT_PAY_ORDER_TOPIC = "CANCEL_WAIT_PAY_ORDER_TOPIC";
    /**
     * 取消三方支付成功的订单
     */
    public static final String CANCEL_SUCCESS_PAY_ORDER_TOPIC = "CANCEL_SUCCESS_PAY_ORDER_TOPIC";


    /**
     * 消费组名
     */
    public static  final String ORDER_STATUS_CHANGE__UPDATE_ORDER_GROUP = "ORDER_STATUS_CHANGE__UPDATE_ORDER_GROUP";
    public static  final String ORDER_STATUS_CHANGE__UPDATE_STORED_GROUP = "ORDER_STATUS_CHANGE__UPDATE_STORED_GROUP";
    public static  final String REFUND_PAY_GROUP = "REFUND_PAY_GROUP";
    public static  final String USER_REGISTER_GROUP = "USER_REGISTER_GROUP";
    public static  final String AFTER_SALES_ORDER_STATUS_CHANGE_GROUP = "AFTER_SALES_ORDER_STATUS_CHANGE_GROUP";
    public static  final String LIQUIDATION_GROUP = "LIQUIDATION_GROUP";
    public static  final String SETTLEMENT_GROUP = "SETTLEMENT_GROUP";

    public static  final String MONEY_FLOW_GROUP = "MONEY_FLOW_GROUP";
    public static  final String CONTRAST_BILL_TASK_GROUP = "CONTRAST_BILL_TASK_GROUP";

    public static  final String ADD_LOGISTICS_TRACK_GROUP = "ADD_LOGISTICS_TRACK_GROUP";
    public static  final String BATH_ADD_LOGISTICS_TRACK_GROUP = "BATH_ADD_LOGISTICS_TRACK_GROUP";
    public static  final String SEND_SMS_GROUP = "SEND_SMS_GROUP";

    public static  final String CANCEL_ORDER_GROUP = "CANCEL_ORDER_GROUP";
    /**
     * 预支付后，进行延迟关单
     */
    public static  final String PRE_PAY_CLOSE_ORDER_GROUP  = "PRE_PAY_CLOSE_ORDER_GROUP";
    public static  final String PROMISE_ORDER_RETURN_MONEY_GROUP = "PROMISE_ORDER_RETURN_MONEY_GROUP";
    public static  final String PROMISE_ORDER_LIQUIDATION_GROUP = "PROMISE_ORDER_LIQUIDATION_GROUP";
    public static  final String PAY_SUCCESS_ORDER_EVENT_GROUP = "PAY_SUCCESS_ORDER_EVENT_GROUP";

    public static  final String PAY_SUCCESS_LOGISTICS_EVENT_GROUP = "PAY_SUCCESS_LOGISTICS_EVENT_GROUP";
    public static  final String PAY_SUCCESS_FINANCE_EVENT_GROUP = "PAY_SUCCESS_FINANCE_EVENT_GROUP";

    public static  final String OUT_STOCK_FINISH_ORDER_EVENT_GROUP = "OUT_STOCK_FINISH_ORDER_EVENT_GROUP";
    public static  final String OUT_STOCK_FINISH_LOGISTICS_EVENT_GROUP = "OUT_STOCK_FINISH_LOGISTICS_EVENT_GROUP";
    public static  final String PAY_SUCCESS_STORAGE_EVENT_GROUP = "PAY_SUCCESS_STORAGE_EVENT_GROUP";
    public static  final String SAVE_GOODS_ES__GROUP = "SAVE_GOODS_ES__GROUP";
    public static final String USER_STOCK_ADD_GROUP = "USER_STOCK_ADD_GROUP";
    public static final String DEDUCT_COUPON_TEMPLATE_COUNT_GROUP = "DEDUCT_COUPON_TEMPLATE_COUNT_GROUP";

    /**
     * 订单创建成功 - 支付服务消费组
     */
    public static final String ORDER_CREATE_PAY_GROUP = "ORDER_CREATE_PAY_GROUP";
    public static final String COMPENSATE_PAYMENT_GROUP = "COMPENSATE_PAYMENT_GROUP";

    /**
     * 支付流水插入成功 - 支付服务消费组
     */
    public static final String PAY_ORDER_FLOW_PAY_GROUP = "PAY_ORDER_FLOW_PAY_GROUP";

    /**
     * 交易成功事件 - 支付服务消费组
     */
    public static final String TRADE_SUCCESS_PAY_GROUP = "TRADE_SUCCESS_PAY_GROUP";

    /**
     * 交易成功事件 - 订单服务消费组
     */
    public static final String TRADE_SUCCESS_ORDER_GROUP = "TRADE_SUCCESS_ORDER_GROUP";
    /**
     * 取消订单 -库存服务消费组
     */
    public static final String CANCEL_ORDER_EVENT_GOODS_GROUP = "CANCEL_ORDER_EVENT_GOODS_GROUP";
    /**
     * 取消订单 -福利服务消费组
     */
    public static final String CANCEL_ORDER_EVENT_WELFARE_GROUP = "CANCEL_ORDER_EVENT_WELFARE_GROUP";
    /**
     * 取消订单- 支付服务消费组
     */
    public static final String CANCEL_ORDER_EVENT_PAY_GROUP = "CANCEL_ORDER_EVENT_PAY_GROUP";
    public static final String CANCEL_WAIT_PAY_ORDER_GROUP = "CANCEL_WAIT_PAY_ORDER_GROUP";
    public static final String CANCEL_SUCCESS_PAY_ORDER_GROUP = "CANCEL_SUCCESS_PAY_ORDER_GROUP";



}
