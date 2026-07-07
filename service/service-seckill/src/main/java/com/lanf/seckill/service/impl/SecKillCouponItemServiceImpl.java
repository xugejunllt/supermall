package com.lanf.seckill.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.common.utils.BeanCopyUtils;
import com.lanf.cache.service.RedissonCacheService;
import com.lanf.common.utils.IStringUtils;
import com.lanf.common.utils.JsonUtils;
import com.lanf.constant.exception.BizException;
import com.lanf.constant.model.vo.PageResult;
import com.lanf.constant.utils.UserContext;
import com.lanf.rocketmq.util.RocketMqClient;
import com.lanf.seckill.config.SeckillCouponUrlConfig;
import com.lanf.seckill.mapper.SecKillCouponItemMapper;
import com.lanf.seckill.model.dto.AddSecKillCouponItemDTO;
import com.lanf.seckill.model.dto.GetSecKillCouponTokenDTO;
import com.lanf.seckill.model.dto.LauncherSecKillCouponItemDTO;
import com.lanf.seckill.model.entity.SecKillCouponItemDO;
import com.lanf.seckill.model.vo.SecKillCouponItemDetailVO;
import com.lanf.seckill.model.vo.SecKillCouponItemVO;
import com.lanf.seckill.model.vo.SecKillCouponTokenVO;
import com.lanf.seckill.service.ISecKillActivityService;
import com.lanf.seckill.service.ISecKillCouponItemService;
import com.lanf.seckill.model.query.SecKillCouponItemPageQuery;
import com.lanf.seckill.model.vo.SecKillCouponItemPageVO;
import com.lanf.web.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
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
 * 秒杀优惠券项目表 服务实现类
 * </p>
 *
 * @author jarven
 * @since 2026-06-20
 */
@Slf4j
@Service
public class SecKillCouponItemServiceImpl extends ServiceImpl<SecKillCouponItemMapper, SecKillCouponItemDO> implements ISecKillCouponItemService {

    @Autowired
    private ISecKillActivityService seckillActivityService;

    @Autowired
    private RedissonCacheService redissonCacheService;

    @Autowired
    private RedisKeyGenerator redisKeyGenerator;

    @Qualifier("seckillQueryExecutor")
    @Autowired
    private ThreadPoolTaskExecutor taskExecutor;

    @Autowired
    private SeckillCouponUrlConfig seckillCouponUrlConfig;

    @Autowired
    private RocketMqClient rocketMqClient;

    /**
     * 缓存活动优惠券列表
     */
    private static final String SECKILL_COUPON_LIST_KEY_PRX = "seckill:coupon:list";

    /**
     * 缓存活动优惠券详情
     */
    private static final String SECKILL_COUPON_DETAIL_KEY_PRX = "seckill:coupon:detail:%s";

    /**
     * 缓存优惠券库存
     */
    public static final String SECKILL_COUPON_STOCK_KEY_PRX = "seckill:coupon:stock:%s";

    /**
     * Redis 查询超时时间（毫秒）
     */
    private static final long REDIS_QUERY_TIMEOUT_MS = 50L;

    /**
     * 秒杀令牌缓存 key 前缀
     */
    public static final String SECKILL_COUPON_TOKEN_KEY_PRX = "seckill:coupon:token:%s:%s";

    @Override
    public void addSecKillCouponItem(AddSecKillCouponItemDTO dto) {



        Date endTime = dto.getEndTime();
        if (endTime.before(new Date())) {
            log.warn("活动结束时间不能早于当前时间");
            throw new BizException("活动结束时间不能早于当前时间");
        }

        SecKillCouponItemDO couponItemDO = new SecKillCouponItemDO();
        couponItemDO.setCouponTemplateId(dto.getCouponTemplateId());
        couponItemDO.setCouponName(dto.getCouponName());
        couponItemDO.setCouponTitle(dto.getCouponTitle());
        couponItemDO.setSecKillMode(dto.getSecKillMode());
        couponItemDO.setTotalStock(dto.getTotalStock());
        couponItemDO.setRemainingStock(dto.getTotalStock());
        couponItemDO.setLimitPerUser(dto.getLimitPerUser());
        couponItemDO.setSoldStock(0);
        couponItemDO.setShelfStatus(0); // 默认下架状态
        couponItemDO.setStartTime(dto.getStartTime());
        couponItemDO.setEndTime(dto.getEndTime());
        couponItemDO.setTenantId(UserContext.getTenantId());

        try {
            this.save(couponItemDO);
        } catch (DuplicateKeyException e) {
            log.warn("重复插入");
        }

    }

