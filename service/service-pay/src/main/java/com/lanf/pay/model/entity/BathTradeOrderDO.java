package com.lanf.pay.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lanf.mybatis.base.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

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


    @ApiModelProperty(value = "用户id")
    private Long userId;

    @ApiModelProperty(value = "主订单id")
    private Long mainOrderId;

    @ApiModelProperty(value = "批量付款批次号")
    private String batchNo;

    @ApiModelProperty(value = "付款总笔数")
    private Integer batchNum;

    @ApiModelProperty(value = "付款总金额")
    private BigDecimal batchFee;




}
