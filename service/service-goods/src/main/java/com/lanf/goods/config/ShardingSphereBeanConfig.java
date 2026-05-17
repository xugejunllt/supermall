package com.lanf.goods.config;

import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@Configuration
public class ShardingSphereBeanConfig {

    @Bean
    public DataSource shardingSphereDataSource() throws SQLException {
        Map<String, DataSource> dataSourceMap = createDataSources();
        ShardingRuleConfiguration shardingRuleConfig = createShardingRuleConfiguration();

        Properties props = new Properties();
        props.setProperty("sql-show", "true");
        props.setProperty("show-process-list-enabled", "true");
        
        return ShardingSphereDataSourceFactory.createDataSource(
                dataSourceMap,
                Collections.singleton(shardingRuleConfig),
                props
        );
    }

    private Map<String, DataSource> createDataSources() {
        Map<String, DataSource> map = new HashMap<>();
        
        HikariDataSource ds0 = new HikariDataSource();
        ds0.setDriverClassName("com.mysql.cj.jdbc.Driver");
        ds0.setJdbcUrl("jdbc:mysql://localhost:3306/goods_db_0?useUnicode=true&characterEncoding=UTF-8&useSSL=false&serverTimezone=Asia/Shanghai");
        ds0.setUsername("root");
        ds0.setPassword("123456");
        ds0.setMaximumPoolSize(10);
        map.put("ds0", ds0);
        
        HikariDataSource ds1 = new HikariDataSource();
        ds1.setDriverClassName("com.mysql.cj.jdbc.Driver");
        ds1.setJdbcUrl("jdbc:mysql://localhost:3306/goods_db_1?useUnicode=true&characterEncoding=UTF-8&useSSL=false&serverTimezone=Asia/Shanghai");
        ds1.setUsername("root");
        ds1.setPassword("123456");
        ds1.setMaximumPoolSize(10);
        map.put("ds1", ds1);
        
        HikariDataSource ds2 = new HikariDataSource();
        ds2.setDriverClassName("com.mysql.cj.jdbc.Driver");
        ds2.setJdbcUrl("jdbc:mysql://localhost:3306/goods_db_2?useUnicode=true&characterEncoding=UTF-8&useSSL=false&serverTimezone=Asia/Shanghai");
        ds2.setUsername("root");
        ds2.setPassword("123456");
        ds2.setMaximumPoolSize(10);
        map.put("ds2", ds2);
        
        return map;
    }

    private ShardingRuleConfiguration createShardingRuleConfiguration() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();

        ShardingTableRuleConfiguration userStockRule = new ShardingTableRuleConfiguration(
                "user_stock",
                "ds${0..2}.user_stock"
        );
        userStockRule.setDatabaseShardingStrategy(
                new StandardShardingStrategyConfiguration(
                        "goods_id",
                        "user-stock-database-algorithm"
                )
        );
        shardingRuleConfig.getTables().add(userStockRule);
        
        ShardingTableRuleConfiguration userStockFlowRule = new ShardingTableRuleConfiguration(
                "user_stock_flow",
                "ds${0..2}.user_stock_flow"
        );
        userStockFlowRule.setDatabaseShardingStrategy(
                new StandardShardingStrategyConfiguration(
                        "goods_id",
                        "user-stock-flow-database-algorithm"
                )
        );
        shardingRuleConfig.getTables().add(userStockFlowRule);

        Map<String, AlgorithmConfiguration> shardingAlgorithms = new LinkedHashMap<>();
        
        Properties userStockAlgoProps = new Properties();
        userStockAlgoProps.setProperty("strategy", "standard");
        userStockAlgoProps.setProperty("algorithmClassName", "com.lanf.goods.config.GoodsDatabaseShardingAlgorithm");
        shardingAlgorithms.put("user-stock-database-algorithm", new AlgorithmConfiguration("CLASS_BASED", userStockAlgoProps));
        
        Properties userStockFlowAlgoProps = new Properties();
        userStockFlowAlgoProps.setProperty("strategy", "standard");
        userStockFlowAlgoProps.setProperty("algorithmClassName", "com.lanf.goods.config.GoodsDatabaseShardingAlgorithm");
        shardingAlgorithms.put("user-stock-flow-database-algorithm", new AlgorithmConfiguration("CLASS_BASED", userStockFlowAlgoProps));
        
        shardingRuleConfig.setShardingAlgorithms(shardingAlgorithms);

        return shardingRuleConfig;
    }
}
