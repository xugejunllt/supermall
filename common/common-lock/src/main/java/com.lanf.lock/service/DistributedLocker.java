package com.lanf.lock.service;

import org.redisson.api.RLock;

import java.util.concurrent.TimeUnit;


public interface DistributedLocker {


    /***
     * 获取锁：4.尝试获取锁，并且在指定时间内未获取到锁，则放弃获取，给一个租赁时间
     */
    Boolean lock(String lockkey , Long lesstime,Long timeout,TimeUnit timeUtil);


    /**
     * 获取锁，获取失败直接返回 锁时间无限期
     *
     *
     */
    Boolean getLock(String key);

    /**
     * 获取锁，获取失败直接返回 指定锁过期时间
     *
     *
     */
    Boolean getLock(String key, Long lesstime, TimeUnit timeUtil);

    /****
     * 释放锁
     */
    void unlock(String key);

}
