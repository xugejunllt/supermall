package com.lanf.aftersales.model.bo;

import com.lanf.aftersales.model.query.AfterSalesOrderPageQuery;
import com.lanf.constant.model.query.PageQuery;
import lombok.Data;

@Data
public class AfterSalesOrderPageBO extends PageQuery {

    private Long userId ;

    private Long afterSalesOrderId;

    private AfterSalesOrderPageQuery query;

    private Long shopId;


}
