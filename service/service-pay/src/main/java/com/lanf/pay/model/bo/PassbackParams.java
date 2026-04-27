package com.lanf.pay.model.bo;

import com.lanf.pay.model.enums.TradeTypeEnum;
import lombok.Data;

import java.io.Serializable;

/**
 * 支付回调参数
 */
@Data
public class PassbackParams implements Serializable {

    private Long bathTradeOrderId;
    /**
     * 交易单id
     */
    private Long tradeOrderId;

    //是否是批量付款
    private  Boolean bathPay;

    private TradeTypeEnum tradeType;
}
