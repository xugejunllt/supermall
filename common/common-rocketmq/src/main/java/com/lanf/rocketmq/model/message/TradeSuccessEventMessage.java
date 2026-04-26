package com.lanf.rocketmq.model.message;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 交易成功事件消息
 */
@Data
public class TradeSuccessEventMessage implements Serializable {


    private Boolean bathPay;

    private Long mainOrderId;

    private List<OrderPayInfo> orderPayInfoList;

}
