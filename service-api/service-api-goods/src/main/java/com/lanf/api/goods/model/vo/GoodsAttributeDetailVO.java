package com.lanf.api.goods.model.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class GoodsAttributeDetailVO implements Serializable {

    @ApiModelProperty(value = "属性")
    private String attribute;

    @ApiModelProperty(value = "属性值 多个 用;隔开")
    private String attributeValue;

    private Integer sort;
    private Long tenantId;

    private Long id;

    private Date createTime;

    private Date updateTime;



    private String createBy;

    private String updateBy;
}
