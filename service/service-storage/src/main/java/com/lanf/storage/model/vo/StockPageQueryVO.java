package com.lanf.storage.model.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class StockPageQueryVO implements Serializable {

    private Long id;
    @ApiModelProperty(value = "sku编码")
    private String skuCode;

    @ApiModelProperty(value = "总库存")
    private Integer totalStock;

    @ApiModelProperty(value = "锁住的库存")
    private Integer lockStock;

    @ApiModelProperty(value = "可用库存")
    private Integer usableStock;
    //商品单位
    private String unit;

    private String goodsName;

    private Date createTime;
}
