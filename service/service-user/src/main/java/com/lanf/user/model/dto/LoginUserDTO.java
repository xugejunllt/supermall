package com.lanf.user.model.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class LoginUserDTO implements Serializable {

    //手机号
    @NotNull( message = "手机号不能为空")
    private String phoneNumber;
    //手机验证码
    @NotNull( message = "短信验证码不能为空")
    private String code;
    @NotNull( message = "登入渠道不能为空 ")


    //渠道 0: web 1:android 2:ios
    private Integer loginChannel;

    @NotNull( message = "设备类型不能为空")
    private String deviceType;

    @NotNull( message = "设备ID不能为空")
    private String deviceId;

    @NotNull( message = "设备型号不能为空")
    private String deviceModel;

    @NotNull( message = "应用版本不能为空")
    private String appVersion;
}
