package com.lanf.finance.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lanf.finance.model.enums.LiquidationTypeEnum;
import com.lanf.mybatis.base.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * <p>
 * 平台清算流水
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-06-20
 */
@Data
@TableName("liquidation_flow")
public class LiquidationFlowDO extends BaseEntity {

private static final long serialVersionUID=1L;

    @ApiModelProperty(value = "商家id")
    private Long merchantId;

    @ApiModelProperty(value = "清分单id")
    private Long liquidationId;



    @ApiModelProperty(value = "清分单类型 0:商家收入")
    private LiquidationTypeEnum liquidationType;

    @ApiModelProperty(value = "费率百分比")
    private BigDecimal rate;

    @ApiModelProperty(value = "收入支出金额")
    private BigDecimal incomeMoney;

}
