package com.lanf.goods.config;

import com.lanf.constant.exception.BizException;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.sharding.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.RangeShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.StandardShardingAlgorithm;

import java.util.Collection;

/**
 * MQ发送消息表分库算法
 *
 * <p>根据 sharding_key 进行取模分库，确保数据均匀分布到 3 个数据库中
 */
@Slf4j
public class MqSendMessageDatabaseShardingAlgorithm implements StandardShardingAlgorithm<String> {

    /**
     * 精确分片算法（用于 = 和 IN 查询）
     *
     * <p>执行原理：
     * <ol>
     *   <li>SQL 解析：ShardingSphere 拦截 SQL，发现 WHERE 子句中有 sharding_key</li>
     *   <li>调用算法：对 sharding_key 进行哈希取模，得到数据源后缀（0/1/2）</li>
     *   <li>SQL 改写：将逻辑数据源替换为物理数据源（ds0/ds1/ds2）</li>
     *   <li>结果合并：如果涉及多个数据源，合并结果集后返回</li>
     * </ol>
     *
     * @param availableTargetNames 可用的数据源名称集合（ds0, ds1, ds2）
     * @param shardingValue 分片值（sharding_key）
     * @return 目标数据源名称
     */
    @Override
    public String doSharding(Collection<String> availableTargetNames, PreciseShardingValue<String> shardingValue) {
        String shardingVal = shardingValue.getValue();

        if (shardingVal == null || shardingVal.isEmpty()) {
            log.warn("sharding_key 为空，无法进行分片路由");
            throw new BizException("sharding_key 不能为空");
        }

        // 使用 hashCode 取模，确保均匀分布到 3 个数据库
        int hash = Math.abs(shardingVal.hashCode());
        String suffix = String.valueOf(hash % 3);

        for (String target : availableTargetNames) {
            if (target.endsWith(suffix)) {
                log.debug("MQ发送消息分片路由：sharding_key={}, 目标数据源={}", shardingVal, target);
                return target;
            }
        }

        throw new UnsupportedOperationException("No data source found for sharding_key: " + shardingVal);
    }

    /**
     * 范围分片算法（用于 BETWEEN、>、< 查询）
     *
     * <p>MQ发送消息表不支持范围查询，抛出异常</p>
     *
     * @param availableTargetNames 可用的数据源名称集合
     * @param shardingValue 分片值范围
     * @return 目标数据源名称集合
     */
    @Override
    public Collection<String> doSharding(Collection<String> availableTargetNames, RangeShardingValue<String> shardingValue) {
        throw new BizException("MQ发送消息表暂不支持范围查询");
    }
}
