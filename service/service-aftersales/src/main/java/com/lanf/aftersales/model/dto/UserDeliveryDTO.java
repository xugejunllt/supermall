package com.lanf.aftersales.model.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserDeliveryDTO implements Serializable {

    private Long id;
    /**
     * 快递编号
     */
    private String expressNumber;

    private String expressCompany;

}
