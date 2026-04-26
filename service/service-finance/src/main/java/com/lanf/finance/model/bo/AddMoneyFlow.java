package com.lanf.finance.model.bo;

import com.lanf.finance.model.enums.RecordTypeEnum;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class AddMoneyFlow implements Serializable {
    private Long businessId;

    private Long bizOrderId;

    private RecordTypeEnum recordType;

    private BigDecimal incomeMoney;


}
