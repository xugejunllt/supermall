package com.lanf.api.goods.model.query;

import com.lanf.constant.model.query.PageQuery;
import lombok.Data;

/**
 * 商品分页查询
 */
@Data
public class GoodsPageQuery extends PageQuery {

    /** 商品名称 */
    private String name;

    private String code;

}
