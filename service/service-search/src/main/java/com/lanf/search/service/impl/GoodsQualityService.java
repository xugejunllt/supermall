package com.lanf.search.service.impl;

import org.springframework.stereotype.Service;

@Service
public class GoodsQualityService {

    /**
     * 获取商品质量得分 (fx2)
     * 维度：发布时间、商品级别、商家级别、信息完整度
     */
    public double getQualityScore(Long goodsId) {
        // TODO: 实际应从 MySQL 的 goods_ext 表或 Redis 缓存中读取
        
        // 模拟逻辑：
        // 1. 新品扶持：假设 ID 大于 10000 的是新品，加分
        boolean isNew = isNewProduct(goodsId);
        double newScore = isNew ? 2.0 : 0.0;

        // 2. 商家等级：假设通过 goodsId 查到的商家等级为 A/B/C
        double shopLevelScore = getShopLevelScore(goodsId);

        // 3. 信息完整度：是否有主图、详情图等
        double completenessScore = 1.0; 

        return newScore + shopLevelScore + completenessScore;
    }

    /**
     * 判断是否为新品 (用于重排阶段)
     */
    public boolean isNewProduct(Long goodsId) {
        // 实际应查询 create_time 是否在 7 天内
        return goodsId > 10000L; 
    }

    private double getShopLevelScore(Long goodsId) {
        // 模拟：偶数 ID 为金牌商家，奇数为普通商家
        return (goodsId % 2 == 0) ? 3.0 : 1.0;
    }
}
