package com.lanf.seckill.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.cache.service.RedissonCacheService;
import com.lanf.common.utils.JsonUtils;
import com.lanf.constant.exception.BizException;
import com.lanf.constant.result.RpcResultParser;
import com.lanf.goods.api.GoodsApiService;
import com.lanf.goods.model.dto.SeckillStockPreoccupationDTO;
import com.lanf.seckill.mapper.SeckillActivityMapper;
import com.lanf.seckill.model.bo.SeckillItemList;
import com.lanf.seckill.model.dto.AddSeckillActivityDTO;
import com.lanf.seckill.model.dto.AddSeckillItemDTO;
import com.lanf.seckill.model.dto.LauncherSeckillItemDTO;
import com.lanf.seckill.model.entity.SeckillActivityDO;
import com.lanf.seckill.model.entity.SeckillItemDO;
import com.lanf.seckill.model.enums.SeckillActivityStatusEnum;
import com.lanf.seckill.service.ISeckillActivityService;
import com.lanf.seckill.service.ISeckillItemService;
import com.lanf.tcc.service.ITccOperationService;
import lombok.extern.slf4j.Slf4j;
import org.dromara.hmily.annotation.HmilyTCC;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 秒杀活动表 服务实现类
 * </p>
 *
 * @author jarven
 * @since 2026-05-07
 */
@Slf4j
@Service
public class SeckillActivityServiceImpl extends ServiceImpl<SeckillActivityMapper, SeckillActivityDO> implements ISeckillActivityService {

    @Autowired
    private GoodsApiService goodsApiService;
    @Autowired
    private ITccOperationService tccOperationService;
    @Autowired
    private ISeckillItemService seckillItemService;
    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private RedissonCacheService redissonCacheService;
    @Autowired
    private RedisKeyGenerator redisKeyGenerator;

    /**
     * 缓存活动商品列表
     */
    private static final String SECKILL_ITEM_LIST_KEY_PRX = "seckill:item:list:%s";

    @Override
    public void addSeckillActivity(AddSeckillActivityDTO dto) {

        Date endTime = dto.getEndTime();

        if (endTime.before(new Date())) {
            log.warn("活动结束时间不能早于当前时间");
            throw new BizException("活动结束时间不能早于当前时间");
        }

        SeckillActivityDO seckillActivityDO = new SeckillActivityDO();
        seckillActivityDO.setName(dto.getName());
        seckillActivityDO.setStartTime(dto.getStartTime());
        seckillActivityDO.setEndTime(dto.getEndTime());
        //简单处理 默认开始 实际秒杀时间以秒杀开始时间为准 提前预热
        seckillActivityDO.setStatus(SeckillActivityStatusEnum.IN_PROGRESS);
        seckillActivityDO.setMerchantId(null);
        this.save(seckillActivityDO);

    }

    @HmilyTCC(confirmMethod = "confirmAddSeckillItem", cancelMethod = "cancelAddSeckillItem")
    @Override
    public void addAddSeckillItem(AddSeckillItemDTO dto) {

        Long activityId = dto.getActivityId();
        SeckillActivityDO one = this.lambdaQuery()
                .eq(SeckillActivityDO::getId, activityId)
                .one();
        if (one == null) {
            tccOperationService.addInterruptedFlag(buidSeckillItemKey(dto.getOrderNumber()),
                    "活动不存在");
            log.error("活动不存在");
            throw new BizException("活动不存在");
        }

        tccOperationService.tryOperation(buidSeckillItemKey(dto.getOrderNumber()), null);
        /**
         * 预占库存
         */
        seckillStockPreoccupation(dto);

    }


    private void seckillStockPreoccupation(AddSeckillItemDTO dto) {
        SeckillStockPreoccupationDTO stockPreoccupationDTO = new SeckillStockPreoccupationDTO();
        stockPreoccupationDTO.setBizKeyPrx(dto.getOrderNumber());
        stockPreoccupationDTO.setSkuCode(dto.getSkuCode());
        stockPreoccupationDTO.setWarehouseId(dto.getWarehouseId());
        stockPreoccupationDTO.setPreQuantity(dto.getTotalStock());

        RpcResultParser.parseResult(goodsApiService.seckillStockPreoccupation(stockPreoccupationDTO));
    }

    private String buidSeckillItemKey(String bizKeyPrx) {
        return bizKeyPrx + "_" + "addAddSeckillItem";
    }

