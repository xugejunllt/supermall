package com.lanf.rocketmq.model.message;

import com.lanf.constant.mq.base.BaseMessage;
import lombok.Data;

/**
 * 交易成功事件消息
 */
@Data
public class TradeSuccessEventMessage extends BaseMessage {


    private Boolean bathPay;

    private Long mainOrderId;

    private Long userId;

    private Long orderId;

    private Integer payType;


}
