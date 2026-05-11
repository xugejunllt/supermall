package com.lanf.user.model.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@Data
public class LoginSendCodeDTO implements Serializable {


    @NotBlank(message = "手机号不能为空")
    private String phoneNumber;

    @NotBlank(message = "随机数不能为空")
    private String randomKey;

}
