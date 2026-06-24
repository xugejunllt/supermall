package com.lanf.dynamicsrrefresh.gatewayfilter.filter;

import com.lanf.dynamicsrrefresh.gatewayfilter.handle.CloseInterfaceHandle;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 关闭某个接口
 */
@Slf4j
@Component
public class CloseInterfaceGlobalFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        String path = exchange.getRequest().getPath().toString();
        if (CloseInterfaceHandle.close(path)) {
            log.error("接口已关闭");
            throw new RuntimeException("接口已关闭");
        }
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        // 定义过滤器顺序，数字越小，优先级越高
        return 11;
    }
}