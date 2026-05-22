package com.lanf.aftersales.model.query;

import com.lanf.constant.model.query.PageQuery;
import lombok.Data;

@Data
public class AfterSalesOrderPageQuery extends PageQuery {

    private Long userId;
}
