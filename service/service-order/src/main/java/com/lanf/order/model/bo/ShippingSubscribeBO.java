package com.lanf.order.model.bo;


import lombok.Data;

import java.io.Serializable;

@Data
public class ShippingSubscribeBO implements Serializable {

    /**
     * 快递公司编码
     */
    private String logisticsCode;

    /**
     * 物流单号
     */
    private String trackingNumber;
}
