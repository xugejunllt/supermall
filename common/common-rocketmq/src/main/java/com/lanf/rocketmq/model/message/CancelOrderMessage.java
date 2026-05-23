package com.lanf.rocketmq.model.message;

import com.lanf.constant.model.enums.pay.RefundEventTypeEnum;
import com.lanf.constant.mq.base.BaseMessage;
import lombok.Data;

@Data
public class CancelOrderMessage extends BaseMessage {


    private String outTradeNo;

    private Integer payType;

    private Long bizOrderId;
    /**
     *  退款事件类型
     */
    private RefundEventTypeEnum refundEventType;
}
