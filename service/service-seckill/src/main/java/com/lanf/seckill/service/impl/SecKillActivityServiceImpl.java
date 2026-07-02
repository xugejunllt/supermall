package com.lanf.seckill.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.api.goods.api.GoodsApiService;
import com.lanf.api.goods.model.dto.SeckillStockPreoccupationDTO;
import com.lanf.cache.service.RedissonCacheService;
import com.lanf.common.utils.BeanCopyUtils;
import com.lanf.common.utils.IStringUtils;
import com.lanf.common.utils.JsonUtils;
import com.lanf.constant.exception.BizException;
import com.lanf.constant.model.vo.PageResult;
import com.lanf.constant.result.RpcResultParser;
import com.lanf.constant.utils.UserContext;
import com.lanf.rocketmq.util.RocketMqClient;
import com.lanf.seckill.config.SeckillUrlConfig;
import com.lanf.seckill.mapper.SecKillActivityMapper;
import com.lanf.seckill.model.bo.SeckillItemDetail;
import com.lanf.seckill.model.bo.SeckillItemList;
import com.lanf.seckill.model.dto.AddSeckillActivityDTO;
import com.lanf.seckill.model.dto.AddSeckillItemDTO;
import com.lanf.seckill.model.dto.GetSeckillTokenDTO;
import com.lanf.seckill.model.dto.LauncherSeckillItemDTO;
import com.lanf.seckill.model.entity.SecKillActivityDO;
import com.lanf.seckill.model.entity.SecKillItemDO;
import com.lanf.seckill.model.query.SeckillItemPageQuery;
import com.lanf.seckill.model.vo.SecKillActivityListVO;
import com.lanf.seckill.model.vo.SeckillItemDetailVO;
import com.lanf.seckill.model.vo.SeckillItemVO;
import com.lanf.seckill.model.vo.SeckillTokenVO;
import com.lanf.seckill.service.ISecKillActivityService;
import com.lanf.seckill.service.ISecKillItemService;
import com.lanf.web.utils.JwtUtils;
import com.lanf.seckill.model.query.SecKillActivityPageQuery;
import com.lanf.seckill.model.vo.SecKillActivityPageVO;
import lombok.extern.slf4j.Slf4j;
import org.dromara.hmily.annotation.HmilyTCC;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
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
public class SecKillActivityServiceImpl extends ServiceImpl<SecKillActivityMapper, SecKillActivityDO> implements ISecKillActivityService {

    @Autowired
    private GoodsApiService goodsApiService;

    @Autowired
    private ISecKillItemService seckillItemService;
    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private RedissonCacheService redissonCacheService;
    @Autowired
    private RedisKeyGenerator redisKeyGenerator;
    @Qualifier("seckillQueryExecutor")
    @Autowired
    private ThreadPoolTaskExecutor taskExecutor;
    @Autowired
    private SeckillUrlConfig seckillUrlConfig;
    @Autowired
    private RocketMqClient rocketMqClient;


    /**
     * 缓存活动商品列表
     */
    private static final String SECKILL_ITEM_LIST_KEY_PRX = "seckill:item:list:%s";

    /**
     * 缓存活动商品详情
     */
    private static final String SECKILL_ITEM_DETAIL_KEY_PRX = "seckill:item:detail:%s";

    /**
     * 缓存商品库存
     */
    public static final String SECKILL_ITEM_STOCK_KEY_PRX = "seckill:item:stock:%s";
    /**
     * Redis 查询超时时间（毫秒）
     */
    private static final long REDIS_QUERY_TIMEOUT_MS = 50L;

    /**
     * 秒杀令牌缓存 key 前缀
     */
    public static final String SECKILL_TOKEN_KEY_PRX = "seckill:token:%s:%s";

    /**
     * 令牌有效期（秒）
     */
    private static final long TOKEN_EXPIRE_SECONDS = 60L;

    /**
     * 秒杀活动列表缓存 key 前缀
     */
    private static final String SECKILL_ACTIVITY_LIST_KEY = "seckill:activity:list";

    /**
     * 秒杀活动列表缓存过期时间（秒）7天
     */
    private static final long SECKILL_ACTIVITY_LIST_EXPIRE_SECONDS = 7 * 24 * 60 * 60L;

    @Override
    public void addSeckillActivity(AddSeckillActivityDTO dto) {

        SecKillActivityDO seckillActivityDO = new SecKillActivityDO();
        seckillActivityDO.setName(dto.getName());
        this.save(seckillActivityDO);
        redissonCacheService.delete(SECKILL_ACTIVITY_LIST_KEY);
    }

