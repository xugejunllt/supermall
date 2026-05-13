package com.lanf.goods.model.query;

import com.lanf.constant.model.query.PageQuery;
import lombok.Data;


@Data
public class BaseGoodsPageQuery extends PageQuery {

    //商品编码
    private String goodsCode;



}
