package com.lanf.user.model.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@Data
public class LoginSendCodeDTO implements Serializable {


    @NotBlank(message = "手机号不能为空")
   private String phoneNumber;
}
