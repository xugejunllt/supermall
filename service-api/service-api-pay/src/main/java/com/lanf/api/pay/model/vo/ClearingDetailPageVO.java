package com.lanf.api.pay.model.vo;

import com.lanf.api.pay.model.enums.ClearingStatusEnum;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class ClearingDetailPageVO implements Serializable {

    private Long id;

    private Long orderId;

    private BigDecimal payMoney;

    private Date afterSaleExpireTime;

    private Long tenantId;

    private ClearingStatusEnum status;

    private RecipientTypeEnum recipientType;

    private BigDecimal rate;

    private BigDecimal incomeMoney;

    private BigDecimal transferMoney;

    private Long version;

    private Date createTime;
}
