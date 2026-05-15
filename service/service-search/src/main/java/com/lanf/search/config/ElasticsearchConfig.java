package com.lanf.search.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
public class ElasticsearchConfig {
    
    @Value("${spring.elasticsearch.uris}")
    private String[] uris;
    
    @Bean
    public RestHighLevelClient restHighLevelClient() {
        HttpHost[] hosts = Arrays.stream(uris)
                .map(HttpHost::create)
                .toArray(HttpHost[]::new);
        
        return new RestHighLevelClient(
                RestClient.builder(hosts)
        );
    }
}
