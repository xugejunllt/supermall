package com.lanf.finance.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lanf.finance.model.enums.LiquidationStatusEnum;
import com.lanf.mybatis.base.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
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

    @ApiModelProperty(value = "订单id")
    private Long orderId;
    /**
     * 结算状态 0:待结算 ，1：已结算，2：已取消
     */
    private LiquidationStatusEnum status;

    @ApiModelProperty(value = "支付金额")
    private BigDecimal payMoney;

    private Integer payType;
    /**
     * 售后过期时间
     */
    private Date afterSaleExpireTime;

    private Long version;





}
