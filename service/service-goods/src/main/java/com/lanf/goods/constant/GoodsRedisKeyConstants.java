package com.lanf.goods.constant;

/**
 * 商品模块 Redis Key 常量类
 * 统一管理商品相关的所有 Redis Key 格式
 */
public class GoodsRedisKeyConstants {

    // ==================== 商品详情相关 ====================

    /**
     * 用户端商品详情缓存
     * 格式: goods:detail:user:{goodsId}
     * 用途: 存储用户端展示的商品详细信息（包含SKU列表、规格列表等）
     * 过期时间: 30分钟
     */
    public static final String GOODS_DETAIL_USER = "goods:detail:user:%s";
    
    /**
     * 用户端商品详情分布式锁Key
     * 格式: lock:goods:detail:user:{goodsId}
     * 用途: 防止缓存击穿，保护数据库
     */
    public static final String GOODS_DETAIL_USER_LOCK = "lock:goods:detail:user:%s";

    /**
     * 用户端商品详情缓存过期时间（秒）
     * 7天
     */
    public static final long GOODS_DETAIL_USER_EXP_TIME = 7 * 24 * 60 * 60L;

    /**
     * 用户端商品详情空值缓存过期时间（秒）
     * 5分钟，用于防止缓存穿透
     */
    public static final long GOODS_DETAIL_USER_NULL_EXP_TIME = 5 * 60L;

    /**
     * 获取用户端商品详情缓存Key
     * @param goodsId 商品ID
     * @return 缓存Key
     */
    public static String getGoodsDetailUserKey(Long goodsId) {
        return String.format(GOODS_DETAIL_USER, goodsId);
    }

    /**
     * 获取用户端商品详情分布式锁Key
     * @param goodsId 商品ID
     * @return 锁Key
     */
    public static String getGoodsDetailUserLockKey(Long goodsId) {
        return String.format(GOODS_DETAIL_USER_LOCK, goodsId);
    }

}
