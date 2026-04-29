package com.lanf.pay.model.bo;

import com.lanf.pay.model.entity.TradeOrderDO;
import lombok.Data;

import java.io.Serializable;

@Data
public class PostTradeSuccessHandlerContext implements Serializable {

    private Integer payType;
    /**
     * 用户实际支付金额
     */
    private Integer payMoney;

    private TradeOrderDO tradeOrderDO;
}
