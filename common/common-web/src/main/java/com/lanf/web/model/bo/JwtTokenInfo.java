package com.lanf.web.model.bo;


import lombok.Data;

@Data

public class JwtTokenInfo {
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 设备ID
     */
    private String deviceId;
    
    /**
     * 租户ID（仅管理员Token）
     */
    private Long tenantId;
    
    private String signingKey;

    private Long expTime;
}
