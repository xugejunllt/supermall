package com.lanf.order.model.vo;

import com.lanf.constant.model.enums.order.OrderStatusEnum;
import com.lanf.order.model.enums.OrderTypeEnum;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class OrderPageVO implements Serializable {

    /**
     * 店铺id
     */
    private Long shopId;

    private String shopName;

    /**
     * 订单编号
     */
    private String orderNumber;

    /**
     * 0:待付款, 1:待出库 2：已出库 3：已发货，4：已完成，5：已取消 6.已关闭
     */
    private OrderStatusEnum status;

    /**
     * 订单类型 0：普通订单 ,1:秒杀单
     */
    private OrderTypeEnum orderType;

    private List<OrderItemPageVO> orderItemPageVOList;

}
