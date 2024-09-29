package com.xiliulou.electricity.service.impl.meituan;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.entity.meituan.MeiTuanRiderMallConfig;
import com.xiliulou.electricity.mapper.meituan.MeiTuanRiderMallConfigMapper;
import com.xiliulou.electricity.service.meituan.MeiTuanRiderMallConfigService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
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
    
}
