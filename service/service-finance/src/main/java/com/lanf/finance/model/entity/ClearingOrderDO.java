package com.lanf.finance.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lanf.finance.model.enums.ClearingOrderStatusEnum;
import com.lanf.mybatis.base.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**

 结算单
 */
@Data
@TableName("clearing_order")
public class ClearingOrderDO extends BaseEntity {

private static final long serialVersionUID=1L;

    @ApiModelProperty(value = "订单id")
    private Long orderId;

    @ApiModelProperty(value = "支付金额")
    private BigDecimal payMoney;
    /**
     * 售后过期时间
     */
    private Date afterSaleExpireTime;

    private Long version;





}