    @Override
    public void launcherSecKillCouponItem(LauncherSecKillCouponItemDTO dto) {

        Long secKillCouponItemId = dto.getSecKillCouponItemId();
        SecKillCouponItemDO one = this.lambdaQuery()
                .eq(SecKillCouponItemDO::getId, secKillCouponItemId)
                .one();
        if (one == null) {
            log.error("秒杀优惠券不存在");
            throw new BizException("优惠券不存在");
        }

        boolean update = this.lambdaUpdate()
                .eq(SecKillCouponItemDO::getId, secKillCouponItemId)
                .set(SecKillCouponItemDO::getShelfStatus, 1)
                .update();
        if (!update) {
            log.error("更新失败");
            throw new BizException("更新失败");
        }

        long cacheExpireSeconds = calculateCacheExpireSeconds(one.getEndTime());

        cacheSeckillCouponList(one, cacheExpireSeconds);

        cacheSeckillCouponDetail(one, cacheExpireSeconds);

        cacheSeckillCouponStock(one, cacheExpireSeconds);

    }

    /**
     * 缓存秒杀优惠券列表
     */
    private void cacheSeckillCouponList(SecKillCouponItemDO item,
                                        long cacheExpireSeconds) {
        SecKillCouponItemVO vo = convertToVO(item);
        String data = JsonUtils.toJsonString(vo);
        String keyPrefix = String.format(SECKILL_COUPON_LIST_KEY_PRX);

        RedisKeyGenerator.ALL_DIGIT_SUFFIXES.forEach(digit -> {
            String generateKey = redisKeyGenerator.generateKey(keyPrefix, digit);
            if ( !redissonCacheService.exists(generateKey)) {
                redissonCacheService.addToList(generateKey, data, cacheExpireSeconds, TimeUnit.SECONDS);
            }
        });

    }

    /**
     * 缓存秒杀优惠券详情
     */
    private void cacheSeckillCouponDetail(SecKillCouponItemDO item,
                                         long cacheExpireSeconds) {
        SecKillCouponItemDetailVO detailVO = convertToDetailVO(item);
        String data = JsonUtils.toJsonString(detailVO);
        log.info("添加缓存中的数据:{}", data);
        String keyPrefix = String.format(SECKILL_COUPON_DETAIL_KEY_PRX, item.getId());

        RedisKeyGenerator.ALL_DIGIT_SUFFIXES.forEach(digit -> {
            String generateKey = redisKeyGenerator.generateKey(keyPrefix, digit);
            redissonCacheService.set(generateKey, data, cacheExpireSeconds, TimeUnit.SECONDS);
        });

    }

    /**
     * 缓存秒杀优惠券库存（只存储在一个节点）
     */
    private void cacheSeckillCouponStock(SecKillCouponItemDO item,  long cacheExpireSeconds) {

        String stockKey = String.format(SECKILL_COUPON_STOCK_KEY_PRX, item.getId());

        Integer totalStock = item.getTotalStock();
        if (totalStock == null || totalStock < 0) {
            log.warn("库存数量异常:  secKillCouponItemId={}, totalStock={}"
                    ,item.getId(), totalStock);
            totalStock = 0;
        }

        redissonCacheService.setAtomicLong(stockKey, totalStock, cacheExpireSeconds, TimeUnit.SECONDS);


    }

