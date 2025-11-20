package com.lanf.user.model.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class UserDetailVO implements Serializable {

    @ApiModelProperty(value = "账号")
    private String account;


    @ApiModelProperty(value = "邮箱")
    private String email;

    @ApiModelProperty(value = "手机号码")
    private String phoneNumber;

    @ApiModelProperty(value = "昵称")
    private String nickName;

    @ApiModelProperty(value = "头像")
    private String headImageUrl;

    /**
     * 等级信息
     */
    //等级
    private Integer level;

    @ApiModelProperty(value = "等级名称，如VIP1")
    private String levelName;

    @ApiModelProperty(value = "等级图标")
    private String levelIcon;



}
