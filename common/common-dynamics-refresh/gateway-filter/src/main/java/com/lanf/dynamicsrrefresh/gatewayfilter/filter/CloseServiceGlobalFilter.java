package com.lanf.dynamicsrrefresh.gatewayfilter.filter;

import com.lanf.constant.exception.BizException;
import com.lanf.dynamicsrrefresh.gatewayfilter.handle.CloseServiceHandle;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class CloseServiceGlobalFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        log.info("服务关闭过滤器");

        String path = exchange.getRequest().getPath().toString();
        if (CloseServiceHandle.close(path)) {
            log.error("服务已关闭");
            throw new BizException("服务已关闭");
        }
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        // 定义过滤器顺序，数字越小，优先级越高
        return 10;
    }
}