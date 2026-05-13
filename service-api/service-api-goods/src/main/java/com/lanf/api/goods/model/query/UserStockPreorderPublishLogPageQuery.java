package com.lanf.api.goods.model.query;


import com.lanf.constant.model.query.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 库存预售发布日志分页查询
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UserStockPreorderPublishLogPageQuery extends PageQuery {

    /** SKU编码 */
    private String skuCode;

    /** 库存ID */
    private Long stockId;

    /** 仓库ID */
    private Long warehouseId;

    /** 发布平台 */
    private Integer publishPlatform;

    /** 状态 */
    private Integer status;

}
