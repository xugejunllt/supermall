package com.lanf.user.service.manager;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.lanf.common.utils.JsonUtils;
import com.lanf.redis.constant.CacheConstants;
import com.lanf.redis.service.RedisCache;
import com.lanf.user.model.vo.AddressVO;
import com.sun.xml.internal.ws.util.UtilException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
@Slf4j
@Component
public  class  AddersCache{


    @Autowired
    private RedisCache redisCache;


    public void  addCache(Long userId, List<AddressVO> addressVOList){

        String key = CacheConstants.getADDRESS(userId);

        String addressJson = JsonUtils.toJsonString(addressVOList);
        redisCache.setCacheObject(key,addressJson,CacheConstants.ADDRESS_TIME);

    }

    public void removeCache(Long userId){

        String key = CacheConstants.getADDRESS(userId);
        redisCache.deleteObject(key);

    }
    public List<AddressVO> getCache (Long userId){

        String key = CacheConstants.getADDRESS(userId);
        String cacheObject = redisCache.getCacheObject(key);

        if (cacheObject == null ){

            return null;
        }

        List<AddressVO> list = null;
        try {
            list = JsonUtils.toList(cacheObject, AddressVO.class);
        } catch (UtilException e) {
            //如果反序列化失败 返回null 进行降级 依然从数据库中查询
           return null;
        }
        return  list;


    }


}