package com.lanf.finance.mq.message;

import com.lanf.constant.mq.base.BaseMessage;
import com.lanf.finance.model.enums.RecordTypeEnum;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AddMoneyFlowMessage extends BaseMessage {

    private static final long serialVersionUID = 1L;
    private String flowNo;
    private Long businessId;

    private Long bizOrderId;

    private RecordTypeEnum recordType;

    private BigDecimal incomeMoney;
}
