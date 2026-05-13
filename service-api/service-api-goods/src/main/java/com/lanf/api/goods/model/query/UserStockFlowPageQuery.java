package com.lanf.api.goods.model.query;


import com.lanf.constant.model.query.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 库存流水分页查询
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UserStockFlowPageQuery extends PageQuery {

    /** SKU编码 */
    private String skuCode;

    /** 库存ID */
    private Long userStockId;

    /** 仓库ID */
    private Long warehouseId;

    /** 订单ID */
    private Long orderId;

}
