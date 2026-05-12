package com.lanf.security.model.vo;

import lombok.Data;

@Data

public class AdminUserTokenInfoVO {
    
    private String accessToken;
    
    private String refreshToken;
    
    private Long accessTokenExp;
    
    private Long refreshTokenExp;
}
