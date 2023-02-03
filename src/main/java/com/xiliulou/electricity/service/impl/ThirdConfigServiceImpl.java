package com.xiliulou.electricity.service.impl;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.entity.ThirdConfig;
import com.xiliulou.electricity.mapper.ThirdConfigMapper;
import com.xiliulou.electricity.query.ThirdConfigQuery;
import com.xiliulou.electricity.service.ThirdConfigService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.DbUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

import lombok.extern.slf4j.Slf4j;

/**
 * (ThirdConfig)表服务实现类
 *
 * @author zzlong
 * @since 2023-02-02 18:03:58
 */
@Service("thirdConfigService")
@Slf4j
public class ThirdConfigServiceImpl implements ThirdConfigService {
    @Autowired
    private ThirdConfigMapper thirdConfigMapper;

    @Autowired
    private RedisService redisService;

    @Override
    public ThirdConfig selectLatestByTenantId(Integer tenantId) {
        ThirdConfig cacheThirdConfig = redisService.getWithHash(CacheConstant.CACHE_THIRD_CONFIG + tenantId, ThirdConfig.class);
        if (Objects.nonNull(cacheThirdConfig)) {
            return cacheThirdConfig;
        }

        ThirdConfig thirdConfig = this.thirdConfigMapper.selectLatestByTenantId(tenantId);
        if (Objects.isNull(thirdConfig)) {
            return thirdConfig;
        }

        redisService.saveWithHash(CacheConstant.CACHE_THIRD_CONFIG + tenantId, thirdConfig);

        return thirdConfig;
    }

    @Override
    public Integer insertOrUpdate(ThirdConfigQuery thirdConfigQuery) {

        if (Objects.isNull(thirdConfigQuery.getId())) {
            ThirdConfig thirdConfig = new ThirdConfig();
            thirdConfig.setFaceMerchantId(thirdConfigQuery.getFaceMerchantId());
            thirdConfig.setDelFlag(ThirdConfig.DEL_NORMAL);
            thirdConfig.setTenantId(TenantContextHolder.getTenantId());
            thirdConfig.setCreateTime(System.currentTimeMillis());
            thirdConfig.setUpdateTime(System.currentTimeMillis());

            int insert = this.thirdConfigMapper.insert(thirdConfig);
            DbUtils.dbOperateSuccessThen(insert, () -> {
                redisService.delete(CacheConstant.CACHE_THIRD_CONFIG + TenantContextHolder.getTenantId());
                return null;
            });

            return insert;
        }

        ThirdConfig thirdConfigUpdate = new ThirdConfig();
        thirdConfigUpdate.setFaceMerchantId(thirdConfigQuery.getFaceMerchantId());
        thirdConfigUpdate.setTenantId(TenantContextHolder.getTenantId());
        thirdConfigUpdate.setUpdateTime(System.currentTimeMillis());
        int update = this.thirdConfigMapper.update(thirdConfigUpdate);
        DbUtils.dbOperateSuccessThen(update, () -> {
            redisService.delete(CacheConstant.CACHE_THIRD_CONFIG + TenantContextHolder.getTenantId());
            return null;
        });

        return update;
    }
}
