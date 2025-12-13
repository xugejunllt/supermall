package com.lanf.lock.service;


public interface DistributedLocker {





    /**
     * 获取锁，获取失败直接返回 锁时间无限期
     *
     *
     */
    boolean getLock(String key);



    /****
     * 释放锁
     */
    void unlock(String key);

}
