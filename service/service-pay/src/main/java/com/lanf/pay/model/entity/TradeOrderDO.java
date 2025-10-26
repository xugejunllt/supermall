package com.lanf.pay.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lanf.mybatis.base.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
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


    @ApiModelProperty(value = "订单编号")
    private String orderNumber;

    @ApiModelProperty(value = "用户id")
    private Long userId;
    //店铺id
    private Long shopId;


    @ApiModelProperty(value = "业务订单id")
    private Long bizOrderId;
    //批量支付单id
    private Long bathPayOrderId;

    /**
     * 来源 0:订单支付 1订单退款、2充值、3提现 4.售后单退款 5:订单支付结算转账给平台
     */
    private Integer source;

    @ApiModelProperty(value = "下单时间")
    private Date placeOrderTime;

    @ApiModelProperty(value = "订单金额")
    private BigDecimal orderMoney;

    @ApiModelProperty(value = "优惠金额")
    private BigDecimal discountMoney;

    // 支付状态 0:待支付 1.支付完成
    private Integer payStatus;


    @ApiModelProperty(value = "0：多笔订单支付 ，1：单笔支付")
    private Integer bathPay;




}
