package com.lanf.order.model.bo;

import lombok.Data;

import java.io.Serializable;

@Data
public class OrderIdAndUserId implements Serializable {

    private Long orderId;

    private Long userId;
    /**
     * 用于排序
     */
    private Integer sort;

}
