package com.lanf.order.model.bo;

import lombok.Data;

import java.io.Serializable;

@Data
public class CancelOrderOrderStatusBO implements Serializable {

    private Integer currentOrderStatus;
}
