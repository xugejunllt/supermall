package com.lanf.order.model.bo;

import com.lanf.order.model.enums.OrderStatusEnum;
import lombok.Data;

import java.io.Serializable;

@Data
public class CancelOrderOrderStatusBO implements Serializable {

    private OrderStatusEnum currentOrderStatus;
}
