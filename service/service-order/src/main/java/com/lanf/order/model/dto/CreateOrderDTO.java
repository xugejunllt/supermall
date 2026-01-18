package com.lanf.order.model.dto;

import com.lanf.welfare.model.bo.DiscountInfoBO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
public class CreateOrderDTO implements Serializable {

    @ApiModelProperty(value = "店铺id")
    private Long shopId;

    @ApiModelProperty(value = "用户id")
    private Long userId;

    @ApiModelProperty(value = "订单编号")
    private String orderNumber;

    @ApiModelProperty(value = "订单金额")
    private BigDecimal totalMoney;

    @ApiModelProperty(value = "实付金额")
    private BigDecimal actualPayMoney;

    @ApiModelProperty(value = "优惠金额")
    private BigDecimal discountAmount;

    @ApiModelProperty(value = "优惠信息")
    private List<DiscountInfoBO> discountInfoBO;

    @ApiModelProperty(value = "收货地址")
    private TakeAddressDTO takeAddressBO;

    private OrderItemDTO orderItem;
}
