package com.lanf.aftersales.model.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class AddAfterSalesOrderDTO implements Serializable {

    /**
     * 订单id
     */
    private Long orderId;

    /**
     * 售后类型 0:退货退款 1:换货
     */
    private Integer afterSalesType;

    /**
     * 退款原因
     */
    private String returnReason;





}
