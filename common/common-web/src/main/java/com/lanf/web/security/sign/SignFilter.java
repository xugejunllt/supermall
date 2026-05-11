package com.lanf.web.security.sign;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lanf.cache.service.RedissonCacheService;
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
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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
    private SignPathConfig signConfig;
    
    @Autowired
    private SignKeyManager signKeyManager;

    @Autowired
    private RedissonCacheService redissonCacheService;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    
    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    /**
     * nonce缓存前缀
     */
    private static final String NONCE_CACHE_PREFIX = "sign:nonce:%s";

    /**
     * nonce过期时间（5分钟）
     */
    private static final long NONCE_EXPIRE_TIME = 5;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        log.info("开始进行签名验证");
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        //1.判断签名验证是否启用，未启用则直接放行
        if (!signConfig.isEnabled()) {
            log.debug("签名验证未启用，直接放行");
            filterChain.doFilter(request, response);
            return;
        }

        //2.判断请求路径是否需要签名验证，不需要则直接放行
        String uri = request.getRequestURI();
        if (!needSign(uri)) {
            filterChain.doFilter(request, response);
            return;
        }
        log.info("开始进行签名验证, uri={}", uri);
        
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

            //7.从请求头中提取nonce（随机数）并校验
            String nonce = request.getHeader(RequestAuthExtractor.HEADER_NONCE);
            if (nonce == null || nonce.isEmpty()) {
                log.warn("签名验证失败：请求头中nonce为空, uri={}", uri);
                writeErrorResponse(response, "请求头中nonce不能为空");
                return;
            }

            //8.从请求头中提取timestamp（时间戳）并校验
            String timestamp = request.getHeader(RequestAuthExtractor.HEADER_TIMESTAMP);
            if (timestamp == null || timestamp.isEmpty()) {
                log.warn("签名验证失败：请求头中timestamp为空, uri={}", uri);
                writeErrorResponse(response, "请求头中timestamp不能为空");
                return;
            }

            //9.校验时间戳是否过期（防止重放攻击）
            if (!isTimestampValid(timestamp)) {
                log.warn("签名验证失败：请求已过期, uri={}, timestamp={}", uri, timestamp);
                writeErrorResponse(response, "请求已过期");
                return;
            }

            //10.校验nonce是否已使用（防重放）
            if (isNonceUsed(signRandomKey, nonce)) {
                log.warn("签名验证失败：nonce已被使用, uri={}, nonce={}", uri, nonce);
                writeErrorResponse(response, "请求已被处理，请勿重复提交");
                return;
            }

            //11.包装HttpServletRequest，缓存请求体内容以便多次读取
            CachedBodyHttpServletRequest wrappedRequest = new CachedBodyHttpServletRequest(request);
            String body = wrappedRequest.getBody();
            
            //12.校验请求体是否为空，为空则返回错误
            if (body == null || body.isEmpty()) {
                log.warn("签名验证失败：请求体为空, uri={}", uri);
                writeErrorResponse(response, "请求体不能为空");
                return;
            }

            //13.将JSON请求体解析为Map对象
            Map<String, Object> params = OBJECT_MAPPER.readValue(body, new TypeReference<Map<String, Object>>() {});
            
            //14.获取AES密钥字节数组
            byte[] signKeyBytes = signKeyManager.getSignKeyBytes(signRandomKey);
            
            //15.将sign添加到参数Map中用于验签
            params.put("sign", sign);
            
            //16.调用签名工具验证签名是否正确
            boolean valid = signUtils.verifySign(signKeyBytes, params);
            if (!valid) {
                log.warn("签名验证失败, uri={}, signRandomKey={}", uri, signRandomKey);
                writeErrorResponse(response, "签名验证失败");
                return;
            }

            //17.签名验证成功，记录日志并放行请求（nonce已在步骤10中通过incrementAndGet标记）
            log.info("签名验证成功, uri={}, signRandomKey={}, nonce={}", uri, signRandomKey, nonce);
            filterChain.doFilter(wrappedRequest, response);

        } catch (Exception e) {
            //18.捕获签名验证过程中的异常，返回错误响应
            log.error("签名验证异常, uri={}", uri, e);
            writeErrorResponse(response, "签名验证异常");
        }
    }

    /**
     * 校验时间戳是否有效（5分钟内）
     * 
     * @param timestamp 时间戳（毫秒）
     * @return true-有效，false-过期
     */
    private boolean isTimestampValid(String timestamp) {
        try {
            long requestTime = Long.parseLong(timestamp);
            long currentTime = System.currentTimeMillis();
            long diff = Math.abs(currentTime - requestTime);
            
            // 允许5分钟的时间误差
            boolean valid = diff <= (NONCE_EXPIRE_TIME * 60 * 1000);
            log.debug("时间戳校验: 请求时间={}, 当前时间={}, 差值={}ms, 结果={}", 
                    requestTime, currentTime, diff, valid ? "有效" : "过期");
            return valid;
        } catch (NumberFormatException e) {
            log.error("时间戳格式错误: {}", timestamp);
            return false;
        }
    }

    /**
     * 校验nonce是否已被使用（防重放）
     * 使用Redis原子递增，如果返回值大于1表示已使用过
     * 
     * @param signRandomKey 签名随机key
     * @param nonce 随机数
     * @return true-已使用，false-未使用
     */
    private boolean isNonceUsed(String signRandomKey, String nonce) {
        String cacheKey = String.format(NONCE_CACHE_PREFIX, buildNonceCacheKey(signRandomKey, nonce));
        
        // 原子递增，返回递增后的值
        long count = redissonCacheService.incrementAndGet(cacheKey, NONCE_EXPIRE_TIME, TimeUnit.MINUTES);
        
        // 如果大于1，说明之前已经请求过
        boolean used = count > 1;
        
        if (used) {
            log.warn("nonce已被使用: cacheKey={}, count={}", cacheKey, count);
        } else {
            log.debug("nonce首次使用: cacheKey={}, count={}", cacheKey, count);
        }
        
        return used;
    }

    /**
     * 标记nonce为已使用（已合并到isNonceUsed中，此方法保留但不再单独调用）
     * 
     * @param signRandomKey 签名随机key
     * @param nonce 随机数
     */
    private void markNonceAsUsed(String signRandomKey, String nonce) {
        // 该方法已在isNonceUsed中通过incrementAndGet实现，无需单独调用
        log.debug("markNonceAsUsed已废弃，使用isNonceUsed中的incrementAndGet代替");
    }

    /**
     * 构建nonce缓存key
     * 
     * @param signRandomKey 签名随机key
     * @param nonce 随机数
     * @return 缓存key
     */
    private String buildNonceCacheKey(String signRandomKey, String nonce) {
        return signRandomKey + ":" + nonce;
    }

    /**
     * 判断请求路径是否需要签名验证
     * 
     * @param uri 请求URI
     * @return true-需要签名，false-不需要签名
     */
    private boolean needSign(String uri) {
        //1.获取需要签名的路径列表
        List<String> signPaths = signConfig.getPaths();

        return signPaths.contains(uri);
    }

    private void writeErrorResponse(HttpServletResponse response, String message) throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);
        
        Result<Void> result = Result.fail(40001, message);
        response.getWriter().write(JsonUtils.toJsonString(result));
    }
}
