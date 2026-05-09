package com.lanf.order.config;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.shardingsphere.driver.api.ShardingSphereDataSourceFactory;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.*;

/**
 * ShardingSphere 分库分表配置类
 * 
 * <p>通过 Java Config 方式配置 ShardingSphere，实现以下功能：
 * <ul>
 *   <li>配置多个数据源（ds0, ds1）</li>
 *   <li>配置分库策略：根据 user_id 取模分配到不同数据库</li>
 *   <li>配置分表策略：根据 user_id 取模分配到不同表</li>
 *   <li>使用自定义分片算法类实现灵活的分片逻辑</li>
 * </ul>
 * 
 * <p>配置外部化：所有配置参数从 yml 中读取，
 * 便于不同环境使用不同的配置，无需修改代码。
 * 
 *
 *
 */
@Configuration
public class ShardingSphereBeanConfig {

    /**
     * 注入 ShardingSphere 配置属性
     * 从 yml 配置文件中读取：sharding.sphere.*
     */
    @Autowired
    private ShardingSphereProperties shardingSphereProperties;

    /**
     * 创建 ShardingSphere 数据源 Bean
     * 
     * <p>这是整个分库分表配置的核心方法，负责：
     * <ol>
     *   <li>创建并配置所有物理数据源（ds0, ds1）</li>
     *   <li>配置分片规则（分库、分表策略）</li>
     *   <li>创建 ShardingSphere 代理数据源</li>
     * </ol>
     * 
     * <p>Spring 容器中的其他组件（如 MyBatis-Plus）会自动注入这个数据源，
     * 所有的 SQL 操作都会经过 ShardingSphere 进行路由和分片。
     *
     * @return ShardingSphere 代理数据源
     * @throws SQLException 数据源创建失败时抛出异常
     */
    @Bean
    public DataSource shardingSphereDataSource() throws SQLException {
        // 1. 创建物理数据源映射（ds0 -> 数据库0, ds1 -> 数据库1）
        Map<String, DataSource> dataSourceMap = createDataSources();

        // 2. 创建分片规则配置（包含分库、分表策略）
        ShardingRuleConfiguration shardingRuleConfig = createShardingRuleConfiguration();

        // 3. 配置 ShardingSphere 全局属性
        Properties props = new Properties();
        // 开启 SQL 日志打印，便于调试和监控实际执行的 SQL
        props.setProperty("sql-show", String.valueOf(shardingSphereProperties.getSqlShow()));

        // 4. 创建 ShardingSphere 数据源工厂
        // ShardingSphereDataSourceFactory 会根据配置创建代理数据源
        // 所有通过此数据源执行的 SQL 都会被自动路由到正确的库和表
        return ShardingSphereDataSourceFactory.createDataSource(
                dataSourceMap,           // 物理数据源集合
                Collections.singleton(shardingRuleConfig),  // 分片规则集合
                props                    // 全局配置属性
        );
    }

    /**
     * 创建物理数据源映射
     * 
     * <p>从配置文件中读取所有数据源配置，创建对应的 HikariCP 连接池。
     * 例如：ds0 -> order_db_0, ds1 -> order_db_1
     * 
     * <p>HikariCP 是高性能的 JDBC 连接池，Spring Boot 默认使用。
     *
     * @return 数据源名称到 DataSource 对象的映射
     */
    private Map<String, DataSource> createDataSources() {
        Map<String, DataSource> map = new HashMap<>();
        
        // 遍历配置文件中定义的所有数据源
        shardingSphereProperties.getDatasource().forEach((name, dsProps) -> {
            // 为每个数据源创建 HikariCP 连接池
            HikariDataSource ds = new HikariDataSource();
            ds.setDriverClassName(dsProps.getDriverClassName());      // MySQL 驱动类名
            ds.setJdbcUrl(dsProps.getJdbcUrl());                      // 数据库连接 URL
            ds.setUsername(dsProps.getUsername());                    // 数据库用户名
            ds.setPassword(dsProps.getPassword());                    // 数据库密码
            ds.setMaximumPoolSize(dsProps.getMaximumPoolSize());      // 连接池最大连接数
            
            // 将数据源放入映射，key 为数据源名称（如 ds0, ds1）
            map.put(name, ds);
        });
        
        return map;
    }

    /**
     * 创建分片规则配置
     * 
     * <p>这是分库分表的核心配置，包括：
     * <ol>
     *   <li>配置每张表的实际数据节点（actual-data-nodes）</li>
     *   <li>配置分库策略：使用哪个字段、哪种算法进行分库</li>
     *   <li>配置分表策略：使用哪个字段、哪种算法进行分表</li>
     *   <li>注册所有分片算法的具体实现</li>
     * </ol>
     * 
     * <p>示例：user_id=12345 的路由过程
     * <pre>
     * 1. 分库：12345 % 2 = 1 → 路由到 ds1
     * 2. 分表：12345 % 8 = 1 → 路由到 t_order_1
     * 3. 最终路由结果：ds1.t_order_1
     * </pre>
     *
     * @return 完整的分片规则配置对象
     */
    private ShardingRuleConfiguration createShardingRuleConfiguration() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();

        // 遍历配置文件中定义的所有分片表
        shardingSphereProperties.getSharding().getTables().forEach((tableName, tableProps) -> {
            // 创建表级分片规则配置
            // tableName: 逻辑表名（如 t_order）
            // actualDataNodes: 实际数据节点表达式（如 ds${0..1}.t_order_${0..7}）
            ShardingTableRuleConfiguration tableRule = new ShardingTableRuleConfiguration(
                    tableName,
                    tableProps.getActualDataNodes()
            );

            // 配置分库策略
            // databaseShardingColumn: 分库字段（如 user_id）
            // databaseAlgorithmName: 分库算法名称（关联到下方注册的算法）
            tableRule.setDatabaseShardingStrategy(
                    new StandardShardingStrategyConfiguration(
                            tableProps.getDatabaseShardingColumn(),      // 分片键：user_id
                            tableProps.getDatabaseAlgorithmName()        // 算法名：database-sharding-algorithm
                    )
            );

            // 配置分表策略
            // tableShardingColumn: 分表字段（如 user_id）
            // tableAlgorithmName: 分表算法名称（关联到下方注册的算法）
            tableRule.setTableShardingStrategy(
                    new StandardShardingStrategyConfiguration(
                            tableProps.getTableShardingColumn(),         // 分片键：user_id
                            tableProps.getTableAlgorithmName()           // 算法名：table-sharding-algorithm
                    )
            );

            // 将此表的分片规则添加到总配置中
            shardingRuleConfig.getTables().add(tableRule);
        });

        // 注册所有分片算法的实现
        // key: 算法名称（与上方策略配置中的算法名对应）
        // value: 算法配置（指定算法类型和实现类）
        Map<String, AlgorithmConfiguration> shardingAlgorithms = new LinkedHashMap<>();
        shardingSphereProperties.getSharding().getAlgorithms().forEach((algoName, algoProps) -> {
            // 创建算法配置
            Properties props = new Properties();
            // 指定自定义算法类的全路径名
            // ShardingSphere 会通过反射创建此类的实例
            props.setProperty("algorithm-class-name", algoProps.getAlgorithmClassName());
            
            // 创建算法配置对象
            // type: CLASS_BASED 表示使用自定义类实现
            // props: 包含算法类名的属性对象
            shardingAlgorithms.put(algoName, new AlgorithmConfiguration(algoProps.getType(), props));
        });
        
        // 将所有算法配置设置到分片规则中
        shardingRuleConfig.setShardingAlgorithms(shardingAlgorithms);

        return shardingRuleConfig;
    }
}