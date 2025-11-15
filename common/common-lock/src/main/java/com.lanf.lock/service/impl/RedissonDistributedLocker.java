package com.lanf.lock.service.impl;

import com.lanf.common.utils.StackTraceUtil;
import com.lanf.constant.exception.IRedisException;
import com.lanf.lock.service.DistributedLocker;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.client.RedisException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class RedissonDistributedLocker implements DistributedLocker {


    @Autowired
    private RedissonClient redissonClient;


    @Override
    public Boolean lock(String key, Long lesstime, Long timeout, TimeUnit timeUtil) {
        try {
            RLock lock = redissonClient.getLock(key);
            return lock.tryLock(timeout, lesstime, timeUtil);
        } catch (InterruptedException e) {
            log.info("获取锁失败:{}", key);
            return false;
        }

    }

    @Override
    public void unlock(String key) throws IRedisException {

        RLock lock = null;
        try {
            lock = redissonClient.getLock(key);
            lock.unlock();
        } catch (Exception e) {

            log.error("[{}]异常,key[{}],异常堆栈[{}]", "释放分布式锁",key, StackTraceUtil.getStackTrace(e));
            throw new RedisException();
        }

    }


    @Override
    public Boolean getLock(String key) throws IRedisException {

        boolean tryLock = false;
        try {
            RLock lock = redissonClient.getLock(key);
            tryLock = lock.tryLock(0L, TimeUnit.SECONDS);

        } catch (Exception e) {
            log.error("[{}]异常,key[{}],异常堆栈[{}]", "获取分布式锁", key,StackTraceUtil.getStackTrace(e));
            throw new RedisException();

        }

        return tryLock;
    }

    @Override
    public Boolean getLock(String key, Long lesstime, TimeUnit timeUtil) {
        try {
            RLock lock = redissonClient.getLock(key);

            return lock.tryLock(0L, lesstime, timeUtil);

        } catch (InterruptedException e) {
            log.info("获取锁失败:{}", key);
            return false;
        }

    }


}
