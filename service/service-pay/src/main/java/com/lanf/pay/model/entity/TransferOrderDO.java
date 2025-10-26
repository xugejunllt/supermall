package com.lanf.pay.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
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


    @ApiModelProperty(value = "业务来源,0:结算转账给商家")
    private Integer source;

    @ApiModelProperty(value = "商家id")
    private Long shopId;

    @ApiModelProperty(value = "用户id")
    private Long userId;

    @ApiModelProperty(value = "收款用户类型0:商家,1平台用户")
    private Integer toUserType;

    @ApiModelProperty(value = "商家侧唯一订单号")
    private String outBizNo;

    @ApiModelProperty(value = "收款账户类型 0:支付宝 1:银行卡")
    private Integer toAccountType;

    @ApiModelProperty(value = "收款账号")
    private String incomeAccount;

    @ApiModelProperty(value = "支付类型 0:支付宝,1:微信")
    private Integer payType;

    @ApiModelProperty(value = "订单总金额")
    private BigDecimal transAmount;

    @ApiModelProperty(value = "转账订单号")
    private String orderId;

    //转账完成时间
    private Date payFinishTime;
    //转账业务的标题，用于在支付宝用户的账单里显示
    private String orderTitle;
}
