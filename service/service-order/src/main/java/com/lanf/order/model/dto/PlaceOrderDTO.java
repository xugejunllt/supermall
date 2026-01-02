package com.lanf.order.model.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class PlaceOrderDTO implements java.io.Serializable{

    //skuid
    private Long skuId;
    //收货地址id
    private Long addressId;
    //店铺id
    private Long shopId;
    //下单sku数量
    private Integer quantity;
    //优惠卷id
    private List<Long> couponId;
    //订单编号
    private  String orderNumber;

    @ApiModelProperty(value = "支付类型 0支付宝 1微信 2银联 ")
    private Integer payType;
    /**
     * 填充字段
     */
    private Long orderId;

}
