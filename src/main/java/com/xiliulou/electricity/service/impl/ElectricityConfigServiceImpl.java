package com.xiliulou.electricity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.entity.ElectricityConfig;
import com.xiliulou.electricity.mapper.ElectricityConfigMapper;
import com.xiliulou.electricity.query.ElectricityConfigAddAndUpdateQuery;
import com.xiliulou.electricity.service.ElectricityConfigService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * 用户列表(LoginInfo)表服务实现类
 *
 * @author makejava
 * @since 2020-12-07 15:00:00
 */
@Service("electricityConfig")
@Slf4j
public class ElectricityConfigServiceImpl extends ServiceImpl<ElectricityConfigMapper, ElectricityConfig> implements ElectricityConfigService {

    @Resource
    ElectricityConfigMapper electricityConfigMapper;
    @Autowired
    RedisService redisService;


    @Override
    public R edit(ElectricityConfigAddAndUpdateQuery electricityConfigAddAndUpdateQuery) {
        //用户
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        //操作频繁
        boolean result = redisService.setNx(CacheConstant.ELE_CONFIG_EDIT_UID + user.getUid(), "1", 3 * 1000L, false);
        if (!result) {
            return R.fail("ELECTRICITY.0034", "操作频繁");
        }

        //租户
        Integer tenantId = TenantContextHolder.getTenantId();

//        if (ObjectUtil.isEmpty(electricityConfigAddAndUpdateQuery.getLowBatteryExchangeModelList())) {
//            return R.fail("ELECTRICITY.0007", "不合法的参数");
//        }

//        //封装型号押金
//        String lowBatteryExchangeModel = JsonUtil.toJson(electricityConfigAddAndUpdateQuery.getLowBatteryExchangeModelList());
//        electricityConfigAddAndUpdateQuery.setLowBatteryExchangeModel(lowBatteryExchangeModel);


        ElectricityConfig electricityConfig = electricityConfigMapper.selectOne(new LambdaQueryWrapper<ElectricityConfig>().eq(ElectricityConfig::getTenantId, tenantId));
        if (Objects.isNull(electricityConfig)) {
            electricityConfig = new ElectricityConfig();
            electricityConfig.setName(electricityConfigAddAndUpdateQuery.getName());
            electricityConfig.setOrderTime(electricityConfigAddAndUpdateQuery.getOrderTime());
            electricityConfig.setIsManualReview(electricityConfigAddAndUpdateQuery.getIsManualReview());
            electricityConfig.setIsWithdraw(electricityConfigAddAndUpdateQuery.getIsWithdraw());
            electricityConfig.setCreateTime(System.currentTimeMillis());
            electricityConfig.setUpdateTime(System.currentTimeMillis());
            electricityConfig.setTenantId(tenantId);
            electricityConfig.setIsOpenDoorLock(electricityConfigAddAndUpdateQuery.getIsOpenDoorLock());
            electricityConfig.setIsBatteryReview(electricityConfigAddAndUpdateQuery.getIsBatteryReview());
            electricityConfig.setDisableMemberCard(electricityConfigAddAndUpdateQuery.getDisableMemberCard());
            electricityConfig.setIsLowBatteryExchange(electricityConfigAddAndUpdateQuery.getIsLowBatteryExchange());
            electricityConfig.setLowBatteryExchangeModel(electricityConfigAddAndUpdateQuery.getLowBatteryExchangeModel());
            electricityConfig.setIsEnableSelfOpen(electricityConfigAddAndUpdateQuery.getIsEnableSelfOpen());
            electricityConfigMapper.insert(electricityConfig);
            return R.ok();
        }

        electricityConfig.setName(electricityConfigAddAndUpdateQuery.getName());
        electricityConfig.setOrderTime(electricityConfigAddAndUpdateQuery.getOrderTime());
        electricityConfig.setIsManualReview(electricityConfigAddAndUpdateQuery.getIsManualReview());
        electricityConfig.setIsWithdraw(electricityConfigAddAndUpdateQuery.getIsWithdraw());
        electricityConfig.setIsOpenDoorLock(electricityConfigAddAndUpdateQuery.getIsOpenDoorLock());
        electricityConfig.setIsBatteryReview(electricityConfigAddAndUpdateQuery.getIsBatteryReview());
        electricityConfig.setUpdateTime(System.currentTimeMillis());
        electricityConfig.setDisableMemberCard(electricityConfigAddAndUpdateQuery.getDisableMemberCard());
        electricityConfig.setIsLowBatteryExchange(electricityConfigAddAndUpdateQuery.getIsLowBatteryExchange());
        electricityConfig.setLowBatteryExchangeModel(electricityConfigAddAndUpdateQuery.getLowBatteryExchangeModel());
        electricityConfig.setIsEnableSelfOpen(electricityConfigAddAndUpdateQuery.getIsEnableSelfOpen());
        int updateResult = electricityConfigMapper.updateById(electricityConfig);
        if (updateResult > 0) {
            redisService.delete(CacheConstant.CACHE_ELE_SET_CONFIG + tenantId);
        }
        return R.ok();
    }

    @Override
    public ElectricityConfig queryFromCacheByTenantId(Integer tenantId) {
        ElectricityConfig cache = redisService.getWithHash(CacheConstant.CACHE_ELE_SET_CONFIG + tenantId, ElectricityConfig.class);
        if (Objects.nonNull(cache)) {
            return cache;
        }

        ElectricityConfig electricityConfig = electricityConfigMapper.selectOne(new LambdaQueryWrapper<ElectricityConfig>()
                .eq(ElectricityConfig::getTenantId, tenantId));
        if (Objects.isNull(electricityConfig)) {
            return null;
        }

        redisService.saveWithHash(CacheConstant.CACHE_ELE_SET_CONFIG + tenantId, electricityConfig);
        return electricityConfig;
    }

    @Override
    public void insertElectricityConfig(ElectricityConfig electricityConfig) {
        this.electricityConfigMapper.insert(electricityConfig);
    }
}
