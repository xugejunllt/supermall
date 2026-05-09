package com.lanf.order.config;

import org.apache.shardingsphere.sharding.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.RangeShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.StandardShardingAlgorithm;

import java.util.Collection;

// 分库算法：根据 user_id % 2 确定数据源名称 ds0/ds1
public class DatabaseShardingAlgorithm implements StandardShardingAlgorithm<Long> {

    @Override
    public String doSharding(Collection<String> availableTargetNames, PreciseShardingValue<Long> shardingValue) {
        long userId = shardingValue.getValue();
        String suffix = String.valueOf(userId % 2);
        for (String target : availableTargetNames) {
            if (target.endsWith(suffix)) {
                return target;
            }
        }
        throw new UnsupportedOperationException("No target found for userId: " + userId);
    }

    @Override
    public Collection<String> doSharding(Collection<String> availableTargetNames, RangeShardingValue<Long> shardingValue) {
        // 范围查询可根据需要实现，这里简单返回所有节点
        return availableTargetNames;
    }



}

