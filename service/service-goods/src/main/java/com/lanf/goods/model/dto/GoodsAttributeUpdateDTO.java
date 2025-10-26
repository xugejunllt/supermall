package com.lanf.goods.model.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;


@Data
public class GoodsAttributeUpdateDTO implements Serializable {

    @NotNull(message ="id不能为空" )
    private Long id;

    @ApiModelProperty(value = "属性值 多个 用;隔开")
    @NotBlank(message = "属性值不能为空")
    private String attributeValue;

    @NotNull(message ="排序序号不能为空" )
    private Integer sort;
}
