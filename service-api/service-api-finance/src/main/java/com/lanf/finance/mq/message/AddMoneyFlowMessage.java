package com.lanf.finance.mq.message;

import com.lanf.finance.model.enums.RecordTypeEnum;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class AddMoneyFlowMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long businessId;

    private Long bizOrderId;

    private RecordTypeEnum recordType;

    private BigDecimal incomeMoney;
}
