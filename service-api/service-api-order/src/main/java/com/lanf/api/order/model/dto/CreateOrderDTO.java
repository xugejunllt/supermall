package com.lanf.api.order.model.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
public class CreateOrderDTO implements Serializable {


    private Long orderId;
    /**
     * 店铺id
     */
    private Long shopId;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 订单编号
     */
    private String orderNumber;

    /**
     * 订单金额
     */
    private BigDecimal totalMoney;

    /**
     * 实付金额
     */
    private BigDecimal actualPayMoney;

    /**
     * 优惠金额
     */
    private BigDecimal discountAmount;

    /**
     * 收货地址
     */
    private TakeAddressDTO takeAddressBO;

    private List<OrderItemDTO> orderItems;
}
