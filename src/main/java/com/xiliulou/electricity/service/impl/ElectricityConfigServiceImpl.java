package com.xiliulou.electricity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.entity.ElectricityConfig;
import com.xiliulou.electricity.entity.ElectricityPayParams;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.entity.FranchiseeMoveInfo;
import com.xiliulou.electricity.mapper.ElectricityConfigMapper;
import com.xiliulou.electricity.query.ElectricityConfigAddAndUpdateQuery;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.TenantConfigVO;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    @Autowired
    FranchiseeService franchiseeService;
    @Autowired
    FranchiseeInsuranceService franchiseeInsuranceService;
    @Autowired
    ElectricityMemberCardService electricityMemberCardService;


    @Override
    @Transactional(rollbackFor = Exception.class)
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

        String franchiseeMoveDetail = null;
        //若开启了迁移加盟商
        if (Objects.equals(electricityConfigAddAndUpdateQuery.getIsMoveFranchisee(), ElectricityConfig.MOVE_FRANCHISEE_OPEN)) {
            if (Objects.isNull(electricityConfigAddAndUpdateQuery.getFranchiseeMoveInfo()) || Objects.isNull(electricityConfigAddAndUpdateQuery.getFranchiseeMoveInfo().getBatteryModel())) {
                return R.fail("ELECTRICITY.0007", "加盟商迁移信息不能为空");
            }

            //旧加盟商校验
            FranchiseeMoveInfo franchiseeMoveInfoQuery = electricityConfigAddAndUpdateQuery.getFranchiseeMoveInfo();
            Franchisee oldFranchisee = franchiseeService.queryByIdFromCache(franchiseeMoveInfoQuery.getFromFranchiseeId());
            if (Objects.isNull(oldFranchisee)) {
                log.error("ELE ERROR! not found old franchisee,franchiseeId={}", franchiseeMoveInfoQuery.getFromFranchiseeId());
                return R.fail("ELECTRICITY.0038", "旧加盟商不存在");
            }
            if (Objects.equals(oldFranchisee.getModelType(), Franchisee.NEW_MODEL_TYPE)) {
                log.error("ELE ERROR! old franchisee not allow new MODEL TYPE,franchiseeId={}", franchiseeMoveInfoQuery.getFromFranchiseeId());
                return R.fail("100350", "旧加盟商不能为多型号");
            }

            //新加盟商校验
            Franchisee newFranchisee = franchiseeService.queryByIdFromCache(franchiseeMoveInfoQuery.getToFranchiseeId());
            if (Objects.isNull(newFranchisee)) {
                log.error("ELE ERROR! not found new franchisee,franchiseeId={}", franchiseeMoveInfoQuery.getToFranchiseeId());
                return R.fail("ELECTRICITY.0038", "新加盟商不存在");
            }
            if (Objects.equals(newFranchisee.getModelType(), Franchisee.OLD_MODEL_TYPE)) {
                log.error("ELE ERROR! new franchisee not allow new MODEL TYPE,franchiseeId={}", franchiseeMoveInfoQuery.getToFranchiseeId());
                return R.fail("100351", "新加盟商不能为单型号");
            }


            FranchiseeMoveInfo franchiseeMoveInfo = new FranchiseeMoveInfo();
            franchiseeMoveInfo.setFromFranchiseeId(electricityConfigAddAndUpdateQuery.getFranchiseeMoveInfo().getFromFranchiseeId());
            franchiseeMoveInfo.setToFranchiseeId(electricityConfigAddAndUpdateQuery.getFranchiseeMoveInfo().getToFranchiseeId());
            franchiseeMoveInfo.setBatteryModel(electricityConfigAddAndUpdateQuery.getFranchiseeMoveInfo().getBatteryModel());
            franchiseeMoveInfo.setFromFranchiseeName(oldFranchisee.getName());
            franchiseeMoveInfo.setToFranchiseeName(newFranchisee.getName());
            franchiseeMoveDetail = JsonUtil.toJson(franchiseeMoveInfo);

            //将旧加盟商下套餐迁移到新加盟商
            electricityMemberCardService.moveMemberCard(franchiseeMoveInfo,newFranchisee);


            //将旧加盟商下保险迁移到新加盟商
            franchiseeInsuranceService.moveInsurance(franchiseeMoveInfo,newFranchisee);


        }


        ElectricityConfig electricityConfig = electricityConfigMapper.selectOne(new LambdaQueryWrapper<ElectricityConfig>().eq(ElectricityConfig::getTenantId, TenantContextHolder.getTenantId()));
        if (Objects.isNull(electricityConfig)) {
            electricityConfig = new ElectricityConfig();
            electricityConfig.setName(electricityConfigAddAndUpdateQuery.getName());
            electricityConfig.setOrderTime(electricityConfigAddAndUpdateQuery.getOrderTime());
            electricityConfig.setIsManualReview(electricityConfigAddAndUpdateQuery.getIsManualReview());
            electricityConfig.setIsWithdraw(electricityConfigAddAndUpdateQuery.getIsWithdraw());
            electricityConfig.setCreateTime(System.currentTimeMillis());
            electricityConfig.setUpdateTime(System.currentTimeMillis());
            electricityConfig.setTenantId(TenantContextHolder.getTenantId());
            electricityConfig.setIsOpenDoorLock(electricityConfigAddAndUpdateQuery.getIsOpenDoorLock());
            electricityConfig.setIsBatteryReview(electricityConfigAddAndUpdateQuery.getIsBatteryReview());
            electricityConfig.setDisableMemberCard(electricityConfigAddAndUpdateQuery.getDisableMemberCard());
            electricityConfig.setIsLowBatteryExchange(electricityConfigAddAndUpdateQuery.getIsLowBatteryExchange());
            electricityConfig.setLowBatteryExchangeModel(electricityConfigAddAndUpdateQuery.getLowBatteryExchangeModel());
            electricityConfig.setIsEnableSelfOpen(electricityConfigAddAndUpdateQuery.getIsEnableSelfOpen());
            electricityConfig.setIsEnableReturnBoxCheck(electricityConfigAddAndUpdateQuery.getIsEnableReturnBoxCheck());
            electricityConfig.setIsMoveFranchisee(electricityConfigAddAndUpdateQuery.getIsMoveFranchisee());
            electricityConfig.setFranchiseeMoveInfo(franchiseeMoveDetail);
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
        electricityConfig.setIsMoveFranchisee(electricityConfigAddAndUpdateQuery.getIsMoveFranchisee());
        electricityConfig.setFranchiseeMoveInfo(franchiseeMoveDetail);
        int updateResult = electricityConfigMapper.updateById(electricityConfig);
        if (updateResult > 0) {
            redisService.delete(CacheConstant.CACHE_ELE_SET_CONFIG + TenantContextHolder.getTenantId());
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
