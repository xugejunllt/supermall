package com.lanf.api.storage.model.query;

import com.lanf.constant.model.query.PageQuery;
import lombok.Data;

@Data
public class SalesOutStockOrderPageQuery extends PageQuery {

    /** 入库状态0:待入库,1:部分入库 2:已入库 */
    private Integer inStockStatus;
    private String orderId;

}
