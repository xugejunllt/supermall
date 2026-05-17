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
        
        ShardingTableRuleConfiguration cartRule = new ShardingTableRuleConfiguration(
                "cart",
                "ds${0..2}.cart"
        );
        cartRule.setDatabaseShardingStrategy(
                new StandardShardingStrategyConfiguration(
                        "user_id",
                        "cart-database-algorithm"
                )
        );
        shardingRuleConfig.getTables().add(cartRule);
        
        ShardingTableRuleConfiguration goodsRule = new ShardingTableRuleConfiguration(
                "goods",
                "ds0.goods"
        );
        shardingRuleConfig.getTables().add(goodsRule);
        
        ShardingTableRuleConfiguration goodsSkuRule = new ShardingTableRuleConfiguration(
                "goods_sku",
                "ds0.goods_sku"
        );
        shardingRuleConfig.getTables().add(goodsSkuRule);
        
        ShardingTableRuleConfiguration baseGoodsRule = new ShardingTableRuleConfiguration(
                "base_goods",
                "ds0.base_goods"
        );
        shardingRuleConfig.getTables().add(baseGoodsRule);
        
        ShardingTableRuleConfiguration baseGoodsSkuRule = new ShardingTableRuleConfiguration(
                "base_goods_sku",
                "ds0.base_goods_sku"
        );
        shardingRuleConfig.getTables().add(baseGoodsSkuRule);
        
        ShardingTableRuleConfiguration goodsAttributeRule = new ShardingTableRuleConfiguration(
                "goods_attribute",
                "ds0.goods_attribute"
        );
        shardingRuleConfig.getTables().add(goodsAttributeRule);
        
        ShardingTableRuleConfiguration goodsBrandRule = new ShardingTableRuleConfiguration(
                "goods_brand",
                "ds0.goods_brand"
        );
        shardingRuleConfig.getTables().add(goodsBrandRule);
        
        ShardingTableRuleConfiguration goodsCategoryRule = new ShardingTableRuleConfiguration(
                "goods_category",
                "ds0.goods_category"
        );
        shardingRuleConfig.getTables().add(goodsCategoryRule);
        
        ShardingTableRuleConfiguration goodsHistoryVersionRule = new ShardingTableRuleConfiguration(
                "goods_history_version",
                "ds0.goods_history_version"
        );
        shardingRuleConfig.getTables().add(goodsHistoryVersionRule);
        
        ShardingTableRuleConfiguration goodsSkuHistoryVersionRule = new ShardingTableRuleConfiguration(
                "goods_sku_history_version",
                "ds0.goods_sku_history_version"
        );
        shardingRuleConfig.getTables().add(goodsSkuHistoryVersionRule);
        
        ShardingTableRuleConfiguration goodsSyncEsRecordRule = new ShardingTableRuleConfiguration(
                "goods_sync_es_record",
                "ds0.goods_sync_es_record"
        );
        shardingRuleConfig.getTables().add(goodsSyncEsRecordRule);
        
        ShardingTableRuleConfiguration shopRule = new ShardingTableRuleConfiguration(
                "shop",
                "ds0.shop"
        );
        shardingRuleConfig.getTables().add(shopRule);
        
        ShardingTableRuleConfiguration userStockPreorderPublishLogRule = new ShardingTableRuleConfiguration(
                "user_stock_preorder_publish_log",
                "ds0.user_stock_preorder_publish_log"
        );
        shardingRuleConfig.getTables().add(userStockPreorderPublishLogRule);

        Map<String, AlgorithmConfiguration> shardingAlgorithms = new LinkedHashMap<>();
        
        Properties userStockAlgoProps = new Properties();
        userStockAlgoProps.setProperty("strategy", "standard");
        userStockAlgoProps.setProperty("algorithmClassName", "com.lanf.goods.config.GoodsDatabaseShardingAlgorithm");
        shardingAlgorithms.put("user-stock-database-algorithm", new AlgorithmConfiguration("CLASS_BASED", userStockAlgoProps));
        
        Properties userStockFlowAlgoProps = new Properties();
        userStockFlowAlgoProps.setProperty("strategy", "standard");
        userStockFlowAlgoProps.setProperty("algorithmClassName", "com.lanf.goods.config.GoodsDatabaseShardingAlgorithm");
        shardingAlgorithms.put("user-stock-flow-database-algorithm", new AlgorithmConfiguration("CLASS_BASED", userStockFlowAlgoProps));
        
        Properties cartAlgoProps = new Properties();
        cartAlgoProps.setProperty("strategy", "standard");
        cartAlgoProps.setProperty("algorithmClassName", "com.lanf.goods.config.CartDatabaseShardingAlgorithm");
        shardingAlgorithms.put("cart-database-algorithm", new AlgorithmConfiguration("CLASS_BASED", cartAlgoProps));
        
        shardingRuleConfig.setShardingAlgorithms(shardingAlgorithms);

        return shardingRuleConfig;
    }

}
