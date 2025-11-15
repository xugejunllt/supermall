package com.lanf.rocketmq.model.message;

import lombok.Data;

import java.util.List;

@Data
public class PaySuccessEventMessage  {

    private Long orderId;

    private LiquidationDTO settlementDTO;

    private LogisticsTrackBathAddDTO logisticsTrackBathAddDTO;
}
