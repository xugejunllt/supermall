package com.lanf.goods.config;


import com.lanf.constant.exception.BizException;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.sharding.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.RangeShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.StandardShardingAlgorithm;

import java.util.Collection;

/**
 * 购物车表分库算法
 * 根据 user_id % 3 进行分库
 */
@Slf4j
public class CartDatabaseShardingAlgorithm implements StandardShardingAlgorithm<Long> {
    
    private static final int DATABASE_COUNT = 3;
    
    @Override
    public String doSharding(Collection<String> availableTargetNames, PreciseShardingValue<Long> shardingValue) {
        Long userId = shardingValue.getValue();
        if (userId == null) {
            throw new BizException("user_id 不能为空，无法进行分库路由");
        }
        
        int index = (int) (userId % DATABASE_COUNT);
        String targetDataSource = "ds" + index;
        log.info("cart分库路由: {},availableTargetNames{}", targetDataSource,availableTargetNames);
        if (availableTargetNames.contains(targetDataSource)) {
            return targetDataSource;
        }
        
        throw new BizException("找不到对应的数据源: " + targetDataSource);
    }
    
    @Override
    public Collection<String> doSharding(Collection<String> availableTargetNames, RangeShardingValue<Long> shardingValue) {
        return availableTargetNames;
    }
    

    
    @Override
    public String getType() {
        return "CLASS_BASED";
    }
}
