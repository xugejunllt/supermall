//package com.lanf.security.filter;
//
//import com.lanf.common.utils.BeanUtil;
//import com.lanf.common.utils.IStringUtils;
//import com.lanf.common.utils.StackTraceUtil;
//import com.lanf.constant.exception.IRedisException;
//import com.lanf.constant.constant.Constants;
//import com.lanf.cache.service.RedisCache;
//import com.lanf.security.config.FilterPathConfig;
//import com.lanf.security.model.ValidateTokenBO;
//import com.lanf.web.utils.JwtUtils;
//import com.lanf.security.utils.UserIdContext;
//import com.lanf.security.utils.UserSessionCache;
//import com.lanf.constant.code.CommonResultCodeEnum;
//import com.lanf.constant.exception.BizException;
//import com.lanf.constant.result.Result;
//import com.lanf.web.utils.ResponseUtil;
//import io.jsonwebtoken.ExpiredJwtException;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
//import javax.servlet.*;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import java.io.IOException;
//import java.util.List;
//
//@Slf4j
//@Component
//public class UserTokenFilter implements Filter {
//
//    @Autowired
//    private RedisCache redisCache;
//    @Autowired
//    private UserSessionCache userSessionCache;
//
//    @Override
//    public void doFilter(ServletRequest request, ServletResponse response1, FilterChain chain) {
//
//        HttpServletRequest request1 = (HttpServletRequest) request;
//
//        log.info("请求路径[{}]", request1.getRequestURI());
//
//        HttpServletResponse response = (HttpServletResponse) response1;
//
//        try {
//            userTokenHandle((HttpServletRequest) request, chain, response);
//
//        } catch (BizException e) {
//
//            ResponseUtil.out(response, Result.fail(e.getCode(), e.getMessage()));
//
//        } catch (Exception e) {
//
//            log.error("用户权限处理异常,异常堆栈[{}]", StackTraceUtil.getStackTrace(e));
//            ResponseUtil.out(response, Result.fail(CommonResultCodeEnum.SERVICE_ERROR.getCode(), CommonResultCodeEnum.SERVICE_ERROR.getMessage()));
//        }
//    }
//
//
//    /**
//     * 用戶token拦截
//     */
//    private void userTokenHandle(HttpServletRequest request, FilterChain chain, HttpServletResponse response) throws ServletException, IOException {
//
//        FilterPathConfig filterPathConfig = BeanUtil.getBean(FilterPathConfig.class);
//        String url = request.getRequestURI();
//        List<String> userNotTokenPath = filterPathConfig.getUserNotTokenPath();
//        if (userNotTokenPath.contains(url)) {
//            chain.doFilter(request, response);
//            return;
//        }
//        if (url.startsWith("/system")){
//            //解决restfult风格
//            chain.doFilter(request, response);
//            return;
//        }
//
//        String channel = request.getHeader(Constants.CHANEL);
//        String deviceId = request.getHeader(Constants.DEVICE_ID);
//        String userToken = request.getHeader(Constants.USER_TOKEN);
//
//        //校验token
//        ValidateTokenBO tokenBO = validateToken(request, channel, deviceId, userToken);
//
//        //处理token过期
//        if (tokenBO.getSessionExpired()){
//            processSessionExpired();
//
//        }
//        //刷新token
//        refreshToken(tokenBO);
//
//        //userid添加到 context中
//        UserIdContext.setUserId(tokenBO.getUserId());
//        log.info("当前用户是[{}]",tokenBO.getUserId());
//        try {
//            //请求放行
//            chain.doFilter(request, response);
//
//        } finally {
//            UserIdContext.clear();
//
//        }
//    }
//
//
//    private ValidateTokenBO validateToken(HttpServletRequest request, String channel, String deviceId,
//                                          String userToken) throws IRedisException {
//
//
//        if (IStringUtils.isEmpty(channel) || IStringUtils.isEmpty(deviceId) || IStringUtils.isEmpty(userToken)) {
//            log.info("请求头参数为空");
//            throw new BizException("请求头参数为空");
//        }
//
//        Integer channel2 = null;
//        Boolean sessionExpired = false;
//        String deviceId2 = null;
//        Long cacheUserId = null;
//        try {
//
//            deviceId2 = JwtUtils.parseDeviceId(userToken);
//            cacheUserId = JwtUtils.parseUserId(userToken);
//        } catch (ExpiredJwtException e) {
//            log.info(" JWT token 过期");
//
//            return expiredTokenProcess();
//
//        } catch (Exception e) {
//
//            log.info("JWT 解析异常 [{}]", StackTraceUtil.getStackTrace(e));
//            throw new BizException("jwt解析异常");
//        }
//
//        if (!deviceId.equals(deviceId2)) {
//            //访问的客户端与登入时客户端一致  如果不一致跑出异常
//            log.info("设备id错误");
//            throw new BizException("设备id错误");
//        }
//
//        try {
//            channel2 = Integer.parseInt(channel);
//        } catch (NumberFormatException e) {
//            log.info("channel错误");
//            throw new BizException("channel错误");
//        }
//
//
//        String token = userSessionCache.getToken(channel2, cacheUserId);
//        if (IStringUtils.isEmpty(token)) {
//            log.info("缓存 token过期");
//            return expiredTokenProcess();
//        }
//
//        if (!IStringUtils.isEmpty(token) && !userToken.equals(token)) {
//            log.info("请求头token与缓存token不一致");
//            throw new BizException("请求头token与缓存token不一致");
//        }
//        //
//        ValidateTokenBO bo = new ValidateTokenBO();
//        bo.setUserId(cacheUserId);
//        bo.setSessionExpired(sessionExpired);
//        bo.setToken(token);
//        bo.setDeviceId(deviceId2);
//        bo.setChannel(channel2);
//        return bo;
//    }
//
//    private ValidateTokenBO expiredTokenProcess(){
//
//        ValidateTokenBO validateTokenBO = new ValidateTokenBO();
//        validateTokenBO.setSessionExpired(true);
//
//        return validateTokenBO;
//    }
//    private void processSessionExpired() {
//
//
//
//            throw new BizException(CommonResultCodeEnum.SESSION_EXPIRED.getCode(),
//                    CommonResultCodeEnum.SESSION_EXPIRED.getMessage());
//
//
//
//    }
//
//    private void refreshToken(ValidateTokenBO tokenBO) {
//        Boolean refreshSession = userSessionCache.refreshToken(tokenBO.getChannel(), tokenBO.getUserId());
//
//        if (!refreshSession) {
//            //如果续期失败 可能key刚好过期了 统一刷新token
//            log.info("token刷新失败");
//            processSessionExpired();
//        }
//    }
//}
