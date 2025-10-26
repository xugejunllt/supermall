package com.lanf.log.model.dto;

import lombok.Data;

import java.util.Date;

@Data
public class SysLoginLogDTO  {

    private static final long serialVersionUID = 1L;

    //@ApiModelProperty(value = "用户账号")
    private String username;

   // @ApiModelProperty(value = "登录IP地址")
    private String ipaddr;

   // @ApiModelProperty(value = "登录状态（0成功 1失败）")
    private Integer status;

    //@ApiModelProperty(value = "提示信息")
    private String msg;

   // @ApiModelProperty(value = "访问时间")
    private Date accessTime;
    private Long id;

    private Date createTime;

    private Date updateTime;

    private Integer isDeleted;


    private Long version;

    private String createBy;

    private String updateBy;

    private String  tenantCode;
}
