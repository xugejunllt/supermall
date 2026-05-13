package com.lanf.api.goods.model.query;

import com.lanf.constant.model.query.PageQuery;
import lombok.Data;

/**
 * 基础商品分页查询
 */
@Data
public class BaseGoodsPageQuery extends PageQuery {

    /** 商品编码 */
    private String goodsCode;

}
