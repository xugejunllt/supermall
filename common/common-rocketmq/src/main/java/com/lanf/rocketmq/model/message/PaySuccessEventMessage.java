package com.lanf.rocketmq.model.message;

import lombok.Data;

@Data
public class PaySuccessEventMessage  {

    private Long orderId;



    private LogisticsTrackBathAddDTO logisticsTrackBathAddDTO;
}
