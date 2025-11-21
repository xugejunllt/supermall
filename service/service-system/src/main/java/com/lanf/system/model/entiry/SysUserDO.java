package com.lanf.system.model.entiry;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lanf.mybatis.base.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@ApiModel(description = "用户")
@TableName("sys_user")
public class SysUserDO extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "用户名")
    @TableField("username")
    private String username;

    @ApiModelProperty(value = "密码")
    @TableField("password")
    private String password;

    @ApiModelProperty(value = "新密码")
    @TableField(exist = false)
    private String newpassword;

    @ApiModelProperty(value = "姓名")
    @TableField("name")
    private String name;

    @ApiModelProperty(value = "手机")
    @TableField("phone")
    private String mobile;

    @ApiModelProperty(value = "邮箱")
    @TableField("email")
    private String email;

    @ApiModelProperty(value = "头像地址")
    @TableField("head_url")
    private String headUrl;

    @TableField(exist = false)
    private MultipartFile file;

    @ApiModelProperty(value = "部门id")
    @TableField("dept_id")
    private String deptId;

    @ApiModelProperty(value = "描述")
    @TableField("description")
    private String description;

    @ApiModelProperty(value = "状态（1：正常 0：停用）")
    @TableField("status")
    private Integer status;
    @TableField(exist = false)
    private Boolean dataStatus;

    @TableField(exist = false)
    private List<Long> roleList;

    @TableField(exist = false)
    private String roleIds;
    //部门
    @TableField(exist = false)
    private String deptName;

    private Long  tenantId;
}

