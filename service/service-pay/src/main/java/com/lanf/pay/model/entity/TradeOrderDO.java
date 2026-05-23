package com.lanf.pay.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lanf.api.pay.model.enums.PayChannelEnum;
import com.lanf.api.pay.model.enums.PayMethodEnum;
import com.lanf.api.pay.model.enums.TradePurposeEnum;
import com.lanf.mybatis.base.BaseEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * <p>
 * 交易订单
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-06-14
 */
@Data
@TableName("trade_order")
public class TradeOrderDO extends BaseEntity {

private static final long serialVersionUID=1L;



    /**
     * 批量支付单id
     */
    private Long bathPayOrderId;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 订单id
     */
    private Long orderId;

    /**
     *  订单编号
     */
    private String orderNumber;

    /**
     * 交易订单号，与三方支付单唯一关联号。如果是单笔付款实施生成，如果是批量付款，批量订单号
     */
    private String outTradeNo;

    /**
     * 交易金额
     */
    private BigDecimal tradeMoney;
    /**
     * 交易用途 0：实时下单 ，1：钱包充值
     * 支付回调通知时更新
     */
    private TradePurposeEnum tradePurpose;
    /**
     * 下单付款方式：0：三方支付，1：钱包余额
     * 支付回调通知时更新
     */
    private PayMethodEnum payMethod;
    /**
     * 支付方式：0：支付宝，1：微信，2：银行卡
     */
    private PayChannelEnum payType;

    /**
     * 支付状态 0:待支付 1.支付完成
     */
    private Integer payStatus;

    /**
     * 0:单笔付款，1：批量付款
     */
    private Integer bathPay;
    /**
     * 过期时间
     */
    private Date expireTime;

    /**
     * 过期时间间隔（秒）
     */
    private Integer expireInterval;

    private String passBackParams;

    private Long version;




}
