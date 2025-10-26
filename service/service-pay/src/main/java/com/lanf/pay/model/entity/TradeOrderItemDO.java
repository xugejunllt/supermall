package com.lanf.pay.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lanf.mybatis.base.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * <p>
 * 
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-06-17
 */
@Data
@TableName("trade_order_item")
public class TradeOrderItemDO extends BaseEntity {

private static final long serialVersionUID=1L;


    @ApiModelProperty(value = "交易单id")
    private Long tradeOrderId;

    @ApiModelProperty(value = "如果是支付类型是3，就是优惠券id，其他是订单id")
    private Long bizOrderId;

    @ApiModelProperty(value = "支付类型  支付类型 0支付宝 1微信 2银联 3优惠券")
    private Integer payType;

    @ApiModelProperty(value = "交易金额")
    private BigDecimal tradeMoney;





}
