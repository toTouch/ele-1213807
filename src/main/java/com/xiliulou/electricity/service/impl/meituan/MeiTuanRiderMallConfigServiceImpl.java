package com.xiliulou.electricity.service.impl.meituan;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.CommonConstant;
import com.xiliulou.electricity.entity.meituan.MeiTuanRiderMallConfig;
import com.xiliulou.electricity.mapper.meituan.MeiTuanRiderMallConfigMapper;
import com.xiliulou.electricity.request.meituan.MeiTuanRiderMallConfigRequest;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.service.meituan.MeiTuanRiderMallConfigService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.DbUtils;
import org.apache.commons.collections.CollectionUtils;
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
    private UserInfoService userInfosService;
    
    @Override
    public Integer insertOrUpdate(MeiTuanRiderMallConfigRequest meiTuanRiderMallConfigRequest) {
        Integer tenantId = TenantContextHolder.getTenantId();
        MeiTuanRiderMallConfig meiTuanRiderMallConfig = queryByTenantIdFromCache(tenantId);
        if (Objects.isNull(meiTuanRiderMallConfig)) {
            meiTuanRiderMallConfig = MeiTuanRiderMallConfig.builder().appId(meiTuanRiderMallConfigRequest.getAppId()).appKey(meiTuanRiderMallConfigRequest.getAppKey())
                    .secret(meiTuanRiderMallConfigRequest.getSecret()).tenantId(tenantId).delFlag(CommonConstant.DEL_N).createTime(System.currentTimeMillis())
                    .updateTime(System.currentTimeMillis()).build();
            Integer insert = meiTuanRiderMallConfigMapper.insert(meiTuanRiderMallConfig);
            DbUtils.dbOperateSuccessThen(insert, () -> {
                redisService.delete(CacheConstant.CACHE_MEI_TUAN_RIDER_MALL_CONFIG + tenantId);
                return null;
            });
            
            return insert;
        }
        
        meiTuanRiderMallConfig.setAppId(meiTuanRiderMallConfigRequest.getAppId());
        meiTuanRiderMallConfig.setAppKey(meiTuanRiderMallConfigRequest.getAppKey());
        meiTuanRiderMallConfig.setSecret(meiTuanRiderMallConfigRequest.getSecret());
        
        Integer update = meiTuanRiderMallConfigMapper.update(meiTuanRiderMallConfig);
        DbUtils.dbOperateSuccessThen(update, () -> {
            redisService.delete(CacheConstant.CACHE_MEI_TUAN_RIDER_MALL_CONFIG + tenantId);
            return null;
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
    
    @Slave
    @Override
    public MeiTuanRiderMallConfig checkEnableMeiTuanRiderMall(Integer tenantId) {
        return meiTuanRiderMallConfigMapper.checkEnableMeiTuanRiderMall(tenantId);
    }
    
}
