package com.lanf.user.model.dto;

import com.lanf.common.utils.JsonUtils;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class RegisterUserDTO implements Serializable {


    @NotNull( message = "账号不能为空")
    private String account;

    @NotNull( message = "手机号不能为空")
    private String phoneNumber;

    @NotNull( message = "短信验证码不能为空")
    private String code;

    //昵称
    private String nickName;

    //头像
    private String headImageUrl;

    @NotNull( message = "注册渠道不能为空 ")
    //注册渠道 0: web 1:android 2:ios
    private Integer registerChannel;


    @NotNull( message = "设备类型不能为空")
    private String deviceType;

    @NotNull( message = "设备ID不能为空")
    private String deviceId;

    @NotNull( message = "设备型号不能为空")
    private String deviceModel;

    @NotNull( message = "应用版本不能为空")
    private String appVersion;



}
