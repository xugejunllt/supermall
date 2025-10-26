package com.lanf.order.model.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
public class CreateOrderDTO implements Serializable {

    private Long mainOrderId;
    //所有订单总金额
    private BigDecimal totalMoney;
    private String orderNumber;


    private List<OrderDTO> orderDTOList;
}
