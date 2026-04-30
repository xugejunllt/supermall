package com.lanf.pay.model.bo;

import com.lanf.client.pay.model.enums.PayChannelEnum;
import com.lanf.pay.model.enums.ReconciliationBusinessTypeEnum;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class ReconciliationTradeInfo implements Serializable {

    private String outTradeNo;

    private BigDecimal receiptMoney;

    private PayChannelEnum payChannel;

    private ReconciliationBusinessTypeEnum reconciliationBusinessType;

}
