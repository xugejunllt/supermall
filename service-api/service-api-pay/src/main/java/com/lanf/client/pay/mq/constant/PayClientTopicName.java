package com.lanf.client.pay.mq.constant;

public class PayClientTopicName {

    /**
     * 处理退款
     */
    public static final String PROCESS_REFUND_TOPIC = "PROCESS_REFUND_TOPIC";
    /**
     * 支付流水插入成功通知
     */
    public static final String PAY_ORDER_FLOW_INSERT_SUCCESS_TOPIC = "PAY_ORDER_FLOW_INSERT_SUCCESS_TOPIC";
    /**
     * 钱包充值
     */
    public static final String WALLET_RECHARGE_TOPIC = "WALLET_RECHARGE_TOPIC";

    /**
     * 处理退款 - 支付服务消费组
     */
    public static final String PROCESS_REFUND_PAY_GROUP = "PROCESS_REFUND_PAY_GROUP";
    /**
     * 转账
     */
    public static final String TRANSFER_TOPIC = "TRANSFER_TOPIC";

    /**
     * 转账成功通知
     */
    public static final String TRANSFER_SUCCESS_EVENT_TOPIC = "TRANSFER_SUCCESS_EVENT_TOPIC";

}
