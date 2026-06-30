package com.lanf.pay.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lanf.mybatis.base.BaseEntity;
import com.lanf.api.pay.model.enums.ClearingStatusEnum;
import com.lanf.api.pay.model.vo.RecipientTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 结算单明细
 */
@Data
@TableName("clearing_detail")
public class ClearingDetailDO extends BaseEntity {

private static final long serialVersionUID=1L;

    private Long orderId;

    private BigDecimal payMoney;

    private Date afterSaleExpireTime;

    private Long tenantId;

    private ClearingStatusEnum status;

    private RecipientTypeEnum recipientType;

    @ApiModelProperty(value = "费率百分比")
    private BigDecimal rate;

    @ApiModelProperty(value = "收入金额")
    private BigDecimal incomeMoney;

    @ApiModelProperty(value = "实际转账金额")
    private BigDecimal transferMoney;

    private Long version;



}
