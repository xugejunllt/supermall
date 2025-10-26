package com.lanf.storage.model.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@Data
public class SupplierAddDTO implements Serializable {


    @NotBlank(message = "供应商名称不能为空")
    @ApiModelProperty(value = "供应商名称")
    private String name;

    @NotBlank(message = "省不能为空")
    @ApiModelProperty(value = "省")
    private String province;

    @NotBlank(message = "市不能为空")
    @ApiModelProperty(value = "市")
    private String city;

    @NotBlank(message = "区不能为空")
    @ApiModelProperty(value = "区")
    private String area;

    @NotBlank(message = "详细地址不能为空")
    @ApiModelProperty(value = "详细地址")
    private String detailAddress;

    @NotBlank(message = "手机不能为空")
    @ApiModelProperty(value = "手机")
    private String phone;

    @NotBlank(message = "职位不能为空")
    @ApiModelProperty(value = "职位")
    private String position;

    @NotBlank(message = "邮箱不能为空")
    @ApiModelProperty(value = "邮箱")
    private String email;

    @NotBlank(message = "税号不能为空")
    @ApiModelProperty(value = "税号")
    private String dutyParagraph;

    @NotBlank(message = "开户行不能为空")
    @ApiModelProperty(value = "开户行")
    private String bankName;

    @NotBlank(message = "发票title不能为空")
    @ApiModelProperty(value = "发票title")
    private String invoiceTitle;

    @NotBlank(message = "银行账号不能为空")
    @ApiModelProperty(value = "银行账号")
    private String bankAccount;

    @NotBlank(message = "支付宝账号不能为空")
    @ApiModelProperty(value = "支付宝账号")
    private String alipayAccount;

}
