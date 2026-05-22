package com.lanf.order.config;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.shardingsphere.driver.api.ShardingSphereDataSourceFactory;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
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
 *
 *
 */
@Configuration
public class ShardingSphereBeanConfig {

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
        props.setProperty("sql-show", "true");

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
     * <p>创建对应的 HikariCP 连接池。
     * 例如：ds0 -> order_db_0, ds1 -> order_db_1
     * 
     * <p>HikariCP 是高性能的 JDBC 连接池，Spring Boot 默认使用。
     *
     * @return 数据源名称到 DataSource 对象的映射
     */
    private Map<String, DataSource> createDataSources() {
        Map<String, DataSource> map = new HashMap<>();
        
        // 创建数据源 ds0
        HikariDataSource ds0 = new HikariDataSource();
        ds0.setDriverClassName("com.mysql.cj.jdbc.Driver");
        ds0.setJdbcUrl("jdbc:mysql://localhost:3306/order_db_0?useUnicode=true&characterEncoding=UTF-8&useSSL=false&serverTimezone=Asia/Shanghai");
        ds0.setUsername("root");
        ds0.setPassword("123456");
        ds0.setMaximumPoolSize(10);
        map.put("ds0", ds0);
        
        // 创建数据源 ds1
        HikariDataSource ds1 = new HikariDataSource();
        ds1.setDriverClassName("com.mysql.cj.jdbc.Driver");
        ds1.setJdbcUrl("jdbc:mysql://localhost:3306/order_db_1?useUnicode=true&characterEncoding=UTF-8&useSSL=false&serverTimezone=Asia/Shanghai");
        ds1.setUsername("root");
        ds1.setPassword("123456");
        ds1.setMaximumPoolSize(10);
        map.put("ds1", ds1);
        
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
     * <p>分片策略说明：
     * <ul>
     *   <li>分库：user_id % 2，均匀分布到 2 个数据库</li>
     *   <li>分表：user_id % 2，均匀分布到 2 张表</li>
     * </ul>
     * 
     * <p>示例：user_id=12345 的路由过程
     * <pre>
     * 1. 分库：12345 % 2 = 1 → 路由到 ds1
     * 2. 分表：12345 % 2 = 1 → 路由到 main_order_1
     * 3. 最终路由结果：ds1.main_order_1
     * </pre>
     *
     * @return 完整的分片规则配置对象
     */
    private ShardingRuleConfiguration createShardingRuleConfiguration() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();

        // 1. 配置 main_order 表的分片规则（每个库 2 张表）
        ShardingTableRuleConfiguration mainOrderRule = new ShardingTableRuleConfiguration(
                "main_order",
                "ds${0..1}.main_order_${0..1}"
        );
        mainOrderRule.setDatabaseShardingStrategy(
                new StandardShardingStrategyConfiguration(
                        "user_id",
                        "database-sharding-algorithm"
                )
        );
        mainOrderRule.setTableShardingStrategy(
                new StandardShardingStrategyConfiguration(
                        "user_id",
                        "table-sharding-algorithm"
                )
        );
        shardingRuleConfig.getTables().add(mainOrderRule);

        // 2. 配置 orders 表的分片规则（每个库 2 张表）
        ShardingTableRuleConfiguration orderRule = new ShardingTableRuleConfiguration(
                "orders",
                "ds${0..1}.orders_${0..1}"
        );
        orderRule.setDatabaseShardingStrategy(
                new StandardShardingStrategyConfiguration(
                        "user_id",
                        "database-sharding-algorithm"
                )
        );
        orderRule.setTableShardingStrategy(
                new StandardShardingStrategyConfiguration(
                        "user_id",
                        "table-sharding-algorithm"
                )
        );
        shardingRuleConfig.getTables().add(orderRule);

        // 3. 配置 order_item 表的分片规则（每个库 2 张表）
        ShardingTableRuleConfiguration orderItemRule = new ShardingTableRuleConfiguration(
                "order_item",
                "ds${0..1}.order_item_${0..1}"
        );
        orderItemRule.setDatabaseShardingStrategy(
                new StandardShardingStrategyConfiguration(
                        "user_id",
                        "database-sharding-algorithm"
                )
        );
        orderItemRule.setTableShardingStrategy(
                new StandardShardingStrategyConfiguration(
                        "user_id",
                        "table-sharding-algorithm"
                )
        );
        shardingRuleConfig.getTables().add(orderItemRule);

        // 4. 配置 order_status_trace 表的分片规则（每个库 2 张表）
        ShardingTableRuleConfiguration orderStatusTraceRule = new ShardingTableRuleConfiguration(
                "order_status_trace",
                "ds${0..1}.order_status_trace_${0..1}"
        );
        orderStatusTraceRule.setDatabaseShardingStrategy(
                new StandardShardingStrategyConfiguration(
                        "user_id",
                        "database-sharding-algorithm"
                )
        );
        orderStatusTraceRule.setTableShardingStrategy(
                new StandardShardingStrategyConfiguration(
                        "user_id",
                        "table-sharding-algorithm"
                )
        );
        shardingRuleConfig.getTables().add(orderStatusTraceRule);

        // 5. 配置 express 表：不参与分库分表，默认路由到 ds0（order_db_0）
        ShardingTableRuleConfiguration expressRule = new ShardingTableRuleConfiguration(
                "express",
                "ds0.express"
        );
        shardingRuleConfig.getTables().add(expressRule);

        // 6. 配置 shipping_info 表：不参与分库分表，默认路由到 ds0（order_db_0）
        ShardingTableRuleConfiguration shippingInfoRule = new ShardingTableRuleConfiguration(
                "shipping_info",
                "ds0.shipping_info"
        );
        shardingRuleConfig.getTables().add(shippingInfoRule);

        // 7. 配置 shipping_track 表：不参与分库分表，默认路由到 ds0（order_db_0）
        ShardingTableRuleConfiguration shippingTrackRule = new ShardingTableRuleConfiguration(
                "shipping_track",
                "ds0.shipping_track"
        );
        shardingRuleConfig.getTables().add(shippingTrackRule);

        // 注册分片算法
        Map<String, AlgorithmConfiguration> shardingAlgorithms = new LinkedHashMap<>();
        
        // 配置分库算法：database-sharding-algorithm
        Properties dbAlgorithmProps = new Properties();
        dbAlgorithmProps.put("strategy", "standard");
        dbAlgorithmProps.put("algorithmClassName", "com.lanf.order.config.DatabaseShardingAlgorithm");
        shardingAlgorithms.put("database-sharding-algorithm", 
                new AlgorithmConfiguration("CLASS_BASED", dbAlgorithmProps));
        
        // 配置分表算法：table-sharding-algorithm
        Properties tableAlgorithmProps = new Properties();
        tableAlgorithmProps.put("strategy", "standard");
        tableAlgorithmProps.put("algorithmClassName", "com.lanf.order.config.TableShardingAlgorithm");
        shardingAlgorithms.put("table-sharding-algorithm", 
                new AlgorithmConfiguration("CLASS_BASED", tableAlgorithmProps));
        
        // 将所有算法配置设置到分片规则中
        shardingRuleConfig.setShardingAlgorithms(shardingAlgorithms);

        return shardingRuleConfig;
    }
}