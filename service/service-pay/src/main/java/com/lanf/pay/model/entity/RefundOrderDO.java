package com.lanf.pay.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lanf.client.pay.model.enums.PayChannelEnum;
import com.lanf.client.pay.model.enums.RefundEventTypeEnum;
import com.lanf.mybatis.base.BaseEntity;
import com.lanf.pay.model.enums.RefundStatusEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

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

    @ApiModelProperty(value = "退款金额")
    private BigDecimal returnMoney;
    /**
     * 0:退款中 1：退款成功 2：退款失败
     */
    private RefundStatusEnum status;

    @ApiModelProperty(value = " 退款事件类型")
    private RefundEventTypeEnum refundEventType;

    @ApiModelProperty(value = "支付类型：0-支付宝，1-微信，2-银联")
    private PayChannelEnum payChannel;

    @ApiModelProperty(value = "业务单id")
    private Long bizOrderId;

    @ApiModelProperty(value = "退款原因")
    private String refundReason;

    private Long version;






}
