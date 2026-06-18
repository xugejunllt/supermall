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
 * 封装订单详情的缓存读写操作，采用事件驱动刷新策略：
 * 1.读请求先查缓存，命中直接返回，降低数据库压力
 * 2.缓存未命中时回源数据库，并将结果回填缓存
 * 3.订单状态变更时通过MQ监听器异步刷新缓存，保证最终一致性
 * 4.缓存过期时间7天，作为数据一致性兜底机制
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
     * 设计亮点：缓存反序列化异常时返回null，由调用方回源数据库，避免缓存异常影响主流程
     *
     * @param orderId 订单ID
     * @return 订单详情，缓存未命中或异常返回null
     */
    public OrderDetailForAdminVO getOrderDetailFromCache(Long orderId) {
        //1.构建Redis缓存Key，格式：order:detail:{orderId}
        String cacheKey = buildCacheKey(orderId);
        //2.从Redis读取缓存数据
        String cachedValue = redissonCacheService.get(cacheKey);
        //3.缓存未命中或Redis异常时返回null，由调用方回源数据库
        if (cachedValue == null || RedissonCacheService.isErrorValue(cachedValue)) {
            return null;
        }
        //4.缓存命中，反序列化为订单详情对象
        return JsonUtils.toObject(cachedValue, OrderDetailForAdminVO.class);
    }

    /**
     * 将订单详情写入缓存
     * 设计亮点：数据为空时不写入缓存，避免缓存空值导致查询异常
     *
     * @param orderId 订单ID
     * @param detail  订单详情
     */
    public void setOrderDetailToCache(Long orderId, OrderDetailForAdminVO detail) {
        //1.订单详情为空时不写入缓存，防止缓存无效数据
        if (detail == null) {
            return;
        }
        //2.构建Redis缓存Key
        String cacheKey = buildCacheKey(orderId);
        //3.将订单详情序列化为JSON字符串写入Redis，过期时间7天
        redissonCacheService.set(cacheKey, JsonUtils.toJsonString(detail), 7, TimeUnit.DAYS);
        log.debug("订单详情缓存写入成功, orderId={}", orderId);
    }

    /**
     * 删除订单详情缓存
     * 适用场景：订单数据发生变更需要强制失效缓存时调用
     *
     * @param orderId 订单ID
     */
    public void deleteOrderDetailCache(Long orderId) {
        //1.构建Redis缓存Key
        String cacheKey = buildCacheKey(orderId);
        //2.删除Redis中的订单详情缓存
        redissonCacheService.delete(cacheKey);
        log.debug("订单详情缓存删除成功, orderId={}", orderId);
    }

    private String buildCacheKey(Long orderId) {
        return String.format(ORDER_DETAIL_CACHE_KEY_PREFIX, orderId);
    }
}
