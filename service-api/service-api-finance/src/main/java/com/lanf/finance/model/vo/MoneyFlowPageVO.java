package com.lanf.finance.model.vo;

import com.lanf.finance.model.enums.RecordTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class MoneyFlowPageVO implements Serializable {

    /**
     * 流水号
     */
    private String flowNo;

    @ApiModelProperty(value = "商家id")
    private Long tenantId;

    @ApiModelProperty(value = "业务订单id")
    private Long bizOrderId;

    private RecordTypeEnum recordType;

    @ApiModelProperty(value = "收入支出金额")
    private BigDecimal incomeMoney;

    @ApiModelProperty(value = "收支账户")
    private String incomeAccount;

    @ApiModelProperty(value = "变更前账户余额")
    private BigDecimal beforeRemainMoney;
    /**
     * 变更金额
     */
    private BigDecimal changeMoney;

    @ApiModelProperty(value = "变更后账户余额")
    private BigDecimal afterRemainMoney;

    private Long id;

    private Date createTime;

    private Date updateTime;


}
