package com.lanf.goods.model.vo;


import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class GoodsBrandVO implements Serializable {

    @ApiModelProperty(value = "名称")
    private String name;

    @ApiModelProperty(value = "排序坐标，越大越靠前")
    private Integer sortIndex;

    private Long id;

    private Date createTime;

    private Date updateTime;


    private String createBy;

    private String updateBy;
}
