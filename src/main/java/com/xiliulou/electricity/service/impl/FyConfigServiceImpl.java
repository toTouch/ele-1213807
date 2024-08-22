package com.xiliulou.electricity.service.impl;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.entity.FyConfig;
import com.xiliulou.electricity.mapper.FyConfigServiceMapper;
import com.xiliulou.electricity.service.FyConfigService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName: FyConfigServiceImpl
 * @description:
 * @author: renhang
 * @create: 2024-08-22 19:19
 */

@Service
public class FyConfigServiceImpl implements FyConfigService {
    
    @Resource
    private FyConfigServiceMapper fyConfigServiceMapper;
    
    @Resource
    private RedisService redisService;
    
    @Override
    public FyConfig queryByTenantIdFromDB(Integer tenantId) {
        return fyConfigServiceMapper.selectByTenantId(tenantId);
    }
    
    @Override
    public FyConfig queryByTenantIdFromCache(Integer tenantId) {
        if (Objects.isNull(tenantId)) {
            return null;
        }
        FyConfig cache = redisService.getWithHash(CacheConstant.CACHE_FY_CONFIG + tenantId, FyConfig.class);
        if (Objects.nonNull(cache)) {
            return cache;
        }
        
        FyConfig fyConfig = queryByTenantIdFromDB(tenantId);
        if (Objects.isNull(fyConfig)) {
            return null;
        }
        
        redisService.saveWithHash(CacheConstant.CACHE_PXZ_CONFIG + tenantId, fyConfig);
        redisService.expire(CacheConstant.CACHE_FY_CONFIG + tenantId, TimeUnit.HOURS.toMillis(24 * 30), true);
        return fyConfig;
    }
}
