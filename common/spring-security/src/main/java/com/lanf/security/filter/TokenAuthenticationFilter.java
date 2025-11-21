package com.lanf.security.filter;

import com.lanf.common.utils.*;
import com.lanf.constant.constant.Constants;
import com.lanf.constant.exception.IRedisException;
import com.lanf.security.model.ValidateTokenBO;
import com.lanf.security.utils.*;
import com.lanf.system.model.bo.SysUserBO;
import com.lanf.web.code.CommonResultCodeEnum;
import com.lanf.web.exception.BizException;
import com.lanf.web.result.Result;
import com.lanf.web.utils.ResponseUtil;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author tanlingfei
 * @version 1.0
 * @description 认证解析过滤器
 * @date 2023/2/27 10:23
 */
@Slf4j
public class TokenAuthenticationFilter extends OncePerRequestFilter {

    private RedisTemplate redisTemplate;


    private AdminSessionCache adminSessionCache;
    public TokenAuthenticationFilter(RedisTemplate redisTemplate,AdminSessionCache adminSessionCache) {

        this.redisTemplate = redisTemplate;
        this.adminSessionCache = adminSessionCache;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {


        if ("/system/admin/system/index/login".equals(request.getRequestURI())) {
            //如果是登录接口，直接放行 然后走账号密码拦截器
            chain.doFilter(request, response);
            return;
        }
        UsernamePasswordAuthenticationToken authentication = null;
        try {
            authentication =  tokenHandle( request);
        } catch (BizException e) {
            ResponseUtil.out(response, Result.fail(e.getCode(), e.getMessage()));
            return;
        } catch (Exception e) {
            ResponseUtil.out(response, Result.fail(CommonResultCodeEnum.FAIL.getCode(), CommonResultCodeEnum.FAIL.getMessage()));
            return;
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);
        try {
            chain.doFilter(request, response);
        } finally {
            UserIdContext.clear();
            MerchantIdContext.clear();
        }


    }


    /**
     * 用戶token拦截
     */
    private UsernamePasswordAuthenticationToken tokenHandle(HttpServletRequest request) throws ServletException, IOException {


        String channel = request.getHeader(Constants.CHANEL);
        String deviceId = request.getHeader(Constants.DEVICE_ID);
        String userToken = request.getHeader(Constants.USER_TOKEN);

        //校验token
        ValidateTokenBO tokenBO = validateToken(request, channel, deviceId, userToken);

        //处理token过期
        if (tokenBO.getSessionExpired()){
            processSessionExpired();

        }

        //刷新token
        refreshToken(tokenBO);

        //添加到threadlocal
        UserIdContext.setUserId(tokenBO.getUserId());
        MerchantIdContext.setMerchantId(tokenBO.getMerchantId());

        //构建UsernamePasswordAuthenticationToken
        return  buildUsernamePasswordAuthenticationToken(  tokenBO);
    }

    private UsernamePasswordAuthenticationToken buildUsernamePasswordAuthenticationToken( ValidateTokenBO tokenBO){

        //从缓存获取权限
        String auth = adminSessionCache.getAuth(tokenBO.getChannel(), tokenBO.getUserId());
        List<Map> mapList = JsonUtils.toList(auth, Map.class);
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        for (Map map : mapList) {
            authorities.add(new SimpleGrantedAuthority((String) map.get("authority")));
        }
        return new UsernamePasswordAuthenticationToken(tokenBO.getUserName(), null, authorities);
    }

    private ValidateTokenBO validateToken(HttpServletRequest request, String channel, String deviceId,
                                          String userToken) throws IRedisException {


        if (IStringUtils.isEmpty(channel) || IStringUtils.isEmpty(deviceId) || IStringUtils.isEmpty(userToken)) {
            log.info("请求头参数为空");
            throw new BizException("请求头参数为空");
        }

        Integer channel2 = null;
        Boolean sessionExpired = false;
        String  deviceId2 = null;
        Long   cacheUserId = null;
        try {

            deviceId2 = com.lanf.security.utils.JwtUtils.parseDeviceId(userToken);
            cacheUserId = com.lanf.security.utils.JwtUtils.parseUserId(userToken);

        } catch (ExpiredJwtException e) {
            log.info(" JWT token 过期");

            return expiredTokenProcess();

        } catch (Exception e) {

            log.info("JWT 解析异常 [{}]", StackTraceUtil.getStackTrace(e));
            throw new BizException("jwt解析异常");
        }

        if (!deviceId.equals(deviceId2)) {
            //访问的客户端与登入时客户端一致  如果不一致跑出异常
            log.info("设备id错误");
            throw new BizException("设备id错误");
        }

        try {
            channel2 = Integer.parseInt(channel);
        } catch (NumberFormatException e) {
            log.info("channel错误");
            throw new BizException("channel错误");
        }


        String token = adminSessionCache.getToken(channel2, cacheUserId);
        if (IStringUtils.isEmpty(token)) {
            log.info("缓存 token过期");
            return expiredTokenProcess();
        }

        if (!IStringUtils.isEmpty(token) && !userToken.equals(token)) {
            log.info("请求头token与缓存token不一致");
            throw new BizException("请求头token与缓存token不一致");
        }
        SysUserBO sysUser = adminSessionCache.getSysUser(cacheUserId);

        //
        ValidateTokenBO bo = new ValidateTokenBO();
        bo.setUserId(cacheUserId);
        bo.setSessionExpired(sessionExpired);
        bo.setToken(token);
        bo.setDeviceId(deviceId2);
        bo.setChannel(channel2);
        bo.setMerchantId(sysUser.getMerchantId());

        return bo;
    }

    private ValidateTokenBO expiredTokenProcess(){

        ValidateTokenBO validateTokenBO = new ValidateTokenBO();
        validateTokenBO.setSessionExpired(true);

        return validateTokenBO;
    }
    private void processSessionExpired() {



        throw new BizException(CommonResultCodeEnum.SESSION_EXPIRED.getCode(),
                CommonResultCodeEnum.SESSION_EXPIRED.getMessage());



    }

    private void refreshToken(ValidateTokenBO tokenBO) {
        Boolean refreshSession = adminSessionCache.refreshToken(tokenBO.getChannel(), tokenBO.getUserId());

        if (!refreshSession) {
            //如果续期失败 可能key刚好过期了 统一刷新token
            log.info("token刷新失败");
            processSessionExpired();
        }
    }

}
