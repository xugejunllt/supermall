package com.lanf.pay.model.vo;

import com.lanf.pay.model.enums.WalletEventTypeEnum;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class WalletAccountFlowPageVO implements Serializable {

    private String flowNo;


    private BigDecimal changeBalance;

    private WalletEventTypeEnum eventType;

    private Date createTime;
}
