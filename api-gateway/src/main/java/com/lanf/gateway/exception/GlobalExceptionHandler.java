package com.lanf.gateway.exception;

import com.google.gson.Gson;
import com.lanf.constant.exception.BizException;
import com.lanf.constant.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 全局异常处理器
 */
@Slf4j
@Component
@Order(-1)
public class GlobalExceptionHandler implements ErrorWebExceptionHandler {

    private final Gson gson = new Gson();

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        Result<Void> result;
        if (ex instanceof BizException) {
            BizException bizException = (BizException) ex;
            log.warn("业务异常: {}", bizException.getMessage());
            result = Result.fail(bizException.getCode(), bizException.getMessage());
        } else {
            log.error("系统异常: {}", ex.getMessage(), ex);
            result = Result.fail(HttpStatus.INTERNAL_SERVER_ERROR.value(), "系统繁忙，请稍后重试");
        }

        response.setStatusCode(HttpStatus.OK);
        DataBufferFactory dataBufferFactory = response.bufferFactory();
        DataBuffer dataBuffer = dataBufferFactory.wrap(gson.toJson(result).getBytes());
        return response.writeWith(Mono.just(dataBuffer));
    }
}
