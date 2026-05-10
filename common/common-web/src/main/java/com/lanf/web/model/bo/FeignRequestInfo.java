package com.lanf.web.model.bo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FeignRequestInfo {
    
    private Long userId;
    
    private String deviceId;
    
    private Long tenantId;
}
