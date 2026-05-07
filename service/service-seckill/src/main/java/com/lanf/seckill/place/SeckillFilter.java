package com.lanf.seckill.place;

import com.lanf.cache.service.RedissonCacheService;
import com.lanf.common.utils.IStringUtils;
import com.lanf.common.utils.JsonUtils;
import com.lanf.constant.exception.BizException;
import com.lanf.constant.result.Result;
import com.lanf.seckill.config.SeckillUrlConfig;
import com.lanf.seckill.model.dto.PlaceDTO;
import com.lanf.seckill.service.ISeckillActivityService;
import com.lanf.security.utils.JwtUtils;
import com.lanf.web.utils.ResponseUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.lanf.seckill.service.impl.SeckillActivityServiceImpl.SECKILL_TOKEN_KEY_PRX;

@Slf4j
@Component
public class SeckillFilter implements Filter {

    @Autowired
    private SeckillUrlConfig seckillUrlConfig;

    @Autowired
    private RedissonCacheService redissonCacheService;
    @Autowired
    private ISeckillActivityService seckillActivityService;

    @Qualifier("seckillQueryExecutor")
    @Autowired
    private ThreadPoolTaskExecutor taskExecutor;
    /**
     * 用户参与秒杀的缓存 key 前缀
     * 格式: seckill:user:participated:{userId}:{seckillItemId}
     */
    public static final String USER_PARTICIPATED_KEY_PRX = "seckill:user:participated:%s:%s";

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
            filterChain.doFilter(request, response);
            return;
        }

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
            ResponseUtil.out(response, Result.fail("请求参数为空"));
            return;
        }

        PlaceDTO object = null;
        try {
            object = JsonUtils.toObject(jsonBody, PlaceDTO.class);
        } catch (Exception e) {
            ResponseUtil.out(response, Result.fail("请求参数无效"));
            return;
        }
        String token = object.getToken();
        String skillItemId = null;
        Long userId = null;
        try {
            //校验token是否合法
            userId = JwtUtils.parseUserId(token);
            skillItemId = JwtUtils.parseDeviceId(token);
            Long userId1 = object.getUserId();
            Long seckillItemId = object.getSeckillItemId();
            if (!userId.equals(userId1) || !skillItemId.equals(seckillItemId)) {
                ResponseUtil.out(response, Result.fail(100004, "请求人数太多,清稍微再试"));
                return;
            }

        } catch (Exception e) {
            ResponseUtil.out(response, Result.fail(100004, "请求人数太多,清稍微再试"));
            return;
        }
        String userTokenKey = String.format(SECKILL_TOKEN_KEY_PRX, userId, skillItemId);
        String cacheToken = redissonCacheService.get(userTokenKey);
        if (IStringUtils.isEmpty(cacheToken)){
            ResponseUtil.out(response, Result.fail(100005, "你已参与过秒杀了"));
            return;
        }
        //token只能用一次
        redissonCacheService.delete(userTokenKey);
        // 检查用户是否已经参与过该商品的秒杀（使用 Redis 递增）
        String participatedKey = String.format(USER_PARTICIPATED_KEY_PRX, userId, skillItemId);
        long participateCount = redissonCacheService.incrementAndGet(participatedKey, 1, TimeUnit.DAYS);
        // 如果计数大于1，说明用户已经参与过
        if (participateCount > 1) {
            ResponseUtil.out(response, Result.fail(100005, "你已参与过秒杀了"));
            return;
        }
        if (participateCount == -1) {

            ResponseUtil.out(response, Result.fail(100004, "请求人数太多,清稍微再试"));
        }
        /**
         * 开始进行秒杀
         */
        try {
            seckillActivityService.skillPlace(object);
        } catch (BizException e) {
            ResponseUtil.out(response, Result.fail(e.getCode(), e.getMessage()));

        } catch (Exception e) {
            ResponseUtil.out(response, Result.fail(100004, "请求人数太多,清稍微再试"));

        }
    }


}