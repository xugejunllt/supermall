package com.lanf.order.model.dto;

import lombok.Data;

import java.io.Serializable;
@Data
public class CancelOrderDTO implements Serializable {

    private Long orderId;

    private Integer cancelSource;
}
