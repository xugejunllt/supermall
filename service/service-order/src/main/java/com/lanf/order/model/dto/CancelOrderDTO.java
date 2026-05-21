package com.lanf.order.model.dto;

import lombok.Data;

import java.io.Serializable;
@Data
public class CancelOrderDTO implements Serializable {

    private Long orderId;

    private Long userId;

    private Integer cancelSource;

    private String remark;
    /**
     * 订单取消回调函数 通常是网络操作
     */
    private Runnable runnable ;
}
