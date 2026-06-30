package com.lanf.pay.service.reconciliation.excel;

import com.lanf.cache.service.RedissonCacheService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RKeys;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * excel解析进度管理
 */
@Slf4j
@Service
public class ExcelParseProgressManager {


    private static final String CURRENT_ROW_KEY = "excel:parse:current_row:%s:%s";

    @Autowired
    private RedissonCacheService redissonCacheService;

    @Autowired
    private RedissonClient redissonClient;

    /**
     * 是否能够批量保存
     *
     * @return
     */
    public boolean isSaveBath(Integer currentParseCount, String payChannel, String batchId) {

        String key = getKey(payChannel, batchId);

        boolean memberOfSet = redissonCacheService.isMemberOfSet(key, String.valueOf(currentParseCount));

        if (!memberOfSet) {
            log.info("已存储DB中");
            return false;
        }

        return true;


    }

    public String getKey(String payChannel, String batchId) {

        return String.format(CURRENT_ROW_KEY, payChannel, batchId);
    }

    public void  addRows(Integer rows, String payChannel, String batchId){

        redissonCacheService.addToSet(getKey(payChannel, batchId), String.valueOf(rows), 1,
                java.util.concurrent.TimeUnit.DAYS);
    }



    /**
     * 批量删除 excel:parse:current_row: 前缀的 key
     */
    public void deleteAllProgressKeys() {
        String pattern = "excel:parse:current_row:*";
        RKeys keys = redissonClient.getKeys();
        Iterable<String> keyIterator = keys.getKeysByPattern(pattern);
        List<String> keyList = new ArrayList<>();
        for (String key : keyIterator) {
            keyList.add(key);
        }
        if (!keyList.isEmpty()) {
            keys.delete(keyList.toArray(new String[0]));
            log.info("批量删除解析进度key完成，数量: {}", keyList.size());
        }
    }
}
