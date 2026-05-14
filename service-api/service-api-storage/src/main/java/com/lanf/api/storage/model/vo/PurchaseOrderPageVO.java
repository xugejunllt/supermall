package com.lanf.api.storage.model.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class PurchaseOrderPageVO implements Serializable {

    private Long id;

    private Long supplierId;
    private String supplierName;


    @ApiModelProperty(value = "单据编码")
    private String code;


    @ApiModelProperty(value = "采购金额")
    private BigDecimal totalMoney;

    @ApiModelProperty(value = "仓库名称")
    private String warehouseName;


    @ApiModelProperty(value = "状态:0:审核中 1.审核通过 2:审核不通过,3.部分入库 4.已完成")
    private Integer status;


    private Date createTime;

}
