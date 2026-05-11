package com.lanf.web.interceptor;

import com.lanf.web.auth.RequestAuthExtractor;
import com.lanf.web.utils.UserContext;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class FeignConfig {

    @Bean
    public RequestInterceptor userContextInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate requestTemplate) {
                Long userId = UserContext.getUserId();
                String deviceId = UserContext.getDeviceId();
                Long tenantId = UserContext.getTenantId();

                if (userId != null) {
                    requestTemplate.header(RequestAuthExtractor.FEIGN_HEADER_USER_ID, userId.toString());
                }

                if (deviceId != null) {
                    requestTemplate.header(RequestAuthExtractor.FEIGN_HEADER_DEVICE_ID, deviceId);
                }

                if (tenantId != null) {
                    requestTemplate.header(RequestAuthExtractor.FEIGN_HEADER_TENANT_ID, tenantId.toString());
                }

                log.debug("Feign请求透传用户上下文: userId={}, deviceId={}, tenantId={}", 
                        userId, deviceId, tenantId);
            }
        };
    }
}
