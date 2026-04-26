package com.lanf.client.pay.mq;

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
     * 处理退款 - 支付服务消费组
     */
    public static final String PROCESS_REFUND_PAY_GROUP = "PROCESS_REFUND_PAY_GROUP";


}
