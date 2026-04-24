package com.lanf.rocketmq.model.message;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class CancelOrderEventMessage implements Serializable {

    private Long orderId;

    /**
     * 取消来源 -> 0:用户手动取消,1:系统定时任务超时取消
     *
     */
    private Integer cancelSource;
    /**
     * 订单关联的skuId
     */
    private List<Long> skuIdList;



}
