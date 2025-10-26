package com.lanf.lock.service.impl;

import com.lanf.lock.service.DistributedLocker;
import lombok.extern.log4j.Log4j2;
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
    public Boolean lock(String key, Long lesstime, Long timeout,TimeUnit timeUtil) {
        try {
            RLock lock = redissonClient.getLock(key);
            return lock.tryLock(timeout, lesstime, timeUtil);
        } catch (InterruptedException e) {
            log.info("获取锁失败:{}", key);
            return false;
        }

    }

    @Override
    public void unlock(String key) {

        log.info("释放锁:{}", key);
        RLock lock = redissonClient.getLock(key);

        lock.unlock();
    }


    @Override
    public Boolean getLock(String key) {


        RLock lock = redissonClient.getLock(key);
        try {

            return lock.tryLock(0L, TimeUnit.SECONDS);

        } catch (InterruptedException e) {

            log.info("获取锁失败:{}", key);
            return false;
        }

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
