package com.xiliulou.electricity.service.impl;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.EleEsignConstant;
import com.xiliulou.electricity.entity.EleEsignConfig;
import com.xiliulou.electricity.mapper.ElectricityEsignConfigMapper;
import com.xiliulou.electricity.query.EleEsignConfigQuery;
import com.xiliulou.electricity.service.EleEsignConfigService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.DbUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * @author: Kenneth
 * @Date: 2023/7/7 23:20
 * @Description:
 */
@Service
@Slf4j
public class EleEsignConfigServiceImpl implements EleEsignConfigService {
    @Autowired
    private ElectricityEsignConfigMapper esignConfigMapper;
    @Autowired
    private RedisService redisService;

    @Override
    public EleEsignConfig selectLatestByTenantId(Integer tenantId) {
        EleEsignConfig cacheEleEsignConfig = redisService.getWithHash(CacheConstant.CACHE_ELE_CABINET_ESIGN_CONFIG + tenantId, EleEsignConfig.class);
        if (Objects.nonNull(cacheEleEsignConfig)) {
            return cacheEleEsignConfig;
        }
        EleEsignConfig eleEsignConfig = esignConfigMapper.selectLatestByTenantId(tenantId);
        if (Objects.isNull(eleEsignConfig)) {
            return null;
        }
        redisService.saveWithHash(CacheConstant.CACHE_ELE_CABINET_ESIGN_CONFIG + tenantId, eleEsignConfig);
        return eleEsignConfig;
    }

    @Override
    public Integer insertOrUpdate(EleEsignConfigQuery eleEsignConfigQuery) {
        EleEsignConfig eleEsignConfigModel = selectLatestByTenantId(TenantContextHolder.getTenantId());
        if (Objects.isNull(eleEsignConfigModel)) {
            EleEsignConfig eleEsignConfig = new EleEsignConfig();
            eleEsignConfig.setTenantId(TenantContextHolder.getTenantId());
            eleEsignConfig.setAppId(eleEsignConfigQuery.getAppId());
            eleEsignConfig.setAppSecret(eleEsignConfigQuery.getAppSecret());
            eleEsignConfig.setDocTemplateId(eleEsignConfigQuery.getDocTemplateId());
            eleEsignConfig.setSignFileName(eleEsignConfigQuery.getSignFileName());
            eleEsignConfig.setSignFlowName(eleEsignConfigQuery.getSignFlowName());
            eleEsignConfig.setDelFlag(EleEsignConstant.DEL_NO);
            eleEsignConfig.setCreateTime(System.currentTimeMillis());
            eleEsignConfig.setUpdateTime(System.currentTimeMillis());
            int result = esignConfigMapper.insertEsignConfig(eleEsignConfig);
            DbUtils.dbOperateSuccessThen(result, () -> {
                redisService.delete(CacheConstant.CACHE_ELE_CABINET_ESIGN_CONFIG + TenantContextHolder.getTenantId());
                return null;
            });

            return result;
        }
        return updateEsignConfig(eleEsignConfigQuery, eleEsignConfigModel.getId());
    }

    private Integer updateEsignConfig(EleEsignConfigQuery eleEsignConfigQuery, Integer id){
        EleEsignConfig eleEsignConfig = new EleEsignConfig();
        eleEsignConfig.setId(id);
        eleEsignConfig.setTenantId(TenantContextHolder.getTenantId());
        eleEsignConfig.setAppId(eleEsignConfigQuery.getAppId());
        eleEsignConfig.setAppSecret(eleEsignConfigQuery.getAppSecret());
        eleEsignConfig.setDocTemplateId(eleEsignConfigQuery.getDocTemplateId());
        eleEsignConfig.setSignFileName(eleEsignConfigQuery.getSignFileName());
        eleEsignConfig.setSignFlowName(eleEsignConfigQuery.getSignFlowName());
        eleEsignConfig.setUpdateTime(System.currentTimeMillis());
        int result = esignConfigMapper.updateEsignConfig(eleEsignConfig);
        DbUtils.dbOperateSuccessThen(result, () -> {
            redisService.delete(CacheConstant.CACHE_ELE_CABINET_ESIGN_CONFIG + TenantContextHolder.getTenantId());
            return null;
        });
        return result;
    }

}
