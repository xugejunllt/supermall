package com.lanf.order.model.dto;

import com.lanf.order.model.bo.OrderInitParamsBO;
import lombok.Data;

import java.util.List;

@Data
public class PlaceOrderDTO implements java.io.Serializable{


    private Long goodsId;

    private String skuCode;

    private Long warehouseId;

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

    /**
     * 方法里面填充订单初始化参数
     */
    private OrderInitParamsBO orderInitParamsBO;
}
