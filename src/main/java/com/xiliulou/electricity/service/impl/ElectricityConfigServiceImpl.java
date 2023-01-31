package com.xiliulou.electricity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.entity.ElectricityConfig;
import com.xiliulou.electricity.entity.ElectricityPayParams;
import com.xiliulou.electricity.mapper.ElectricityConfigMapper;
import com.xiliulou.electricity.query.ElectricityConfigAddAndUpdateQuery;
import com.xiliulou.electricity.service.ElectricityConfigService;
import com.xiliulou.electricity.service.ElectricityPayParamsService;
import com.xiliulou.electricity.service.TemplateConfigService;
import com.xiliulou.electricity.service.UserService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.TenantConfigVO;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
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
    TemplateConfigService templateConfigService;
    @Autowired
    ElectricityPayParamsService electricityPayParamsService;
    @Autowired
    UserService userService;
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
            electricityConfig.setIsEnableReturnBoxCheck(electricityConfigAddAndUpdateQuery.getIsEnableReturnBoxCheck());
            electricityConfig.setIsFaceid(electricityConfigAddAndUpdateQuery.getIsFaceid());
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
        electricityConfig.setIsEnableReturnBoxCheck(electricityConfigAddAndUpdateQuery.getIsEnableReturnBoxCheck());
        electricityConfig.setIsOpenInsurance(electricityConfigAddAndUpdateQuery.getIsOpenInsurance());
        electricityConfig.setIsFaceid(electricityConfigAddAndUpdateQuery.getIsFaceid());
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

    @Override
    public TenantConfigVO getTenantConfig(String appId) {

        TenantConfigVO tenantConfigVO = new TenantConfigVO();

        //根据appId获取租户id
        ElectricityPayParams electricityPayParams = electricityPayParamsService.selectTenantId(appId);
        if (Objects.isNull(electricityPayParams)) {
            log.error("ELE ERROR! not found tenant,appId={}", appId);
            return tenantConfigVO;
        }
        Integer tenantId = electricityPayParams.getTenantId();

        //获取租户配置信息
        ElectricityConfig electricityConfig = this.queryFromCacheByTenantId(tenantId);
        BeanUtils.copyProperties(electricityConfig, tenantConfigVO);

        //获取租户模板id
        List<String> templateConfigList = templateConfigService.selectTemplateId(tenantId);
        tenantConfigVO.setTemplateConfigList(templateConfigList);

        //获取客服电话
        String servicePhone = userService.selectServicePhone(tenantId);
        tenantConfigVO.setServicePhone(servicePhone);

        return tenantConfigVO;
    }
}
