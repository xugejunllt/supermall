package com.lanf.pay.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lanf.mybatis.base.BaseEntity;
import com.lanf.pay.model.enums.PayOrderFlowStatusEnum;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * <p>
 * 支付流水
 * </p>
 *
 * @author jarven
 * @since 2025-12-28
 */
@Data
@TableName("pay_order_flow")
public class PayOrderFlowDO extends BaseEntity {

private static final long serialVersionUID=1L;



    private Long tradeId;

    /**
     * 支付类型 0支付宝 1微信 2银联 
     */
    private Integer payType;
    /**
     * 交易单outTradeNo ，关联交易单
     */
    private String outTradeNo;
    /**
     * 0:交易成功 1：交易失败
     */
    private PayOrderFlowStatusEnum status;

    /**
     * 交易金额
     */
    private BigDecimal tradeMoney;

    /**
     * 实收金额
     */
    private BigDecimal receiptMoney;

    /**
     * 用户支付完成时间
     */
    private Date payFinishTime;
    /**
     * 添加索引 T+1查询加速
     */
    private String payFinishDate;

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
     */
    private String tradeNo;
    /**
     * 回调参数
     */
    private String passbackParams;

    private String allParams;
    /**
     * 失败原因
      */
    private String failReason;

}
