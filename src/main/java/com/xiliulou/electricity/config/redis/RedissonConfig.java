package com.xiliulou.electricity.config.redis;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @ClassName: RedissonConfig
 * @description:
 * @author: renhang
 * @create: 2024-10-08 11:23
 */
@Configuration
public class RedissonConfig {
    
    @Value("${redisson.address}")
    private String redisAddress;
    
    @Value("${redisson.connectionPoolSize}")
    private int connectionPoolSize;
    
    @Value("${redisson.connectionMinimumIdleSize}")
    private int connectionMinimumIdleSize;
    
    @Bean
    public RedissonClient redissonClient() {
        
        Config config = new Config();
        config.useSingleServer().setAddress(redisAddress).setConnectionPoolSize(connectionPoolSize).setConnectionMinimumIdleSize(connectionMinimumIdleSize);
        
        config.setCodec(new JsonJacksonCodec());
        return Redisson.create(config);
    }
}
