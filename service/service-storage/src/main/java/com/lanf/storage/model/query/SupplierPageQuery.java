package com.lanf.storage.model.query;

import com.lanf.constant.model.query.PageQuery;
import lombok.Data;

@Data
public class SupplierPageQuery extends PageQuery {

    /** 供应商名称 */
    private String name;

}
