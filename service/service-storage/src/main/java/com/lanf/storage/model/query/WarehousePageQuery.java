package com.lanf.storage.model.query;

import com.lanf.constant.web.PageQuery;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


@Data
public class WarehousePageQuery extends PageQuery {

    @ApiModelProperty(value = "仓库名称")
    private String name;


    @ApiModelProperty(value = "仓库编码")
    private String code;

}
