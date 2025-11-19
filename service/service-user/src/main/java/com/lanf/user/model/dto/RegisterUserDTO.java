package com.lanf.user.model.dto;

import com.lanf.common.utils.JsonUtils;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class RegisterUserDTO implements Serializable {




    @NotBlank( message = "手机号不能为空")
    private String phoneNumber;

    @NotBlank( message = "短信验证码不能为空")
    private String code;

    //昵称
    private String nickName;

    //头像
    private String headImageUrl;

    @NotBlank( message = "注册渠道不能为空 ")
    //注册渠道 0: web 1:android 2:ios
    private Integer registerChannel;


    @NotBlank( message = "设备类型不能为空")
    private String deviceType;

    @NotBlank( message = "设备ID不能为空")
    private String deviceId;

    @NotBlank( message = "设备型号不能为空")
    private String deviceModel;

    @NotBlank( message = "应用版本不能为空")
    private String appVersion;



}
