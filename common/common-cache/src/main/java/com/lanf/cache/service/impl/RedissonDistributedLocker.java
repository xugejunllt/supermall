package com.lanf.cache.service.impl;

import com.lanf.cache.service.DistributedLocker;
import com.lanf.common.utils.StackTraceUtil;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class RedissonDistributedLocker implements DistributedLocker {


    @Autowired
    private RedissonClient redissonClient;




    @Override
    public boolean getLock(String key)  {

        try {
            RLock lock = redissonClient.getLock(key);
            return lock.tryLock(0L, TimeUnit.SECONDS);

        } catch (Exception e) {
            log.error("[{}]异常,key[{}],异常堆栈[{}]", "获取分布式锁", key, StackTraceUtil.getStackTrace(e));
            return false;

        }
    }
    @Override
    public void unlock(String key) {

        RLock lock = null;
        try {
            lock = redissonClient.getLock(key);
            lock.unlock();
        } catch (Exception e) {

            log.error("[{}]异常,key[{}],异常堆栈[{}]", "释放分布式锁", key, StackTraceUtil.getStackTrace(e));
        }

    }


}
