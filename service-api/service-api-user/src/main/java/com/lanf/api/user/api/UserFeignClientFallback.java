package com.lanf.api.user.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UserFeignClientFallback implements FallbackFactory<UserApiService> {


    @Override
    public UserApiService create(Throwable cause) {

        log.error("调用服务降级:{}", cause.getMessage());
        return null;
    }
}
