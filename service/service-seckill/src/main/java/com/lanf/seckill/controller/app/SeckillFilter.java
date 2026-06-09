package com.lanf.seckill.controller.app;

import com.lanf.cache.service.RedissonCacheService;
import com.lanf.common.utils.IStringUtils;
import com.lanf.common.utils.JsonUtils;
import com.lanf.constant.exception.BizException;
import com.lanf.constant.result.Result;
import com.lanf.constant.utils.UserContext;
import com.lanf.seckill.config.SeckillUrlConfig;
import com.lanf.seckill.model.dto.PlaceDTO;
import com.lanf.seckill.service.strategy.SecKillStrategy;
import com.lanf.seckill.service.strategy.SecKillStrategyFactory;
import com.lanf.web.utils.JwtUtils;
import com.lanf.web.utils.ResponseUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;

import static com.lanf.seckill.service.impl.SecKillActivityServiceImpl.SECKILL_TOKEN_KEY_PRX;

/**
 * 秒杀下单
 */
@Slf4j
@Component
public class SeckillFilter implements Filter {

    @Autowired
    private SeckillUrlConfig seckillUrlConfig;

    @Autowired
    private RedissonCacheService redissonCacheService;

    @Autowired
    private SecKillStrategyFactory secKillStrategyFactory;

    private final PathMatcher pathMatcher = new AntPathMatcher();


    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        String requestPath = request.getRequestURI();
        List<SeckillUrlConfig.UrlMapping> urlMappings = seckillUrlConfig.getUrlMappings();

        // 查找匹配的 URL 配置
        SeckillUrlConfig.UrlMapping matchedMapping = null;
        for (SeckillUrlConfig.UrlMapping urlMapping : urlMappings) {
            if (pathMatcher.match(urlMapping.getPath(), requestPath)) {
                matchedMapping = urlMapping;
                break;
            }
        }

        // 如果没有匹配的配置，直接放行
        if (matchedMapping == null) {
            log.info("非秒杀接口");
            filterChain.doFilter(request, response);
            return;
        }
        log.info("开始进行秒杀");
        // 读取 Body 为字符串
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        String jsonBody = sb.toString();
        if (IStringUtils.isEmpty(jsonBody)) {
            ResponseUtil.outFail(response, Result.fail("系统繁忙，请重试"));
            return;
        }

        PlaceDTO object = null;
        try {
            object = JsonUtils.toObject(jsonBody, PlaceDTO.class);
        } catch (Exception e) {
            log.error("请求参反序列化错误");
            ResponseUtil.outFail(response, Result.fail("系统繁忙，请重试"));
            return;
        }
        String token = object.getToken();
        Long userId = UserContext.getUserId();
        Long skillItemId = object.getSeckillItemId();

        if (userId == null || skillItemId == null || IStringUtils.isEmpty(token)){
            log.error("请求参数错误");
            ResponseUtil.outFail(response, Result.fail("系统繁忙，请重试"));
            return;
        }
        Integer secKillModel = null;
        try {
            //校验token是否合法

            Claims claims = JwtUtils.getClaims(token);
            Long jwtUserId =  claims.get(JwtUtils.CLAIM_USER_ID,Long.class);
            Long jwtSkillItemId =  claims.get(JwtUtils.CLAIM_SEC_KILL_ITEM_ID,Long.class);
            secKillModel = claims.get(JwtUtils.CLAIM_SEC_KILL_MODE,Integer.class);

            if ( !userId.equals(jwtUserId) ||
                    !skillItemId.equals(jwtSkillItemId)
                 || secKillModel == null) {
                log.error("token信息不一致");
                ResponseUtil.outFail(response, Result.fail(100004, "系统繁忙，请重试"));
                return;
            }

        } catch (Exception e) {
            log.error("token校验失败");
            ResponseUtil.outFail(response, Result.fail(100004, "系统繁忙，请重试"));
            return;
        }
        String tokenKey = String.format(SECKILL_TOKEN_KEY_PRX, userId, skillItemId);
        //每个token只能使用一次
        redissonCacheService.delete(tokenKey);

        /**
         * 开始进行秒杀
         */
        try {
            SecKillStrategy strategy = secKillStrategyFactory.getStrategy(secKillModel);
            object.setUserId(userId);
            strategy.executeSecKill(object);
            ResponseUtil.outSuccess(response, Result.ok());

        } catch (BizException e) {
            log.error("秒杀异常");
            ResponseUtil.outFail(response, Result.fail(e.getCode(), e.getMessage()));

        } catch (Exception e) {
            log.error("秒杀异常",e);
            ResponseUtil.outFail(response, Result.fail(100004, "太火爆了，再试一次"));

        }
    }


}