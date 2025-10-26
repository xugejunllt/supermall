package com.lanf.user.model.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class UserPageVO implements Serializable {

    private Long id;

    @ApiModelProperty(value = "用户名")
    private String userName;
    @ApiModelProperty(value = "邮箱")
    private String email;

    @ApiModelProperty(value = "手机号码")
    private String phoneNumber;

    @ApiModelProperty(value = "昵称")
    private String nickName;

    @ApiModelProperty(value = "0.待审核 1.正常 2.禁用")
    private Integer userStatus;

    @ApiModelProperty(value = "注册来源 0:app 1:web")
    private Integer registerSource;

    private Date createTime;
}
