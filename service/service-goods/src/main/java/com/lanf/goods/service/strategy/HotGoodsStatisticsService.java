package com.lanf.goods.service.strategy;


/**
 * 热门商品统计服务
 */
public interface HotGoodsStatisticsService {

    /**
     * 记录热门 商品
     *
     *
     *
     */
    void recordGoodsAction(Long goodsId);


    boolean isHotGoods(Long goodsId);
}
