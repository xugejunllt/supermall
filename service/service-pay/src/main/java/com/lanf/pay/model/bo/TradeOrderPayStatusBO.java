package com.lanf.pay.model.bo;

import com.lanf.api.pay.model.vo.OutTradeNoAndPayType;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class TradeOrderPayStatusBO implements Serializable {

    /**
     * 支付成功的交易单
     */
    private List<OutTradeNoAndPayType> successPayList;
    /**
     * 等待付款交易单
     */
    private List<OutTradeNoAndPayType> waitPayList;
}
