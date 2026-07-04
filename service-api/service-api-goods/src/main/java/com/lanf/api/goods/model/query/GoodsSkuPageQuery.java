package com.lanf.api.goods.model.query;

import com.lanf.constant.model.query.PageQuery;
import lombok.Data;

/**
 * 商品SKU分页查询
 *
 * @author lanf
 */
@Data
public class GoodsSkuPageQuery extends PageQuery {

    private static final long serialVersionUID = 1L;

    /**
     * 商品ID
     */
    private Long goodsId;

}
