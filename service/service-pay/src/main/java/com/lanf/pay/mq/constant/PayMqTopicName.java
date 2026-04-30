package com.lanf.pay.mq.constant;

public class PayMqTopicName {

    /**
     * 下载、解析并存储对账单
     */
    public static final String BILL_SYNCHRONIZER_TOPIC = "BILL_SYNCHRONIZER_TOPIC";

    /**
     * 解析账单 批量保存失败时
     *   通过mq进行补偿
     *   mq可以自动进行重试
     */
    public static final String FUND_BILL_DETAIL_COMPENSATION_TOPIC = "FUND_BILL_DETAIL_COMPENSATION_TOPIC";

}
