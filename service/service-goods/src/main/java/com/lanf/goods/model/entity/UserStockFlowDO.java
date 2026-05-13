package com.lanf.goods.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lanf.constant.model.enums.goods.UserStockFlowEventTypeEnum;
import com.lanf.mybatis.base.BaseEntity;
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


    private String flowNo;

    /** 库存id */
    private Long userStockId;

    private String skuCode;

    private Long warehouseId;

    /** 订单id */
    private Long orderId;


    private UserStockFlowEventTypeEnum eventType;

    /** 变更前的数量 */
    private Integer beforeQuantity;
    
    /**
     * 变更数量
     */
    private Integer changeQuantity;

    /** 变更后数量 */
    private Integer afterQuantity;

}
