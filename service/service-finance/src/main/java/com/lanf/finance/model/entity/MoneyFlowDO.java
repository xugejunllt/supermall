package com.lanf.finance.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.lanf.mybatis.base.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * <p>
 * 资金流水
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-06-22
 */
@Data
@TableName("money_flow")
public class MoneyFlowDO extends BaseEntity {

    private static final long serialVersionUID = 1L;



    @ApiModelProperty(value = "结算单id")
    private Long settlementFlowId;

    @ApiModelProperty(value = "订单id")
    private Long orderId;

    @ApiModelProperty(value = "商家id")
    private Long shopId;

    @ApiModelProperty(value = "科目名称")
    private String incomeSubjectName;

    @ApiModelProperty(value = "0:收入，1:支出")
    private Integer income;

    @ApiModelProperty(value = "来源 0:用户下单")
    private Integer source;

    @ApiModelProperty(value = "收入支出金额")
    private BigDecimal incomeMoney;

    @ApiModelProperty(value = "账户类型0:支付宝")
    private Integer accountType;

    @ApiModelProperty(value = "收支账户")
    private String incomeAccount;

    @ApiModelProperty(value = "变更前账户余额")
    private BigDecimal beforeRemainMoney;

    @ApiModelProperty(value = "变更后账户余额")
    private BigDecimal afterRemainMoney;

    @ApiModelProperty(value = "交易完成时间")
    private Date tradeFinishTime;

    @ApiModelProperty(value = "交易完成时间 ")
    private String tradeFinishTimeFormat;

}
