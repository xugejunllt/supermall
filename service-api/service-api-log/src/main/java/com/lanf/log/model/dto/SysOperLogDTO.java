package com.lanf.log.model.dto;


import lombok.Data;

import java.util.Date;

@Data
public class SysOperLogDTO  {

    private static final long serialVersionUID = 1L;

   // @ApiModelProperty(value = "模块标题")
    private String title;

   // @ApiModelProperty(value = "业务类型（0其它 1新增 2修改 3删除）")
    private String businessType;

   // @ApiModelProperty(value = "方法名称")
    private String method;

   // @ApiModelProperty(value = "请求方式")
    private String requestMethod;

   // @ApiModelProperty(value = "操作类别（0其它 1后台用户 2手机端用户）")
    private String operatorType;

   // @ApiModelProperty(value = "操作人员")
    private String operName;

   // @ApiModelProperty(value = "部门名称")
    private String deptName;

   // @ApiModelProperty(value = "请求URL")
    private String operUrl;

   // @ApiModelProperty(value = "主机地址")
    private String operIp;

  //  @ApiModelProperty(value = "请求参数")
    private String operParam;

   // @ApiModelProperty(value = "返回参数")
    private String jsonResult;

  //  @ApiModelProperty(value = "操作状态（0正常 1异常）")
    private Integer status;

   // @ApiModelProperty(value = "错误消息")
    private String errorMsg;

   // @ApiModelProperty(value = "操作时间")
    private Date operTime;
    private Long id;

    private Date createTime;

    private Date updateTime;

    private Integer isDeleted;


    private Long version;

    private String createBy;

    private String updateBy;

    private String  tenantCode;
}
