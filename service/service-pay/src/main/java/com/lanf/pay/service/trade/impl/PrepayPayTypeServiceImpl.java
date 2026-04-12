package com.lanf.pay.service.trade.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.cache.service.RedissonCacheService;
import com.lanf.pay.mapper.PrepayPayTypeMapper;
import com.lanf.pay.model.entity.PrepayPayTypeDO;
import com.lanf.pay.service.pay.config.PayConfig;
import com.lanf.pay.service.trade.IPrepayPayTypeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PrepayPayTypeServiceImpl extends ServiceImpl<PrepayPayTypeMapper, PrepayPayTypeDO> implements IPrepayPayTypeService {

    @Autowired
    private RedissonCacheService redissonCacheService;

    private static final String PREPAY_PAY_TYPE_CACHE_KEY = "prepay_pay_type:%s";
    private static final int CACHE_EXPIRE_TIME = 10;

    @Autowired
    private PayConfig payConfig;

    /**
     * Redis 缓冲支付类型
     *
     *
     */
    @Override
    public void checkAndSavePrepayPayType(String outTradeNo, Integer payType) {

        String cacheKey = buildPrepayPayTypeCacheKey(outTradeNo);
        String payTypeValue = String.valueOf(payType);

        boolean existsInRedis = checkExistsInRedis(cacheKey, payTypeValue);
        if (existsInRedis) {
            log.debug("预支付类型记录已存在于Redis缓存:outTradeNo={},payType={}", outTradeNo, payType);
            return;
        }

        PrepayPayTypeDO existRecord = queryPrepayPayTypeFromDB(outTradeNo, payType);
        if (existRecord != null) {
            log.debug("预支付类型记录已存在于数据库:outTradeNo={},payType={}", outTradeNo, payType);
            saveToRedisCache(cacheKey, payTypeValue);
            return;
        }

        createPrepayPayTypeRecord(outTradeNo, payType);
        saveToRedisCache(cacheKey, payTypeValue);
        log.info("创建新的预支付类型记录:outTradeNo={},payType={}", outTradeNo, payType);
    }

    @Override
    public List<String> getPrepayPayTypesByOutTradeNo(String outTradeNo) {

        List<String> cachedPayTypes = getPayTypesFromRedisCache(outTradeNo);
        if (cachedPayTypes != null && !cachedPayTypes.isEmpty()) {
            log.debug("从Redis缓存获取预支付类型成功:outTradeNo={},count={}", outTradeNo, cachedPayTypes.size());
            return cachedPayTypes;
        }

        log.debug("Redis缓存未命中，从数据库加载:outTradeNo={}", outTradeNo);
        List<String> dbPayTypes = queryPayTypesFromDB(outTradeNo);
        
        if (dbPayTypes != null && !dbPayTypes.isEmpty()) {
            log.debug("从数据库加载预支付类型成功:outTradeNo={},count={}", outTradeNo, dbPayTypes.size());
            savePayTypesToRedisCache(outTradeNo, dbPayTypes);
        } else {
            log.debug("数据库中未找到预支付类型记录:outTradeNo={}", outTradeNo);
        }

        return dbPayTypes;
    }

    private String buildPrepayPayTypeCacheKey(String outTradeNo) {

        return String.format(PREPAY_PAY_TYPE_CACHE_KEY, outTradeNo);
    }

    private boolean checkExistsInRedis(String cacheKey, String payTypeValue) {

        return redissonCacheService.isMemberOfSet(cacheKey, payTypeValue);
    }

    private PrepayPayTypeDO queryPrepayPayTypeFromDB(String outTradeNo, Integer payType) {

        return this.lambdaQuery()
                .eq(PrepayPayTypeDO::getOutTradeNo, outTradeNo)
                .eq(PrepayPayTypeDO::getPayType, String.valueOf(payType))
                .one();
    }

    private PrepayPayTypeDO createPrepayPayTypeRecord(String outTradeNo, Integer payType) {

        PrepayPayTypeDO record = new PrepayPayTypeDO();
        record.setOutTradeNo(outTradeNo);
        record.setPayType(String.valueOf(payType));
        
        try {
            this.save(record);
            log.info("保存预支付类型记录到数据库成功:outTradeNo={},payType={}", outTradeNo, payType);
        } catch (DuplicateKeyException e) {
            log.info("预支付类型记录已存在（并发插入）:outTradeNo={},payType={}", outTradeNo, payType);
            return queryPrepayPayTypeFromDB(outTradeNo, payType);
        }
        
        return record;
    }

    private void saveToRedisCache(String cacheKey, String payTypeValue) {

        redissonCacheService.addToSet(cacheKey, payTypeValue, CACHE_EXPIRE_TIME, TimeUnit.MINUTES);
        log.debug("保存预支付类型到Redis Set缓存:key={},payType={}", cacheKey, payTypeValue);
    }

    private List<String> getPayTypesFromRedisCache(String outTradeNo) {

        String cacheKey = buildPrepayPayTypeCacheKey(outTradeNo);
        Set<String> payTypeSet = redissonCacheService.getSetMembers(cacheKey);
        
        if (payTypeSet == null || payTypeSet.isEmpty()) {
            return null;
        }

        return new ArrayList<>(payTypeSet);
    }

    private List<String> queryPayTypesFromDB(String outTradeNo) {

        List<PrepayPayTypeDO> records = this.lambdaQuery()
                .eq(PrepayPayTypeDO::getOutTradeNo, outTradeNo)
                .select(PrepayPayTypeDO::getPayType)
                .list();
        
        if (records == null || records.isEmpty()) {
            return null;
        }

        return records.stream()
                .map(PrepayPayTypeDO::getPayType)
                .collect(Collectors.toList());
    }

    private void savePayTypesToRedisCache(String outTradeNo, List<String> payTypes) {

        String cacheKey = buildPrepayPayTypeCacheKey(outTradeNo);
        
        redissonCacheService.addAllToSet(cacheKey, payTypes, CACHE_EXPIRE_TIME, TimeUnit.MINUTES);
        
        log.debug("批量保存预支付类型到Redis Set缓存:outTradeNo={},count={}", outTradeNo, payTypes.size());
    }

}
