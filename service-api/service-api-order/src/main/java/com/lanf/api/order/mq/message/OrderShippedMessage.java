package com.lanf.api.order.mq.message;

import com.lanf.constant.mq.base.BaseMessage;
import lombok.Data;

@Data
public class OrderShippedMessage extends BaseMessage {

    private Long orderId;



}
