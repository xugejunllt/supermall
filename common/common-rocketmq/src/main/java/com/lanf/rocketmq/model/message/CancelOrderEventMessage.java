package com.lanf.rocketmq.model.message;

import com.lanf.constant.mq.base.BaseMessage;
import lombok.Data;

import java.util.List;

@Data
public class CancelOrderEventMessage extends BaseMessage {


    private String orderNumber;

    private Long orderId;

    private List<OrderGoodsInfo> orderGoodsInfoList;



}
