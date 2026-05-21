package com.lanf.pay.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lanf.api.pay.model.enums.PayChannelEnum;
import com.lanf.mybatis.base.BaseEntity;
import com.lanf.pay.model.enums.RefundFlowStatusEnum;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * <p>
 * 退款单流水
 * </p>
 *
 * @author jarven
 * @since 2026-05-02
 */
@Data
@TableName("refund_order_flow")
public class RefundOrderFlowDO extends BaseEntity {

private static final long serialVersionUID=1L;

    private Long refundOrderId;

    /**
     * 商户订单号
     */
    private String outTradeNo;

    /**
     * 退款请求号， 标识一次退款请求
     */
    private String outRequestNo;

    /**
     * 支付宝交易号
     */
    private String tradeNo;

    /**
     * 实际支付的金额
     */
    private BigDecimal payMoney;

    /**
     * 实际退款金额
     */
    private BigDecimal returnMoney;


    /**
     * 0:退款成功 1：退款失败
     */
    private RefundFlowStatusEnum status;




    private PayChannelEnum payChannel;

    /**
     * 退款完成时间
     */
    private Date payFinishTime;

    private String payFinishDate;
    /**
     * 失败原因
     */
    private String failReason;


}