    @HmilyTCC(confirmMethod = "confirmAddAddSeckillItem", cancelMethod = "cancelAddAddSeckillItem")
    @Override
    public void addSeckillItem(AddSeckillItemDTO dto) {

        Long activityId = dto.getActivityId();
        SecKillActivityDO one = this.lambdaQuery()
                .eq(SecKillActivityDO::getId, activityId)
                .one();
        if (one == null) {

            log.error("活动不存在");
            throw new BizException("活动不存在");
        }
        //冻结库存
        seckillStockPreoccupation(dto);

    }


    private void seckillStockPreoccupation(AddSeckillItemDTO dto) {
        SeckillStockPreoccupationDTO stockPreoccupationDTO = new SeckillStockPreoccupationDTO();
        stockPreoccupationDTO.setBizKeyPrx(dto.getOrderNumber());
        stockPreoccupationDTO.setSkuCode(dto.getSkuCode());
        stockPreoccupationDTO.setWarehouseId(dto.getWarehouseId());
        stockPreoccupationDTO.setPreQuantity(dto.getTotalStock());
        stockPreoccupationDTO.setGoodsId(dto.getItemId());
        RpcResultParser.parseResult(goodsApiService.seckillStockPreoccupation(stockPreoccupationDTO));
    }


    public void confirmAddAddSeckillItem(AddSeckillItemDTO dto) {
        SecKillItemDO seckillItemDO = new SecKillItemDO();
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
        seckillItemDO.setShopId(dto.getShopId());
        /**
         * 每人限购数量1
         */
        seckillItemDO.setLimitPerUser(1);
        seckillItemDO.setSoldStock(0);
        //默认下架状态
        seckillItemDO.setShelfStatus(0);
        seckillItemDO.setTenantId(UserContext.getTenantId());
        seckillItemDO.setGoodsName(dto.getGoodsName());
        seckillItemDO.setSkuId(dto.getSkuId());
        seckillItemDO.setStartTime(dto.getStartTime());
        seckillItemDO.setEndTime(dto.getEndTime());
        seckillItemDO.setSecKillMode(dto.getSecKillMode());
        seckillItemDO.setRemainingStock(dto.getTotalStock());
        seckillItemDO.setShopName(dto.getShopName());

        try {
            seckillItemService.save(seckillItemDO);
        } catch (DuplicateKeyException e) {
            log.warn("重复插入");
        }

    }

    public void cancelAddAddSeckillItem(AddSeckillItemDTO dto) {


    }

    @Override
    public void launcherSeckillItem(LauncherSeckillItemDTO itemDTO) {

        Long seckillItemId = itemDTO.getSeckillItemId();
        SecKillItemDO one = seckillItemService.lambdaQuery()
                .eq(SecKillItemDO::getId, seckillItemId)
                .one();
        if (one == null) {
            log.error("秒杀商品不存在");
            throw new BizException("商品不存在");
        }
        Long activityId = one.getActivityId();

        boolean update = seckillItemService.lambdaUpdate()
                .eq(SecKillItemDO::getId, seckillItemId)
                .set(SecKillItemDO::getShelfStatus, 1)
                .update();
        if (!update) {
            log.error("更新失败");
            throw new BizException("更新失败");
        }

        long cacheExpireSeconds = calculateCacheExpireSeconds(one.getEndTime());

        cacheSeckillItemList(one, activityId, cacheExpireSeconds);

        cacheSeckillItemDetail(one, activityId, cacheExpireSeconds);

        cacheSeckillItemStock(one, activityId, cacheExpireSeconds);

    }

    /**
     * 缓存秒杀商品列表
     */
    private void cacheSeckillItemList(SecKillItemDO item,
                                      Long activityId, long cacheExpireSeconds) {
        SeckillItemList seckillItemList = getSeckillItemList(item);
        String data = JsonUtils.toJsonString(seckillItemList);
        log.info("添加的缓存商品列表数据是:{}", data);
        String keyPrefix = String.format(SECKILL_ITEM_LIST_KEY_PRX, activityId);

        RedisKeyGenerator.ALL_DIGIT_SUFFIXES.forEach(digit -> {
            String generateKey = redisKeyGenerator.generateKey(keyPrefix, digit);
            redissonCacheService.addToList(generateKey, data, cacheExpireSeconds, TimeUnit.SECONDS);
        });

        log.info("秒杀商品列表缓存成功: activityId={}, seckillItemId={}", activityId, item.getId());
    }

