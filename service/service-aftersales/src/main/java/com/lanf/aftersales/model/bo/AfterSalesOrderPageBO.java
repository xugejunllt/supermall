package com.lanf.aftersales.model.bo;

import com.lanf.aftersales.model.query.AfterSalesOrderPageQuery;
import com.lanf.mybatis.base.PageQuery;
import lombok.Data;

import java.io.Serializable;

@Data
public class AfterSalesOrderPageBO extends PageQuery {

    private Long userId ;

    private Long afterSalesOrderId;

    private AfterSalesOrderPageQuery query;

    private Long shopId;


}
