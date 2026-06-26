package com.lanf.api.user.api;

import com.lanf.api.user.model.vo.AddressListVO;
import com.lanf.cache.service.RedissonCacheService;
import com.lanf.common.utils.IStringUtils;
import com.lanf.common.utils.JsonUtils;
import com.lanf.constant.result.Result;
import com.lanf.constant.result.RpcResultParser;
import com.lanf.constant.utils.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static com.lanf.constant.constant.RedisKeyConstants.ADDRESS_CACHE_KEY_PREFIX;
@Slf4j
@Service
public class UserCacheService {
    /**
     * 懒加载 否则空指针
     */

    @Autowired(required = false)    private UserApiService userApiService;

    @Autowired
    private RedissonCacheService redissonCacheService;



    /**
     * 获取地址列表（带缓存）
     */
    public Result<List<AddressListVO>> addressListQuery() {
        // 1. 构建缓存 Key
        String cacheKey = String.format(ADDRESS_CACHE_KEY_PREFIX, UserContext.getUserId());

        // 2. 尝试从缓存获取
        String cachedList = redissonCacheService.get(cacheKey);

        if ( !IStringUtils.isEmpty(cachedList)) {
            // 如果缓存中有数据（即使是空列表），直接返回
             log.info("从缓存中加载用户地址列表");
            return Result.ok(JsonUtils.toList(cachedList, AddressListVO.class));
        }
        // 3. 缓存未命中，调用远程服务获取
        log.info("远程调用加载用户地址列表");
        return userApiService.addressListQuery();
    }

    public  AddressListVO getDefaultAddress() {
        Result<List<AddressListVO>> listResult = addressListQuery();
        List<AddressListVO> listVOList = RpcResultParser.parseResult(listResult);

        Optional<AddressListVO> first = listVOList.stream().filter(
                        data -> data.getDefaultAddress().equals(0))
                .findFirst();
        if (first.isPresent()) {
            return first.get();
        } else {
            AddressListVO addressListVO = new AddressListVO();
            addressListVO.setConsignee("刘先生");
            addressListVO.setPhone("183209811823");
            addressListVO.setArea("安徽省-合肥市-瑶海区");
            addressListVO.setAddress("桃园街道");
            addressListVO.setAreaCode("440305");
            addressListVO.setLatitude(new BigDecimal(39.9042));
            addressListVO.setLongitude(new BigDecimal(116.4074));

            return addressListVO;
        }


    }
}
