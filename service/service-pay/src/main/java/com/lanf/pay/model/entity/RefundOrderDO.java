package com.lanf.pay.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lanf.mybatis.base.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * <p>
 * 退款单
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-08-27
 */
@Data
@TableName("refund_order")
public class RefundOrderDO extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "商户订单号")
    private String outTradeNo;

    /**
     * 退款请求号， 标识一次退款请求
     * 全部退款时 outRequestNo = outTradeNo
     * 部分退款 根据实际的业务场景生成 保证唯一性
     */
    private String outRequestNo;

    @ApiModelProperty(value = "支付宝交易号")
    private String tradeNo;

    @ApiModelProperty(value = "退款金额")
    private BigDecimal returnMoney;

    @ApiModelProperty(value = "用户的登录id【示例值】159****5620")
    private String buyerLogonId;

    @ApiModelProperty(value = " 退款事件类型: 0: 取消已支付的订单(全部退款)")
    private Integer refundEventType;

    @ApiModelProperty(value = "取消订单的来源：0:用户手动取消")
    private Integer cancelSource;

    @ApiModelProperty(value = "支付订单ID")
    private Long payOrderId;
    /**
     *  0:全部退款 1：部分退款
     *
     */
    private Integer partialRefund;

    @ApiModelProperty(value = "支付类型：0-支付宝，1-微信，2-银联")
    private Integer payType;

    @ApiModelProperty(value = "退款原因")
    private String refundReason;

    @ApiModelProperty(value = "退款完成时间")
    private Date payFinishTime;


}
