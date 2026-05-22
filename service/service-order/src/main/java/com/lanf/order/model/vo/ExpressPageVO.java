package com.lanf.order.model.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class ExpressPageVO implements Serializable {

    private Long id;

    @ApiModelProperty(value = "快递名称")
    private String expressName;

    @ApiModelProperty(value = "快递公司")
    private String expressCompany;
    //快递100快递公司编码表
    private String companyCode;

    @ApiModelProperty(value = "省")
    private String province;

    @ApiModelProperty(value = "市")
    private String city;

    @ApiModelProperty(value = "区")
    private String area;

    @ApiModelProperty(value = "详细地址")
    private String address;

    @ApiModelProperty(value = "联系人")
    private String contacts;

    @ApiModelProperty(value = "手机")
    private String phone;
}
