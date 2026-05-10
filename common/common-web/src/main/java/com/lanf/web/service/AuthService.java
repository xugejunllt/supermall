package com.lanf.web.service;

import com.lanf.cache.service.RedissonCacheService;
import com.lanf.constant.code.CommonResultCodeEnum;
import com.lanf.constant.constant.RedisKeyConstants;
import com.lanf.constant.exception.BizException;
import com.lanf.constant.result.Result;
import com.lanf.web.config.AuthPathConfig;
import com.lanf.web.model.bo.AuthRequestInfo;
import com.lanf.web.model.bo.FeignRequestInfo;
import com.lanf.web.model.bo.JwtTokenInfo;
import com.lanf.web.utils.JwtUtils;
import com.lanf.web.utils.ResponseUtil;
import com.lanf.web.utils.UserContext;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.FilterChain;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Slf4j
@Component
public class AuthService {
    
    @Autowired
    private AuthPathConfig authPathConfig;
    @Autowired
    private RedissonCacheService redissonCacheService;


    
    public  void authenticate(ServletRequest servletRequest,
                                    ServletResponse servletResponse,
                                    FilterChain filterChain, boolean isAdmin) {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        StringBuffer requestURL = request.getRequestURL();
        String requestURI = request.getRequestURI();
        List<String> excludeAuthPaths = authPathConfig.getExcludeAuthPaths();
        List<String> internalServicePaths = authPathConfig.getInternalServicePaths();
        try {
            if (excludeAuthPaths.contains(requestURI)) {
                AuthRequestInfo authRequestInfo = RequestAuthExtractor.extractBasicInfo(request);
                log.info("接收到请求,请求类型[{}],请求路径[{}],请求头[{}]", request.getMethod(), requestURL, authRequestInfo);
                UserContext.setDeviceId(authRequestInfo.getDeviceId());
                filterChain.doFilter(servletRequest, servletResponse);
                return;
            }
            if (internalServicePaths.contains(requestURI)) {
                FeignRequestInfo authRequestInfo = RequestAuthExtractor.extractFeignAuthInfo(request);
                log.info("接收到请求,请求类型[{}],请求路径[{}],请求头[{}]", request.getMethod(), requestURL, authRequestInfo);

                UserContext.setDeviceId(authRequestInfo.getDeviceId());
                UserContext.setTenantId(authRequestInfo.getTenantId());
                UserContext.setUserId(authRequestInfo.getUserId());

                filterChain.doFilter(servletRequest, servletResponse);
                return;
            }

            AuthRequestInfo authRequestInfo = RequestAuthExtractor.extractAuthInfo(request, isAdmin);
            log.info("接收到请求,请求类型[{}],请求路径[{}],请求头[{}]", request.getMethod(), requestURL, authRequestInfo);

            String accessToken = authRequestInfo.getAccessToken();
            String deviceId = authRequestInfo.getDeviceId();
            Long tenantId = authRequestInfo.getTenantId();
            JwtTokenInfo jwtTokenInfo;
            try {
                if (isAdmin) {
                    jwtTokenInfo = JwtUtils.parseAdminToken(accessToken);
                } else {
                    jwtTokenInfo = JwtUtils.parseUserToken(accessToken);

                }
            } catch (ExpiredJwtException e) {
                log.warn("Token已过期");
                ResponseUtil.out(response, Result.fail(CommonResultCodeEnum.TOKEN_EXPIRED.getCode(), CommonResultCodeEnum.TOKEN_EXPIRED.getMessage()));
                return;

            } catch (BizException e) {
                ResponseUtil.out(response, Result.fail(e.getCode(), e.getMessage()));
                return;
            } catch (Exception e) {
                log.warn("Token解析失败: {}", e.getMessage());
                ResponseUtil.out(response, Result.fail(CommonResultCodeEnum.AUTH_FAILED.getCode(), CommonResultCodeEnum.AUTH_FAILED.getMessage()));
                return;
            }


            String jwtDeviceId = jwtTokenInfo.getDeviceId();
            Long jwtTenantId = jwtTokenInfo.getTenantId();
            if (!deviceId.equals(jwtDeviceId)) {
                log.warn("设备ID不匹配，请求头: {}, Token中: {}", deviceId, jwtDeviceId);
                ResponseUtil.out(response, Result.fail(CommonResultCodeEnum.AUTH_FAILED.getCode(), "设备信息不匹配"));
                return;
            }
            if (isAdmin) {
                if (!tenantId.equals(jwtTenantId)) {
                    log.warn("租户id不匹配，请求头: {}, Token中: {}", tenantId, jwtTenantId);
                    ResponseUtil.out(response, Result.fail(CommonResultCodeEnum.AUTH_FAILED.getCode(), "设备信息不匹配"));
                    return;
                }
            }
            /**
             * 校验缓存中
             *
             */
            String key = String.format(RedisKeyConstants.USER_ACCESS_TOKEN, jwtTokenInfo.getUserId(),
                    authRequestInfo.getChannel());
            String accessTokenCache = redissonCacheService.get(key);
            if (accessTokenCache == null ) {
                log.warn("Token已过期");
                ResponseUtil.out(response, Result.fail(CommonResultCodeEnum.TOKEN_EXPIRED.getCode(), CommonResultCodeEnum.TOKEN_EXPIRED.getMessage()));
                return;
            }
            if ( !accessTokenCache.equals(accessToken)) {
                /**
                 * 已被踢出了
                 */
                log.warn("与缓存token不一致");
                ResponseUtil.out(response, Result.fail(CommonResultCodeEnum.KICKED_OUT.getCode(), CommonResultCodeEnum.KICKED_OUT.getMessage()));
                return;
            }

            UserContext.setUserId(jwtTokenInfo.getUserId());
            UserContext.setDeviceId(jwtTokenInfo.getDeviceId());
            UserContext.setTenantId(jwtTokenInfo.getTenantId());

            filterChain.doFilter(servletRequest, servletResponse);

        } catch (Exception e) {
            log.error("用户认证过滤器异常", e);
            ResponseUtil.out(response, Result.fail(CommonResultCodeEnum.AUTH_FAILED.getCode(), CommonResultCodeEnum.AUTH_FAILED.getMessage()));
        } finally {
            UserContext.clear();
        }


    }



    

    

    


}
