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



    @ApiModelProperty(value = "批量支付单id")
    private Long bathPayOrderId;

    @ApiModelProperty(value = "用户id")
    private Long userId;

    @ApiModelProperty(value = "订单id")
    private Long orderId;

    /**
     *  订单编号
     */
    private String orderNumber;

    @ApiModelProperty(value = "交易订单号，与三方支付单唯一关联号。如果是单笔付款实施生成，如果是批量付款，批量订单号")
    private String outTradeNo;

    @ApiModelProperty(value = "交易金额")
    private BigDecimal tradeMoney;


    @ApiModelProperty(value = "支付状态 0:待支付 1.支付完成 3:已取消")
    private Integer payStatus;

    @ApiModelProperty(value = "0:单笔付款，1：批量付款")
    private Integer bathPay;
    @ApiModelProperty(value = "过期时间")
    private Date expireTime;

    @ApiModelProperty(value = "过期时间间隔（秒）")
    private Integer expireInterval;
    private Long version;




}
