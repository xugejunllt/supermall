package com.lanf.finance.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lanf.finance.model.enums.LiquidationTypeEnum;
import com.lanf.mybatis.base.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 结算单明细
 */
@Data
@TableName("clearing_detail")
public class ClearingDetailDO extends BaseEntity {

private static final long serialVersionUID=1L;

    @ApiModelProperty(value = "商家id")
    private Long merchantId;

    @ApiModelProperty(value = "清分单id")
    private Long liquidationId;
    /**
     * 实际转账金额
     */
    private BigDecimal transferMoney;

    @ApiModelProperty(value = "清分单类型 0:商家收入")
    private LiquidationTypeEnum liquidationType;

    @ApiModelProperty(value = "费率百分比")
    private BigDecimal rate;

    @ApiModelProperty(value = "收入支出金额")
    private BigDecimal incomeMoney;

}
