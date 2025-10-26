package com.lanf.security.filter;

import com.lanf.common.utils.BeanUtil;
import com.lanf.common.utils.JwtUtils;
import com.lanf.redis.constant.CacheConstants;
import com.lanf.constant.constant.Constants;
import com.lanf.redis.service.RedisCache;
import com.lanf.security.config.FilterPathConfig;
import com.lanf.security.utils.TokenUtils;
import com.lanf.web.code.CommonResultCodeEnum;
import com.lanf.web.exception.BizException;
import com.lanf.web.result.Result;
import com.lanf.web.utils.ResponseUtil;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.lanf.security.code.SystemResultCodeEnum.TOKENEXPIRED;

@Slf4j
@Component
public class UserTokenFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response1, FilterChain chain) throws IOException, ServletException {

        HttpServletResponse response = (HttpServletResponse) response1;
        try {
            userTokenHandle((HttpServletRequest) request, chain,  response);
        } catch (BizException e) {
            ResponseUtil.out(response, Result.fail(e.getCode(), e.getMessage()));
        } catch (Exception e) {
            ResponseUtil.out(response, Result.fail(CommonResultCodeEnum.FAIL.getCode(), CommonResultCodeEnum.FAIL.getMessage()));
        }
    }


    /**
     * 用戶token拦截
     */
    private void userTokenHandle(HttpServletRequest request, FilterChain chain, HttpServletResponse response) throws ServletException, IOException {

        FilterPathConfig filterPathConfig = BeanUtil.getBean(FilterPathConfig.class);
        RedisCache redisCache = BeanUtil.getBean(RedisCache.class);

        List<String> userTokenPath = filterPathConfig.getUserTokenPath();
        if (userTokenPath.contains(request.getRequestURI())) {
            //校验用户token
            String userToken = request.getHeader(Constants.USER_TOKEN);
            String refreshToken = request.getHeader(Constants.REFRESH_TOKEN);

            if (StringUtils.isEmpty(userToken) || StringUtils.isEmpty(refreshToken)) {
                throw new BizException(TOKENEXPIRED.getCode(), TOKENEXPIRED.getMessage());
            }

            Long userId = null;
            try {
                userId = JwtUtils.getUserId(userToken);
            } catch (ExpiredJwtException e) {
                throw new BizException(TOKENEXPIRED.getCode(), TOKENEXPIRED.getMessage());
            }
            boolean refresh = JwtUtils.refresh(refreshToken);
            if (refresh) {
                //刷新tokenn
                TokenUtils tokenUtils = BeanUtil.getBean(TokenUtils.class);
                log.info("客户端token过期,刷新token");
                tokenUtils.refreshClientToken(request, response);
            }
            String key = CacheConstants.USER_TOKEN + userId;
            String cacheObject = redisCache.getCacheObject(key);
            //刷新token过期时间
            redisCache.setCacheObject(key, cacheObject, 8, TimeUnit.DAYS);
            chain.doFilter(request, response);


        } else {
            chain.doFilter(request, response);

        }


    }
}
