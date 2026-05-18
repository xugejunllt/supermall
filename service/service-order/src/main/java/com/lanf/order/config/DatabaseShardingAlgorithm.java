package com.lanf.order.config;

import com.lanf.constant.exception.BizException;
import org.apache.shardingsphere.sharding.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.RangeShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.StandardShardingAlgorithm;

import java.util.Collection;

/**
 * 分库算法：根据 user_id % 2 确定数据源名称 ds0/ds1
 * 
 * <p>确保数据均匀分布到 2 个数据库中
 */
public class DatabaseShardingAlgorithm implements StandardShardingAlgorithm<Long> {
    /**
     * 精确分片算法（用于 = 和 IN 查询）
     *
     * <p>执行原理：
     * <ol>
     *   <li>SQL 解析：ShardingSphere 拦截 SQL，发现 WHERE 子句中有 user_id</li>
     *   <li>调用算法：计算 user_id % 2，得到数据源后缀（0/1）</li>
     *   <li>SQL 改写：将逻辑数据源替换为物理数据源（ds0/ds1）</li>
     *   <li>结果合并：如果涉及多个数据源，合并结果集后返回</li>
     * </ol>
     *
     * @param availableTargetNames 可用的数据源名称集合（ds0, ds1）
     * @param shardingValue 分片值（user_id）
     * @return 目标数据源名称
     */
    @Override
    public String doSharding(Collection<String> availableTargetNames, PreciseShardingValue<Long> shardingValue) {
        long userId = shardingValue.getValue();
        // 取模 2，确保均匀分布到 2 个数据库
        String suffix = String.valueOf(userId % 2);
        for (String target : availableTargetNames) {
            if (target.endsWith(suffix)) {
                return target;
            }
        }
        throw new UnsupportedOperationException("No data source found for userId: " + userId);
    }

    /**
     * 范围分片算法（用于 BETWEEN、>、< 查询）
     *
     * <p>当前不支持范围查询，抛出异常
     *
     * @param availableTargetNames 可用的数据源名称集合
     * @param shardingValue 分片值范围
     * @return 目标数据源名称集合
     */
    @Override
    public Collection<String> doSharding(Collection<String> availableTargetNames, RangeShardingValue<Long> shardingValue) {
        throw new BizException("暂不支持范围查询");
    }
}

