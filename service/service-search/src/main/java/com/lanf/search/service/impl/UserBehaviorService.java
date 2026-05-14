package com.lanf.search.service.impl;

import com.lanf.cache.service.RedissonCacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class UserBehaviorService {
    @Autowired
    private RedissonCacheService redissonCacheService;

    private static final String CLICK_COUNT_KEY = "search:stats:click:";
    private static final String TRANS_COUNT_KEY = "search:stats:trans:";

    /**
     * 获取商品组合分数 (fx3)
     * 维度：点击得分、交易得分、卖家服务能力
     */
    public double getComboScore(Long goodsId) {
        // 1. 从 Redis 获取近 7 天的点击量
        Long clicks = getRedisCount(CLICK_COUNT_KEY + goodsId);
        
        // 2. 从 Redis 获取近 7 天的成交量
        Long transactions = getRedisCount(TRANS_COUNT_KEY + goodsId);

        // 3. 计算得分：转化率权重更高
        // 公式示例：log(点击量 + 1) * 0.5 + log(成交量 + 1) * 2.0
        double clickScore = Math.log(clicks + 1) * 0.5;
        double transScore = Math.log(transactions + 1) * 2.0;

        return clickScore + transScore;
    }

    /**
     * 获取用户对特定商品的偏好系数 (用于精排阶段)
     */
    public double getUserPreference(Long userId, Long goodsId) {
        if (userId == null) return 0.0;
        
        // 逻辑：如果用户曾经点击或购买过该商品所属的类目/品牌，则给予加分
        // 实际应查询用户画像标签与商品标签的匹配度
        String userCategoryKey = "user:category:" + userId;
//        Boolean hasInteraction = redissonCacheService.isMemberOfSet(userCategoryKey, goodsId);
        
//        return (hasInteraction != null && hasInteraction) ? 0.2 : 0.0;
        return 0.2;
    }

    private Long getRedisCount(String key) {
        Long count = redissonCacheService.getAtomicLong(key);
        return count != null ? count.longValue() : 0L;
    }

    /**
     * 辅助方法：当用户点击商品时，调用此方法增加 Redis 计数
     */
    public void incrementClickCount(Long goodsId) {
        redissonCacheService.incrementGet(CLICK_COUNT_KEY + goodsId);
        redissonCacheService.expire(CLICK_COUNT_KEY + goodsId, 7, TimeUnit.DAYS);
    }
}
