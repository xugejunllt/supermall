package com.lanf.order.model.bo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class FromAddressJson implements Serializable {


    private String area;

    /**
     * 详细地址
     */
    private String address;

    /**
     * 纬度
     */
    private BigDecimal latitude;
    /**
     * 经度
     */
    private BigDecimal longitude;
}
