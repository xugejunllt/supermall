package com.lanf.web.service;

import com.lanf.constant.code.CommonResultCodeEnum;
import com.lanf.constant.exception.BizException;
import com.lanf.constant.result.Result;
import com.lanf.web.model.bo.AuthRequestInfo;
import com.lanf.web.model.bo.JwtTokenInfo;
import com.lanf.web.utils.JwtUtils;
import com.lanf.web.utils.ResponseUtil;
import com.lanf.web.utils.UserContext;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.FilterChain;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
public class AuthService {
    /**
     * isAdmin: true = Admin 请求，false = C 端请求
     */
    public static void authenticate(ServletRequest servletRequest,
                                    ServletResponse servletResponse,
                                    FilterChain filterChain, boolean isAdmin) {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        StringBuffer requestURL = request.getRequestURL();
        try {
            AuthRequestInfo authRequestInfo = RequestAuthExtractor.extractAuthInfo(request, isAdmin);
            /**
             * 打印请求路径 方便排查问题
             */
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
                log.warn("Token已过期: {}", e.getMessage());
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
             * 添加到UserContext
             */
            UserContext.setUserId(jwtTokenInfo.getUserId());
            UserContext.setDeviceId(jwtTokenInfo.getDeviceId());
            UserContext.setTenantId(jwtTokenInfo.getTenantId());

            filterChain.doFilter(servletRequest, servletResponse);

        } catch (Exception e) {
            log.error("用户认证过滤器异常", e);
            ResponseUtil.out(response, Result.fail(CommonResultCodeEnum.AUTH_FAILED.getCode(), CommonResultCodeEnum.AUTH_FAILED.getMessage()));
        } finally {
            /**
             * 清除上下文信息
             *
             */
            UserContext.clear();
        }


    }

}
