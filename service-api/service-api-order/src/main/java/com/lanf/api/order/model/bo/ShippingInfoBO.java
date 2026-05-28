package com.lanf.api.order.model.bo;

import lombok.Data;

import java.io.Serializable;

@Data
public class ShippingInfoBO implements Serializable {

    /**
     * 物流公司名称
     */
    private String logisticsCompany;

    /**
     * 物流单号
     */
    private String trackingNumber;
}
