package com.lanf.rocketmq.model.message;

import lombok.Data;

@Data
public class OutStockFinishEventMessage  {

    private Long orderId;

    private LogisticsTrackBathAddDTO logisticsTrackBathAddDTO;
}
