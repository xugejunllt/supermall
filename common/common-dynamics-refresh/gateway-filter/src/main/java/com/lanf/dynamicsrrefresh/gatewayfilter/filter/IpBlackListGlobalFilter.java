package com.lanf.dynamicsrrefresh.gatewayfilter.filter;

import com.lanf.dynamicsrrefresh.gatewayfilter.handle.IpBlackListHandle;
import com.lanf.dynamicsrrefresh.gatewayfilter.util.IpUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class IpBlackListGlobalFilter implements GlobalFilter, Ordered {


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        log.info("ip黑名单过滤器");

        HttpHeaders headers = exchange.getRequest().getHeaders();
        String ip = IpUtils.getIp(headers);

        if (IpBlackListHandle.include(ip)){
            log.error("ip已被禁用");
            throw new RuntimeException("ip已被禁用");
        }
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        // 定义过滤器顺序，数字越小，优先级越高
        return 12;
    }
}