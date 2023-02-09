package com.xiliulou.electricity.service.impl;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.entity.FaceidConfig;
import com.xiliulou.electricity.mapper.FaceidConfigMapper;
import com.xiliulou.electricity.query.FaceidConfigQuery;
import com.xiliulou.electricity.service.FaceidConfigService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.DbUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

import lombok.extern.slf4j.Slf4j;

/**
 * (FaceidConfig)表服务实现类
 *
 * @author zzlong
 * @since 2023-02-02 18:03:58
 */
@Service("thirdConfigService")
@Slf4j
public class FaceidConfigServiceImpl implements FaceidConfigService {
    @Autowired
    private FaceidConfigMapper faceidConfigMapper;

    @Autowired
    private RedisService redisService;

    @Override
    public FaceidConfig selectLatestByTenantId(Integer tenantId) {
        FaceidConfig cacheFaceidConfig = redisService.getWithHash(CacheConstant.CACHE_FACEID_CONFIG + tenantId, FaceidConfig.class);
        if (Objects.nonNull(cacheFaceidConfig)) {
            return cacheFaceidConfig;
        }

        FaceidConfig faceidConfig = this.faceidConfigMapper.selectLatestByTenantId(tenantId);
        if (Objects.isNull(faceidConfig)) {
            return faceidConfig;
        }

        redisService.saveWithHash(CacheConstant.CACHE_FACEID_CONFIG + tenantId, faceidConfig);

        return faceidConfig;
    }

    @Override
    public Integer insertOrUpdate(FaceidConfigQuery faceidConfigQuery) {

        if (Objects.isNull(faceidConfigQuery.getId())) {
            FaceidConfig faceidConfig = new FaceidConfig();
            faceidConfig.setFaceMerchantId(faceidConfigQuery.getFaceMerchantId());
            faceidConfig.setFaceidPrivateKey(faceidConfigQuery.getFaceidPrivateKey());
            faceidConfig.setDelFlag(FaceidConfig.DEL_NORMAL);
            faceidConfig.setTenantId(TenantContextHolder.getTenantId());
            faceidConfig.setCreateTime(System.currentTimeMillis());
            faceidConfig.setUpdateTime(System.currentTimeMillis());

            int insert = this.faceidConfigMapper.insert(faceidConfig);
            DbUtils.dbOperateSuccessThen(insert, () -> {
                redisService.delete(CacheConstant.CACHE_FACEID_CONFIG + TenantContextHolder.getTenantId());
                return null;
            });

            return insert;
        }

        FaceidConfig faceidConfigUpdate = new FaceidConfig();
        faceidConfigUpdate.setFaceMerchantId(faceidConfigQuery.getFaceMerchantId());
        faceidConfigUpdate.setFaceidPrivateKey(faceidConfigQuery.getFaceidPrivateKey());
        faceidConfigUpdate.setTenantId(TenantContextHolder.getTenantId());
        faceidConfigUpdate.setUpdateTime(System.currentTimeMillis());
        int update = this.faceidConfigMapper.update(faceidConfigUpdate);
        DbUtils.dbOperateSuccessThen(update, () -> {
            redisService.delete(CacheConstant.CACHE_FACEID_CONFIG + TenantContextHolder.getTenantId());
            return null;
        });

        return update;
    }
}