    /**
     * 根据活动结束时间计算缓存过期时间（秒）
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

    @Override
    public List<SecKillCouponItemVO> seckillCouponItemList() {


        String keyPrefix = String.format(SECKILL_COUPON_LIST_KEY_PRX);
        Integer randomDigit = RedisKeyGenerator.getRandomDigitSuffix();
        String key = redisKeyGenerator.generateKey(keyPrefix, randomDigit);

        List<String> items = redissonCacheService.getListPage(key, 1, 10);
        log.info("获取秒杀优惠券列表: {}", items);
        if (IStringUtils.isEmpty(items)) {
            return Collections.emptyList();
        }

        List<SecKillCouponItemVO> result = new ArrayList<>(items.size());
        for (String itemJson : items) {
            try {
                SecKillCouponItemVO vo = JsonUtils.toObject(itemJson, SecKillCouponItemVO.class);
                result.add(vo);
            } catch (Exception e) {
                log.error("解析秒杀优惠券数据失败: {}", itemJson, e);
            }
        }

        return result;
    }

    @Override
    public SecKillCouponItemDetailVO seckillCouponItemDetailQuery(Long secKillCouponItemId) {

        if (secKillCouponItemId == null) {
            log.warn("优惠券ID不能为空");
            return null;
        }

        String keyPrefix = String.format(SECKILL_COUPON_DETAIL_KEY_PRX, secKillCouponItemId);
        String stockKey = String.format(SECKILL_COUPON_STOCK_KEY_PRX, secKillCouponItemId);

        Integer randomDigit = RedisKeyGenerator.getRandomDigitSuffix();
        String detailKey = redisKeyGenerator.generateKey(keyPrefix, randomDigit);

        try {
            CompletableFuture<String> detailFuture =
                    CompletableFuture.supplyAsync(() ->
                            redissonCacheService.get(detailKey), taskExecutor);

            CompletableFuture<Long> stockFuture =
                    CompletableFuture.supplyAsync(() ->
                            redissonCacheService.getAtomicLong(stockKey), taskExecutor);

            CompletableFuture.allOf(detailFuture, stockFuture)
                    .get(REDIS_QUERY_TIMEOUT_MS, TimeUnit.MILLISECONDS);

            String data = detailFuture.getNow(null);
            long stock = stockFuture.getNow(-1L);

            if (IStringUtils.isEmpty(data)) {
                return null;
            }

            SecKillCouponItemDetailVO vo = JsonUtils.toObject(data, SecKillCouponItemDetailVO.class);
            vo.setStockCount(stock > 0 ? stock : 0);
            return vo;

        } catch (Exception e) {
            log.error("查询优惠券详情异常: secKillCouponItemId={}", secKillCouponItemId, e);
            return null;
        }
    }

    @Override
    public SecKillCouponTokenVO getSecKillCouponToken(GetSecKillCouponTokenDTO dto) {

        Long secKillCouponItemId = dto.getSecKillCouponItemId();
        SecKillCouponItemDetailVO seckillCouponItemDetail = seckillCouponItemDetailQuery(secKillCouponItemId);
        if (seckillCouponItemDetail == null) {
            log.warn("优惠券不存在");
            throw new BizException("系统繁忙,请稍后再试");
        }

        Date startTime = seckillCouponItemDetail.getStartTime();
        Date endTime = seckillCouponItemDetail.getEndTime();
        if (startTime.after(new Date())) {
            log.warn("秒杀活动即将开始");
            throw new BizException("秒杀活动即将开始");
        }
        if (endTime.before(new Date())) {
            log.warn("秒杀活动已结束");
            throw new BizException("秒杀活动已结束");
        }
        Long stockCount = seckillCouponItemDetail.getStockCount();
        if (stockCount <= 0) {
            log.warn("优惠券已售罄");
            throw new BizException("优惠券已售罄");
        }

        Long userId = UserContext.getUserId();

        SeckillCouponUrlConfig.UrlMapping urlMapping = seckillCouponUrlConfig.getUrlMappings().get(0);

        String skillToken = JwtUtils.createSecKillToken(userId, secKillCouponItemId,
                seckillCouponItemDetail.getSecKillMode(), 1);
        String tokenKey = String.format(SECKILL_COUPON_TOKEN_KEY_PRX, userId, secKillCouponItemId);
        redissonCacheService.set(tokenKey, skillToken, 1, TimeUnit.MINUTES);

        SecKillCouponTokenVO vo = new SecKillCouponTokenVO();
        vo.setToken(skillToken);
        vo.setOrderUrl(urlMapping.getPath());
        return vo;
    }

    private SecKillCouponItemVO convertToVO(SecKillCouponItemDO item) {
        SecKillCouponItemVO vo = new SecKillCouponItemVO();
        vo.setSecKillCouponItemId(item.getId());
        vo.setCouponTemplateId(item.getCouponTemplateId());
        vo.setCouponName(item.getCouponName());
        vo.setCouponTitle(item.getCouponTitle());
        vo.setStartTime(item.getStartTime());
        vo.setEndTime(item.getEndTime());
        vo.setTotalStock(item.getTotalStock());
        vo.setRemainingStock(item.getRemainingStock());
        return vo;
    }

    private SecKillCouponItemDetailVO convertToDetailVO(SecKillCouponItemDO item) {
        SecKillCouponItemDetailVO vo = new SecKillCouponItemDetailVO();
        vo.setSecKillCouponItemId(item.getId());
        vo.setCouponTemplateId(item.getCouponTemplateId());
        vo.setCouponName(item.getCouponName());
        vo.setCouponTitle(item.getCouponTitle());
        vo.setSecKillMode(item.getSecKillMode().getCode());
        vo.setStartTime(item.getStartTime());
        vo.setEndTime(item.getEndTime());
        vo.setTotalStock(item.getTotalStock());
        vo.setRemainingStock(item.getRemainingStock());
        vo.setLimitPerUser(item.getLimitPerUser());
        return vo;
    }

    @Override
    public PageResult<SecKillCouponItemPageVO> seckillCouponItemPageQuery(SecKillCouponItemPageQuery query) {
        Integer shelfStatus = query.getShelfStatus();
        long page = query.getPage();
        long pageSize = query.getPageSize();

        Page<SecKillCouponItemDO> pageParam = new Page<>(page, pageSize);

        LambdaQueryWrapper<SecKillCouponItemDO> wrapper = new LambdaQueryWrapper<>();

        if (shelfStatus != null) {
            wrapper.eq(SecKillCouponItemDO::getShelfStatus, shelfStatus);
        }
        wrapper.orderByDesc(SecKillCouponItemDO::getCreateTime);

        Page<SecKillCouponItemDO> resultPage = this.page(pageParam, wrapper);

        List<SecKillCouponItemDO> records = resultPage.getRecords();
        if (records.isEmpty()) {
            return PageResult.emptyResult();
        }

        return new PageResult<>(BeanCopyUtils.copyBeanList(records, SecKillCouponItemPageVO.class), pageSize, resultPage.getTotal());
    }

}
