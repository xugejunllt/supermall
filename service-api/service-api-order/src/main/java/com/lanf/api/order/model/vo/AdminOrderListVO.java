package com.lanf.api.order.model.vo;

import lombok.Data;

import java.io.Serializable;
@Data
public class AdminOrderListVO implements Serializable {

    private Long orderId;


    private String orderNumber;



    private Integer sort;
}
