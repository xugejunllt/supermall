package com.lanf.logistics.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lanf.mybatis.base.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * <p>
 * 
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-06-17
 */
@Data
@TableName("express")
public class ExpressDO extends BaseEntity {

private static final long serialVersionUID=1L;



    private Long businessId;
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
