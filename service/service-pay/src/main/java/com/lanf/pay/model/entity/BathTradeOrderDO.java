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
    /**
     * 唯一索引，避免重复插入
     */
    private  String mainOrderNumber;

    @ApiModelProperty(value = "交易订单号，与三方支付单唯一关联号")
    private String outTradeNo;

    @ApiModelProperty(value = "付款总笔数")
    private Integer batchNum;

    @ApiModelProperty(value = "付款总金额")
    private BigDecimal batchFee;

    /**
     * 批量付款取消标识
     * 取消合并 0:未取消，1:已取消
     */
    private Integer cancelMerge;

    @ApiModelProperty(value = "支付状态 0:待支付 1.支付完成 3:已取消")
    private Integer payStatus;


}
