package com.lanf.system.service.impl;

import com.lanf.cache.service.RedissonCacheService;
import com.lanf.common.utils.DateUtils;
import com.lanf.common.utils.IStringUtils;
import com.lanf.constant.code.CommonResultCodeEnum;
import com.lanf.constant.constant.RedisKeyConstants;
import com.lanf.constant.exception.BizException;
import com.lanf.security.config.AdminTokenConfig;
import com.lanf.security.service.PermissionCacheService;
import com.lanf.system.model.dto.RefreshTokenDTO;
import com.lanf.system.model.vo.AdminTokenInfoVO;
import com.lanf.system.service.IAdminAuthService;
import com.lanf.web.auth.RequestAuthExtractor;
import com.lanf.web.model.bo.AuthRequestInfo;
import com.lanf.web.model.bo.JwtTokenInfo;
import com.lanf.web.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.TimeUnit;

/**
 * 管理员认证服务实现类
 */
@Slf4j
@Service
public class AdminAuthServiceImpl implements IAdminAuthService {

    @Autowired
    private RedissonCacheService redissonCacheService;
    @Autowired
    private PermissionCacheService permissionCacheService;

    @Autowired
    private AdminTokenConfig adminTokenConfig;


    @Override
    public AdminTokenInfoVO refreshToken(RefreshTokenDTO dto, HttpServletRequest request) {
        try {
            // 1. 提取请求基本信息
            AuthRequestInfo authRequestInfo = RequestAuthExtractor.extractBasicInfo(request);
            
            String refreshToken = dto.getRefreshToken();
            if (IStringUtils.isEmpty(refreshToken)) {
                log.warn("刷新令牌为空");
                throw new BizException("刷新令牌为空");
            }

            // 2. 解析 Refresh Token
            JwtTokenInfo jwtTokenInfo;
            try {
                jwtTokenInfo = JwtUtils.parseAdminToken(refreshToken);
            } catch (Exception e) {
                log.warn("刷新令牌解析失败: {}", e.getMessage());
                throw new BizException("刷新令牌解析失败");
            }

            // 3. 验证设备 ID 是否一致
            String requestDeviceId = authRequestInfo.getDeviceId();
            String tokenDeviceId = jwtTokenInfo.getDeviceId();
            if (!requestDeviceId.equals(tokenDeviceId)) {
                log.warn("设备ID不一致，请求头: {}, Token中: {}", requestDeviceId, tokenDeviceId);
                throw new BizException("设备ID不一致");
            }

            // 4. 验证 Refresh Token 是否在缓存中存在且匹配
            Long userId = jwtTokenInfo.getUserId();
            String channel = authRequestInfo.getChannel();
            String refreshKey = String.format(RedisKeyConstants.USER_REFRESH_TOKEN, userId, channel);
            String cachedRefreshToken = redissonCacheService.get(refreshKey);

            if (IStringUtils.isEmpty(cachedRefreshToken)) {
                log.warn("刷新令牌已失效，可能已被使用或过期: userId={}", userId);
                throw new BizException("刷新令牌已失效");
            }

            if (!refreshToken.equals(cachedRefreshToken)) {
                log.warn("刷新令牌与缓存不一致，可能存在安全风险: userId={}", userId);
                throw new BizException("刷新令牌与缓存不一致，可能存在安全风险");
            }

            // 5. 生成新的 Token 并缓存
            AdminTokenInfoVO tokenInfo = generateAndCacheTokens(userId,jwtTokenInfo.getTenantId(), authRequestInfo);

            boolean renewed = permissionCacheService.renewPermission(userId, channel);
            if (!renewed) {
                log.warn("权限缓存已过期，请重新登录: userId={}, deviceId={}, channel={}", userId, requestDeviceId, channel);
                throw new BizException("权限缓存已过期");
            }

            log.info("刷新令牌成功: userId={}, deviceId={}, channel={}", userId, requestDeviceId, channel);

            return tokenInfo;

        }  catch (Exception e) {
            log.error("刷新令牌异常", e);
            throw new BizException(CommonResultCodeEnum.KICKED_OUT.getCode(),
                    CommonResultCodeEnum.KICKED_OUT.getMessage());
        }
    }

    /**
     * 生成并缓存新的 Token
     * 
     * @param userId 用户ID
     * @param authRequestInfo 认证请求信息
     * @return Token 信息
     */
    private AdminTokenInfoVO generateAndCacheTokens(Long userId,Long tenantId, AuthRequestInfo authRequestInfo) {

        String deviceId = authRequestInfo.getDeviceId();
        String channel = authRequestInfo.getChannel();
        Long accessTokenExpMinutes = adminTokenConfig.getAccessTokenExpMinutes();
        Long refreshTokenExpMinutes = adminTokenConfig.getRefreshTokenExpMinutes();

        String accessToken = JwtUtils.createTokenForAdminWithMinutes(userId, deviceId,tenantId, accessTokenExpMinutes);
        String refreshToken = JwtUtils.createTokenForAdminWithMinutes(userId, deviceId,tenantId, refreshTokenExpMinutes);

        String accessKey = String.format(RedisKeyConstants.USER_ACCESS_TOKEN, userId, channel);
        String refreshKey = String.format(RedisKeyConstants.USER_REFRESH_TOKEN, userId, channel);

        redissonCacheService.set(accessKey, accessToken, accessTokenExpMinutes, TimeUnit.MINUTES);
        redissonCacheService.set(refreshKey, refreshToken, refreshTokenExpMinutes, TimeUnit.MINUTES);

        // 构建返回对象
        AdminTokenInfoVO tokenInfoVO = new AdminTokenInfoVO();
        tokenInfoVO.setAccessToken(accessToken);
        tokenInfoVO.setRefreshToken(refreshToken);
        tokenInfoVO.setAccessTokenExp(DateUtils.getExpireTimestampFromMinutes(accessTokenExpMinutes));
        tokenInfoVO.setRefreshTokenExp(DateUtils.getExpireTimestampFromMinutes(refreshTokenExpMinutes));

        return tokenInfoVO;
    }


}
