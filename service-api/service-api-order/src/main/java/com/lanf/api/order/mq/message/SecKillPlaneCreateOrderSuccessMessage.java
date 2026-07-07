package com.lanf.api.order.mq.message;

import com.lanf.constant.mq.base.BaseMessage;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SecKillPlaneCreateOrderSuccessMessage extends BaseMessage{


    private Long orderId;

    private String orderNumber;

    private Long userId;

    private BigDecimal tradeMoney;


    private String skuCode;

    private Long warehouseId;
    /**
     * 扣减库存的数量
     */
    private Integer quantity;


}
