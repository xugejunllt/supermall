package com.lanf.rocketmq.model.message;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 交易成功事件消息
 */
@Data
public class TradeSuccessEventMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 交易单ID
     */
    private Long tradeOrderId;

    /**
     * 业务订单ID
     */
    private Long bizOrderId;

    /**
     * 批量支付单ID（如果是批量支付）
     */
    private Long bathPayOrderId;

    /**
     * 交易订单号
     */
    private String outTradeNo;

    /**
     * 订单编号
     */
    private String orderNumber;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 交易金额
     */
    private BigDecimal tradeMoney;

    /**
     * 支付类型 0支付宝 1微信 2银联
     */
    private Integer payType;

    /**
     * 支付完成时间
     */
    private Date payFinishTime;

    /**
     * 是否批量支付
     */
    private Boolean bathPay;

    /**
     * 主订单ID（批量支付时使用）
     */
    private Long mainOrderId;

    /**
     * 主订单编号（批量支付时使用）
     */
    private String mainOrderNumber;
}
