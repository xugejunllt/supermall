package com.lanf.order.model.vo;

import lombok.Data;

import java.io.Serializable;
@Data
public class AdminOrderListVO implements Serializable {

    private Long orderId;


    private String orderNumber;



    private Integer sort;
}
