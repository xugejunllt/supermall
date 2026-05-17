//package com.lanf.goods.config;
//
//
//import lombok.Data;
//import org.springframework.boot.context.properties.ConfigurationProperties;
//import org.springframework.context.annotation.Configuration;
//
//import java.util.LinkedHashMap;
//import java.util.Map;
//
//@Data
//@Configuration
//@ConfigurationProperties(prefix = "sharding.sphere")
//public class ShardingSphereProperties {
//
//    private Boolean sqlShow = true;
//
//    private Map<String, DataSourceProperties> datasource = new LinkedHashMap<>();
//
//    private ShardingProperties sharding = new ShardingProperties();
//
//    @Data
//    public static class DataSourceProperties {
//        private String driverClassName;
//        private String jdbcUrl;
//        private String username;
//        private String password;
//        private Integer maximumPoolSize = 10;
//    }
//
//    @Data
//    public static class ShardingProperties {
//        private Map<String, TableRuleProperties> tables = new LinkedHashMap<>();
//        private Map<String, AlgorithmProperties> algorithms = new LinkedHashMap<>();
//    }
//
//    @Data
//    public static class TableRuleProperties {
//        private String actualDataNodes;
//        private String databaseShardingColumn;
//        private String databaseAlgorithmName;
//    }
//
//    @Data
//    public static class AlgorithmProperties {
//        private String type;
//        private String algorithmClassName;
//    }
//}
