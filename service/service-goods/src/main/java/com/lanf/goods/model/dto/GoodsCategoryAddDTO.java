package com.lanf.goods.model.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@Data
public class GoodsCategoryAddDTO implements Serializable {

    @ApiModelProperty(value = "名称")
    private String name;
    @ApiModelProperty(value = "排序坐标，越大越靠前")
    @Max(value = 3,message = "超过最大级别")
    @Min(value = 1,message = "小于最小级别")
    private Integer level;


    private Long parentId;

    private Integer sortIndex;



}
