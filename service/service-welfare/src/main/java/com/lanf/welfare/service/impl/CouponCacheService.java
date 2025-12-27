package com.lanf.welfare.service.impl;

import com.lanf.common.utils.JsonUtils;
import com.lanf.redis.constant.CacheConstants;
import com.lanf.redis.service.RedisCache;
import com.lanf.welfare.model.bo.CacheCouponTemplateListBO;
import com.lanf.welfare.model.bo.DeductShopCouponRemainCountCacheBO;
import com.lanf.welfare.model.bo.ShopCouponRemainCountCacheBO;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 优惠券缓存服务  聚会优惠卷相关缓存
 */
@Slf4j
@Service
@Data
public class CouponCacheService {

    @Autowired
    private RedisCache redisCache;


    /**
     * 执行减一操作
     * @param key Redis key
     * @param hashField Hash字段
     * @param expireTime 过期时间（秒）
     *
     *
     * -1： key不存在
     * 0：数量不足
     * 1： 扣減成功
     *
     */

    private static final String LUA_SCRIPT =
                    "local key = KEYS[1] \n" +
                    "local hashField = ARGV[1] \n" +
                    "local expireTime = tonumber(ARGV[2]) \n" +
                    "local refreshThreshold = 0.2 \n" +
                    " \n" +
                    "-- 1. 检查key是否存在 \n" +
                    "if redis.call('exists', key) == 0 then \n" +
                    "    return  tostring(-1) \n" +
                    "end \n" +
                    " \n" +
                    "-- 2. 获取hash字段的值 \n" +
                    "local  value = redis.call('hget', key,hashField) \n" +
                    "if  not value then \n" +
                    "    return tostring(-1) \n" +
                    "end \n" +


                    " \n" +
                    "-- 3. 检查剩余过期时间 \n" +
                    "local ttl = redis.call('ttl', key) \n" +

                    "if ttl > 0 and ttl <= expireTime * refreshThreshold then \n" +
                    "    -- 剩余时间小于等于20%，刷新过期时间 \n" +
                    "    redis.call('expire', key, expireTime) \n" +
                    "end \n" +
                    " \n" +
                    "-- 4. 处理value值 \n" +
                    "local numValue = tonumber(value) \n"+

                    "if numValue > 0 then \n" +
                    "    -- value大于0，减1并更新 \n" +

                    "    local newValue = numValue - 1 \n" +
                    "    redis.call('hset', key, hashField, tostring(newValue)) \n" +
                    "    return  tostring(1)  \n" +
                    "else \n" +
                    "    -- value小于等于0，返回原值 \n" +
                    "    return   tostring(0) \n" +
                    "end";


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
        Map<String, String> remainCountMap = new HashMap<>();
        for (ShopCouponRemainCountCacheBO bo : boList) {
            remainCountMap.put(bo.getCouponTemplateId().toString(), bo.getRemainCount().toString());
        }
        redisCache.setCacheMap(CacheConstants.getSHOP_COUPON_COUNT(shopId), remainCountMap);
        redisCache.expire(CacheConstants.getSHOP_COUPON_COUNT(shopId),
                CacheConstants.SHOP_COUPON_COUNT_EXP_TIME);

    }

    /**
     * 获取店铺优惠卷剩余数量缓存
     */
    public Map<String, String> getShopCouponRemainCountCache(Long shopId) {



        return redisCache.getCacheMap(CacheConstants.getSHOP_COUPON_COUNT(shopId));

    }

    /**
     * 扣减 缓存 hahs中的优惠卷剩余数量
     *
     *
     *
     */
    public DeductShopCouponRemainCountCacheBO deductShopCouponRemainCountCache(Long shopId, Long couponTemplateId){

        DefaultRedisScript<String > script = new DefaultRedisScript<>();
        script.setScriptText(LUA_SCRIPT);
        script.setResultType(String .class);
        String  resultStatus = redisCache.decrementIfPositive(script,
                CacheConstants.getSHOP_COUPON_COUNT(shopId),
                couponTemplateId.toString(), CacheConstants.SHOP_COUPON_COUNT_EXP_TIME);


        DeductShopCouponRemainCountCacheBO bo = new DeductShopCouponRemainCountCacheBO();
        bo.setResultStatus(Integer.parseInt(resultStatus));

        return  bo;


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
