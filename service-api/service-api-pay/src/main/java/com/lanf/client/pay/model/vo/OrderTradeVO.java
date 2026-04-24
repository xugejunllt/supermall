package com.lanf.client.pay.model.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 订单交易信息
 */
@Data
public class OrderTradeVO implements Serializable {

    private Long orderId;
    /**
     * 支付类型 0支付宝 1微信 2银联
     */
    private Integer payType;

    /**
     * 实际支付金额
     */
    private BigDecimal payMoney;

    /**
     * 订单金额
     */
    private BigDecimal orderMoney;
    /**
     * 来源 0:订单支付 1订单退款、2充值、3提现
     */
    private Integer source;

    private Date payFinishTime;
    //优惠金额
    private BigDecimal discountMoney;
    //优惠方式
    private Integer discountType;
    private String discountTypeName;
    private String payTypeName;
    //实收金额
    private BigDecimal receiptMoney;
    //收款账户
    private String incomeAccount;
    private Long shopId;
    //支付状态 0:待支付 1.支付完成
    private Integer payStatus;
}
