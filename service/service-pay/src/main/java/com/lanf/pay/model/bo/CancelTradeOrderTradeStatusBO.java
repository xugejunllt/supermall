package com.lanf.pay.model.bo;

import com.lanf.pay.model.enums.TradeStatusEnum;
import lombok.Data;

import java.io.Serializable;
@Data
public class CancelTradeOrderTradeStatusBO implements Serializable {

    private  String outTradeNo;

     private Integer payType;
    /**
     * 交易状态
     */
    private TradeStatusEnum tradeStatus;

}
