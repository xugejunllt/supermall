package com.lanf.rocketmq.model.message;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class CancelOrderEventMessage implements Serializable {

    private Long orderId;
    /**
     * 订单关联的skuId
     */
    private List<Long> skuIdList;



}
