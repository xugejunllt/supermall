package com.lanf.web.model.bo;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthRequestInfo {
    
    /**
     * 设备ID
     */
    private String deviceId;
    
    /**
     * 访问令牌
     */
    private String accessToken;
    
    /**
     * 渠道：app、web、pc等
     */
    private String channel;
    
    /**
     * 租户ID（可选）
     */
    private Long tenantId;
}
