package com.lanf.order.model.entity;

import com.lanf.mybatis.base.BaseEntity;
import lombok.Data;

import java.math.BigDecimal;

/**
 * <p>
 * 主订单
 * </p>
 *
 * @author jarven
 * @since 2026-02-27
 */
@Data
public class MainOrderDO extends BaseEntity {

    private static final long serialVersionUID=1L;


    /**
     * 订单编号
     */
    private String mainOrderNumber;

    private Long userId;

    /**
     * 总商品金额（所有子订单商品金额之和) 
     */
    private BigDecimal totalAmount;

    /**
     * 实际应付金额（通常 = 总商品金额 + 总运费 - 总优惠）
     */
    private BigDecimal paymentAmount;

    /**
     * 总运费（所有子订单运费之和）
     */
    private BigDecimal freightAmount;

    /**
     * 支付状态（0：待支付，1：已支付）
     */
    private Integer payStatus;

    private Long version;
    private Long tenantId;


}
