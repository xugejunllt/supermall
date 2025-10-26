package com.lanf.system.model.entiry;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lanf.mybatis.base.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * <p>
 * 公司信息
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-05-28
 */
@Data
@TableName("company")
public class CompanyDO extends BaseEntity {

private static final long serialVersionUID=1L;



    @ApiModelProperty(value = "公司名称")
    private String company;
    //用户类型 0:平台 1:商户
    private Integer userType;

    @ApiModelProperty(value = "admin密码")
    private String adminPassword;
    @ApiModelProperty(value = "姓名")
    private String userName;

    @ApiModelProperty(value = "手机")
    private String phoneNumber;

    @ApiModelProperty(value = "审核状态  0:审核中,1: 已审核,2: 审核失败")
    private Integer status;


    @TableField( fill = FieldFill.INSERT)
    private String  tenantCode;


}
