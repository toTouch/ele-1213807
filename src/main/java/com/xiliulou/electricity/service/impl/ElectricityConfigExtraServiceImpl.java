package com.xiliulou.electricity.service.impl;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.entity.ElectricityConfigExtra;
import com.xiliulou.electricity.mapper.ElectricityConfigExtraMapper;
import com.xiliulou.electricity.service.ElectricityConfigExtraService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * @author HeYafeng
 * @date 2025/2/12 17:07:47
 */
@Service
public class ElectricityConfigExtraServiceImpl implements ElectricityConfigExtraService {
    
    @Resource
    private RedisService redisService;
    
    @Resource
    private ElectricityConfigExtraMapper electricityConfigExtraMapper;
    
    @Override
    public ElectricityConfigExtra queryByTenantIdFromCache(Integer tenantId) {
        ElectricityConfigExtra cache = redisService.getWithHash(CacheConstant.CACHE_ELE_SET_CONFIG_EXTRA + tenantId, ElectricityConfigExtra.class);
        if (Objects.nonNull(cache)) {
            return cache;
        }
        
        ElectricityConfigExtra electricityConfigExtra = this.queryByTenantId(tenantId);
        if (Objects.isNull(electricityConfigExtra)) {
            return null;
        }
        
        redisService.saveWithHash(CacheConstant.CACHE_ELE_SET_CONFIG_EXTRA + tenantId, electricityConfigExtra);
        return electricityConfigExtra;
    }
    
    @Slave
    @Override
    public ElectricityConfigExtra queryByTenantId(Integer tenantId) {
        return electricityConfigExtraMapper.selectByTenantId(tenantId);
    }
    
    @Override
    public Integer insert(ElectricityConfigExtra electricityConfigExtra) {
        return electricityConfigExtraMapper.insert(electricityConfigExtra);
    }
    
    @Override
    public Integer update(ElectricityConfigExtra electricityConfigExtra) {
        electricityConfigExtra.setUpdateTime(System.currentTimeMillis());
        Integer update = electricityConfigExtraMapper.update(electricityConfigExtra);
        if (update > 0) {
            redisService.delete(CacheConstant.CACHE_ELE_SET_CONFIG_EXTRA + electricityConfigExtra.getTenantId());
        }
        return update;
    }
}
