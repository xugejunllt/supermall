package com.lanf.system.model.query;

import com.lanf.mybatis.base.PageQuery;
import lombok.Data;

@Data
public class ShopPageQuery extends PageQuery {

    /**
     * 店铺名称
     */
    private String name;


}
