package com.lanf.seckill.controller.app;

import com.lanf.cache.service.RedissonCacheService;
import com.lanf.common.utils.IStringUtils;
import com.lanf.common.utils.JsonUtils;
import com.lanf.constant.result.Result;
import com.lanf.constant.utils.UserContext;
import com.lanf.rocketmq.util.MqSendMessageUtils;
import com.lanf.seckill.config.SeckillCouponUrlConfig;
import com.lanf.seckill.model.dto.PlaceDTO;
import com.lanf.seckill.model.enums.SeckillModeEnum;
import com.lanf.seckill.mq.constant.SecKillMqTopicName;
import com.lanf.seckill.mq.message.SecKillCouponMqExecuteMessage;
import com.lanf.seckill.service.ISecKillCouponItemService;
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
import java.util.concurrent.TimeUnit;

import static com.lanf.seckill.service.impl.SecKillCouponItemServiceImpl.SECKILL_COUPON_STOCK_KEY_PRX;
import static com.lanf.seckill.service.impl.SecKillCouponItemServiceImpl.SECKILL_COUPON_TOKEN_KEY_PRX;

/**
 * 优惠券秒杀下单Filter
 */
@Slf4j
@Component
public class SecKillCouponFilter implements Filter {



    @Autowired
    private SeckillCouponUrlConfig seckillCouponUrlConfig;
    @Autowired
    private RedissonCacheService redissonCacheService;

    @Autowired
    private ISecKillCouponItemService seckillCouponItemService;

    @Autowired
    private MqSendMessageUtils mqSendMessageUtils;

    private final PathMatcher pathMatcher = new AntPathMatcher();

    /**
     * 用户参与限制key前缀
     */
    public static final String USER_PARTICIPATED_KEY_PRX = "seckill:coupon:user:participated:%s:%s";

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        String requestPath = request.getRequestURI();
        List<SeckillCouponUrlConfig.UrlMapping> urlMappings = seckillCouponUrlConfig.getUrlMappings();

        // 查找匹配的 URL 配置
        SeckillCouponUrlConfig.UrlMapping matchedMapping = null;
        for (SeckillCouponUrlConfig.UrlMapping urlMapping : urlMappings) {
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
        log.info("开始进行优惠券秒杀");
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
        Long couponItemId = object.getSeckillItemId();

        if (userId == null || couponItemId == null || IStringUtils.isEmpty(token)) {
            log.error("请求参数错误");
            ResponseUtil.outFail(response, Result.fail("系统繁忙，请重试"));
            return;
        }
        Integer secKillModel = null;
        try {
            //校验token是否合法
            Claims claims = JwtUtils.getClaims(token);
            Long jwtUserId = claims.get(JwtUtils.CLAIM_USER_ID, Long.class);
            Long jwtSkillItemId = claims.get(JwtUtils.CLAIM_SEC_KILL_ITEM_ID, Long.class);
            secKillModel = claims.get(JwtUtils.CLAIM_SEC_KILL_MODE, Integer.class);


            if (!userId.equals(jwtUserId) ||
                    !couponItemId.equals(jwtSkillItemId)
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
        String tokenKey = String.format(SECKILL_COUPON_TOKEN_KEY_PRX, userId, couponItemId);
        if (!token.equals(redissonCacheService.get(tokenKey))) {
            log.warn("token已失效或与缓存不一致");
            ResponseUtil.outFail(response, Result.fail(100004, "太火爆了，再试一次"));
            return;
        }
        //每个token只能使用一次
        redissonCacheService.delete(tokenKey);


        String stockKey = String.format(SECKILL_COUPON_STOCK_KEY_PRX, couponItemId);

        // 检查用户是否已经参与过该优惠券的秒杀（使用 Redis 递增）
        String participatedKey = String.format(USER_PARTICIPATED_KEY_PRX, userId, couponItemId);
        long participateCount = redissonCacheService.incrementAndGet(participatedKey, 1, TimeUnit.DAYS);
        // 如果计数大于1，说明用户已经参与过
        if (participateCount > 1) {
            ResponseUtil.outFail(response, Result.fail("您已经参与过该优惠券秒杀"));
            return;
        }

        // 扣减库存
        long decremented = redissonCacheService.decrementAndGet(stockKey);
        if (decremented >= 0) {
            log.info("优惠券秒杀成功userId={},couponItemId={}", userId, couponItemId);
            // 发送MQ消息
            SecKillCouponMqExecuteMessage message = new SecKillCouponMqExecuteMessage();
            message.setUserId(userId);
            message.setSecKillCouponItemId(couponItemId);
            message.setSeckillModeEnum(SeckillModeEnum.REAL_TIME);

            mqSendMessageUtils.sendMessage(SecKillMqTopicName.SEC_KILL_COUPON_MQ_EXECUTE_TOPIC,
                    JsonUtils.toJsonString(message),null);
            ResponseUtil.outSuccess(response, Result.ok());
        } else if (decremented == -1 || participateCount == -1) {
            redissonCacheService.delete(stockKey);
            ResponseUtil.outFail(response, Result.fail("太火爆了，再试一次"));
        } else {
            ResponseUtil.outFail(response, Result.fail("优惠券已售罄"));
        }
    }

}
