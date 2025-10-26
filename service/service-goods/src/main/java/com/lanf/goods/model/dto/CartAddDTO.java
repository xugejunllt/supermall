package com.lanf.goods.model.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class CartAddDTO implements Serializable {




    private Long skuId;

    @ApiModelProperty(value = "数量")
    private Integer quantity;

}
