package com.lanf.finance.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lanf.finance.model.enums.RecordTypeEnum;
import com.lanf.mybatis.base.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

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


    /**
     * 流水号
     */
    private String flowNo;

    @ApiModelProperty(value = "商家id")
    private Long businessId;

    @ApiModelProperty(value = "业务订单id")
    private Long bizOrderId;

    @ApiModelProperty(value = "0:下单, 1:售后退款, 2: 取消订单退款, 3: 平台结算支出 , 4: 商家结算收入 ")
    private RecordTypeEnum recordType;

    @ApiModelProperty(value = "收入支出金额")
    private BigDecimal incomeMoney;

    @ApiModelProperty(value = "收支账户")
    private String incomeAccount;

    @ApiModelProperty(value = "变更前账户余额")
    private BigDecimal beforeRemainMoney;

    @ApiModelProperty(value = "变更后账户余额")
    private BigDecimal afterRemainMoney;



}
