package com.lanf.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lanf.cache.service.RedissonCacheService;
import com.lanf.common.utils.DateUtils;
import com.lanf.constant.code.CommonResultCodeEnum;
import com.lanf.constant.constant.Constants;
import com.lanf.constant.constant.RedisKeyConstants;
import com.lanf.constant.exception.BizException;
import com.lanf.constant.result.Result;
import com.lanf.security.config.AdminTokenConfig;
import com.lanf.security.model.bo.AdminUser;
import com.lanf.security.model.bo.AdminUserBO;
import com.lanf.security.model.dto.LoginDTO;
import com.lanf.security.model.vo.AdminUserTokenInfoVO;
import com.lanf.security.service.PermissionCacheService;
import com.lanf.web.auth.RequestAuthExtractor;
import com.lanf.web.model.bo.AuthRequestInfo;
import com.lanf.web.utils.JwtUtils;
import com.lanf.web.utils.ResponseUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 登录过滤器，继承UsernamePasswordAuthenticationFilter，对用户名密码进行登录校验
 * </p>
 */
@Slf4j
public class AdminLoginFilter extends UsernamePasswordAuthenticationFilter {





    private AuthenticationManager authenticationManager;

    private RedissonCacheService redissonCacheService;

    private AdminTokenConfig adminTokenConfig;

    private PermissionCacheService permissionCacheService;

    public AdminLoginFilter(AuthenticationManager authenticationManager,
                            RedissonCacheService redissonCacheService,
                            PermissionCacheService permissionCacheService,
                            AdminTokenConfig adminTokenConfig) {
        this.setAuthenticationManager(authenticationManager);
        this.setPostOnly(false);
        this.redissonCacheService = redissonCacheService;
        this.permissionCacheService = permissionCacheService;
        /**
         * 这个过滤器只拦截 /admin/system/index/login 接口
         */
        this.setRequiresAuthenticationRequestMatcher(new
                AntPathRequestMatcher("/admin/system/index/login", "POST"));
    }

    /**
     * 登录认证前组装参数
     * 透传给登入过滤器
     *
     *
       结合 WebSecurityConfig.configure 里指定了
       自定义加载用户方法，需要租户TenantCode 所以这里获取参数后 透传下去
     *
     *
     */
    @Override
    public Authentication attemptAuthentication(HttpServletRequest req, HttpServletResponse res)
            throws AuthenticationException {
        try {

            LoginDTO loginDTO = new ObjectMapper().readValue(req.getInputStream(), LoginDTO.class);
            Authentication authenticationToken = new UsernamePasswordAuthenticationToken(
                    loginDTO.getUsername(), loginDTO.getPassword());

            //将租户code 透传到 自定义查找用户方法里 UserDetailsServiceImpl.loadUserByUsername
            req.setAttribute(Constants.TENANT_CODE,loginDTO.getTenantCode());
            return this.getAuthenticationManager().authenticate(authenticationToken);
        } catch (IOException e) {
            log.error("登录认证前组装参数异常",e);
            throw new BizException("登录认证前组装参数异常");
        }

    }

    /**
     * 登录成功处理
     * 响应web数据
     *
     */
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
                                            Authentication auth) {

        /**
         * 这个AdminUserBO 从UserDetails loadUserByUsername(String username) 返回
         */
        AdminUserBO customUser = (AdminUserBO) auth.getPrincipal();
        AdminUser sysUser = customUser.getSysUser();
        //权限
        Collection<GrantedAuthority> authorities = customUser.getAuthorities();

        /**
         * 过期时间与刷新token一致 当token刷新时 刷新这个权限缓存
         */
        permissionCacheService.cachePermissions(sysUser.getId(),
                sysUser.getChannel(), authorities, adminTokenConfig.getRefreshTokenExpMinutes());

        AuthRequestInfo authRequestInfo = null;
        try {
            authRequestInfo =  RequestAuthExtractor.extractBasicInfo( request);
        } catch (Exception e) {
            log.error("登录认证异常",e);
            ResponseUtil.outFail(response, Result.fail(CommonResultCodeEnum.FAIL.getCode(),
                    CommonResultCodeEnum.FAIL.getMessage()));
            return;
        }
        //1.创建token
        AdminUserTokenInfoVO adminUserTokenInfoVO = createToken(authRequestInfo, sysUser.getId(),
                sysUser.getTenantId());
        //2.响应数据
        ResponseUtil.outSuccess(response, Result.ok(adminUserTokenInfoVO));
    }

    private AdminUserTokenInfoVO createToken(AuthRequestInfo authRequestInfo,Long userId,Long tenantId){

        String deviceId = authRequestInfo.getDeviceId();
        String channel = authRequestInfo.getChannel();
        String accessToken = JwtUtils.createTokenForAdminWithMinutes(userId, deviceId,tenantId, adminTokenConfig.getAccessTokenExpMinutes());
        String refreshToken = JwtUtils.createTokenForAdminWithMinutes(userId, deviceId,tenantId, adminTokenConfig.getRefreshTokenExpMinutes());

        String accessKey = String.format(RedisKeyConstants.USER_ACCESS_TOKEN, userId, channel);
        String refreshKey = String.format(RedisKeyConstants.USER_REFRESH_TOKEN, userId, channel);

        redissonCacheService.set(accessKey, accessToken, adminTokenConfig.getAccessTokenExpMinutes(), TimeUnit.MINUTES);
        redissonCacheService.set(refreshKey, refreshToken, adminTokenConfig.getRefreshTokenExpMinutes(), TimeUnit.MINUTES);


        AdminUserTokenInfoVO tokenInfo = new AdminUserTokenInfoVO();
        tokenInfo.setAccessToken(accessToken);
        tokenInfo.setRefreshToken(refreshToken);
        // 计算过期时间戳（当前时间 + 有效期）
        tokenInfo.setAccessTokenExp(DateUtils.getExpireTimestampFromMinutes(adminTokenConfig.getAccessTokenExpMinutes()));
        tokenInfo.setRefreshTokenExp(DateUtils.getExpireTimestampFromDays(adminTokenConfig.getRefreshTokenExpMinutes()));


        return tokenInfo;
    }

    /**
     * 登录失败 响应失败结果
     *
     */
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                              AuthenticationException e) throws IOException, ServletException {
        log.error("授权失败",e);

        if (e instanceof BadCredentialsException) {
            ResponseUtil.outFail(response, Result.fail("用户名称或密码错误"));
        } else {
            ResponseUtil.outFail(response, Result.fail(CommonResultCodeEnum.FAIL.getCode(),
                    CommonResultCodeEnum.FAIL.getMessage()));
        }
    }

}

