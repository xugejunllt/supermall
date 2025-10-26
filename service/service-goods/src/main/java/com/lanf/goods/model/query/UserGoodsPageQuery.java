package com.lanf.goods.model.query;

import com.lanf.mybatis.base.PageQuery;
import lombok.Data;

@Data
public class UserGoodsPageQuery extends PageQuery {

    //商品名称
    private String name;
    //商品标题
    private String title;



}
