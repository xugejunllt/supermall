package com.lanf.storage.model.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class SalesOutStockOrderPageVO implements Serializable {

    private Long id;

    @ApiModelProperty(value = "单据编码")
    private String code;

    private Integer inOutStatus;

    @ApiModelProperty(value = "预计出库数量")
    private Integer expectQuantity;

    @ApiModelProperty(value = "实际出库数量")
    private Integer actualQuantity;

    @ApiModelProperty(value = "出库状态0:待出库,1:部分出库 2:已出库 ")
    private Integer storageStatus;

    @ApiModelProperty(value = "仓库名称")
    private String warehouseName;

    @ApiModelProperty(value = "备注")
    private String remarks;
    @ApiModelProperty(value = "订单id")
    private Long orderId;
    @ApiModelProperty(value = "仓库id")
    private Long warehouseId;

    private Date createTime;
}
