package com.lanf.pay.mq.constant;

public class PayMqGroupName {
    /**
     * 转账
     */
    public static final String TRANSFER_GROUP = "TRANSFER_GROUP";

    /**
     * 转账成功事件 - 钱包提现处理组
     */
    public static final String TRANSFER_SUCCESS_WALLET_WITHDRAW_GROUP = "TRANSFER_SUCCESS_WALLET_WITHDRAW_GROUP";

    /**
     * 下载、解析并存储对账单
     *
     */
    public static final String BILL_SYNCHRONIZER_GROUP = "BILL_SYNCHRONIZER_GROUP";


    public static final String BILL_EXCEL_PARSE_RETRY_GROUP = "BILL_EXCEL_PARSE_RETRY_GROUP";

    public static final String RECONCILIATION_START_GROUP = "RECONCILIATION_START_GROUP";
    /**
     * 查询退款结果
     */
    public static final String QUERY_REFUND_RESULT_GROUP = "QUERY_REFUND_RESULT_GROUP";

    /**
     * 查询转账结果
     */
    public static final String QUERY_TRANSFER_RESULT_GROUP = "QUERY_TRANSFER_RESULT_GROUP";


    public static final String CANCEL_ORDER_CANCEL_PAY_ORDER_GROUP = "CANCEL_ORDER_CANCEL_PAY_ORDER_GROUP";

}
