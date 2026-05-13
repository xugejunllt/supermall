package com.lanf.goods.constant;

/**
 * 商品模块 Redis Key 常量类
 * 统一管理商品相关的所有 Redis Key 格式
 */
public class GoodsRedisKeyConstants {

    // ==================== 商品详情相关 ====================

    /**
     * 商品详情缓存
     * 格式: goods:detail:{goodsId}
     * 用途: 存储商品详细信息（包含SKU属性、规格等）
     * 过期时间: 30分钟
     */
    public static final String GOODS_DETAIL = "goods:detail:%s";
    
    /**
     * 商品详情缓存过期时间（秒）
     */
    public static final long GOODS_DETAIL_EXP_TIME = 1800L;

    /**
     * 获取商品详情缓存Key
     * @param goodsId 商品ID
     * @return 缓存Key
     */
    public static String getGoodsDetailKey(Long goodsId) {
        return String.format(GOODS_DETAIL, goodsId);
    }

}
