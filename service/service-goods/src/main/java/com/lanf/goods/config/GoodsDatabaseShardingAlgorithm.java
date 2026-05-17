package com.lanf.goods.config;

import com.lanf.constant.exception.BizException;
import org.apache.shardingsphere.sharding.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.RangeShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.StandardShardingAlgorithm;

import java.util.Collection;

public class GoodsDatabaseShardingAlgorithm implements StandardShardingAlgorithm<Long> {
    
    private static final int DATABASE_COUNT = 3;
    
    @Override
    public String doSharding(Collection<String> availableTargetNames, PreciseShardingValue<Long> shardingValue) {
        Long goodsId = shardingValue.getValue();
        if (goodsId == null) {
            throw new BizException("goodsId 不能为空，无法进行分库路由");
        }
        
        int index = (int) (goodsId % DATABASE_COUNT);
        String targetDataSource = "ds" + index;
        
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
