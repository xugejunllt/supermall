package com.lanf.pay.mq.constant;

public class PayMqTopicName {

    /**
     * 下载、解析并存储对账单
     */
    public static final String BILL_SYNCHRONIZER_TOPIC = "BILL_SYNCHRONIZER_TOPIC";

    /**
     *
     *下载解析对账单 Excel 超时后重试
     *
     */
    public static final String BILL_EXCEL_PARSE_RETRY_TOPIC = "BILL_EXCEL_PARSE_RETRY_TOPIC";
    /**
     * 开始对账任务
     */
    public static final String RECONCILIATION_START_TOPIC = "RECONCILIATION_START_TOPIC";

    /**
     * 查询退款结果
     */
    public static final String QUERY_REFUND_RESULT_TOPIC = "QUERY_REFUND_RESULT_TOPIC";
    /**
     * 查询转账结果
     */
    public static final String QUERY_TRANSFER_RESULT_TOPIC = "QUERY_REFUND_RESULT_TOPIC";


    /**
     * 转账查询结果处理
     */
    public static final String TRANSFER_QUERY_RESULT_TOPIC = "TRANSFER_QUERY_RESULT_TOPIC";

}
