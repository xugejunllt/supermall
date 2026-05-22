package com.lanf.order.mq.message;

import com.lanf.constant.mq.base.BaseMessage;
import com.lanf.order.model.enums.Express100StatusEnum;
import com.lanf.order.model.enums.ShippingStatusEnum;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
public class BathAddShippingTrackMessage extends BaseMessage {

    private Long orderId;

    private List<ShippingTrackMessage> shippingTrackList;


}
