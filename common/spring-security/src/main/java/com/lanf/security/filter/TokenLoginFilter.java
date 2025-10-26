package com.lanf.security.filter;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lanf.common.utils.IpUtil;
import com.lanf.common.utils.JwtUtils;
import com.lanf.constant.constant.Constants;
import com.lanf.log.api.SystemLogService;
import com.lanf.log.model.dto.SysLoginLogDTO;
import com.lanf.security.code.SystemResultCodeEnum;
import com.lanf.common.utils.ThreadLocalUtils;
import com.lanf.system.model.bo.CustomUserBO;
import com.lanf.system.model.vo.LoginVO;
import com.lanf.web.exception.BizException;
import com.lanf.web.result.Result;
import com.lanf.web.utils.ResponseUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * 登录过滤器，继承UsernamePasswordAuthenticationFilter，对用户名密码进行登录校验
 * </p>
 */
@Slf4j
public class TokenLoginFilter extends UsernamePasswordAuthenticationFilter {
    private RedisTemplate redisTemplate;

    private SystemLogService systemLoginLogService;

    public TokenLoginFilter(AuthenticationManager authenticationManager, RedisTemplate redisTemplate, SystemLogService systemLoginLogService) {
        this.systemLoginLogService = systemLoginLogService;
        this.redisTemplate = redisTemplate;
        this.setAuthenticationManager(authenticationManager);
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
            String tenantCode = loginVo.getTenantCode();
            if (StringUtils.isEmpty(tenantCode)){
                throw new BizException("租户code不能为空");
            }
            //添加到threadlocal
            ThreadLocalUtils.addTenantCode(tenantCode);
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
                                            Authentication auth) throws IOException, ServletException {
        CustomUserBO customUser = (CustomUserBO) auth.getPrincipal();
        String tenantCode = customUser.getSysUser().getTenantCode();
        String token = JwtUtils.createAdminToken(customUser.getSysUser().getId(), customUser.getSysUser().getUsername(),tenantCode);
        String refreshToken = JwtUtils.createRefreshToken(customUser.getSysUser().getId(), customUser.getSysUser().getUsername(), tenantCode);

        //保存权限数据
        redisTemplate.opsForValue().set(customUser.getUsername()+":"+tenantCode, JSON.toJSONString(customUser.getAuthorities()),
                Duration.ofHours(24));
        //保存用户数据redis
        redisTemplate.opsForValue().set(customUser.getSysUser().getId()+"", JSON.toJSONString(customUser.getSysUser()),
                Duration.ofHours(24));
        Map<String, Object> map = new HashMap<>();
        map.put(Constants.TOKEN, token);
        map.put(Constants.REFRESH_TOKEN, refreshToken);
        map.put("name", customUser.getSysUser().getUsername());

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

