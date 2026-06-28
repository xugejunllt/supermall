package com.lanf.pay.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lanf.api.pay.model.enums.PayChannelEnum;
import com.lanf.api.pay.model.enums.TransferEventTypeEnum;
import com.lanf.mybatis.base.BaseEntity;
import com.lanf.pay.model.enums.TransferStatusEnum;
import lombok.Data;

import java.math.BigDecimal;

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



    /**
     * 商家侧唯一订单号
     */
    private String outTradeNo;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 商家id
     */
    private Long merchantId;

    /**
     * 关联事件对应的业务单id
     */
    private Long bizOrderId;
    /**
     * 转账来源账户
     */
    private String fromAccount;

    /**
     * 收款账号
     */
    private String incomeAccount;

    private String incomeAccountUserName;
    /**
     * 事件类型 0：订单结算给商家，1：用户钱包提现
     */
    private TransferEventTypeEnum eventType;

    private PayChannelEnum transferChannel;

    /**
     * 订单总金额，即发起转账时传入的金额
     */
    private BigDecimal totalAmount;



    /**
     * 0:退款中 1：退款成功 2：退款失败
     */
    private TransferStatusEnum status;

    private Long version;

}
