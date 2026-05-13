package com.lanf.goods.model.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class ShopDTO implements Serializable {


    @ApiModelProperty(value = "店铺名称")
    private String name;

    @ApiModelProperty(value = "头像")
    private String headUrl;

}
