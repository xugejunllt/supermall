package com.lanf.order.model.query;

import com.lanf.mybatis.base.PageQuery;
import lombok.Data;

@Data
public class OrderPageQuery2 extends PageQuery {

    private Integer status;
}
