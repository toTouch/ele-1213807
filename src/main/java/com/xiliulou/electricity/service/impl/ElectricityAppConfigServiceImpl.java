package com.xiliulou.electricity.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.CommonConstant;
import com.xiliulou.electricity.entity.ElectricityAppConfig;
import com.xiliulou.electricity.entity.ElectricityConfig;
import com.xiliulou.electricity.enums.SelectionExchageEunm;
import com.xiliulou.electricity.mapper.ElectricityAppConfigMapper;
import com.xiliulou.electricity.query.ElectricityAppConfigQuery;
import com.xiliulou.electricity.service.ElectricityAppConfigService;
import com.xiliulou.electricity.service.ElectricityConfigService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.DbUtils;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * 用户配置(LoginInfo)表服务实现类
 *
 * @author zhangyongbo
 * @since 2023-10-11
 */
@Service("electricityAppConfig")
@Slf4j
public class ElectricityAppConfigServiceImpl extends ServiceImpl<ElectricityAppConfigMapper, ElectricityAppConfig> implements ElectricityAppConfigService {
    
    @Autowired
    private ElectricityConfigService electricityConfigService;
    
    @Autowired
    private ElectricityAppConfigMapper electricityAppConfigMapper;
    
    @Autowired
    private RedisService redisService;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public R edit(ElectricityAppConfigQuery electricityAppConfigQuery) {
        //用户
        TokenUser userInfo = SecurityUtils.getUserInfo();
        if (Objects.isNull(userInfo)) {
            log.warn("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        ElectricityAppConfig electricityAppConfig = electricityAppConfigMapper.selectElectricityAppConfig(userInfo.getUid(), TenantContextHolder.getTenantId());
        
        //新增
        if (Objects.isNull(electricityAppConfig)) {
            electricityAppConfig = new ElectricityAppConfig();
            electricityAppConfig.setUid(userInfo.getUid());
            electricityAppConfig.setTenantId(TenantContextHolder.getTenantId());
            electricityAppConfig.setCreateTime(System.currentTimeMillis());
            electricityAppConfig.setUpdateTime(System.currentTimeMillis());
            electricityAppConfig.setIsSelectionExchange(electricityAppConfigQuery.getIsSelectionExchange());
            electricityAppConfigMapper.insert(electricityAppConfig);
            return R.ok();
        }
        
        //更新
        electricityAppConfig.setId(electricityAppConfigQuery.getId());
        electricityAppConfig.setUid(userInfo.getUid());
        electricityAppConfig.setTenantId(TenantContextHolder.getTenantId());
        electricityAppConfig.setUpdateTime(System.currentTimeMillis());
        electricityAppConfig.setIsSelectionExchange(electricityAppConfigQuery.getIsSelectionExchange());
        this.updateByUid(electricityAppConfig);
        return R.ok();
    }
    
    @Override
    public R queryUserAppConfigInfo() {
        //用户
        TokenUser userInfo = SecurityUtils.getUserInfo();
        if (Objects.isNull(userInfo)) {
            log.warn("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        String updateFlag = redisService.get(CacheConstant.CACHE_ELE_SELECTION_EXCHANGE_UPDATE_FLAG + TenantContextHolder.getTenantId());
        if (StringUtils.equals(CommonConstant.SELECTION_EXCHANGE_UPDATE, updateFlag)) {
            return R.ok();
        }
        return R.ok(queryFromCacheByUid(userInfo.getUid()));
    }
    
    public ElectricityAppConfig queryFromCacheByUid(Long uid) {
        //查询用户配置缓存
        ElectricityAppConfig cache = redisService.getWithHash(CacheConstant.CACHE_ELE_APP_SET_CONFIG + uid, ElectricityAppConfig.class);
        if (Objects.nonNull(cache)) {
            return cache;
        }
        ElectricityAppConfig electricityAppConfig = electricityAppConfigMapper.selectElectricityAppConfig(uid, TenantContextHolder.getTenantId());
        if (Objects.isNull(electricityAppConfig)) {
            return null;
        }
        redisService.saveWithHash(CacheConstant.CACHE_ELE_APP_SET_CONFIG + uid, electricityAppConfig);
        return electricityAppConfig;
    }
    
    @Override
    public Integer updateByUid(ElectricityAppConfig electricityAppConfig) {
        int update = electricityAppConfigMapper.update(electricityAppConfig);
        DbUtils.dbOperateSuccessThen(update, () -> {
            redisService.delete(CacheConstant.CACHE_ELE_APP_SET_CONFIG + electricityAppConfig.getUid());
            return null;
        });
        
        return update;
    }
    
}
