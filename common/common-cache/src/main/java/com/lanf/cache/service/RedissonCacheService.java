package com.lanf.cache.service;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class RedissonCacheService {

    @Autowired
    private RedissonClient redissonClient;



    public RBuckets getBuckets(){

     return redissonClient.getBuckets();
    }

    public  void set(String key, String value, long expireTime, TimeUnit timeUnit) {
        try {
            RBucket<String> bucket = redissonClient.getBucket(key);

            if (expireTime > 0) {
                bucket.set(value, expireTime, timeUnit);
                log.debug("设置缓存:key={},expire={}{}", key, expireTime, timeUnit);
            } else {
                bucket.set(value);
                log.debug("设置缓存(无过期):key={}", key);
            }
        } catch (Exception e) {
            log.error("设置缓存异常:key={}", key, e);
        }
    }

    public String  get(String key) {
        
        try {
            RBucket<String> bucket = redissonClient.getBucket(key);
            String value = bucket.get();
            
            if (value != null) {
                log.debug("缓存命中:key={}", key);
            } else {
                log.debug("缓存未命中:key={}", key);
            }
            return value;
        } catch (Exception e) {
            log.error("获取缓存异常:key={}", key, e);
            return null;
        }
    }


    public void delete(String key) {
        
        try {
            boolean deleted = redissonClient.getBucket(key).delete();
            if (deleted) {
                log.debug("删除缓存成功:key={}", key);
            }
        } catch (Exception e) {
            log.error("删除缓存异常:key={}", key, e);

        }
    }

    public void addToSet(String key, String value, long expireTime, TimeUnit timeUnit) {
        
        try {
            RSet<String> set = redissonClient.getSet(key);
            set.add(value);
            
            if (expireTime > 0) {
                set.expire(expireTime, timeUnit);
                log.debug("添加到Set缓存:key={},value={},expire={}{}", key, value, expireTime, timeUnit);
            } else {
                log.debug("添加到Set缓存(无过期):key={},value={}", key, value);
            }
        } catch (Exception e) {
            log.error("添加到Set缓存异常:key={},value={}", key, value, e);
        }
    }

    public Set<String> getSetMembers(String key) {
        
        try {
            RSet<String> set = redissonClient.getSet(key);
            Set<String> members = set.readAll();
            
            if (members != null && !members.isEmpty()) {
                log.debug("获取Set缓存命中:key={},size={}", key, members.size());
            } else {
                log.debug("获取Set缓存未命中:key={}", key);
            }
            return members;
        } catch (Exception e) {
            log.error("获取Set缓存异常:key={}", key, e);
            return null;
        }
    }

    public boolean isMemberOfSet(String key, String value) {
        
        try {
            RSet<String> set = redissonClient.getSet(key);
            boolean isMember = set.contains(value);
            
            if (isMember) {
                log.debug("Set缓存成员存在:key={},value={}", key, value);
            } else {
                log.debug("Set缓存成员不存在:key={},value={}", key, value);
            }
            return isMember;
        } catch (Exception e) {
            log.error("检查Set缓存成员异常:key={},value={}", key, value, e);
            return false;
        }
    }

    public void addAllToSet(String key, java.util.Collection<String> values, long expireTime, TimeUnit timeUnit) {
        
        try {
            if (values == null || values.isEmpty()) {
                log.debug("批量添加到Set缓存：集合为空,key={}", key);
                return;
            }
            
            RSet<String> set = redissonClient.getSet(key);
            set.addAll(values);
            
            if (expireTime > 0) {
                set.expire(expireTime, timeUnit);
                log.debug("批量添加到Set缓存成功:key={},count={},expire={}{}", key, values.size(), expireTime, timeUnit);
            } else {
                log.debug("批量添加到Set缓存成功(无过期):key={},count={}", key, values.size());
            }
        } catch (Exception e) {
            log.error("批量添加到Set缓存异常:key={},count={}", key, values.size(), e);
        }
    }

    public void addToList(String key, String value, long expireTime, TimeUnit timeUnit) {
        
        try {
            RList<String> list = redissonClient.getList(key);
            list.add(value);
            
            if (expireTime > 0) {
                list.expire(expireTime, timeUnit);
                log.debug("添加到List缓存:key={},value={},expire={}{}", key, value, expireTime, timeUnit);
            } else {
                log.debug("添加到List缓存(无过期):key={},value={}", key, value);
            }
        } catch (Exception e) {
            log.error("添加到List缓存异常:key={},value={}", key, value, e);
        }
    }

    public void deleteFromList(String key, String value) {
        
        try {
            RList<String> list = redissonClient.getList(key);
            boolean removed = list.remove(value);
            
            if (removed) {
                log.debug("从List缓存删除成功:key={},value={}", key, value);
            } else {
                log.debug("从List缓存删除失败(元素不存在):key={},value={}", key, value);
            }
        } catch (Exception e) {
            log.error("从List缓存删除异常:key={},value={}", key, value, e);
        }
    }

    public List<String> getListPage(String key, int pageNum, int pageSize) {
        
        try {
            RList<String> list = redissonClient.getList(key);
            int size = list.size();
            
            if (size == 0) {
                log.debug("List缓存为空:key={}", key);
                return java.util.Collections.emptyList();
            }
            
            int fromIndex = (pageNum - 1) * pageSize;
            int toIndex = Math.min(fromIndex + pageSize, size);
            
            if (fromIndex >= size) {
                log.debug("List缓存分页超出范围:key={},pageNum={},pageSize={},totalSize={}", key, pageNum, pageSize, size);
                return java.util.Collections.emptyList();
            }
            
            List<String> pageData = list.subList(fromIndex, toIndex);
            log.debug("List缓存分页查询:key={},pageNum={},pageSize={},resultSize={},totalSize={}", 
                    key, pageNum, pageSize, pageData.size(), size);
            return pageData;
        } catch (Exception e) {
            log.error("List缓存分页查询异常:key={},pageNum={},pageSize={}", key, pageNum, pageSize, e);
            return java.util.Collections.emptyList();
        }
    }

    /**
     * 设置 AtomicLong 初始值
     * 
     * @param key Redis key
     * @param initialValue 初始值
     * @param expireTime 过期时间（秒），0表示不过期
     * @param timeUnit 时间单位
     */
    public void setAtomicLong(String key, long initialValue, long expireTime, TimeUnit timeUnit) {
        try {
            RAtomicLong atomicLong = redissonClient.getAtomicLong(key);
            atomicLong.set(initialValue);
            
            if (expireTime > 0) {
                atomicLong.expire(expireTime, timeUnit);
                log.info("设置AtomicLong初始值:key={},value={},expire={}{}", key, initialValue, expireTime, timeUnit);
            } else {
                log.info("设置AtomicLong初始值(无过期):key={},value={}", key, initialValue);
            }
        } catch (Exception e) {
            log.error("设置AtomicLong初始值异常:key={},value={}", key, initialValue, e);
        }
    }

    /**
     * 原子递增（+1）
     * 
     * @param key Redis key
     * @return 递增后的值
     */
    public long incrementAndGet(String key,long expireTime, TimeUnit timeUnit) {

        try {
            RAtomicLong atomicLong = redissonClient.getAtomicLong(key);
            long newValue = atomicLong.incrementAndGet();
            if (newValue == 1) {
                //第一次初始化
                atomicLong.expire(expireTime, timeUnit);
            }
            log.debug("AtomicLong递增:key={},newValue={}", key, newValue);
            return newValue;
        } catch (Exception e) {
            log.error("AtomicLong递增异常:key={}", key, e);
            return -1L;
        }
    }
    public long incrementGet(String key) {

        try {
            RAtomicLong atomicLong = redissonClient.getAtomicLong(key);
            long newValue = atomicLong.get();
            log.debug("AtomicLong递增:key={},newValue={}", key, newValue);
            return newValue;
        } catch (Exception e) {
            log.error("AtomicLong递增异常:key={}", key, e);
            return -1L;
        }
    }
    /**
     * 原子递增指定值
     * 
     * @param key Redis key
     * @param delta 递增值
     * @return 递增后的值
     */
    public long incrementAndGet(String key, long delta) {
        try {
            RAtomicLong atomicLong = redissonClient.getAtomicLong(key);
            long newValue = atomicLong.addAndGet(delta);
            log.debug("AtomicLong递增:key={},delta={},newValue={}", key, delta, newValue);
            return newValue;
        } catch (Exception e) {
            log.error("AtomicLong递增异常:key={},delta={}", key, delta, e);
            return -1L;
        }
    }

    /**
     * 原子递减（-1）
     *
     * 当 key 不存在时，Redisson 会：
     * 自动创建该 key
     * 初始值设为 0
     * 执行递减操作：0 - 1 = -1
     * 返回 -1
     * @param key Redis key
     * @return 递减后的值
     */
    public long decrementAndGet(String key) {
        try {
            RAtomicLong atomicLong = redissonClient.getAtomicLong(key);
            long newValue = atomicLong.decrementAndGet();
            if (newValue == -1) {
                /**
                 * 与redis异常返回值区分开来
                 */
                return -2;
            }
            log.debug("AtomicLong递减:key={},newValue={}", key, newValue);
            return newValue;
        } catch (Exception e) {
            log.error("AtomicLong递减异常:key={}", key, e);
            return -1L;
        }
    }

    /**
     * 原子递减指定值
     * 
     * @param key Redis key
     * @param delta 递减值
     * @return 递减后的值
     */
    public long decrementAndGet(String key, long delta) {
        try {
            RAtomicLong atomicLong = redissonClient.getAtomicLong(key);
            long newValue = atomicLong.addAndGet(-delta);
            log.debug("AtomicLong递减:key={},delta={},newValue={}", key, delta, newValue);
            return newValue;
        } catch (Exception e) {
            log.error("AtomicLong递减异常:key={},delta={}", key, delta, e);
            return -1L;
        }
    }

    /**
     * 获取当前值
     * 
     * @param key Redis key
     * @return 当前值，如果不存在返回 -1
     */
    public long getAtomicLong(String key) {
        try {
            RAtomicLong atomicLong = redissonClient.getAtomicLong(key);
            if (!atomicLong.isExists()) {
                log.debug("AtomicLong不存在:key={}", key);
                return -1L;
            }
            long value = atomicLong.get();
            log.debug("获取AtomicLong值:key={},value={}", key, value);
            return value;
        } catch (Exception e) {
            log.error("获取AtomicLong值异常:key={}", key, e);
            return -1L;
        }
    }

    /**
     * 比较并设置（CAS操作）
     * 
     * @param key Redis key
     * @param expectedValue 期望值
     * @param newValue 新值
     * @return 是否设置成功
     */
    public boolean compareAndSet(String key, long expectedValue, long newValue) {
        try {
            RAtomicLong atomicLong = redissonClient.getAtomicLong(key);
            boolean success = atomicLong.compareAndSet(expectedValue, newValue);
            log.debug("AtomicLong CAS操作:key={},expected={},new={},success={}", 
                    key, expectedValue, newValue, success);
            return success;
        } catch (Exception e) {
            log.error("AtomicLong CAS操作异常:key={},expected={},new={}", key, expectedValue, newValue, e);
            return false;
        }
    }

    /**
     * 删除 AtomicLong
     * 
     * @param key Redis key
     * @return 是否删除成功
     */
    public boolean deleteAtomicLong(String key) {
        try {
            RAtomicLong atomicLong = redissonClient.getAtomicLong(key);
            boolean deleted = atomicLong.delete();
            if (deleted) {
                log.debug("删除AtomicLong成功:key={}", key);
            }
            return deleted;
        } catch (Exception e) {
            log.error("删除AtomicLong异常:key={}", key, e);
            return false;
        }
    }





}
