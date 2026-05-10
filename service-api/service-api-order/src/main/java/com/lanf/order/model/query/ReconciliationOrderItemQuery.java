package com.lanf.order.model.query;

import com.lanf.constant.enums.order.OrderStatusEnum;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class ReconciliationOrderItemQuery implements Serializable {

    @NotNull(message = "订单ID不能为空")
    private Long orderId;
    @NotNull(message = "订单状态不能为空")
    private OrderStatusEnum orderStatus;


}
