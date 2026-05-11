package com.lanf.web.security.sign;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lanf.common.utils.JsonUtils;
import com.lanf.constant.result.Result;
import com.lanf.web.auth.RequestAuthExtractor;
import com.lanf.web.config.SignPathConfig;
import com.lanf.web.security.keygen.SignKeyManager;
import com.lanf.web.utils.CachedBodyHttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * 签名验证过滤器
 */
@Slf4j
@Component
@Order(2)
public class SignFilter implements Filter {

    @Autowired
    private SignUtils signUtils;
    @Autowired
    private SignPathConfig signPaths ;

    @Autowired
    private SignKeyManager signKeyManager ;



    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        String uri = request.getRequestURI();

        log.info("开始进行签名验证, uri={}", uri);

        //1.判断签名验证是否启用，未启用则直接放行
        if (!signPaths.isEnabled()) {
            filterChain.doFilter(request, response);
            return;
        }

        //2.判断请求路径是否需要签名验证，不需要则直接放行

        if (!needSign(uri)) {
            filterChain.doFilter(request, response);
            return;
        }
        //3.校验请求方法是否为POST，不是则返回错误
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            log.warn("签名验证失败：只支持POST请求, uri={}", uri);
            writeErrorResponse(response, "只支持POST请求");
            return;
        }

        //4.校验Content-Type是否为application/json格式，不是则返回错误
        String contentType = request.getContentType();
        if (contentType == null || !contentType.contains(MediaType.APPLICATION_JSON_VALUE)) {
            log.warn("签名验证失败：只支持JSON格式, uri={}, contentType={}", uri, contentType);
            writeErrorResponse(response, "只支持JSON格式请求");
            return;
        }

        try {
            //5.从请求头中提取signRandomKey并校验是否为空
            String signRandomKey = request.getHeader(RequestAuthExtractor.HEADER_SIGN_RANDOM_KEY);
            if (signRandomKey == null || signRandomKey.isEmpty()) {
                log.warn("签名验证失败：请求头中signRandomKey为空, uri={}", uri);
                writeErrorResponse(response, "请求头中signRandomKey不能为空");
                return;
            }

            //6.从请求头中提取sign并校验是否为空
            String sign = request.getHeader(RequestAuthExtractor.HEADER_SIGN);
            if (sign == null || sign.isEmpty()) {
                log.warn("签名验证失败：请求头中sign为空, uri={}", uri);
                writeErrorResponse(response, "请求头中sign不能为空");
                return;
            }

            //7.包装HttpServletRequest，缓存请求体内容以便多次读取
            CachedBodyHttpServletRequest wrappedRequest = new CachedBodyHttpServletRequest(request);
            String body = wrappedRequest.getBody();
            
            //8.校验请求体是否为空，为空则返回错误
            if (body == null || body.isEmpty()) {
                log.warn("签名验证失败：请求体为空, uri={}", uri);
                writeErrorResponse(response, "请求体不能为空");
                return;
            }

            //9.将JSON请求体解析为Map对象
            Map<String, Object> params = OBJECT_MAPPER.readValue(body, new TypeReference<Map<String, Object>>() {});
            log.info("待加密的参数");
            //
            byte[] signKeyBytes = signKeyManager.getSignKeyBytes(signRandomKey);

            params.put("sign", sign);
            //11.调用签名工具验证签名是否正确
            boolean valid = signUtils.verifySign(signKeyBytes, params);
            if (!valid) {
                log.warn("签名验证失败, uri={}, signRandomKey={}", uri, signRandomKey);
                writeErrorResponse(response, "签名验证失败");
                return;
            }

            //12.签名验证成功，记录日志并放行请求
            log.info("签名验证成功, uri={}, signRandomKey={}", uri, signRandomKey);
            filterChain.doFilter(wrappedRequest, response);

        } catch (Exception e) {
            //13.捕获签名验证过程中的异常，返回错误响应
            log.error("签名验证异常, uri={}", uri, e);
            writeErrorResponse(response, "签名验证异常");
        }
    }

    /**
     * 判断请求路径是否需要签名验证
     * 
     * @param uri 请求URI
     * @return true-需要签名，false-不需要签名
     */
    private boolean needSign(String uri) {

        return  signPaths.getPaths().contains( uri);
    }

    private void writeErrorResponse(HttpServletResponse response, String message) throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);
        
        Result<Void> result = Result.fail(40001, message);
        response.getWriter().write(JsonUtils.toJsonString(result));
    }
}
