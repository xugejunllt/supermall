package com.lanf.rocketmq.model.message;

import com.lanf.constant.mq.base.BaseMessage;
import lombok.Data;

@Data
public class CompensatePaymentOrderMessage extends BaseMessage {

    private String outTradeNo;

    /**
     * 支付类型
     */
    private Integer payType;
    /**
     * 重试次数（第几次重试）
     *
     */
    private Integer retryLevel;

    private Boolean bathOrder;
}
