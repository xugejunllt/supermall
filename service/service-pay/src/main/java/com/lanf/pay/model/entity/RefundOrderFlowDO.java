package com.lanf.pay.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lanf.client.pay.model.enums.PayChannelEnum;
import com.lanf.mybatis.base.BaseEntity;
import com.lanf.pay.model.enums.RefundFlowStatusEnum;
import io.swagger.annotations.ApiModelProperty;
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


    @ApiModelProperty(value = "商户订单号")
    private String outTradeNo;

    @ApiModelProperty(value = "退款请求号， 标识一次退款请求")
    private String outRequestNo;

    @ApiModelProperty(value = "支付宝交易号")
    private String tradeNo;

    @ApiModelProperty(value = "实际支付的金额")
    private BigDecimal payMoney;

    @ApiModelProperty(value = "实际退款金额")
    private BigDecimal returnMoney;

    @ApiModelProperty(value = "用户的登录id【示例值】159****5620")
    private String buyerLogonId;

    /**
     * 0:退款成功 1：退款失败
     */
    private RefundFlowStatusEnum status;


    @ApiModelProperty(value = "支付订单ID")
    private Long payOrderId;

    private PayChannelEnum payChannelEnum;

    @ApiModelProperty(value = "退款完成时间")
    private Date payFinishTime;

    private String payFinishDate;
    /**
     * 失败原因
     */
    private String failReason;


}
