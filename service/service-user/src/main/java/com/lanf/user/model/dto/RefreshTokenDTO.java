package com.lanf.user.model.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@Data
public class RefreshTokenDTO implements Serializable {


    @NotBlank(message = "刷新令牌不能为空")
    private String refreshToken;


}
