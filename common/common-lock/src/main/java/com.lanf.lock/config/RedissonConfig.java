package com.lanf.lock.config;

import org.apache.commons.lang3.StringUtils;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;



@Configuration
public class RedissonConfig {

    @Value("${spring.redis.host}")
    private String urls;
    @Value("${spring.redis.port}")
    private String port;
    @Value("${spring.redis.password}")
    private String password;

    @Bean(name = "redissonClient")
    public RedissonClient redissonClientSingle()  {
        RedissonClient redisson = null;
        Config config = new Config();
        config.useSingleServer().setAddress( "redis://"+urls+":"+port);
        if ( !StringUtils.isEmpty(password)){
            config.useSingleServer().setPassword(password);
        }

        /**
         * 当加锁不指定过期时间 ，默认30秒过期，启动看门狗机制，每30秒自动续期30秒；
         *
         */
        //自动续约时间
        // config.setLockWatchdogTimeout(lockWatchdogTimeoutL*1000);



        redisson = Redisson.create(config);
        return redisson;
    }

}