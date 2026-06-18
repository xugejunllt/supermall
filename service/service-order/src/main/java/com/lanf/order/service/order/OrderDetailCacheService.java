package com.lanf.order.service.order;

import com.lanf.api.order.model.vo.OrderDetailForAdminVO;
import com.lanf.cache.service.RedissonCacheService;
import com.lanf.common.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 订单详情缓存服务
 * 封装订单详情的缓存读写操作
 *
 * @author lanf
 */
@Slf4j
@Service
public class OrderDetailCacheService {

    private static final String ORDER_DETAIL_CACHE_KEY_PREFIX = "order:detail:%s";

    @Autowired
    private RedissonCacheService redissonCacheService;

    /**
     * 从缓存获取订单详情
     *
     * @param orderId 订单ID
     * @return 订单详情，缓存未命中或异常返回null
     */
    public OrderDetailForAdminVO getOrderDetailFromCache(Long orderId) {
        String cacheKey = buildCacheKey(orderId);
        String cachedValue = redissonCacheService.get(cacheKey);
        if (cachedValue == null || RedissonCacheService.isErrorValue(cachedValue)) {
            return null;
        }

        return JsonUtils.toObject(cachedValue, OrderDetailForAdminVO.class);

    }

    /**
     * 将订单详情写入缓存
     *
     * @param orderId 订单ID
     * @param detail  订单详情
     */
    public void setOrderDetailToCache(Long orderId, OrderDetailForAdminVO detail) {
        if (detail == null) {
            return;
        }
        String cacheKey = buildCacheKey(orderId);
        redissonCacheService.set(cacheKey, JsonUtils.toJsonString(detail), 7, TimeUnit.DAYS);
        log.debug("订单详情缓存写入成功, orderId={}", orderId);
    }

    /**
     * 删除订单详情缓存
     *
     * @param orderId 订单ID
     */
    public void deleteOrderDetailCache(Long orderId) {
        String cacheKey = buildCacheKey(orderId);
        redissonCacheService.delete(cacheKey);
        log.debug("订单详情缓存删除成功, orderId={}", orderId);
    }

    private String buildCacheKey(Long orderId) {
        return String.format(ORDER_DETAIL_CACHE_KEY_PREFIX, orderId);
    }
}
