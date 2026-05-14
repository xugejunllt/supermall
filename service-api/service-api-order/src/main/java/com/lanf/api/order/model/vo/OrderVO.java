package com.lanf.api.order.model.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
public class OrderVO implements Serializable {
    //订单商品总数量
    private Integer totalQuantity;

    @ApiModelProperty(value = "物流公司")
    private String expressCompany;

    private Long goodsId;

    private Long orderId;
    @ApiModelProperty(value = "店铺id")
    private Long shopId;
    @ApiModelProperty(value = "商家id")
    private Long businessId;
    //订单状态
    private Integer orderStatus;
    //履约完成时间
    private Date finishTime;
    private List<OrderItemVO> inOutStockOrderItemDTOList;
    //履约单退款状态--订单售后是否退款 0:未退款,1:已退款
    private Integer promiseOrderReturnMoney;

}
