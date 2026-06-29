package com.lanf.pay.service.reconciliation.strategy;

import com.lanf.cache.service.RedissonCacheService;
import com.lanf.pay.model.enums.ReconciliationBusinessTypeEnum;
import com.lanf.pay.model.enums.ReconciliationDiffTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 批量对账，并且在每完成一笔对账后，将当前处理到的最大账单ID写入Redis（用于记录进度、断点续传等）
 */
@Slf4j
@Service
public class MaxIdTrackingBatchReconciler {

    //batchId : diffType: businessType
    private static final String CURRENT_MAX_ID_KEY = "reconciliantion:%s:%s:%s";

    @Autowired
    private RedissonCacheService redissonCacheService;

    /**
     * 是否能够批量保存
     *
     * @return
     */
    public boolean isSaveBath(String batchId, ReconciliationDiffTypeEnum diffType,
                              ReconciliationBusinessTypeEnum businessType,Long bathMaxId) {

        String key = getKey( batchId,  diffType, businessType);

        boolean memberOfSet = redissonCacheService.isMemberOfSet(key, String.valueOf(bathMaxId));

        if (memberOfSet) {
            log.info("已存储DB中");
            return true;
        }

        return false;


    }

    private String getKey(String batchId, ReconciliationDiffTypeEnum diffType,
                         ReconciliationBusinessTypeEnum businessType) {

        return String.format(CURRENT_MAX_ID_KEY, batchId, diffType.getCode(), businessType.getCode());
    }

    public void  addMaxId(String batchId, ReconciliationDiffTypeEnum diffType,
                         ReconciliationBusinessTypeEnum businessType,Long bathMaxId){
        String key = getKey( batchId,  diffType, businessType);

        redissonCacheService.addToSet(key, String.valueOf(bathMaxId), 1,
                java.util.concurrent.TimeUnit.DAYS);
    }
}
