package com.lanf.pay.model.bo;

import com.lanf.pay.model.entity.TradeOrderDO;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class PostTradeSuccessHandlerContext implements Serializable {

    private Integer payType;
    /**
     * 用户实际支付金额
     */
    private BigDecimal payMoney;

    private TradeOrderDO tradeOrderDO;
}
