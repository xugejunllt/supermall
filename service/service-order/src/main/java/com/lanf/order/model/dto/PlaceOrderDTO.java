package com.lanf.order.model.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class PlaceOrderDTO implements java.io.Serializable{

    private String skuCode;
    //收货地址id
    private Long addressId;
    //店铺id
    private Long shopId;
    //下单sku数量
    private Integer quantity;
    //优惠卷id
    private List<Long> couponIds;
    //订单编号
    private  String orderNumber;

    //支付类型 0支付宝 1微信 2银联
    private Integer payType;
    @ApiModelProperty(value = "收货地址")
    private TakeAddressDTO takeAddress;







}
