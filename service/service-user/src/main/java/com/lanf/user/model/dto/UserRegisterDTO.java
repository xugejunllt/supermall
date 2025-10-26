package com.lanf.user.model.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class UserRegisterDTO implements Serializable {

    @NotNull( message = "手机号不能为空")
    private String phoneNumber;

    //短信验证码
    @NotNull( message = "短信验证码不能为空")
    private String code;

    @NotNull( message = "注册来源不能为空")
    private Integer registerSource;
    //密码
    private String passWord;



}
