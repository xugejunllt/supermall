package com.lanf.goods.model.bo;

import com.lanf.rocketmq.model.message.OrderGoodsInfo;
import lombok.Data;

import java.io.Serializable;

@Data
public class RollbackStockBO implements Serializable {

    private String orderNumber;

    private Long orderId;

    private OrderGoodsInfo orderGoodsInfo;

}
