package com.lanf.user.model.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class UserLoginDTO implements Serializable {

    //手机号
    @NotNull( message = "手机号不能为空")
    private String phoneNumber;
    //手机验证码
    @NotNull( message = "短信验证码不能为空")
    private String code;

}
