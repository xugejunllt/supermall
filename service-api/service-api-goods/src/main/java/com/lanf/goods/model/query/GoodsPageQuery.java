package com.lanf.goods.model.query;

import com.lanf.constant.model.query.PageQuery;
import lombok.Data;

@Data
public class GoodsPageQuery extends PageQuery {

    //商品名称
    private String name;

    private String code;


}
