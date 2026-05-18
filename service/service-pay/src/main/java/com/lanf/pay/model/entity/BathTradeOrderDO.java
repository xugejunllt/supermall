package com.lanf.pay.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lanf.mybatis.base.BaseEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * <p>
 * 批量交易订单
 * </p>
 *
 * @author jarven
 * @since 2025-12-28
 */
@Data
@TableName("bath_trade_order")
public class BathTradeOrderDO extends BaseEntity {

private static final long serialVersionUID=1L;

    /**
     * 商家id
     */
    private Long businessId;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 主订单id
     */
    private Long mainOrderId;
    /**
     * 唯一索引，避免重复插入
     */
    private  String mainOrderNumber;

    /**
     * 交易订单号，与三方支付单唯一关联号
     */
    private String outTradeNo;

    /**
     * 付款总笔数
     */
    private Integer batchNum;

    /**
     * 付款总金额
     */
    private BigDecimal batchFee;

    /**
     * 支付状态 0:待支付 1.支付完成 3.合并转单笔
     * 0 > 1  0 > 3
     *
     *
     */
    private Integer payStatus;
    /**
     * 过期时间
     */
    private Date expireTime;

    /**
     * 过期时间间隔
     */
    private Integer expireInterval;

    private String passbackParams;

    private Long version;

}
