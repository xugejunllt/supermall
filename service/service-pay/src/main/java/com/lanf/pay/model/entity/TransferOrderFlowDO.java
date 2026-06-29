package com.lanf.pay.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lanf.api.pay.model.enums.PayChannelEnum;
import com.lanf.mybatis.base.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * <p>
 * 转账单
 * </p>
 *
 * @author jarven
 * @since 2026-05-02
 */
@Data
@TableName("transfer_order_flow")
public class TransferOrderFlowDO extends BaseEntity {

private static final long serialVersionUID=1L;


    private String outTradeNo;

    @ApiModelProperty(value = "转账渠道：1-支付宝，2-微信支付，3-银行卡")
    private PayChannelEnum transferChannel;

    @ApiModelProperty(value = "转账来源账户")
    private String fromAccount;

    @ApiModelProperty(value = "收款账号")
    private String incomeAccount;

    @ApiModelProperty(value = "订单总金额，即发起转账时传入的金额")
    private BigDecimal totalAmount;

    @ApiModelProperty(value = "转账完成时间")
    private Date payFinishTime;

    private String payFinishDate;






}
