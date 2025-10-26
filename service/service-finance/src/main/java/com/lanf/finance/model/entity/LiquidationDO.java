package com.lanf.finance.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lanf.mybatis.base.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * <p>
 * 清算单

 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-06-20
 */
@Data
@TableName("liquidation")
public class LiquidationDO extends BaseEntity {

private static final long serialVersionUID=1L;



    @ApiModelProperty(value = "交易单id")
    private Long orderId;

    //订单来源 0:用户下单支付,1:履约完成,2:用户订单退款

    private Integer source;

    @ApiModelProperty(value = "支付金额")
    private BigDecimal payMoney;









}
