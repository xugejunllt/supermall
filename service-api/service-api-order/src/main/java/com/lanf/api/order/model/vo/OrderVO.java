package com.lanf.api.order.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class OrderVO implements Serializable {
    //订单商品总数量
    private Integer totalQuantity;

    /**
     * 物流公司
     */
    private String expressCompany;

    private Long goodsId;

    private Long orderId;
    /**
     * 店铺id
     */
    private Long shopId;
    /**
     * 商家id
     */
    private Long businessId;
    //订单状态
    private Integer orderStatus;


    private List<OrderItemVO> inOutStockOrderItemDTOList;


}
