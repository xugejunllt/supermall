package com.lanf.pay.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lanf.client.pay.model.enums.PayTypeEnum;
import com.lanf.client.pay.model.enums.TransferEventTypeEnum;
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
 * @author 江帅帅 Jss_forever
 * @since 2024-08-03
 */
@Data
@TableName("transfer_order")
public class TransferOrderDO extends BaseEntity {

private static final long serialVersionUID=1L;


    @ApiModelProperty(value = "商家侧唯一订单号")
    private String outBizNo;

    @ApiModelProperty(value = "用户id")
    private Long userId;

    @ApiModelProperty(value = "商家id")
    private Long merchantId;

    @ApiModelProperty(value = "关联事件对应的业务单id")
    private Long bizOrderId;

    @ApiModelProperty(value = "事件类型 0：订单结算给商家，1：用户钱包提现")
    private TransferEventTypeEnum eventType;

    @ApiModelProperty(value = "转账渠道：1-支付宝，2-微信支付，3-银行卡")
    private PayTypeEnum transferChannel;

    @ApiModelProperty(value = "转账来源账户")
    private String fromAccount;

    @ApiModelProperty(value = "收款账号")
    private String incomeAccount;

    @ApiModelProperty(value = "订单总金额")
    private BigDecimal transAmount;

    @ApiModelProperty(value = "转账订单号")
    private String orderId;

    @ApiModelProperty(value = "转账完成时间")
    private Date payFinishTime;
    /**
     * 添加索引 T+1查询加速
     */
    private String payFinishDate;
    @ApiModelProperty(value = "转账业务的标题，用于在支付宝用户的账单里显示")
    private String orderTitle;
}
