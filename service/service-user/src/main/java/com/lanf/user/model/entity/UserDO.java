package com.lanf.user.model.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lanf.mybatis.base.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * <p>
 * User对象
 * </p>
 *
 * @author Jarven
 * @since 2025-10-27
 */
@Data
@TableName("user")
public class UserDO extends BaseEntity {

private static final long serialVersionUID=1L;


    @ApiModelProperty(value = "用户名")
    private String account;

    @ApiModelProperty(value = "密码")
    private String password;

    @ApiModelProperty(value = "邮箱")
    private String email;

    @ApiModelProperty(value = "手机号码")
    private String phoneNumber;

    @ApiModelProperty(value = "昵称")
    private String nickName;

    @ApiModelProperty(value = "0.待审核 1.正常 2.禁用")
    private Integer status;

    @ApiModelProperty(value = "头像")
    private String headImageUrl;

    @ApiModelProperty(value = "注册渠道 0: web 1:android 2:ios")
    private Integer registerChannel;

    @ApiModelProperty(value = "设备类型")
    private String deviceType;

    @ApiModelProperty(value = "设备ID")
    private String deviceId;

    @ApiModelProperty(value = "设备型号")
    private String deviceModel;

    @ApiModelProperty(value = "应用版本")
    private String appVersion;

    @ApiModelProperty(value = "注册时间")
    private LocalDateTime registerTime;



}
