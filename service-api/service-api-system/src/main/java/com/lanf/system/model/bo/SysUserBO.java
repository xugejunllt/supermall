package com.lanf.system.model.bo;


import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;

@Data
public class SysUserBO {

    private static final long serialVersionUID = 1L;

   // @ApiModelProperty(value = "用户名")
    private String username;

   // @ApiModelProperty(value = "密码")
    private String password;

   // @ApiModelProperty(value = "新密码")
    private String newpassword;

    //@ApiModelProperty(value = "姓名")
    private String name;

   // @ApiModelProperty(value = "手机")
    private String mobile;

  //  @ApiModelProperty(value = "邮箱")
    private String email;

    //@ApiModelProperty(value = "头像地址")
    private String headUrl;

    private MultipartFile file;

   // @ApiModelProperty(value = "部门id")
    private String deptId;

  //  @ApiModelProperty(value = "描述")
    private String description;

   // @ApiModelProperty(value = "状态（1：正常 0：停用）")
    private Integer statusData;

    private Boolean status;

    private List<Long> roleList;

    private String roleIds;
    //部门
    private String deptName;

    private Long id;

    private Date createTime;

    private Date updateTime;

    private Integer isDeleted;

    //版本号
    private Long version;

    private String createBy;

    private String updateBy;

    private String  tenantCode;

    //商家id
    private Long businessId;
    //店铺id
    private Long shopId;

}

