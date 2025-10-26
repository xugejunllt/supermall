package com.lanf.pay.model.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class TradeOrderApiVO implements Serializable {

    private Long id;
    @ApiModelProperty(value = "用户id")
    private Long userId;
    //店铺id
    private Long shopId;

    @ApiModelProperty(value = "商家id")
    private Long businessId;

    /**
     * 来源 0:订单支付 1订单退款、2充值、3提现 4.售后单退货退款
     */
    private Integer source;

    @ApiModelProperty(value = "实际支付金额")
    private BigDecimal actualPayMoney;
    //收款账户
    private String incomeAccount;
    //账户类型 0:支付宝
    private Integer accountType;
    //实际收入金额
    private BigDecimal receiptMoney;


}
