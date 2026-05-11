package com.lanf.user.model.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class RegisterUserDTO implements Serializable {





    /**
     * 手机号
     */
    @NotBlank( message = "手机号不能为空")
    private String phoneNumber;

    /**
     * 短信验证码
     */
    @NotBlank( message = "短信验证码不能为空")
    private String code;

    /**
     * 昵称
     */
    private String nickName;

    /**
     * 头像
     */
    private String headImageUrl;

    /**
     * 注册渠道 0: web 1:android 2:ios
     */
    @NotNull( message = "注册渠道不能为空 ")
    private Integer registerChannel;


    /**
     * 设备类型
     */
    @NotBlank( message = "设备类型不能为空")
    private String deviceType;

    /**
     * 设备ID
     */
    @NotBlank( message = "设备ID不能为空")
    private String deviceId;

    /**
     * 设备型号
     */
    @NotBlank( message = "设备型号不能为空")
    private String deviceModel;

    /**
     * 应用版本
     */
    @NotBlank( message = "应用版本不能为空")
    private String appVersion;



}
