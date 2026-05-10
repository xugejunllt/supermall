package com.lanf.web.service;

import com.lanf.constant.exception.BizException;
import com.lanf.web.model.bo.AuthRequestInfo;
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

    public static final String CHANNEL_ANDROID = "android";
    public static final String CHANNEL_IOS = "ios";
    public static final String CHANNEL_APP = "app";
    public static final String CHANNEL_WEB = "web";
    public static final String CHANNEL_PC = "pc";

    /**
     * 从HTTP请求中提取认证信息
     *
     * @param request HTTP请求对象
     * @return 认证请求信息
     * @throws Exception 当必填参数缺失时抛出异常
     */
    public static AuthRequestInfo extractAuthInfo(HttpServletRequest request, boolean isAdmin) throws Exception {
        if (request == null) {
            log.error("HTTP请求对象为空");
            throw new Exception("HTTP请求对象为空");
        }

        String deviceId = request.getHeader(HEADER_DEVICE_ID);
        String accessToken = request.getHeader(HEADER_ACCESS_TOKEN);
        String channel = request.getHeader(HEADER_CHANNEL);
        String tenantIdStr = request.getHeader(HEADER_TENANT_ID);
        String version = request.getHeader(HEADER_ACCESS_TOKEN);

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
        Long tenantId = null;
        if (isAdmin) {
            if (!StringUtils.hasText(tenantIdStr)) {
                log.warn("请求头中缺少tenantId参数");
                throw new BizException("请求头中缺少tenantId参数");
            }
            try {
                tenantId = Long.parseLong(tenantIdStr);
            } catch (NumberFormatException e) {
                log.warn("tenantId格式错误: {}", tenantIdStr);
                throw new BizException("tenantId格式错误");
            }
        }
        channel = normalizeChannel(channel);
        AuthRequestInfo authRequestInfo = new AuthRequestInfo();
        authRequestInfo.setDeviceId(deviceId);
        authRequestInfo.setAccessToken(accessToken);
        authRequestInfo.setChannel(channel);
        authRequestInfo.setTenantId(tenantId);
        authRequestInfo.setVersion( version);

        return authRequestInfo;
    }

    /**
     * 标准化渠道参数
     * android和ios转换为app，其他保持不变
     *
     * @param channel 原始渠道参数
     * @return 标准化后的渠道参数
     */
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