    /**
     * 缓存秒杀商品详情
     */
    private void cacheSeckillItemDetail(SecKillItemDO item,
                                        Long activityId, long cacheExpireSeconds) {
        SeckillItemDetail detail = getSeckillItemDetail(item);
        String data = JsonUtils.toJsonString(detail);
        String keyPrefix = String.format(SECKILL_ITEM_DETAIL_KEY_PRX, item.getId());

        RedisKeyGenerator.ALL_DIGIT_SUFFIXES.forEach(digit -> {
            String generateKey = redisKeyGenerator.generateKey(keyPrefix, digit);
            redissonCacheService.set(generateKey, data, cacheExpireSeconds, TimeUnit.SECONDS);
        });

        log.info("秒杀商品详情缓存成功: activityId={}, seckillItemId={}", activityId, item.getId());
    }

    /**
     * 缓存秒杀商品库存（只存储在一个节点）
     */
    private void cacheSeckillItemStock(SecKillItemDO item, Long activityId, long cacheExpireSeconds) {


        String stockKey = String.format(SECKILL_ITEM_STOCK_KEY_PRX, item.getId());

        Integer totalStock = item.getTotalStock();
        if (totalStock == null || totalStock < 0) {
            log.warn("库存数量异常: activityId={}, seckillItemId={}, totalStock={}",
                    activityId, item.getId(), totalStock);
            totalStock = 0;
        }

        redissonCacheService.setAtomicLong(stockKey, totalStock, cacheExpireSeconds, TimeUnit.SECONDS);

        log.info("秒杀商品库存缓存成功: activityId={}, seckillItemId={}, totalStock={}",
                activityId, item.getId(), totalStock);
    }

    private static SeckillItemList getSeckillItemList(SecKillItemDO one) {
        SeckillItemList seckillItemList = new SeckillItemList();
        seckillItemList.setActivityId(one.getActivityId());
        seckillItemList.setSeckillItemId(one.getId());
        seckillItemList.setStartTime(one.getStartTime());
        seckillItemList.setItemTitle(one.getItemTitle());
        seckillItemList.setItemImage(one.getItemImage());
        seckillItemList.setAttributes(one.getAttributes());
        seckillItemList.setOriginalPrice(one.getOriginalPrice());
        seckillItemList.setSeckillPrice(one.getSeckillPrice());
        seckillItemList.setEndTime(one.getEndTime());
        return seckillItemList;
    }

    private static SeckillItemDetail getSeckillItemDetail(SecKillItemDO one) {
        SeckillItemDetail detail = new SeckillItemDetail();
        detail.setActivityId(one.getActivityId());
        detail.setSeckillItemId(one.getId());
        detail.setStartTime(one.getStartTime());
        detail.setEndTime(one.getEndTime());
        detail.setItemTitle(one.getItemTitle());
        detail.setItemImage(one.getItemImage());
        detail.setAttributes(one.getAttributes());
        detail.setOriginalPrice(one.getOriginalPrice());
        detail.setSeckillPrice(one.getSeckillPrice());
        detail.setSecKillMode(one.getSecKillMode());
        return detail;
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
            return 7 * 24 * 60 * 60;
        }

        long now = System.currentTimeMillis();
        long endTimestamp = endTime.getTime();
        long remainingSeconds = (endTimestamp - now) / 1000;

        if (remainingSeconds <= 0) {
            log.warn("活动已结束，使用最小缓存时间60秒");
            return 60;
        }

        long maxCacheSeconds = 30L * 24 * 60 * 60;
        long minCacheSeconds = 60;

        long cacheSeconds = Math.min(Math.max(remainingSeconds, minCacheSeconds), maxCacheSeconds);

        log.info("活动剩余时间: {}秒, 缓存过期时间: {}秒", remainingSeconds, cacheSeconds);

