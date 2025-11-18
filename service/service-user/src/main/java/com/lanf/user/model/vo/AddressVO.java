package com.lanf.user.model.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class AddressVO implements Serializable {

    private Long id;

    @ApiModelProperty(value = "用户id")
    private Long userId;

    @ApiModelProperty(value = "联系人")
    private String consignee;

    @ApiModelProperty(value = "手机号")
    private String phone;

    @ApiModelProperty(value = "地区")
    private String area;
    //详细地址
    private String address;

    @ApiModelProperty(value = "是否默认 0默认 1.不是")
    private Integer defaultAddress;

}