    @Transactional
    public void confirmAddSeckillItem(AddSeckillItemDTO dto) {

        SeckillItemDO seckillItemDO = new SeckillItemDO();
        seckillItemDO.setActivityId(dto.getActivityId());
        seckillItemDO.setItemId(dto.getItemId());
        seckillItemDO.setItemTitle(dto.getItemTitle());
        seckillItemDO.setItemImage(dto.getItemImage());
        seckillItemDO.setImages(dto.getImages());
        seckillItemDO.setSkuCode(dto.getSkuCode());
        seckillItemDO.setWarehouseId(dto.getWarehouseId());
        seckillItemDO.setAttributes(dto.getAttributes());
        seckillItemDO.setOriginalPrice(dto.getOriginalPrice());
        seckillItemDO.setSeckillPrice(dto.getSeckillPrice());
        seckillItemDO.setTotalStock(dto.getTotalStock());
        seckillItemDO.setLimitPerUser(dto.getLimitPerUser());
        seckillItemDO.setSoldStock(0);
        //默认下架状态
        seckillItemDO.setShelfStatus(0);
        seckillItemDO.setMerchantId(null);
        boolean operation = tccOperationService.confirmOperation(buidSeckillItemKey(dto.getOrderNumber()));
        if (!operation) {
            log.info("已执行");
            return;
        }
        seckillItemService.save(seckillItemDO);
    }

    public void cancelAddSeckillItem(AddSeckillItemDTO dto) {

        tccOperationService.cancelOperation(buidSeckillItemKey(dto.getOrderNumber()));

    }

    @Override
    public void launcherSeckillItem(LauncherSeckillItemDTO itemDTO) {

        Long seckillItemId = itemDTO.getSeckillItemId();
        SeckillItemDO one = seckillItemService.lambdaQuery()
                .eq(SeckillItemDO::getId, seckillItemId)
                .one();
        if (one == null) {
            log.error("秒杀商品不存在");
            throw new BizException("商品不存在");
        }
        Long activityId = one.getActivityId();
        SeckillActivityDO activityDO = this.lambdaQuery().eq(SeckillActivityDO::getId, activityId)
                .one();
        //1.更新商品状态
        boolean update = seckillItemService.lambdaUpdate()
                .eq(SeckillItemDO::getId, seckillItemId)
                .set(SeckillItemDO::getShelfStatus, 1)
                .update();
        if (!update) {
            log.error("更新失败");
            throw new BizException("更新失败");
        }
        //2.添加到缓存列表中
        SeckillItemList seckillItemList = getSeckillItemList(one, activityDO);
        String data = JsonUtils.toJsonString(seckillItemList);
        String key = String.format(SECKILL_ITEM_LIST_KEY_PRX, activityId);
        long cacheExpireSeconds = calculateCacheExpireSeconds(activityDO.getEndTime());
        /**
         * 写入集群中的每个节点中
         */
        RedisKeyGenerator.ALL_DIGIT_SUFFIXES.forEach(digit -> {
            String generateKey = redisKeyGenerator.generateKey(key, digit);
            redissonCacheService.addToList(generateKey, data, 
                    cacheExpireSeconds, TimeUnit.SECONDS);
        });

    }

    private static SeckillItemList getSeckillItemList(SeckillItemDO one, SeckillActivityDO activityDO) {
        SeckillItemList seckillItemList = new SeckillItemList();
        seckillItemList.setActivityId(one.getActivityId());
        seckillItemList.setSeckillItemId(one.getId());
        seckillItemList.setStartTime(activityDO.getStartTime());
        seckillItemList.setItemTitle(one.getItemTitle());
        seckillItemList.setItemImage(one.getItemImage());
        seckillItemList.setAttributes(one.getAttributes());
        seckillItemList.setOriginalPrice(one.getOriginalPrice());
        seckillItemList.setSeckillPrice(one.getSeckillPrice());
        return seckillItemList;
    }

    /**
     * 根据活动结束时间计算缓存过期时间（秒）
     * 
     * @param endTime 活动结束时间
     * @return 缓存过期时间（秒），最小60秒，最大30天
     */
    private long calculateCacheExpireSeconds(Date endTime) {
        if (endTime == null) {
            log.warn("活动结束时间为空，使用默认缓存时间7天");
            return 7 * 24 * 60 * 60; // 7天
        }
        
        long now = System.currentTimeMillis();
        long endTimestamp = endTime.getTime();
        long remainingSeconds = (endTimestamp - now) / 1000;
        
        if (remainingSeconds <= 0) {
            log.warn("活动已结束，使用最小缓存时间60秒");
            return 60; // 活动结束后保留1分钟
        }
        
        // 最大缓存时间：30天
        long maxCacheSeconds = 30L * 24 * 60 * 60;
        
        // 最小缓存时间：60秒
        long minCacheSeconds = 60;
        
        long cacheSeconds = Math.min(Math.max(remainingSeconds, minCacheSeconds), maxCacheSeconds);
        
        log.info("活动剩余时间: {}秒, 缓存过期时间: {}秒", remainingSeconds, cacheSeconds);
        
        return cacheSeconds;
    }

}
