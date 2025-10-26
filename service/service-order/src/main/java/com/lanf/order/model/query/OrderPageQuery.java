package com.lanf.order.model.query;

import com.lanf.mybatis.base.PageQuery;
import lombok.Data;


@Data
public class OrderPageQuery extends PageQuery {

    private Integer status;
    

}
