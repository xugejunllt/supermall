package com.lanf.pay.model.bo;

import lombok.Data;

import java.io.Serializable;

@Data
public class CancelPayOrderContext implements Serializable {

    private Long tradeOrderId;

    private String outTradeNo;

}
