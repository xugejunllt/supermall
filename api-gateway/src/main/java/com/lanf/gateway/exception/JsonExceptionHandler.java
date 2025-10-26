//package com.lanf.gateway.exception;
//
//import lombok.extern.log4j.Log4j2;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.boot.autoconfigure.web.ErrorProperties;
//import org.springframework.boot.autoconfigure.web.reactive.error.DefaultErrorWebExceptionHandler;
//import org.springframework.boot.web.reactive.error.ErrorAttributes;
//import org.springframework.cloud.gateway.support.TimeoutException;
//import org.springframework.context.ApplicationContext;
//import org.springframework.web.reactive.function.server.*;
//import org.springframework.boot.autoconfigure.web.WebProperties.Resources;
//import java.util.HashMap;
//import java.util.Map;
//
///**
// * 自定义异常处理
// *
// * <p>异常时用JSON代替HTML异常信息<p>
// *
// * @author yinjihuan
// *
// */
//@Slf4j
//public class JsonExceptionHandler extends DefaultErrorWebExceptionHandler {
//
//    public JsonExceptionHandler(ErrorAttributes errorAttributes, Resources resourceProperties,
//                                ErrorProperties errorProperties, ApplicationContext applicationContext) {
//        super(errorAttributes, resourceProperties, errorProperties, applicationContext);
//    }
//
//    /**
//     * 获取异常属性
//     *如果是网关转发404，则异常不会被捕获,下游抛出的异常也不会捕获，超时异常被捕获
//     *
//     */
//    @Override
//    protected Map<String, Object> getErrorAttributes(ServerRequest request, boolean includeStackTrace) {
//        log.info("buhyiccsdsd");
//            Map<String, Object> map = new HashMap<>();
//
//          Throwable error = getError(request);
//          error.printStackTrace();
//
//
//
//
//        return map;
//    }
//
//    /**
//     * 指定响应处理方法为JSON处理的方法
//     * @param errorAttributes
//     */
//    @Override
//    protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {
//
//
//        return RouterFunctions.route(RequestPredicates.all(), this::renderErrorResponse);
//    }
//
//    /**
//     * 根据code获取对应的HttpStatus
//     * @param errorAttributes
//     */
//    @Override
//    protected int getHttpStatus(Map<String, Object> errorAttributes) {
//
//        return 200;
//    }
//}