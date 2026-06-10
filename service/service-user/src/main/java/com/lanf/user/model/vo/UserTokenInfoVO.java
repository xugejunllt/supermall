package com.lanf.user.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserTokenInfoVO {
    
    private String accessToken;
    
    private String refreshToken;
    
    private Long accessTokenExp;
    
    private Long refreshTokenExp;

    private String signKey;
}
