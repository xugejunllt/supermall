package com.lanf.gateway.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.NettyWriteResponseFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Component
public class CorsResponseHeaderFilter implements GlobalFilter, Ordered {


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return chain.filter(exchange).then(Mono.defer(() -> {


            try {

                Stream<Map.Entry<String, List<String>>> entryStream = exchange.getResponse().getHeaders().entrySet().stream()
                        .filter(kv -> (kv.getValue() != null && kv.getValue().size() > 1))
                        .filter(kv -> (kv.getKey().equals(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN)
                                || kv.getKey().equals(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS)));
                for (Map.Entry<String, List<String>> kv : entryStream.collect(Collectors.toList())) {
                    List<String> list = new ArrayList<>();
                    List<String> value = kv.getValue();
                    list.add(value.get(0));
                    kv.setValue(list);
                }

            } catch (Exception e) {
                log.error("过滤出错--");
                e.printStackTrace();
            }
            return chain.filter(exchange);
        }));
    }

    @Override
    public int getOrder() {
        // 指定此过滤器位于NettyWriteResponseFilter之后
        // 即待处理完响应体后接着处理响应头
        return NettyWriteResponseFilter.WRITE_RESPONSE_FILTER_ORDER + 1;
    }

}
