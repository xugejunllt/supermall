package com.lanf.api.order.mq.message;

import com.lanf.constant.mq.base.BaseMessage;
import lombok.Data;

import java.util.List;

@Data
public class OrderWaitOutboundMessage extends BaseMessage {


    private Long orderId;

    private Long tenantId;

    private List<InOutStockOrderItem> items;

}
