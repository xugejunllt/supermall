package com.lanf.goods.service.strategy.impl;

import com.lanf.cache.service.RedissonCacheService;
import com.lanf.goods.service.strategy.HotGoodsStatisticsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class HotGoodsStatisticsServiceImpl implements HotGoodsStatisticsService {
    /**
     * 缓存商品访问次数
     */
    private static final String HOT_GOODS_KEY = "hot:goods:count:%s";
    /**
     *热门商品标记
     */
    private static final String HOT_GOODS_SET_KEY = "hot:goods:mark%s";

    private static final long HOT_THRESHOLD = 3;

    @Autowired
    private RedissonCacheService redissonCacheService;

    @Autowired
    @Qualifier("hotGoodsStatisticsExecutor")
    private ThreadPoolTaskExecutor hotGoodsStatisticsExecutor;

    @Override
    public void recordGoodsAction(Long goodsId) {
        hotGoodsStatisticsExecutor.execute(() -> {
            try {
                /**
                 * 每一秒统计一次
                 */
                long incremented = redissonCacheService.incrementAndGet(String.format(HOT_GOODS_KEY, goodsId),
                        1, TimeUnit.SECONDS);
                
                if (incremented > HOT_THRESHOLD) {

                    boolean exists = redissonCacheService.exists(String.format(HOT_GOODS_SET_KEY, goodsId));
                    if (exists){
                        return;
                    }
                    /**
                     * 热门商品 缓存3天
                     */
                    long incremented1 = redissonCacheService.incrementAndGet(String.format(HOT_GOODS_SET_KEY, goodsId),
                            3, TimeUnit.DAYS);
                    if (incremented1 >= 1){
                       log.info("添加热门商品标记成功");
                       //平衡商品库存
                       // TODO

                    }

                } else {
                    log.debug("记录商品行为成功，goodsId={}, 当前计数={}", goodsId, incremented);
                }
            } catch (Exception e) {
                log.error("记录商品行为失败，goodsId={}", goodsId, e);
            }
        });
    }

    @Override
    public boolean isHotGoods(Long goodsId) {


        return redissonCacheService.exists(String.format(HOT_GOODS_SET_KEY, goodsId));
    }


}
