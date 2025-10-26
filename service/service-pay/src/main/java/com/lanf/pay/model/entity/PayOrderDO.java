package com.lanf.pay.model.entity;

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


    @ApiModelProperty(value = "订单编号 ")
    private String orderNumber;

    @ApiModelProperty(value = "批量支付单id")
    private Long bathPayOrderId;

    @ApiModelProperty(value = "用户id")
    private Long userId;

    @ApiModelProperty(value = "店铺id")
    private Long shopId;

    @ApiModelProperty(value = "业务订单id")
    private Long bizOrderId;

    @ApiModelProperty(value = "交易单id")
    private Long tradeOrderId;


    @ApiModelProperty(value = "支付类型 0支付宝 1微信 2银联 ")
    private Integer payType;

    @ApiModelProperty(value = "支付状态 0:待支付 1.支付完成")
    private Integer payStatus;

    @ApiModelProperty(value = "支付金额")
    private BigDecimal payMoney;

    @ApiModelProperty(value = "实收金额")
    private BigDecimal receiptMoney;

    @ApiModelProperty(value = "商户订单号 如果是单笔支付，就是order_number;如果是批量支付，就是批量付款单的号")
    private String outTradeNo;

    @ApiModelProperty(value = "用户支付完成时间")
    private Date payFinishTime;

    private String tradeFinishTimeFormat;

    @ApiModelProperty(value = "支付账户")
    private String payAccount;

    @ApiModelProperty(value = "收款账户")
    private String incomeAccount;


    @ApiModelProperty(value = "通知时间")
    private Date notifyTime;

    /**
     * 支付宝交易号。支付宝交易凭证号。
     *
     *
     */
    private String tradeNo;


}
