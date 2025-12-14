package com.lanf.redis.service;

import com.lanf.common.utils.StackTraceUtil;
import com.lanf.constant.exception.BizException;
import com.lanf.constant.exception.IRedisException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * spring redis 工具类
 *
 * @author ruoyi
 **/
@SuppressWarnings(value = {"unchecked", "rawtypes"})
@Component
@Slf4j
public class RedisCache {
    @Autowired
    public RedisTemplate redisTemplate;

    /**
     * 缓存基本的对象，Integer、String、实体类等
     *
     * @param key   缓存的键值
     * @param value 缓存的值
     */
    public <T> void setCacheObject(final String key, final T value) {
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * 缓存基本的对象
     *
     * @param key      缓存的键值
     * @param value    缓存的值
     * @param timeout  时间 默认分钟
     * @param
     */
    public  void setCacheObject(final String key, final String value, final long timeout) throws IRedisException {

        TimeUnit minutes = TimeUnit.MINUTES;
        try {

            redisTemplate.opsForValue().set(key, value, timeout, minutes);
        } catch (Exception e) {
            log.error("添加Redis缓存失败,key[{}],value[{}],timeout[{}],timeUnit[{}],异常堆栈[{}]",key,
                    value, timeout, minutes, StackTraceUtil.getStackTrace(e));
            throw new IRedisException();
        }
    }
    /**
     * 获得缓存的基本对象。
     *
     * @param key 缓存键值
     * @return 缓存键值对应的数据
     */
    public String getCacheObject(final String key) throws IRedisException {

        try {
            ValueOperations<String, String> operation = redisTemplate.opsForValue();
            return operation.get(key);

        } catch (Exception e) {

            log.error("获取缓存失败,key[{}],异常堆栈[{}]",key,
                     StackTraceUtil.getStackTrace(e));
            throw new IRedisException();
        }

    }


    /**
     * 设置有效时间
     *
     * @param key     Redis键
     * @param timeout 超时时间
     * @return true=设置成功；false=设置失败
     */
    public Boolean expire(final String key, final long timeout) {

        TimeUnit minutes = TimeUnit.MINUTES;

        try {
            Boolean expire = redisTemplate.expire(key, timeout, minutes);
            if (Boolean.FALSE.equals(expire)) {
                //告警 人工处理
                log.error("设置过期时间失败,key[{}],timeout[{}],timeUnit[{}]",key,
                        timeout, minutes);
            }
            return expire;
        } catch (Exception e) {
            log.error("设置过期时间失败,key[{}],timeout[{}],timeUnit[{}],异常堆栈",key,
                    timeout, minutes,e);
            throw new IRedisException();
        }

    }

    /**
     * 获取有效时间
     *
     * @param key Redis键
     * @return 有效时间
     */
    public long getExpire(final String key) {
        return redisTemplate.getExpire(key);
    }

    /**
     * 判断 key是否存在
     *
     * @param key 键
     * @return true 存在 false不存在
     */
    public Boolean hasKey(String key) {

        Boolean hasKey = null;
        try {
            hasKey = redisTemplate.hasKey(key);
        } catch (Exception e) {

            log.error("判断key失败key[{}],异常堆栈[{}]",key, StackTraceUtil.getStackTrace(e));
            throw new IRedisException("判断key失败");
        }
        return hasKey;

    }



    /**
     * 删除单个对象
     *
     * @param key
     */
    public void deleteObject(final String key) {

        Boolean delete = null;
        try {
            delete = redisTemplate.delete(key);
            if (Boolean.FALSE.equals(delete)) {
                //告警 人工处理
                log.error("删除key失败,key[{}]",key);
            }
        } catch (Exception e) {
            log.error("删除key失败,异常堆栈[{}]", StackTraceUtil.getStackTrace(e));
            throw new IRedisException("删除key失败");
        }

    }


    /**
     * 缓存List数据
     *
     * @param key      缓存的键值
     * @param dataList 待缓存的List数据
     * @return 缓存的对象
     */
    public <T> long setCacheList(final String key, final List<T> dataList) {
        Long count = redisTemplate.opsForList().rightPushAll(key, dataList);
        return count == null ? 0 : count;
    }

    /**
     * 获得缓存的list对象
     *
     * @param key 缓存的键值
     * @return 缓存键值对应的数据
     */
    public <T> List<T> getCacheList(final String key) {
        return redisTemplate.opsForList().range(key, 0, -1);
    }

    /**
     * 缓存Set
     *
     * @param key     缓存键值
     * @param dataSet 缓存的数据
     * @return 缓存数据的对象
     */
    public <T> void setCacheSet(final String key, final Set<T> dataSet) {


        try {
            BoundSetOperations<String, T> setOperation = redisTemplate.boundSetOps(key);
            setOperation.add((T) dataSet.toArray());
        } catch (Exception e) {
            log.error("添加Set缓存失败,key[{}],set[{}]",key,dataSet, e);
            throw new IRedisException("添加Set缓存失败");
        }

    }

    public Long increment(String key){

        Long increment = null;
        try {
            increment = redisTemplate.opsForValue().increment(key);
        } catch (Exception e) {
            log.error("递增key失败key[{}],异常堆栈[{}]",key, StackTraceUtil.getStackTrace(e));
            throw new IRedisException("递增key失败key");
        }

        return increment;
    }
    /**
     * 获得缓存的set
     *
     * @param key
     * @return
     */
    public <T> Set<T> getCacheSet(final String key) {

        try {
            return redisTemplate.opsForSet().members(key);

        } catch (Exception e) {
            log.error("获取set失败,key[{}],异常堆栈",key, e);
            throw new BizException("获取set失败");
        }

    }

    /**
     * 缓存Map
     *
     * @param key
     * @param dataMap
     */
    public <T> void setCacheMap(final String key, final Map<String, T> dataMap) {

        try {
            redisTemplate.opsForHash().putAll(key, dataMap);
        } catch (Exception e) {
            log.error("添加map缓存失败,key[{}],map[{}]",key,dataMap, e);
            throw new IRedisException("添加map缓存失败");
        }

    }

    /**
     * 获得缓存的Map
     *
     * @param key
     * @return
     */
    public <T> Map<String, T> getCacheMap(final String key) {
        try {
            return redisTemplate.opsForHash().entries(key);
        } catch (Exception e) {
           log.error("获取map失败,key[{}],异常堆栈",key, e);
           throw new IRedisException("获取map失败");
        }
    }

    /**
     * 往Hash中存入数据
     *
     * @param key   Redis键
     * @param hKey  Hash键
     * @param value 值
     */
    public <T> void setCacheMapValue(final String key, final String hKey, final T value) {
        redisTemplate.opsForHash().put(key, hKey, value);
    }

    /**
     * 获取Hash中的数据
     *
     * @param key  Redis键
     * @param hKey Hash键
     * @return Hash中的对象
     */
    public <T> T getCacheMapValue(final String key, final String hKey) {
        HashOperations<String, String, T> opsForHash = null;
        try {
            opsForHash = redisTemplate.opsForHash();
        } catch (Exception e) {
            log.error("获取Hash数据失败,key[{}],hKey[{}]",key,hKey, e);
            throw new IRedisException("获取Hash数据失败");
        }
        return opsForHash.get(key, hKey);
    }

    /**
     * 获取多个Hash中的数据
     *
     * @param key   Redis键
     * @param hKeys Hash键集合
     * @return Hash对象集合
     */
    public <T> List<T> getMultiCacheMapValue(final String key, final Collection<Object> hKeys) {
        return redisTemplate.opsForHash().multiGet(key, hKeys);
    }

    /**
     * 删除Hash中的某条数据
     *
     * @param key  Redis键
     * @param hKey Hash键
     * @return 是否成功
     */
    public boolean deleteCacheMapValue(final String key, final String hKey) {
        return redisTemplate.opsForHash().delete(key, hKey) > 0;
    }

    /**
     * 获取递增数 每次+1
     */
    public Long getInc(final String key, final long add) {


        return redisTemplate.opsForValue().increment(key, add);
    }

}
