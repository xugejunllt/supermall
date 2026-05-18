package com.lanf.pay.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lanf.mybatis.base.BaseEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * <p>
 * 支付订单
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-06-14
 */
@Data
@TableName("pay_order")
public class PayOrderDO extends BaseEntity {

private static final long serialVersionUID=1L;


    /**
     * 订单编号 
     */
    private String orderNumber;

    /**
     * 批量支付单id
     */
    private Long bathPayOrderId;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 店铺id
     */
    private Long shopId;

    /**
     * 业务订单id
     */
    private Long bizOrderId;

    /**
     * 交易单id
     */
    private Long tradeOrderId;


    /**
     * 支付类型 0支付宝 1微信 2银联 
     */
    private Integer payType;

    /**
     * 支付状态 0:待支付 1.支付完成
     */
    private Integer payStatus;

    /**
     * 支付金额
     */
    private BigDecimal payMoney;

    /**
     * 实收金额
     */
    private BigDecimal receiptMoney;

    /**
     * 商户订单号 如果是单笔支付，就是order_number;如果是批量支付，就是批量付款单的号
     */
    private String outTradeNo;

    /**
     * 用户支付完成时间
     */
    private Date payFinishTime;

    private String tradeFinishTimeFormat;

    /**
     * 支付账户
     */
    private String payAccount;

    /**
     * 收款账户
     */
    private String incomeAccount;


    /**
     * 通知时间
     */
    private Date notifyTime;

    /**
     * 支付宝交易号。支付宝交易凭证号。
     *
     *
     */
    private String tradeNo;


}
