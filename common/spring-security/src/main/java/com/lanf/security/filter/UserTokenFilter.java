package com.lanf.security.filter;

import com.lanf.common.utils.BeanUtil;
import com.lanf.common.utils.IStringUtils;
import com.lanf.common.utils.StackTraceUtil;
import com.lanf.constant.exception.IRedisException;
import com.lanf.redis.constant.CacheConstants;
import com.lanf.constant.constant.Constants;
import com.lanf.redis.service.RedisCache;
import com.lanf.security.config.FilterPathConfig;
import com.lanf.security.model.ValidateTokenBO;
import com.lanf.security.utils.UserContext;
import com.lanf.security.utils.UserSessionCache;
import com.lanf.web.code.CommonResultCodeEnum;
import com.lanf.web.exception.BizException;
import com.lanf.web.result.Result;
import com.lanf.web.utils.ResponseUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@Slf4j
@Component
public class UserTokenFilter implements Filter {

    @Autowired
    private RedisCache redisCache;
    @Autowired
    private UserSessionCache userSessionCache;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response1, FilterChain chain) {

        HttpServletRequest request1 = (HttpServletRequest) request;
        log.info("请求路径[{}]", request1.getRequestURI());

        HttpServletResponse response = (HttpServletResponse) response1;

        try {
            userTokenHandle((HttpServletRequest) request, chain, response);

        } catch (BizException e) {

            ResponseUtil.out(response, Result.fail(e.getCode(), e.getMessage()));

        } catch (Exception e) {

            log.error("用户权限处理异常,异常堆栈[{}]", StackTraceUtil.getStackTrace(e));
            ResponseUtil.out(response, Result.fail(CommonResultCodeEnum.SERVICE_ERROR.getCode(), CommonResultCodeEnum.SERVICE_ERROR.getMessage()));
        }
    }


    /**
     * 用戶token拦截
     */
    private void userTokenHandle(HttpServletRequest request, FilterChain chain, HttpServletResponse response) throws ServletException, IOException {

        FilterPathConfig filterPathConfig = BeanUtil.getBean(FilterPathConfig.class);

        List<String> userNotTokenPath = filterPathConfig.getUserNotTokenPath();
        if (userNotTokenPath.contains(request.getRequestURI())) {
            log.info("直接放行请求");
            chain.doFilter(request, response);
            return;
        }

        log.info("处理用户token开始");
        String channel = request.getHeader(Constants.CHANEL);
        String userId = request.getHeader(Constants.USERID);
        String userToken = request.getHeader(Constants.USER_TOKEN);
        //校验token
        ValidateTokenBO tokenBO = validateToken(request, channel, userId, userToken);

        //处理session过期
        processSessionExpired(tokenBO.getSessionExpired());

        //刷新token过期时间
        refreshSession(tokenBO);

        //userid添加到 context中
        UserContext.setUserId(tokenBO.getUserId());
        log.info("处理用户token完成");
        //请求放行
        chain.doFilter(request, response);


    }

    private void refreshSession(ValidateTokenBO tokenBO) {
        Boolean refreshSession = userSessionCache.refreshSession(tokenBO.getChannel(), tokenBO.getUserId());
        if (!refreshSession) {
            //如果续期失败 可能key刚好过期了 统一用户重新登入
            log.info("token过期");
            processSessionExpired(tokenBO.getSessionExpired());
        }
    }

    private ValidateTokenBO validateToken(HttpServletRequest request, String channel, String userId, String userToken) throws IRedisException {


        if (IStringUtils.isEmpty(channel) || IStringUtils.isEmpty(userId) || IStringUtils.isEmpty(userToken)) {
            log.info("请求头参数为空");
            throw new BizException("请求头参数为空");
        }
        Long id = null;
        Integer channel2 = null;
        Boolean sessionExpired = false;
        try {
            //用户id必须是Long类型
            id = Long.parseLong(userId);
        } catch (NumberFormatException e) {
            log.info("userId错误");
            throw new BizException("userId错误");
        }

        try {
            //用户id必须是Long类型
            channel2 = Integer.parseInt(channel);
        } catch (NumberFormatException e) {
            log.info("userId错误");
            throw new BizException("userId错误");
        }

        String sessionKey = String.format(CacheConstants.USER_SESSION, channel, userId);
        String token = userSessionCache.getSession(channel2, id);
        if (IStringUtils.isEmpty(token)) {
            log.info("token过期");
            sessionExpired = true;
        }
        if (!IStringUtils.isEmpty(token) && !userToken.equals(token)) {
            log.info("请求头token与缓存token不一致");
            throw new BizException("请求头token与缓存token不一致");
        }
        //
        ValidateTokenBO bo = new ValidateTokenBO();
        bo.setUserId(id);
        bo.setSessionKey(sessionKey);
        bo.setSessionExpired(sessionExpired);
        bo.setToken(token);

        return bo;
    }

    private void processSessionExpired(Boolean sessionExpired) {

        if (sessionExpired) {

            throw new BizException(CommonResultCodeEnum.SESSION_EXPIRED.getCode(),
                    CommonResultCodeEnum.SESSION_EXPIRED.getMessage());
        }


    }
}
