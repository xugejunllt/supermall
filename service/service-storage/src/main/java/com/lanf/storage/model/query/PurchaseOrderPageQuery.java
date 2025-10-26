package com.lanf.storage.model.query;

import com.lanf.mybatis.base.PageQuery;
import lombok.Data;

@Data
public class PurchaseOrderPageQuery extends PageQuery {

    private Integer status;

}
