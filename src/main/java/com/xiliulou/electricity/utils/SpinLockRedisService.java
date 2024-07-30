package com.xiliulou.electricity.utils;

import com.xiliulou.cache.redis.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * @Description ...
 * @Author: SongJP
 * @Date: 2024/7/26 15:33
 */
@Component
@Slf4j
public class SpinLockRedisService extends RedisService {
    
    final Long DEFAULT_EXPIRE_TIME = 5 * 1000L;
    
    final Long DEFAULT_MAX_SPIN_TIME = 5 * 1000L;
    
    @Resource
    private RedisService redisService;
    
    public boolean tryLockWithSpin(String lockKey) {
        return tryLockWithSpin(lockKey, DEFAULT_EXPIRE_TIME, DEFAULT_MAX_SPIN_TIME);
    }
    
    public boolean tryLockWithSpin(String lockKey, long expireTime, long maxSpinTime) {
        long startTime = System.currentTimeMillis();
        
        while (System.currentTimeMillis() - startTime < maxSpinTime) {
            if (redisService.setNx(lockKey, "1", expireTime, false)) {
                return true;
            }
            
            try {
                Thread.sleep(50L);
            } catch (InterruptedException e) {
                log.warn("GET LOCK FAIL, thread sleep error, lockKey:{}", lockKey, e);
            }
        }
        
        log.warn("GET LOCK FAIL, That's a dangerous situation, lockKey:{}", lockKey);
        return false;
    }
}
