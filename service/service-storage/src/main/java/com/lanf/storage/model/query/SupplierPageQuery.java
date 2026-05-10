package com.lanf.storage.model.query;

import com.lanf.constant.web.PageQuery;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class SupplierPageQuery extends PageQuery {

    @ApiModelProperty(value = "供应商名称")
    private String name;

}
