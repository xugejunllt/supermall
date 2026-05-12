package com.lanf.system.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 管理员 Token 信息 VO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "管理员Token信息")
public class AdminTokenInfoVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "访问令牌")
    private String accessToken;

    @ApiModelProperty(value = "刷新令牌")
    private String refreshToken;

    @ApiModelProperty(value = "访问令牌过期时间戳（毫秒）")
    private Long accessTokenExp;

    @ApiModelProperty(value = "刷新令牌过期时间戳（毫秒）")
    private Long refreshTokenExp;
}
