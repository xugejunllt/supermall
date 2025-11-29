package com.lanf.storage.model.bo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class StockUpdateBO implements Serializable {


    private Long  id;

    @ApiModelProperty(value = "总库存")
    private Integer totalStock;

    @ApiModelProperty(value = "可用库存")
    private Integer usableStock;

    private Long setVersion;

}
