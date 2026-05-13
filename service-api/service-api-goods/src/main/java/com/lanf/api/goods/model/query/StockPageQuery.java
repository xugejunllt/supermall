package com.lanf.api.goods.model.query;


import com.lanf.constant.model.query.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 库存分页查询
 */
@Data
public class StockPageQuery extends PageQuery {

    /** SKU编码 */
    private String skuCode;

    /** 仓库ID */
    private Long warehouseId;

    /** 商品名称 */
    private String goodsName;

}
