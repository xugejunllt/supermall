package com.lanf.goods.model.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class BaseGoodsPageVO implements Serializable {


    private Long id;
    @ApiModelProperty(value = "商品编码")
    private String code;

    @ApiModelProperty(value = "商品名称")
    private String name;

    @ApiModelProperty(value = "图片地址，多个,用“，”隔开")
    private String pictureAddress;

    private Date createTime;


}
