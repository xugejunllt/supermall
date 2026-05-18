package com.lanf.api.order.model.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class OrderVO2 implements Serializable {


    private Long id;
    /**
     * 收货地址
     */
    private String takeAddress;
    /**
     * 用户id
     */
    private Long userId;
}
