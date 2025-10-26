package com.lanf.storage.model.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lanf.mybatis.base.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * <p>
 * 供应商
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-05-30
 */
@Data
@TableName("supplier")
public class SupplierDO extends BaseEntity {

private static final long serialVersionUID=1L;


    @ApiModelProperty(value = "供应商编码")
    private String code;

    @ApiModelProperty(value = "供应商名称")
    private String name;

    @ApiModelProperty(value = "省")
    private String province;

    @ApiModelProperty(value = "市")
    private String city;

    @ApiModelProperty(value = "区")
    private String area;

    @ApiModelProperty(value = "详细地址")
    private String detailAddress;

    @ApiModelProperty(value = "手机")
    private String phone;

    @ApiModelProperty(value = "职位")
    private String position;

    @ApiModelProperty(value = "邮箱")
    private String email;

    @ApiModelProperty(value = "税号")
    private String dutyParagraph;

    @ApiModelProperty(value = "开户行")
    private String bankName;

    @ApiModelProperty(value = "发票title")
    private String invoiceTitle;

    @ApiModelProperty(value = "银行账号")
    private String bankAccount;

    @ApiModelProperty(value = "支付宝账号")
    private String alipayAccount;

    @TableField( fill = FieldFill.INSERT)
    private String  tenantCode;
}
