package com.lanf.pay.model.bo;

import com.lanf.pay.model.enums.CompensatePaymentStatusEnum;
import lombok.Data;

import java.io.Serializable;

@Data
public class QueryThirdPartyPaymentStatusBO implements Serializable {

    private CompensatePaymentStatusEnum paymentStatus;

    private TradeStatusBO tradeStatusBO;

    public QueryThirdPartyPaymentStatusBO(CompensatePaymentStatusEnum paymentStatus, TradeStatusBO tradeStatusBO) {
        this.paymentStatus = paymentStatus;
        this.tradeStatusBO = tradeStatusBO;
    }
}
