package com.lanf.client.pay.model.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class ContrastPayOrderDTO implements Serializable {


    private Long businessId;

    private String tradeFinishTimeFormat;
    //支付类型 0支付宝 1微信 2银联
    private Integer payType;




}
