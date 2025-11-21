package com.lanf.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lanf.common.utils.BeanUtil;
import com.lanf.constant.constant.Constants;
import com.lanf.log.api.SystemLogService;
import com.lanf.security.code.SystemResultCodeEnum;
import com.lanf.common.utils.ThreadLocalUtils;
import com.lanf.security.model.CacheSessionBO;
import com.lanf.security.utils.AdminSessionCache;
import com.lanf.system.model.bo.CustomUserBO;
import com.lanf.system.model.bo.SysUserBO;
import com.lanf.system.model.vo.LoginVO;
import com.lanf.web.result.Result;
import com.lanf.web.utils.ResponseUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * 登录过滤器，继承UsernamePasswordAuthenticationFilter，对用户名密码进行登录校验
 * </p>
 */
@Slf4j
@Component
public class AdminLoginFilter extends UsernamePasswordAuthenticationFilter {



    private AdminSessionCache adminSessionCache;

    private AuthenticationManager authenticationManager;

    public AdminLoginFilter(AuthenticationManager authenticationManager, AdminSessionCache adminSessionCache           ) {
        this.setAuthenticationManager(authenticationManager);
        this.adminSessionCache = adminSessionCache;
        this.setPostOnly(false);
        //指定登录接口及提交方式，可以指定任意路径
        this.setRequiresAuthenticationRequestMatcher(new AntPathRequestMatcher("/admin/system/index/login", "POST"));
    }

    /**
     * 登录认证
     *
     * @param req
     * @param res
     * @return
     * @throws AuthenticationException
     */
    @Override
    public Authentication attemptAuthentication(HttpServletRequest req, HttpServletResponse res)
            throws AuthenticationException {
        try {

            LoginVO loginVo = new ObjectMapper().readValue(req.getInputStream(), LoginVO.class);
            Authentication authenticationToken = new UsernamePasswordAuthenticationToken(loginVo.getUsername(), loginVo.getPassword());
            //将租户code 透传到 自定义查找用户方法里 UserDetailsServiceImpl.loadUserByUsername
            req.setAttribute(Constants.TENANT_CODE,loginVo.getTenantCode());
            return this.getAuthenticationManager().authenticate(authenticationToken);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * 登录成功
     *
     * @param request
     * @param response
     * @param chain
     * @param auth
     * @throws IOException
     * @throws ServletException
     */
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
                                            Authentication auth) {


        CustomUserBO customUser = (CustomUserBO) auth.getPrincipal();
        SysUserBO sysUser = customUser.getSysUser();
        String username = sysUser.getUsername();
        Long merchantId = sysUser.getMerchantId();
        //权限
        Collection<GrantedAuthority> authorities = customUser.getAuthorities();
        //加入缓存信息
        CacheSessionBO cacheSessionBO = adminSessionCache.cacheSession(sysUser.getChannel(), sysUser.getId(),
                sysUser.getDeviceId(),username,merchantId, authorities);

        Map<String, Object> map = new HashMap<>();
        map.put(Constants.USER_TOKEN, cacheSessionBO.getToken());
        map.put(Constants.REFRESH_TOKEN, cacheSessionBO.getRefreshToken());

//        //保存登录日志
//        if (systemLoginLogService != null) {
//            SysLoginLogDTO sysLoginLog = new SysLoginLogDTO();
//            sysLoginLog.setUsername(customUser.getUsername());
//            sysLoginLog.setStatus(1);
//            sysLoginLog.setIpaddr(IpUtil.getIpAddress(request));
//            sysLoginLog.setMsg("登录成功");
//            sysLoginLog.setAccessTime(new Date());
//            request.setAttribute(Constants.TOKEN, token);
//            Result r = systemLoginLogService.save(sysLoginLog);
//        }
        ResponseUtil.out(response, Result.ok(map));
    }

    /**
     * 登录失败
     *
     * @param request
     * @param response
     * @param e
     * @throws IOException
     * @throws ServletException
     */
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                              AuthenticationException e) throws IOException, ServletException {
        log.error("授权失败");
        e.printStackTrace();
        ThreadLocalUtils.removeTenantCodeThreadLocal();

        if (e instanceof BadCredentialsException) {
            ResponseUtil.out(response, Result.fail("用户名称或密码错误"));
        } else {
            ResponseUtil.out(response, Result.fail(SystemResultCodeEnum.LOGIN_MOBLE_ERROR.getCode(), SystemResultCodeEnum.LOGIN_MOBLE_ERROR.getMessage()));
        }
    }

}

