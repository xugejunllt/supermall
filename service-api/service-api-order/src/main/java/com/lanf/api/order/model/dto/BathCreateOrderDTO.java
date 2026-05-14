package com.lanf.api.order.model.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
public class BathCreateOrderDTO implements Serializable {


    @NotNull(message = "主订单id不能为空")
    private Long mainOrderId;

    @NotBlank(message = "订单编号不能为空")
    private  String mainOrderNumber;

    private Long userId;

    //总商品金额（所有子订单商品金额之和
    private BigDecimal totalAmount;

    @NotEmpty(message = "订单列表不能为空")
    private List<CreateOrderDTO> createOrderDTOList;

}
