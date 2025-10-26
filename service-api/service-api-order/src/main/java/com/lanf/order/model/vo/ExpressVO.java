package com.lanf.order.model.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class ExpressVO implements Serializable {

    @ApiModelProperty(value = "快递编码")
    private String code;

    @ApiModelProperty(value = "快递名称")
    private String expressName;

    @ApiModelProperty(value = "快递公司")
    private String expressCompany;

    @ApiModelProperty(value = "省")
    private String province;


}
