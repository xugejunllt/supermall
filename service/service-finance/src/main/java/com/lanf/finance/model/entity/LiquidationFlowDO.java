package com.lanf.finance.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lanf.mybatis.base.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

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



    @ApiModelProperty(value = "清分单id")
    private Long liquidationId;

    @ApiModelProperty(value = "费率百分比")
    private BigDecimal rate;

    @ApiModelProperty(value = "0:收入 1:支出")
    private Integer income;

    @ApiModelProperty(value = "收入支出金额")
    private BigDecimal incomeMoney;

    private Long shopId;

    private Integer accountType;

    private String incomeAccount;
    //支付完成时间
    private Date payFinishTime;

}
