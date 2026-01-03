package com.lanf.goods.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lanf.mybatis.base.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * <p>
 * 库存流水
 * </p>
 *
 * @author jarven
 * @since 2026-01-03
 */
@Data
@TableName("user_stock_flow")
public class UserStockFlowDO extends BaseEntity {

private static final long serialVersionUID=1L;


    @ApiModelProperty(value = "库存id")
    private Long userStockId;

    @ApiModelProperty(value = "订单id")
    private Long orderId;

    @ApiModelProperty(value = "0:下单出库，1：取消订单入库")
    private Integer eventType;

    @ApiModelProperty(value = "变更前的数量")
    private Integer beforeQuantity;

    @ApiModelProperty(value = "变更后数量")
    private Integer afterQuantity;

    @ApiModelProperty(value = "出库数量")
    private Integer outQuantity;

    @ApiModelProperty(value = "入库数量")
    private Integer inQuantity;



}
