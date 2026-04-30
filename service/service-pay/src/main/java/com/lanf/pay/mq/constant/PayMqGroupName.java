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

}
