package com.lanf.rocketmq.model.message;

import com.lanf.messagemanager.client.base.BaseMessage;
import lombok.Data;

import java.util.List;

@Data
public class PaySuccessEventMessage extends BaseMessage {

    private Long orderId;

    private LiquidationDTO settlementDTO;

    private LogisticsTrackBathAddDTO logisticsTrackBathAddDTO;
}
