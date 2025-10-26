package com.lanf.goods.model.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class GoodsPageVO implements Serializable {


    private Long id;

    @ApiModelProperty(value = "商品编码")
    private String code;

    @ApiModelProperty(value = "商品名称")
    private String name;

    @ApiModelProperty(value = "上下架状态 0:上架 ,1:下架")
    private Integer upDownStatus;

    private Date createTime;
}
