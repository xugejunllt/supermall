package com.lanf.welfare.service.impl;

import com.lanf.common.utils.JsonUtils;
import com.lanf.redis.constant.CacheConstants;
import com.lanf.redis.service.RedisCache;
import com.lanf.welfare.model.bo.CacheCouponTemplateListBO;
import com.lanf.welfare.model.bo.ShopCouponRemainCountCacheBO;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 优惠券缓存服务  聚会优惠卷相关缓存
 */
@Service
@Data
public class CouponCacheService {

    @Autowired
    private RedisCache redisCache;

    /**
     * 获取店铺优惠券缓存
     */
    public List<CacheCouponTemplateListBO> getShopCouponCache(Long shopId) {

        String key = CacheConstants.getSHOP_COUPON(shopId);
        String cache = redisCache.getCacheObject(key);

        if (cache == null) {

            return null;
        }
        return JsonUtils.toList(cache, CacheCouponTemplateListBO.class);

    }

    /**
     * 设置店铺优惠券缓存
     */
    public void setShopCouponCache(Long shopId, List<CacheCouponTemplateListBO> list) {

        String key = CacheConstants.getSHOP_COUPON(shopId);

        redisCache.setCacheObject(key, JsonUtils.toJsonString(list), CacheConstants.SHOP_COUPON_EXP_TIME);

    }

    /**
     * 删除店铺优惠券缓存
     */
    public void removeShopCouponCache(Long shopId) {

        String key = CacheConstants.getSHOP_COUPON(shopId);
        redisCache.deleteObject(key);

    }

    /**
     * 设置店铺优惠卷剩余数量缓存
     */
    public void setShopCouponRemainCountCache(Long shopId, List<ShopCouponRemainCountCacheBO> boList) {

        // 构造Map缓存：key为couponTemplateId，value为remainCount
        Map<String, Integer> remainCountMap = new HashMap<>();
        for (ShopCouponRemainCountCacheBO bo : boList) {
            remainCountMap.put(bo.getCouponTemplateId().toString(), bo.getRemainCount());
        }
        redisCache.setCacheMap(CacheConstants.getSHOP_COUPON_COUNT(shopId), remainCountMap);
        redisCache.expire(CacheConstants.getSHOP_COUPON_COUNT(shopId),
                CacheConstants.SHOP_COUPON_COUNT_EXP_TIME);

    }

    /**
     * 获取店铺优惠卷剩余数量缓存
     */
    public Map<String, Integer> getShopCouponRemainCountCache(Long shopId) {

        return redisCache.getCacheMap(CacheConstants.getSHOP_COUPON_COUNT(shopId));

    }

    /**
     * 删除 店铺优惠卷剩余数量缓存
     */
    public void removeShopCouponRemainCountCache(Long shopId) {

        redisCache.deleteObject(CacheConstants.getSHOP_COUPON_COUNT(shopId));

    }

    /**
     * 添加优惠券撤销缓存
     */

    public void setRevokeCouponCache(Set<Long> templateIdSet) {

        redisCache.setCacheSet(CacheConstants.COUPON_REVOKE, templateIdSet);
        redisCache.expire(CacheConstants.COUPON_REVOKE, CacheConstants.COUPON_REVOKE_EXP_TIME);

    }

    /**
     * 获取优惠券撤销缓存
     */
    public Set<Long> getRevokeCouponCache() {

        return redisCache.getCacheSet(CacheConstants.COUPON_REVOKE);

    }

    /**
     * 删除优惠券撤销缓存
     */
    public void removeRevokeCouponCache() {

        redisCache.deleteObject(CacheConstants.COUPON_REVOKE);
    }


}
