package com.xiliulou.electricity.service.impl.meituan;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.CommonConstant;
import com.xiliulou.electricity.constant.meituan.MeiTuanConfigConstant;
import com.xiliulou.electricity.entity.ElectricityConfig;
import com.xiliulou.electricity.entity.meituan.MeiTuanRiderMallConfig;
import com.xiliulou.electricity.mapper.meituan.MeiTuanRiderMallConfigMapper;
import com.xiliulou.electricity.request.meituan.MeiTuanRiderMallConfigRequest;
import com.xiliulou.electricity.service.ElectricityConfigService;
import com.xiliulou.electricity.service.meituan.MeiTuanRiderMallConfigService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.DbUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author HeYafeng
 * @description 美团骑手商城配置信息服务接口
 * @date 2024/8/28 10:32:42
 */

@Service
public class MeiTuanRiderMallConfigServiceImpl implements MeiTuanRiderMallConfigService {
    
    @Resource
    private RedisService redisService;
    
    @Resource
    private MeiTuanRiderMallConfigMapper meiTuanRiderMallConfigMapper;
    
    @Resource
    private ElectricityConfigService electricityConfigService;
    
    @Override
    public Integer insertOrUpdate(MeiTuanRiderMallConfigRequest meiTuanRiderMallConfigRequest) {
        Integer tenantId = TenantContextHolder.getTenantId();
        String appId = meiTuanRiderMallConfigRequest.getAppId();
        String appKey = meiTuanRiderMallConfigRequest.getAppKey();
        String secret = meiTuanRiderMallConfigRequest.getSecret();
        
        MeiTuanRiderMallConfig meiTuanRiderMallConfig = queryByTenantIdFromCache(tenantId);
        if (Objects.isNull(meiTuanRiderMallConfig)) {
            meiTuanRiderMallConfig = MeiTuanRiderMallConfig.builder().appId(appId).appKey(appKey).secret(secret).tenantId(tenantId).delFlag(CommonConstant.DEL_N)
                    .createTime(System.currentTimeMillis()).updateTime(System.currentTimeMillis()).build();
            Integer insert = meiTuanRiderMallConfigMapper.insert(meiTuanRiderMallConfig);
            DbUtils.dbOperateSuccessThenHandleCache(insert, i -> {
                redisService.delete(CacheConstant.CACHE_MEI_TUAN_RIDER_MALL_CONFIG + tenantId);
                redisService.delete(String.format(CacheConstant.CACHE_MEI_TUAN_RIDER_MALL_CONFIG_APP, appId, appKey));
            });
            
            return insert;
        }
        
        MeiTuanRiderMallConfig updateMeiTuanRiderMallConfig = MeiTuanRiderMallConfig.builder().id(meiTuanRiderMallConfig.getId()).tenantId(tenantId).appId(appId).appKey(appKey)
                .secret(secret).updateTime(System.currentTimeMillis()).build();
        
        Integer update = meiTuanRiderMallConfigMapper.update(updateMeiTuanRiderMallConfig);
        DbUtils.dbOperateSuccessThenHandleCache(update, i -> {
            redisService.delete(CacheConstant.CACHE_MEI_TUAN_RIDER_MALL_CONFIG + tenantId);
            redisService.delete(String.format(CacheConstant.CACHE_MEI_TUAN_RIDER_MALL_CONFIG_APP, appId, appKey));
            
        });
        
        return update;
    }
    
    @Override
    public MeiTuanRiderMallConfig queryByTenantIdFromCache(Integer tenantId) {
        MeiTuanRiderMallConfig cacheConfig = redisService.getWithHash(CacheConstant.CACHE_MEI_TUAN_RIDER_MALL_CONFIG + tenantId, MeiTuanRiderMallConfig.class);
        if (Objects.nonNull(cacheConfig)) {
            return cacheConfig;
        }
        
        MeiTuanRiderMallConfig meiTuanRiderMallConfig = this.queryByTenantId(tenantId);
        if (Objects.isNull(meiTuanRiderMallConfig)) {
            return null;
        }
        
        redisService.saveWithHash(CacheConstant.CACHE_MEI_TUAN_RIDER_MALL_CONFIG + tenantId, meiTuanRiderMallConfig);
        return meiTuanRiderMallConfig;
    }
    
    @Slave
    @Override
    public MeiTuanRiderMallConfig queryByTenantId(Integer tenantId) {
        return meiTuanRiderMallConfigMapper.selectByTenantId(tenantId);
    }
    
    @Slave
    @Override
    public MeiTuanRiderMallConfig queryByConfig(MeiTuanRiderMallConfig config) {
        return meiTuanRiderMallConfigMapper.selectByConfig(config);
    }
    
    @Slave
    @Override
    public MeiTuanRiderMallConfig queryByConfigFromCache(MeiTuanRiderMallConfig config) {
        String appId = config.getAppId();
        String appKey = config.getAppKey();
        String key = String.format(CacheConstant.CACHE_MEI_TUAN_RIDER_MALL_CONFIG_APP, appId, appKey);
        
        MeiTuanRiderMallConfig cacheConfig = redisService.getWithHash(key, MeiTuanRiderMallConfig.class);
        if (Objects.nonNull(cacheConfig)) {
            return cacheConfig;
        }
        
        MeiTuanRiderMallConfig meiTuanRiderMallConfig = this.queryByConfig(config);
        if (Objects.isNull(meiTuanRiderMallConfig)) {
            return null;
        }
        
        redisService.saveWithHash(key, meiTuanRiderMallConfig);
        return meiTuanRiderMallConfig;
    }
    
    @Slave
    @Override
    public List<MeiTuanRiderMallConfig> listEnableMeiTuanRiderMall(Integer offset, Integer size) {
        return meiTuanRiderMallConfigMapper.selectListEnableMeiTuanRiderMall(offset, size);
    }
    
    @Override
    public List<MeiTuanRiderMallConfig> listAll() {
        List<MeiTuanRiderMallConfig> list = new ArrayList<>();
        int offset = 0;
        int size = 50;
        
        while (true) {
            List<MeiTuanRiderMallConfig> configs = listEnableMeiTuanRiderMall(offset, size);
            if (CollectionUtils.isEmpty(configs)) {
                break;
            }
            
            list.addAll(configs);
            offset += size;
        }
        return list;
    }
    
    @Override
    public MeiTuanRiderMallConfig checkEnableMeiTuanRiderMall(Integer tenantId) {
        ElectricityConfig electricityConfig = electricityConfigService.queryFromCacheByTenantId(tenantId);
        if (Objects.isNull(electricityConfig) || Objects.equals(electricityConfig.getIsEnableMeiTuanRiderMall(), MeiTuanConfigConstant.DISABLE_MEI_TUAN_RIDER_MALL)) {
            return null;
        }
        
        MeiTuanRiderMallConfig meiTuanRiderMallConfig = this.queryByTenantIdFromCache(tenantId);
        if (Objects.isNull(meiTuanRiderMallConfig) || StringUtils.isEmpty(meiTuanRiderMallConfig.getAppId()) || StringUtils.isEmpty(meiTuanRiderMallConfig.getAppKey())
                || StringUtils.isEmpty(meiTuanRiderMallConfig.getSecret())) {
            return null;
        }
        
        return meiTuanRiderMallConfig;
    }
    
}
