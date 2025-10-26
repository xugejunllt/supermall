package com.lanf.user.model.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@Data
public class AddressAddDTO implements Serializable {


    @ApiModelProperty(value = "联系人")
    @NotBlank(message = "联系人不能为空")
    private String consignee;

    @ApiModelProperty(value = "手机号")
    @NotBlank(message = "手机号不能为空")
    private String phone;

    @ApiModelProperty(value = "地区")
    @NotBlank(message = "区不能为空")
    private String area;

    @ApiModelProperty(value = "地址")
    @NotBlank(message = "地址不能为空")
    private String address;

}
