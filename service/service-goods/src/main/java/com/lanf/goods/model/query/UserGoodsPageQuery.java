package com.lanf.goods.model.query;

import com.lanf.constant.model.query.PageQuery;
import lombok.Data;

@Data
public class UserGoodsPageQuery extends PageQuery {

    //商品名称
    private String name;
    //商品标题
    private String title;



}
