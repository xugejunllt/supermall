package com.lanf.aftersales.model.query;

import com.lanf.mybatis.base.PageQuery;
import lombok.Data;

@Data
public class AfterSalesOrderPageQuery extends PageQuery {

    private Long userId;
}