        return cacheSeconds;
    }

    /**
     * 分页查询秒杀商品列表
     */
    @Override
    public List<SeckillItemVO> seckillItemPageQuery(SeckillItemPageQuery query) {

        Long activityId = query.getActivityId();
        long page = query.getPage();
        long pageSize = query.getPageSize();
        if (activityId == null) {
            log.warn("活动ID不能为空");
            return Collections.emptyList();
        }

        if (page < 1) {
            page = 1;
        }

        if (pageSize < 1 || pageSize > 100) {
            pageSize = 10;
        }
        String keyPrefix = String.format(SECKILL_ITEM_LIST_KEY_PRX, activityId);
        Integer randomDigit = RedisKeyGenerator.getRandomDigitSuffix();
        String key = redisKeyGenerator.generateKey(keyPrefix, randomDigit);

        List<String> items = redissonCacheService.getListPage(key, (int) page, (int) pageSize);
        if (IStringUtils.isEmpty(items)) {

            return Collections.emptyList();
        }

        List<SeckillItemVO> result = new ArrayList<>(items.size());
        for (String itemJson : items) {
            try {
                SeckillItemList itemList = JsonUtils.toObject(itemJson, SeckillItemList.class);
                SeckillItemVO vo = convertToVO(itemList);
                result.add(vo);
            } catch (Exception e) {
                log.error("解析秒杀商品数据失败: {}", itemJson, e);
            }
        }

        return result;
    }

    /**
     * 将 SeckillItemList 转换为 SeckillItemVO
     */
    private SeckillItemVO convertToVO(SeckillItemList itemList) {
        SeckillItemVO vo = new SeckillItemVO();
        vo.setActivityId(itemList.getActivityId());
        vo.setSeckillItemId(itemList.getSeckillItemId());
        vo.setStartTime(itemList.getStartTime());
        vo.setItemTitle(itemList.getItemTitle());
        vo.setItemImage(itemList.getItemImage());
        vo.setAttributes(itemList.getAttributes());
        vo.setOriginalPrice(itemList.getOriginalPrice());
        vo.setSeckillPrice(itemList.getSeckillPrice());
        vo.setEndTime(itemList.getEndTime());
        return vo;
    }

    /**
     * 查询秒杀商品详情（并行查询优化）
     *
     * @param seckillItemId 秒杀商品ID
     * @return 商品详情VO
     */
    /**
     * 查询秒杀商品详情（并行查询优化 + 超时控制 + 耗时监控）
     *
     * @param seckillItemId 秒杀商品ID
     * @return 商品详情VO
     */
    @Override
    public SeckillItemDetailVO seckillItemDetailQuery(Long seckillItemId) {


        if (seckillItemId == null) {
            log.warn("商品ID不能为空");
            return null;
        }

        String keyPrefix = String.format(SECKILL_ITEM_DETAIL_KEY_PRX, seckillItemId);
        String stockKey = String.format(SECKILL_ITEM_STOCK_KEY_PRX, seckillItemId);

        Integer randomDigit = RedisKeyGenerator.getRandomDigitSuffix();
        String detailKey = redisKeyGenerator.generateKey(keyPrefix, randomDigit);
        log.info("商品详细缓存key:{}", detailKey);
        try {

            // 使用 taskExecutor 线程池并行查询
            CompletableFuture<String> detailFuture =
                    CompletableFuture.supplyAsync(() ->
                            redissonCacheService.get(detailKey), taskExecutor);

            CompletableFuture<Long> stockFuture =
                    CompletableFuture.supplyAsync(() ->
                            redissonCacheService.getAtomicLong(stockKey), taskExecutor);

            // 等待两个查询完成，设置超时时间
            CompletableFuture.allOf(detailFuture, stockFuture)
                    .get(REDIS_QUERY_TIMEOUT_MS, TimeUnit.MILLISECONDS);

            String data = detailFuture.getNow(null);
            long stock = stockFuture.getNow(-1L);

            if (IStringUtils.isEmpty(data)) {

                return null;
            }

            SeckillItemDetail detail = JsonUtils.toObject(data, SeckillItemDetail.class);
            SeckillItemDetailVO vo = convertToDetailVO(detail);
            vo.setStockCount(stock > 0 ? stock : 0);
            return vo;

        } catch (Exception e) {
            log.error("查询商品详情异常: seckillItemId={}", seckillItemId, e);
            return null;
        }
    }

    /**
     * 将 SeckillItemDetail 转换为 SeckillItemDetailVO
     */
    private SeckillItemDetailVO convertToDetailVO(SeckillItemDetail detail) {
        SeckillItemDetailVO vo = new SeckillItemDetailVO();

        vo.setActivityId(detail.getActivityId());
        vo.setSeckillItemId(detail.getSeckillItemId());
        vo.setStartTime(detail.getStartTime());
        vo.setEndTime(detail.getEndTime());
        vo.setItemTitle(detail.getItemTitle());
        vo.setItemImage(detail.getItemImage());
        vo.setAttributes(detail.getAttributes());
        vo.setOriginalPrice(detail.getOriginalPrice());
        vo.setSeckillPrice(detail.getSeckillPrice());
        vo.setSecKillMode(detail.getSecKillMode());


        return vo;
    }

    /**
     * 获取秒杀令牌（动态秒杀链接）
     * 秒杀开始时生成一次性 token，用于后续下单验证
     * <p>
     * 只有活动开始时候 才能获取到
     * 避免 黑客提前获取秒杀链接
     * 提前校验 等下单时 直接扣减库存
     * <p>
     * 这里还可以添加黑名单、接入风控系统
     */
    @Override
    public SeckillTokenVO getSeckillToken(GetSeckillTokenDTO dto) {

        Long seckillItemId = dto.getSeckillItemId();
        SeckillItemDetailVO seckillItemDetail = seckillItemDetailQuery(seckillItemId);
        if (seckillItemDetail == null) {
            log.warn("商品不存在");
            throw new BizException("系统繁忙,请稍后再试");
        }

        Date startTime = seckillItemDetail.getStartTime();
        Date endTime = seckillItemDetail.getEndTime();
        if (startTime.after(new Date())) {
            log.warn("秒杀活动即将开始");
            throw new BizException("秒杀活动即将开始");
        }
        if (endTime.before(new Date())) {
            log.warn("秒杀活动已结束");
            throw new BizException("秒杀活动已结束");
        }
        Long stockCount = seckillItemDetail.getStockCount();
        if (stockCount <= 0) {
            log.warn("商品已售罄");
            throw new BizException("商品已售罄");
        }

        Long userId = UserContext.getUserId();
        /**
         * token 绑定用户id 秒杀商品id
         * 过期时间1分钟
         */
        //暂时写死 默认取第一个
        SeckillUrlConfig.UrlMapping urlMapping = seckillUrlConfig.getUrlMappings().get(0);

        String skillToken = JwtUtils.createSecKillToken(userId, seckillItemId,
                seckillItemDetail.getSecKillMode().getCode(), 1);
        String tokenKey = String.format(SECKILL_TOKEN_KEY_PRX, userId, seckillItemId);
        redissonCacheService.set(tokenKey, skillToken, 1, TimeUnit.MINUTES);

        SeckillTokenVO vo = new SeckillTokenVO();
        vo.setToken(skillToken);
        vo.setOrderUrl(urlMapping.getPath());
        return vo;
    }

    @Override
    public List<SecKillActivityListVO> seckillActivityListQuery() {

        String cacheKey = SECKILL_ACTIVITY_LIST_KEY;
        String cacheData = redissonCacheService.get(cacheKey);

        if (!IStringUtils.isEmpty(cacheData) && !RedissonCacheService.isErrorValue(cacheData)) {
            log.info("秒杀活动列表缓存命中:key={}", cacheKey);
            return JsonUtils.toList(cacheData, SecKillActivityListVO.class);
        }

        List<SecKillActivityDO> list = this.lambdaQuery().list();
        if (list.isEmpty()) {
            return null;
        }

        List<SecKillActivityListVO> voList = BeanCopyUtils.copyBeanList(list, SecKillActivityListVO.class);
        redissonCacheService.set(cacheKey, JsonUtils.toJsonString(voList), SECKILL_ACTIVITY_LIST_EXPIRE_SECONDS, TimeUnit.SECONDS);
        log.info("秒杀活动列表缓存设置成功:key={}, expire={}秒", cacheKey, SECKILL_ACTIVITY_LIST_EXPIRE_SECONDS);
        return voList;
    }

    @Override
    public PageResult<SecKillActivityPageVO> seckillActivityPageQuery(SecKillActivityPageQuery query) {
        long page = query.getPage();
        long pageSize = query.getPageSize();


        Page<SecKillActivityDO> pageParam =
                new Page<>(page, pageSize);

        LambdaQueryWrapper<SecKillActivityDO> wrapper =
                new LambdaQueryWrapper<>();

        wrapper.orderByDesc(SecKillActivityDO::getCreateTime);

        Page<SecKillActivityDO> resultPage =
                this.page(pageParam, wrapper);

        List<SecKillActivityDO> records = resultPage.getRecords();
        if (records.isEmpty()) {
            return PageResult.emptyResult();
        }

        return new PageResult<>(BeanCopyUtils.copyBeanList(records, SecKillActivityPageVO.class), pageSize, resultPage.getTotal());
    }

}