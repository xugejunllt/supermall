package com.lanf.order.config;

import com.lanf.constant.exception.BizException;
import org.apache.shardingsphere.sharding.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.RangeShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.StandardShardingAlgorithm;

import java.util.Collection;

// 分表算法：根据 user_id % 8 确定表名 t_order_0 ~ t_order_7
public class TableShardingAlgorithm implements StandardShardingAlgorithm<Long> {
    /**
     * 触发条件 =, IN
     *
     * 情况一：SQL 中包含 user_id = 或 user_id IN
     * SELECT * FROM t_order WHERE user_id = 100;
     * SELECT * FROM t_order WHERE user_id IN (100, 101);
     * 执行原理（精确路由）：
     *  1.SQL 解析：ShardingSphere 拦截 SQL，发现 WHERE 子句中有 user_id，且操作符是 = 或 IN。
     *  2.调用算法：它会调用你写的 PreciseShardingAlgorithm.doSharding 方法。
     *      如果是 IN (100, 101)，它会循环调用两次 doSharding：
     *      第一次传入 100：100 % 8 = 4 → 匹配到 t_order_4。
     *      第二次传入 101：101 % 8 = 5 → 匹配到 t_order_5。
     * 3.SQL 改写：ShardingSphere 将逻辑表名 t_order 替换为物理表名。
     *     原始 SQL：SELECT * FROM t_order ...
     *     改写后：SELECT * FROM t_order_4 WHERE user_id = 100
     * 4.结果合并：如果涉及多张表（如 IN 查询），它会在内存中将多个表的结果集合并后返回。
     * 结论：这是最高效的方式，只查询特定的几张表。
     *
     * 情况二：SQL 中不包含 user_id
     * 执行原理（广播路由 / 全路由）：
     * 1.SQL 解析：ShardingSphere 发现 SQL 中没有出现分片键 user_id。
     * 2.算法判断：因为它不知道数据在哪张表里，它不会调用你的 doSharding 方法（因为无法计算取模结果）。
     * 3,路由策略：触发 Broadcast Routing（广播路由）。
     * 4.SQL 改写与执行：
     *    它会将这条 SQL 复制 N 份（N = 分表数量，这里是 8 张表）。
     *    分别发送给：t_order_0, t_order_1, ..., t_order_7。
     * 5.结果归并：
     *   如果是 SELECT *：它会把 8 张表查出来的结果拼在一起返回（性能极差）。
     *   如果是 COUNT(*)：它会把 8 张表查出的数字相加后返回。
     * 结论：这是性能最差的方式，被称为“全表扫描”。在大数据量下，这种查询会导致数据库负载飙升。
     *
     *
     */
    @Override
    public String doSharding(Collection<String> availableTargetNames, PreciseShardingValue<Long> shardingValue) {

        /**
         * 1.取模 获取摸长
         * 2.拼接表名
         *
         */
        long userId = shardingValue.getValue();
        String suffix = String.valueOf(userId % 8);
        for (String target : availableTargetNames) {
            if (target.endsWith(suffix)) {
                return target;
            }
        }
        throw new UnsupportedOperationException("No table found for userId: " + userId);
    }

    /**
     * 触发条件 BETWEEN, >, <
     *
     *
     *
     */
    @Override
    public Collection<String> doSharding(Collection<String> availableTargetNames, RangeShardingValue<Long> shardingValue) {

            throw new BizException("暂不支持范围查询");

    }


}