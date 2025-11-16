package com.lanf.user.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lanf.mybatis.base.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 
 * </p>
 *
 * @author jarven
 * @since 2025-11-15
 */
@Data
@TableName("user_login_log")
public class UserLoginLog extends BaseEntity {

private static final long serialVersionUID=1L;


    private Long userId;

    @ApiModelProperty(value = "手机号码/账号")
    private String account;



    private String sessionId;

    @ApiModelProperty(value = "客户端IP地址")
    private String ipAddress;

    @ApiModelProperty(value = "登入类型 0 sms")
    private Integer loginType;

    @ApiModelProperty(value = "注册渠道 0: web 1:android 2:ios")
    private Integer loginChannel;

    @ApiModelProperty(value = "设备类型")
    private String deviceType;

    @ApiModelProperty(value = "设备ID")
    private String deviceId;

    @ApiModelProperty(value = "设备型号")
    private String deviceModel;

    @ApiModelProperty(value = "应用版本")
    private String appVersion;




}
