package com.lanf.web.auth;

import com.lanf.cache.service.RedissonCacheService;
import com.lanf.constant.code.CommonResultCodeEnum;
import com.lanf.constant.constant.RedisKeyConstants;
import com.lanf.constant.exception.BizException;
import com.lanf.constant.utils.UserContext;
import com.lanf.web.config.AuthPathConfig;
import com.lanf.web.model.bo.AuthRequestInfo;
import com.lanf.web.model.bo.FeignRequestInfo;
import com.lanf.web.model.bo.JwtTokenInfo;
import com.lanf.web.security.sign.SigningKeyContext;
import com.lanf.web.utils.JwtUtils;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Slf4j
@Component
public class AuthService {
    
    @Autowired
    private AuthPathConfig authPathConfig;
    @Autowired
    private RedissonCacheService redissonCacheService;


    
    public  void authenticate(ServletRequest servletRequest, boolean isAdmin) {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        String requestURI = request.getRequestURI();
        List<String> excludeAuthPaths = authPathConfig.getExcludeAuthPaths();
        List<String> internalServicePaths = authPathConfig.getInternalServicePaths();
        try {
            if (excludeAuthPaths.contains(requestURI)) {
                AuthRequestInfo authRequestInfo = RequestAuthExtractor.extractBasicInfo(request);
                log.info("接收到不需要鉴权请求,请求类型[{}],请求路径[{}],请求头[{}]", request.getMethod(), requestURI, authRequestInfo);
                UserContext.setDeviceId(authRequestInfo.getDeviceId());
                return;
            }
            if (internalServicePaths.contains(requestURI)) {
                FeignRequestInfo authRequestInfo = RequestAuthExtractor.extractFeignAuthInfo(request);
                log.info("接收到内部请求,请求类型[{}],请求路径[{}],请求头[{}]", request.getMethod(), requestURI, authRequestInfo);

                UserContext.setDeviceId(authRequestInfo.getDeviceId());
                UserContext.setTenantId(authRequestInfo.getTenantId());
                UserContext.setUserId(authRequestInfo.getUserId());

                return;
            }

            AuthRequestInfo authRequestInfo = RequestAuthExtractor.extractAuthInfo(request);
            String accessToken = authRequestInfo.getAccessToken();
            authRequestInfo.setAccessToken(null);
            log.info("接收到鉴权请求,请求类型[{}],请求路径[{}],请求头[{}]", request.getMethod(), requestURI, authRequestInfo);

            String deviceId = authRequestInfo.getDeviceId();
            JwtTokenInfo jwtTokenInfo;
            try {
                if (isAdmin) {
                    jwtTokenInfo = JwtUtils.parseAdminToken(accessToken);
                } else {
                    jwtTokenInfo = JwtUtils.parseUserToken(accessToken);

                }
            } catch (ExpiredJwtException e) {
                log.warn("Token已过期");
                throw new BizException(CommonResultCodeEnum.TOKEN_EXPIRED);

            } catch (BizException e) {
                throw e;

            } catch (Exception e) {
                log.warn("Token解析失败: {}", e.getMessage());
                throw new BizException(CommonResultCodeEnum.AUTH_FAILED);

            }

            String jwtDeviceId = jwtTokenInfo.getDeviceId();
            if (!deviceId.equals(jwtDeviceId)) {
                log.warn("设备ID不匹配，请求头: {}, Token中: {}", deviceId, jwtDeviceId);
                throw new BizException(CommonResultCodeEnum.AUTH_FAILED);


            }
            /**
             * 校验缓存中
             *
             */
            String key = String.format(RedisKeyConstants.USER_ACCESS_TOKEN, jwtTokenInfo.getUserId(),
                    authRequestInfo.getChannel());
            String accessTokenCache = redissonCacheService.get(key);
            if (accessTokenCache == null ) {
                log.warn("redis中 Token已过期");
                throw new BizException(CommonResultCodeEnum.TOKEN_EXPIRED);
            }
            if ( !accessTokenCache.equals(accessToken)) {
                /**
                 * 已被踢出了
                 */
                log.warn("与缓存token不一致");
                throw new BizException(CommonResultCodeEnum.KICKED_OUT);


            }

            UserContext.setUserId(jwtTokenInfo.getUserId());
            UserContext.setDeviceId(jwtTokenInfo.getDeviceId());
            UserContext.setTenantId(jwtTokenInfo.getTenantId());
            // 提取并缓存signingKey到ThreadLocal
            SigningKeyContext.setFromBase64(jwtTokenInfo.getSigningKey());


        } catch (Exception e) {
            log.error("用户认证过滤器异常", e);
            throw new BizException(CommonResultCodeEnum.AUTH_FAILED);
        }


    }




    

    


    


}
