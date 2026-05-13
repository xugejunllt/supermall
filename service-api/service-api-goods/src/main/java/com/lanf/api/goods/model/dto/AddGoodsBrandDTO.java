package com.lanf.api.goods.model.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;


@Data
public class AddGoodsBrandDTO implements Serializable {

    @ApiModelProperty(value = "名称")
    @NotBlank(message = "品牌名称不存在")
    private String name;


    @ApiModelProperty(value = "排序坐标，越大越靠前")
    @NotNull(message = "排序坐标不能为空")
    private Integer sortIndex;
}
