package com.lanf.storage.model.query;

import com.lanf.constant.model.query.PageQuery;
import lombok.Data;


@Data
public class WarehousePageQuery extends PageQuery {

    /** 仓库名称 */
    private String name;


    /** 仓库编码 */
    private String code;

}
