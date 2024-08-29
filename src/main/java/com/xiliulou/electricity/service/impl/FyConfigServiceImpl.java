package com.xiliulou.electricity.service.impl;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.entity.FyConfig;
import com.xiliulou.electricity.mapper.FyConfigServiceMapper;
import com.xiliulou.electricity.request.fy.FyConfigRequest;
import com.xiliulou.electricity.service.FyConfigService;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
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
    
    @Override
    public Pair<Boolean, String> saveOrUpdate(Integer tenantId, FyConfigRequest params) {
        if (Objects.isNull(params) || (StringUtils.isEmpty(params.getMerchantCode()) && StringUtils.isEmpty(params.getStoreCode()))){
            return Pair.of(false, "参数错误");
        }
        try {
            boolean insert = false;
            FyConfig config = queryByTenantIdFromCache(tenantId);
            if (Objects.isNull(config)){
                config = new FyConfig();
                config.setCreateTime(System.currentTimeMillis());
                config.setDelFlag(FyConfig.DEL_NORMAL);
                config.setTenantId(tenantId);
                insert  = true;
            }
            config.setUpdateTime(System.currentTimeMillis());
            if (StringUtils.isNotEmpty(params.getMerchantCode())){
                config.setMerchantCode(params.getMerchantCode());
            }
            if (StringUtils.isNotEmpty(params.getStoreCode())){
                config.setStoreCode(params.getStoreCode());
            }
            if (StringUtils.isNotEmpty(params.getChannelCode())){
                config.setChannelCode(params.getChannelCode());
            }
            if (insert){
                fyConfigServiceMapper.insert(config);
                return Pair.of(true, "");
            }
            fyConfigServiceMapper.updateByTenantId(config);
            return Pair.of(true, "");
        }finally {
            redisService.delete(CacheConstant.CACHE_PXZ_CONFIG + tenantId);
        }
    }
}
