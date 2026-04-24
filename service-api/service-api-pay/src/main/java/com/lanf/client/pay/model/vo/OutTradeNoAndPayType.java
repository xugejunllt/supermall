package com.lanf.client.pay.model.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class OutTradeNoAndPayType implements Serializable {

    private  String outTradeNo;

    private Integer payType;
}
