package com.lanf.system.model.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 刷新 Token 请求 DTO
 */
@Data
public class RefreshTokenDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "刷新令牌", required = true)
    @NotBlank(message = "刷新令牌不能为空")
    private String refreshToken;
}
