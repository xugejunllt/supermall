package com.lanf.goods.model.query;

import com.lanf.mybatis.base.PageQuery;
import lombok.Data;


@Data
public class BaseGoodsPageQuery extends PageQuery {

    //商品编码
    private String goodsCode;



}
