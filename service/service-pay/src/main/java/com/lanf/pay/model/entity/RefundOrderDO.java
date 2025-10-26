package com.lanf.pay.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lanf.mybatis.base.BaseEntity;
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

    //交易号
    private String tradeNo;

    //退款金额
    private BigDecimal returnMoney;

    //支付订单id
    private Long payOrderId;


    //退款完成时间
    private Date payFinishTime;
}
