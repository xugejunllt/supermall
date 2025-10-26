package com.lanf.security.filter;

import com.alibaba.fastjson.JSON;
import com.lanf.common.utils.BeanUtil;
import com.lanf.common.utils.JwtUtils;
import com.lanf.constant.constant.Constants;
import com.lanf.security.utils.TokenUtils;
import com.lanf.web.code.CommonResultCodeEnum;
import com.lanf.web.exception.BizException;
import com.lanf.web.result.Result;
import com.lanf.web.utils.ResponseUtil;
import io.jsonwebtoken.ExpiredJwtException;
import org.apache.commons.lang3.StringUtils;
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
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.lanf.security.code.SystemResultCodeEnum.TOKENEXPIRED;

/**
 * @author tanlingfei
 * @version 1.0
 * @description 认证解析过滤器
 * @date 2023/2/27 10:23
 */
public class TokenAuthenticationFilter extends OncePerRequestFilter {

    private RedisTemplate redisTemplate;

    public TokenAuthenticationFilter(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        logger.info("uri:" + request.getRequestURI());

        //如果是登录接口，直接放行
        if ("/system/admin/system/index/login".equals(request.getRequestURI())) {

            chain.doFilter(request, response);
            return;
        }

        UsernamePasswordAuthenticationToken authentication = null;
        try {
            authentication = getAuthentication(request, response);

        } catch (BizException e) {
            ResponseUtil.out(response, Result.fail(e.getCode(), e.getMessage()));
            return;
        } catch (Exception e) {
            ResponseUtil.out(response, Result.fail(CommonResultCodeEnum.FAIL.getCode(), CommonResultCodeEnum.FAIL.getMessage()));
            return;
        }

        if (null != authentication) {
            SecurityContextHolder.getContext().setAuthentication(authentication);
            chain.doFilter(request, response);
        } else {
            ResponseUtil.out(response, Result.fail(TOKENEXPIRED.getCode(), TOKENEXPIRED.getMessage()));
        }


    }

    private UsernamePasswordAuthenticationToken getAuthentication(HttpServletRequest request, HttpServletResponse response) {

        String token = request.getHeader(Constants.TOKEN);
        String refreshToken = request.getHeader(Constants.REFRESH_TOKEN);

        if (StringUtils.isEmpty(token) || StringUtils.isEmpty(refreshToken)) {
            throw new BizException(TOKENEXPIRED.getCode(), TOKENEXPIRED.getMessage());
        }

        String useruame = null;
        String tenantCode = null;
        try {
            tenantCode = JwtUtils.getTenantCode(token);
            useruame = JwtUtils.getUsername(token);
        } catch (ExpiredJwtException e) {
            throw new BizException(TOKENEXPIRED.getCode(), TOKENEXPIRED.getMessage());
        }
        String key1 = useruame + ":" + tenantCode;
        String key2 = JwtUtils.getUserId(token) + "";
        /**
         * 登入是否过期
         */
        boolean refresh = JwtUtils.refresh(refreshToken);
        if (refresh) {
            //刷新tokenn
            TokenUtils tokenUtils = BeanUtil.getBean(TokenUtils.class);
            logger.info("token过期,刷新token");
            tokenUtils.refreshToken(request, response);
        }
        String authoritiesString = (String) redisTemplate.opsForValue().get(key1);
        String userInfo = (String) redisTemplate.opsForValue().get(key2);
        //延迟token在缓存中的过期时间
        redisTemplate.opsForValue().set(key1, authoritiesString, Duration.ofHours(24));
        redisTemplate.opsForValue().set(key2, userInfo, Duration.ofHours(24));

        List<Map> mapList = JSON.parseArray(authoritiesString, Map.class);
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        for (Map map : mapList) {
            authorities.add(new SimpleGrantedAuthority((String) map.get("authority")));
        }
        return new UsernamePasswordAuthenticationToken(useruame, null, authorities);


    }


}
