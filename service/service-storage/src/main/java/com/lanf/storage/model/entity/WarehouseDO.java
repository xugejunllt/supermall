package com.lanf.storage.model.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lanf.mybatis.base.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * <p>
 * 仓库
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-05-30
 */
@Data
@TableName("warehouse")
public class WarehouseDO extends BaseEntity {

private static final long serialVersionUID=1L;

    @ApiModelProperty(value = "仓库编码")
    private String code;

    //仓库组编码
    private String groupCode;

    @ApiModelProperty(value = "仓库名称")
    private String name;

    @ApiModelProperty(value = "状态 0停用 1.正常")
    private Integer status;

    @ApiModelProperty(value = "省")
    private String province;

    @ApiModelProperty(value = "市")
    private String city;

    @ApiModelProperty(value = "区")
    private String area;

    @ApiModelProperty(value = "详细地址")
    private String detailAddress;

    @ApiModelProperty(value = "联系人")
    private String contacts;

    @ApiModelProperty(value = "手机")
    private String phone;

    @ApiModelProperty(value = "邮箱")
    private String email;

    @TableField( fill = FieldFill.INSERT)
    private Long  tenantId;
}
