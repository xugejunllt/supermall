package com.lanf.api.pay.model.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class TradeOrderQuantitySumDTO implements Serializable {


    private Integer payType;

    private String payAccount;

    private Long businessId;

    private List<Integer> sources;



}
