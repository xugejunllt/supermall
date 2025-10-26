package com.lanf.rocketmq.model.message;

import com.lanf.messagemanager.client.base.BaseMessage;
import lombok.Data;

@Data
public class OutStockFinishEventMessage extends BaseMessage {

    private Long orderId;

    private LogisticsTrackBathAddDTO logisticsTrackBathAddDTO;
}
