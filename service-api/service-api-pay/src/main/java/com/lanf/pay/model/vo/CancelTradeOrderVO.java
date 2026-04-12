package com.lanf.pay.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class CancelTradeOrderVO implements Serializable {
    /**
     * 支付成功的交易单
     */
    private List<OutTradeNoAndPayType> successPayList;
    /**
     * 等待付款交易单
     */
    private List<OutTradeNoAndPayType> waitPayList;


}
