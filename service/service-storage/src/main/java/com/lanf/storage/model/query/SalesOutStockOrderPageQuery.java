package com.lanf.storage.model.query;

import com.lanf.constant.web.PageQuery;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class SalesOutStockOrderPageQuery extends PageQuery {

    @ApiModelProperty(value = "入库状态0:待入库,1:部分入库 2:已入库 ")
    private Integer inStockStatus;
    private String orderId;

}
