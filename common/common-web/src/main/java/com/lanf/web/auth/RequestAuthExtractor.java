package com.lanf.web.auth;

import com.lanf.constant.exception.BizException;
import com.lanf.web.model.bo.AuthRequestInfo;
import com.lanf.web.model.bo.FeignRequestInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;

@Slf4j
public class RequestAuthExtractor {

    public static final String HEADER_DEVICE_ID = "deviceId";
    public static final String HEADER_ACCESS_TOKEN = "accessToken";
    public static final String HEADER_CHANNEL = "channel";
    public static final String HEADER_TENANT_ID = "tenantId";
    public static final String HEADER_VERSION = "version";
    public static final String HEADER_SIGN_RANDOM_KEY = "signRandomKey";
    public static final String HEADER_SIGN = "X-Signature";
    public static final String HEADER_NONCE = "nonce";
    public static final String HEADER_TIMESTAMP = "timestamp";
    public static final String HEADER_TRACE_ID = "X-Trace-Id";

    public static final String FEIGN_HEADER_USER_ID = "feign-userId";
    public static final String FEIGN_HEADER_DEVICE_ID = "feign-deviceId";
    public static final String FEIGN_HEADER_TENANT_ID = "feign-tenantId";
    public static final String FEIGN_HEADER_TRACE_ID = "feign-traceId";

    public static final String CHANNEL_ANDROID = "android";
    public static final String CHANNEL_IOS = "ios";
    public static final String CHANNEL_APP = "app";
    public static final String CHANNEL_ADMIN_WEB = "admin_web";
    public static final String CHANNEL_PC = "pc";

    public static AuthRequestInfo extractAuthInfo(HttpServletRequest request) throws Exception {
        if (request == null) {
            log.error("HTTP请求对象为空");
            throw new Exception("HTTP请求对象为空");
        }

        String deviceId = request.getHeader(HEADER_DEVICE_ID);
        String accessToken = request.getHeader(HEADER_ACCESS_TOKEN);
        String channel = request.getHeader(HEADER_CHANNEL);
        String version = request.getHeader(HEADER_VERSION);

        if (!StringUtils.hasText(deviceId)) {
            log.warn("请求头中缺少deviceId参数");
            throw new BizException("请求头中缺少deviceId参数");
        }
        if (!StringUtils.hasText(version)) {
            log.warn("请求头中缺少version参数");
            throw new BizException("请求头中缺少version参数");
        }
        if (!StringUtils.hasText(accessToken)) {
            log.warn("请求头中缺少accessToken参数");
            throw new BizException("请求头中缺少accessToken参数");
        }

        if (!StringUtils.hasText(channel)) {
            log.warn("请求头中缺少channel参数");
            throw new BizException("请求头中缺少channel参数");
        }

        channel = normalizeChannel(channel);
        AuthRequestInfo authRequestInfo = new AuthRequestInfo();
        authRequestInfo.setDeviceId(deviceId);
        authRequestInfo.setAccessToken(accessToken);
        authRequestInfo.setChannel(channel);
        authRequestInfo.setVersion( version);
        return authRequestInfo;
    }

    public static AuthRequestInfo extractBasicInfo(HttpServletRequest request) throws Exception {
        if (request == null) {
            log.error("HTTP请求对象为空");
            throw new Exception("HTTP请求对象为空");
        }

        String deviceId = request.getHeader(HEADER_DEVICE_ID);
        String channel = request.getHeader(HEADER_CHANNEL);
        String version = request.getHeader(HEADER_VERSION);

        if (!StringUtils.hasText(deviceId)) {
            log.warn("请求头中缺少deviceId参数");
            throw new BizException("请求头中缺少deviceId参数");
        }
        if (!StringUtils.hasText(version)) {
            log.warn("请求头中缺少version参数");
            throw new BizException("请求头中缺少version参数");
        }
        if (!StringUtils.hasText(channel)) {
            log.warn("请求头中缺少channel参数");
            throw new BizException("请求头中缺少channel参数");
        }

        channel = normalizeChannel(channel);
        
        AuthRequestInfo authRequestInfo = new AuthRequestInfo();
        authRequestInfo.setDeviceId(deviceId);
        authRequestInfo.setAccessToken(null);
        authRequestInfo.setChannel(channel);
        authRequestInfo.setVersion(version);

        return authRequestInfo;
    }

    public static FeignRequestInfo extractFeignAuthInfo(HttpServletRequest request) throws Exception {
        if (request == null) {
            log.error("HTTP请求对象为空");
            throw new Exception("HTTP请求对象为空");
        }

        String userIdStr = request.getHeader(FEIGN_HEADER_USER_ID);
        String deviceId = request.getHeader(FEIGN_HEADER_DEVICE_ID);
        String tenantIdStr = request.getHeader(FEIGN_HEADER_TENANT_ID);

        if (!StringUtils.hasText(userIdStr)) {
            log.warn("Feign请求头中缺少userId参数");
            throw new BizException("Feign请求头中缺少userId参数");
        }
        if (!StringUtils.hasText(deviceId)) {
            log.warn("Feign请求头中缺少deviceId参数");
            throw new BizException("Feign请求头中缺少deviceId参数");
        }
        if (!StringUtils.hasText(tenantIdStr)) {
            log.warn("Feign请求头中缺少tenantId参数");
            throw new BizException("Feign请求头中缺少tenantId参数");
        }

        Long userId;
        Long tenantId;
        try {
            userId = Long.parseLong(userIdStr);
        } catch (NumberFormatException e) {
            log.warn("userId格式错误: {}", userIdStr);
            throw new BizException("userId格式错误");
        }

        try {
            tenantId = Long.parseLong(tenantIdStr);
        } catch (NumberFormatException e) {
            log.warn("tenantId格式错误: {}", tenantIdStr);
            throw new BizException("tenantId格式错误");
        }

        FeignRequestInfo feignRequestInfo = new FeignRequestInfo();
        feignRequestInfo.setUserId(userId);
        feignRequestInfo.setDeviceId(deviceId);
        feignRequestInfo.setTenantId(tenantId);


        return feignRequestInfo;
    }

    /**
     * 提取Feign请求认证信息（不需要租户ID）
     * 适用于跨租户或无需租户隔离的场景
     * 
     * @param request HTTP请求对象
     * @return Feign请求信息
     * @throws Exception 解析异常
     */
    public static FeignRequestInfo extractFeignAuthInfoWithoutTenant(HttpServletRequest request) throws Exception {


        String userIdStr = request.getHeader(FEIGN_HEADER_USER_ID);
        String deviceId = request.getHeader(FEIGN_HEADER_DEVICE_ID);
        Long userId = null;
        if (StringUtils.hasText(userIdStr)){

            try {
                userId = Long.parseLong(userIdStr);
            } catch (NumberFormatException e) {
                log.warn("userId格式错误: {}", userIdStr);
                throw new BizException("userId格式错误");
            }
        }


        FeignRequestInfo feignRequestInfo = new FeignRequestInfo();
        feignRequestInfo.setUserId(userId);
        feignRequestInfo.setDeviceId(deviceId);
        feignRequestInfo.setTenantId(null);


        return feignRequestInfo;
    }

    public static String normalizeChannel(String channel) {
        if (!StringUtils.hasText(channel)) {
            return channel;
        }

        String lowerChannel = channel.toLowerCase();

        if (CHANNEL_ANDROID.equalsIgnoreCase(lowerChannel) || CHANNEL_IOS.equalsIgnoreCase(lowerChannel)) {
            return CHANNEL_APP;
        }

        return lowerChannel;
    }
}
