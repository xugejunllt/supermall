package com.lanf.pay.model.bo;

import com.lanf.client.pay.model.enums.TradePurposeEnum;
import lombok.Data;

import java.io.Serializable;

/**
 * 支付回调参数
 */
@Data
public class PassbackParams implements Serializable {


    /**
     * 交易单id
     * 如果是组合付款 那么是bathTradeOrderId
     */
    private Long tradeOrderId;

    //是否是批量付款
    private  Boolean bathPay;

    private TradePurposeEnum tradeType;
}
