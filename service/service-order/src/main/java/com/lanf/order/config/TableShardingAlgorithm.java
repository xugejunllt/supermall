package com.lanf.order.config;

import org.apache.shardingsphere.sharding.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.RangeShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.StandardShardingAlgorithm;

import java.util.Collection;

// 分表算法：根据 user_id % 8 确定表名 t_order_0 ~ t_order_7
public class TableShardingAlgorithm implements StandardShardingAlgorithm<Long> {

    @Override
    public String doSharding(Collection<String> availableTargetNames, PreciseShardingValue<Long> shardingValue) {
        long userId = shardingValue.getValue();
        String suffix = String.valueOf(userId % 8);
        for (String target : availableTargetNames) {
            if (target.endsWith(suffix)) {
                return target;
            }
        }
        throw new UnsupportedOperationException("No table found for userId: " + userId);
    }

    @Override
    public Collection<String> doSharding(Collection<String> availableTargetNames, RangeShardingValue<Long> shardingValue) {
        return availableTargetNames; // 范围查询简化处理
    }


}