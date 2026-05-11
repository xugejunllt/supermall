package com.lanf.web.security.keygen;


import com.alibaba.fastjson.JSON;
import com.lanf.cache.service.RedissonCacheService;
import com.lanf.common.utils.JsonUtils;
import com.lanf.web.security.keygen.model.IKeyPairInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author: Jarven
 * @date: 2026-02-25 13:40
 * @description:
 */

@Slf4j
public abstract class AbstractKeyManagerService implements KeyManagerService {


    @Qualifier("keyGenExecutor")
    @Autowired
    private Executor executor;
    @Autowired
    private RedissonCacheService redissonCacheService;

    @Autowired
    private KeyConfig keyConfig;
    private final ReentrantLock lock = new ReentrantLock();
    private final ReentrantLock fullLock = new ReentrantLock();

    /**
     * 激活的密钥生成任务数
     */
    private final AtomicInteger activeTaskCount = new AtomicInteger(0);


    /**
     * 是否启动生成密钥任务
     */

    public boolean canStart() {

        /**
         * Redis 队列密钥数 小于 最大密钥数 且 激活任务数 小于 线程数 返回true
         */

        int queueSize = redissonCacheService.getQueueSize(getRedisQueueKey());
        log.info("当前队列秘钥数[{}],预计生成[{}]", queueSize, keyConfig.getMaxPoolSize() -
                queueSize);
        return queueSize <= keyConfig.getMaxPoolSize() && !isFullActiveTaskCount();
    }

    /**
     * 队列数是否已经满了
     */
    private boolean isFullQueue() {
        int queueSize = redissonCacheService.getQueueSize(getRedisQueueKey());
        return queueSize >= keyConfig.getMaxPoolSize();
    }

    /**
     * 激活的密钥生成任务数是否已经满了
     */
    private boolean isFullActiveTaskCount() {


        return activeTaskCount.get() >= keyConfig.getThreadCount();
    }


    /**
     * 开启密钥生成任务
     */
    public void startKeyGen() {

        for (int i = 0; i < keyConfig.getThreadCount(); i++) {
            lock.lock();
            try {
                if (!isFullActiveTaskCount()) {
                    activeTaskCount.incrementAndGet();
                    doStartKeyGen();
                }

            } finally {
                lock.unlock();
            }
        }

    }

    private void doStartKeyGen() {
        KeyGenTask task = new KeyGenTask();
        executor.execute(task);
    }



    @Override
    public IKeyPairInfo findKeyPairInfo() {

        int getMinIndex = keyConfig.getNumAvailableIntervals() - 1;
        int getMaxIndex = keyConfig.getMaxPoolSize() - 1;
        Random random = new Random();
        int randomIndex = random.nextInt(getMaxIndex - getMinIndex + 1) + getMinIndex;
        /**
         * 即使下标越界 返回null
         */
        List<String> list = redissonCacheService.getListPage(getRedisQueueKey(), 1, keyConfig.getMaxPoolSize());
        String poll = list != null && randomIndex < list.size() ? list.get(randomIndex) : null;
        if (poll != null) {
            return JsonUtils.toObject(poll, IKeyPairInfo.class);
        }
        log.error("从队列获取秘钥失败,开始降级本地生成,randomIndex[{}],dequeSize[{}]", randomIndex, list != null ? list.size() : 0);

        return generateKeyPair();

    }

    protected abstract  String getRedisQueueKey();



    public void scheduleKeyGenTask() {


        int batchSize = keyConfig.getNewKeysGenerated();
        List<String> keyPairInfoList = batchKeyGen(batchSize);
        redissonCacheService.addToDequeAll(getRedisQueueKey(), keyPairInfoList);
        /**
         * 从头部删除
         */
        redissonCacheService.pollFirstFromDeque(getRedisQueueKey(), batchSize);

    }


    private List<String> batchKeyGen(int batchSize) {

        List<String> keyPairInfoList = new ArrayList<>(batchSize);
        log.info("开始生成密钥");
        long start = System.currentTimeMillis();
        for (int i = 0; i < batchSize; i++) {
            IKeyPairInfo keyPairInfo2 = generateKeyPair();
            keyPairInfoList.add(JSON.toJSONString(keyPairInfo2));
        }
        log.info("生成密钥完成,耗时:{}ms,数量:{}", System.currentTimeMillis() - start, batchSize);
        return keyPairInfoList;
    }

    protected abstract  IKeyPairInfo generateKeyPair();

    class KeyGenTask implements Runnable {

        @Override
        public void run() {

            while (!isFullQueue()) {
                int batchSize = keyConfig.getBatchSize();
                List<String> keyPairInfoList = batchKeyGen(batchSize);

                fullLock.lock();
                try {
                    if (!isFullQueue()){
                        redissonCacheService.addToDequeAll(getRedisQueueKey(), keyPairInfoList);
                    }
                } finally {
                    fullLock.unlock();
                }

            }
            activeTaskCount.decrementAndGet();
            log.info("定时任务初始化批量生成密钥结束");


        }
    }


}
