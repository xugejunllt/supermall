package com.lanf.pay.service.reconciliation;

import com.lanf.api.pay.model.enums.PayChannelEnum;
import com.lanf.cache.service.RedissonCacheService;
import com.lanf.common.utils.IStringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 下载账单进度管理
 */
@Slf4j
@Component
public class ReconciliationCalculator {

    @Autowired
    private RedissonCacheService redissonCacheService;
    /**
     * 参与对账的支付渠道
     */
    private Set<PayChannelEnum> availableChannels = PayChannelEnum.AVAILABLE_CHANNELS;


    private static final String BILL_DOWNLOAD_PROGRESS_KEY = "bill:download:progress:%s";
    private static final long CACHE_EXPIRE_TIME = 24L;

    /**
     * 标记渠道对账单下载完成
     *
     * @param billDate 账单日期 格式 yyyy-MM-dd
     * @param channel 支付渠道编码
     */
    public void markChannelComplete(String billDate, String channel) {
        String cacheKey = String.format(BILL_DOWNLOAD_PROGRESS_KEY, billDate);
        
        redissonCacheService.addToSet(cacheKey, channel, CACHE_EXPIRE_TIME, TimeUnit.HOURS);

    }

    /**
     * 获取指定日期已完成的渠道列表
     *
     * @param billDate 账单日期
     * @return 已完成的渠道编码集合
     */
    public Set<String> getCompletedChannels(String billDate) {
        String cacheKey = String.format(BILL_DOWNLOAD_PROGRESS_KEY, billDate);
        return redissonCacheService.getSetMembers(cacheKey);
    }

    /**
     * 检查指定日期的所有渠道是否都已完成
     *
     * @param billDate 账单日期
     * @return 是否全部完成
     */
    public boolean isAllChannelsComplete(String billDate) {
        Set<String> completedChannels = getCompletedChannels(billDate);
        
        if (IStringUtils.isEmpty(completedChannels)) {
            return false;
        }

        boolean isComplete = completedChannels.size() >= availableChannels.size();
        
        if (isComplete) {
            log.info("所有渠道对账单下载完成");
        }
        
        return isComplete;
    }

}
