package com.lanf.api.user.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class AddressListVO implements Serializable {

    private Long id;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 联系人
     */
    private String consignee;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 地区
     */
    private String area;
    
    /**
     * 详细地址
     */
    private String address;

    private String areaCode;
    /**
     * 纬度
     */
    private BigDecimal latitude;
    /**
     * 经度
     */
    private BigDecimal longitude;
    /**
     * 是否默认 0默认 1.不是
     */
    private Integer defaultAddress;

}
