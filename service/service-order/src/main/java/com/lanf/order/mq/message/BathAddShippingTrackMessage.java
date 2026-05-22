package com.lanf.order.mq.message;

import com.lanf.constant.mq.base.BaseMessage;
import lombok.Data;

import java.util.List;

@Data
public class BathAddShippingTrackMessage extends BaseMessage {

    private Long orderId;
    private Long userId;

    private Long tenantId;
    private List<ShippingTrackMessage> shippingTrackList;


}
