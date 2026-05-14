package com.lanf.storage.model.query;

import com.lanf.constant.model.query.PageQuery;
import lombok.Data;

@Data
public class PurchaseOrderPageQuery extends PageQuery {

    private Integer status;

}
