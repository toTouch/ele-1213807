package com.xiliulou.electricity.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.map.MapUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.base.enums.ChannelEnum;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.EleEsignConstant;
import com.xiliulou.electricity.entity.AlipayAppConfig;
import com.xiliulou.electricity.entity.EleEsignConfig;
import com.xiliulou.electricity.entity.ElectricityConfig;
import com.xiliulou.electricity.entity.ElectricityConfigExtra;
import com.xiliulou.electricity.entity.ElectricityPayParams;
import com.xiliulou.electricity.entity.FaceRecognizeData;
import com.xiliulou.electricity.entity.PxzConfig;
import com.xiliulou.electricity.entity.meituan.MeiTuanRiderMallConfig;
import com.xiliulou.electricity.enums.CheckFreezeDaysSourceEnum;
import com.xiliulou.electricity.enums.YesNoEnum;
import com.xiliulou.electricity.enums.thirdParthMall.MeiTuanRiderMallEnum;
import com.xiliulou.electricity.exception.BizException;
import com.xiliulou.electricity.mapper.ElectricityConfigMapper;
import com.xiliulou.electricity.query.ElectricityConfigAddAndUpdateQuery;
import com.xiliulou.electricity.query.ElectricityConfigWxCustomerQuery;
import com.xiliulou.electricity.service.AlipayAppConfigService;
import com.xiliulou.electricity.service.EleEsignConfigService;
import com.xiliulou.electricity.service.ElectricityCarModelService;
import com.xiliulou.electricity.service.ElectricityConfigExtraService;
import com.xiliulou.electricity.service.ElectricityConfigService;
import com.xiliulou.electricity.service.ElectricityMemberCardService;
import com.xiliulou.electricity.service.ElectricityPayParamsService;
import com.xiliulou.electricity.service.FaceRecognizeDataService;
import com.xiliulou.electricity.service.FranchiseeInsuranceService;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.PxzConfigService;
import com.xiliulou.electricity.service.ServicePhoneService;
import com.xiliulou.electricity.service.UserService;
import com.xiliulou.electricity.service.template.TemplateConfigService;
import com.xiliulou.electricity.service.thirdPartyMall.MeiTuanRiderMallConfigService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.OperateRecordUtil;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.TenantConfigVO;
import com.xiliulou.security.bean.TokenUser;
import com.xiliulou.security.constant.TokenConstant;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.CollectionUtils;

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
    
    @Autowired
    ElectricityCarModelService electricityCarModelService;
    
    @Autowired
    FaceRecognizeDataService faceRecognizeDataService;
    
    @Autowired
    PxzConfigService pxzConfigService;
    
    @Autowired
    EleEsignConfigService eleEsignConfigService;
    
    @Autowired
    AlipayAppConfigService alipayAppConfigService;
    
    @Resource
    MeiTuanRiderMallConfigService meiTuanRiderMallConfigService;
    
    @Autowired
    OperateRecordUtil operateRecordUtil;
    
    @Autowired
    ServicePhoneService servicePhoneService;
    
    @Resource
    private ElectricityConfigExtraService electricityConfigExtraService;


    @Override
    public R edit(ElectricityConfigAddAndUpdateQuery electricityConfigAddAndUpdateQuery) {
        // 用户
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        // 操作频繁
        boolean result = redisService.setNx(CacheConstant.ELE_CONFIG_EDIT_UID + user.getUid(), "1", 3 * 1000L, false);
        if (!result) {
            return R.fail("ELECTRICITY.0034", "操作频繁");
        }
        
        // 实名审核方式若为人脸核身
        if (Objects.equals(electricityConfigAddAndUpdateQuery.getIsManualReview(), ElectricityConfig.FACE_REVIEW)) {
            // 是否购买资源包
            FaceRecognizeData faceRecognizeData = faceRecognizeDataService.selectByTenantId(TenantContextHolder.getTenantId());
            if (Objects.isNull(faceRecognizeData)) {
                return R.fail("100334", "未购买人脸核身资源包，请联系管理员");
            }
            
            // 资源包是否可用
            if (faceRecognizeData.getFaceRecognizeCapacity() <= 0) {
                return R.fail("100335", "人脸核身资源包余额不足，请充值");
            }
        }
        
        // 若开启免押
        if (Objects.nonNull(electricityConfigAddAndUpdateQuery.getFreeDepositType()) && !Objects.equals(electricityConfigAddAndUpdateQuery.getFreeDepositType(),
                ElectricityConfig.FREE_DEPOSIT_TYPE_DEFAULT)) {
            // 检查免押配置
            PxzConfig pxzConfig = pxzConfigService.queryByTenantIdFromCache(TenantContextHolder.getTenantId());
            if (Objects.isNull(pxzConfig) || StringUtils.isBlank(pxzConfig.getAesKey()) || StringUtils.isBlank(pxzConfig.getMerchantCode())) {
                return R.fail("100400", "免押功能未配置相关信息");
            }
        }
        
        // 若开启签名功能，需要检查签名相关信息是否已经配置
        if (Objects.equals(electricityConfigAddAndUpdateQuery.getIsEnableEsign(), EleEsignConstant.ESIGN_ENABLE)) {
            EleEsignConfig eleEsignConfig = eleEsignConfigService.selectLatestByTenantId(TenantContextHolder.getTenantId());
            if (Objects.isNull(eleEsignConfig) || StringUtils.isBlank(eleEsignConfig.getAppId()) || StringUtils.isBlank(eleEsignConfig.getAppSecret())) {
                return R.fail("100500", "电子签名功能未配置相关信息,请检查");
            }
        }
        
        // 若开启美团骑手商城
        if (Objects.equals(electricityConfigAddAndUpdateQuery.getIsEnableMeiTuanRiderMall(), MeiTuanRiderMallEnum.ENABLE_MEI_TUAN_RIDER_MALL.getCode())) {
            MeiTuanRiderMallConfig meiTuanRiderMallConfig = meiTuanRiderMallConfigService.queryByTenantIdFromCache(TenantContextHolder.getTenantId());
            if (Objects.isNull(meiTuanRiderMallConfig) || StringUtils.isBlank(meiTuanRiderMallConfig.getAppId()) || StringUtils.isBlank(meiTuanRiderMallConfig.getAppKey())
                    || StringUtils.isBlank(meiTuanRiderMallConfig.getSecret())) {
                return R.fail("120130", "美团骑手商城功能未配置相关信息,请检查");
            }
        }
        
        if (Objects.equals(electricityConfigAddAndUpdateQuery.getIsComfortExchange(), ElectricityConfig.COMFORT_EXCHANGE) && Objects.isNull(
                electricityConfigAddAndUpdateQuery.getPriorityExchangeNorm())) {
            return R.fail("100668", "优先换电标准不能为空");
        }
        
        if (Objects.equals(electricityConfigAddAndUpdateQuery.getIsComfortExchange(), ElectricityConfig.COMFORT_EXCHANGE) && Objects.nonNull(
                electricityConfigAddAndUpdateQuery.getPriorityExchangeNorm()) && (
                Double.compare(electricityConfigAddAndUpdateQuery.getPriorityExchangeNorm(), ElectricityConfigAddAndUpdateQuery.MIN_NORM) < 0
                        || Double.compare(electricityConfigAddAndUpdateQuery.getPriorityExchangeNorm(), ElectricityConfigAddAndUpdateQuery.MAX_NORM) > 0)) {
            return R.fail("100669", "电量标准须满足50-100");
        }
        
        ElectricityConfig electricityConfig = electricityConfigMapper.selectOne(
                new LambdaQueryWrapper<ElectricityConfig>().eq(ElectricityConfig::getTenantId, TenantContextHolder.getTenantId()));
        ElectricityConfig oldElectricityConfig = new ElectricityConfig();
        BeanUtil.copyProperties(electricityConfig, oldElectricityConfig, CopyOptions.create().ignoreNullValue().ignoreError());
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
            electricityConfig.setIsSelectionExchange(electricityConfigAddAndUpdateQuery.getIsSelectionExchange());
            electricityConfig.setLowBatteryExchangeModel(electricityConfigAddAndUpdateQuery.getLowBatteryExchangeModel());
            electricityConfig.setIsEnableSelfOpen(electricityConfigAddAndUpdateQuery.getIsEnableSelfOpen());
            electricityConfig.setFreeDepositType(electricityConfigAddAndUpdateQuery.getFreeDepositType());
            electricityConfig.setIsOpenCarBatteryBind(electricityConfigAddAndUpdateQuery.getIsOpenCarBatteryBind());
            electricityConfig.setIsOpenCarControl(electricityConfigAddAndUpdateQuery.getIsOpenCarControl());
            electricityConfig.setIsZeroDepositAuditEnabled(electricityConfigAddAndUpdateQuery.getIsZeroDepositAuditEnabled());
            electricityConfig.setIsEnableEsign(electricityConfigAddAndUpdateQuery.getIsEnableEsign());
            electricityConfig.setAllowFreezeWithAssets(electricityConfigAddAndUpdateQuery.getAllowFreezeWithAssets());
            electricityConfig.setWxCustomer(ElectricityConfig.CLOSE_WX_CUSTOMER);
            electricityConfig.setChannelTimeLimit(electricityConfigAddAndUpdateQuery.getChannelTimeLimit());
            electricityConfig.setChargeRateType(electricityConfigAddAndUpdateQuery.getChargeRateType());
            electricityConfig.setAlipayCustomer(electricityConfigAddAndUpdateQuery.getAlipayCustomer());
            electricityConfig.setIsComfortExchange(electricityConfigAddAndUpdateQuery.getIsComfortExchange());
            electricityConfig.setPriorityExchangeNorm(electricityConfigAddAndUpdateQuery.getPriorityExchangeNorm());
            electricityConfig.setIsEnableMeiTuanRiderMall(electricityConfigAddAndUpdateQuery.getIsEnableMeiTuanRiderMall());
            electricityConfig.setEleLimit(electricityConfigAddAndUpdateQuery.getEleLimit());
            electricityConfig.setEleLimitCount(electricityConfigAddAndUpdateQuery.getEleLimitCount());
            electricityConfig.setIsEnableFlexibleRenewal(electricityConfigAddAndUpdateQuery.getIsEnableFlexibleRenewal());
            electricityConfig.setIsEnableSeparateDeposit(electricityConfigAddAndUpdateQuery.getIsEnableSeparateDeposit());
            electricityConfig.setIsSwapExchange(electricityConfigAddAndUpdateQuery.getIsSwapExchange());
            electricityConfig.setFreezeAutoReviewDays(electricityConfigAddAndUpdateQuery.getFreezeAutoReviewDays());
            electricityConfig.setPackageFreezeCount(electricityConfigAddAndUpdateQuery.getPackageFreezeCount());
            electricityConfig.setPackageFreezeDays(electricityConfigAddAndUpdateQuery.getPackageFreezeDays());
            electricityConfig.setPackageFreezeDaysWithAssets(electricityConfigAddAndUpdateQuery.getPackageFreezeDaysWithAssets());
            electricityConfig.setExpiredProtectionTime(electricityConfigAddAndUpdateQuery.getExpiredProtectionTime());
            electricityConfig.setIsBindBattery(electricityConfigAddAndUpdateQuery.getIsBindBattery());

            electricityConfig.setAllowOriginalInviter(electricityConfigAddAndUpdateQuery.getAllowOriginalInviter());
            electricityConfig.setLostUserDays(electricityConfigAddAndUpdateQuery.getLostUserDays());
            electricityConfig.setLostUserFirst(electricityConfigAddAndUpdateQuery.getLostUserFirst());
            electricityConfigMapper.insert(electricityConfig);

            editElectricityConfigExtra(electricityConfigAddAndUpdateQuery);
            return R.ok();
        }
        
        // 如果选仓换电的配置有更新，则需要更新redis缓存
        Integer selectionExchangeDB = electricityConfig.getIsSelectionExchange();
        Integer selectionExchangeUpdate = electricityConfigAddAndUpdateQuery.getIsSelectionExchange();
        if (Objects.nonNull(selectionExchangeDB) && !Objects.equals(selectionExchangeDB, selectionExchangeUpdate)) {
            redisService.set(CacheConstant.CACHE_ELE_SELECTION_EXCHANGE_UPDATE_TIME + TenantContextHolder.getTenantId(), String.valueOf(System.currentTimeMillis()));
        }
        
        electricityConfig.setTenantId(TenantContextHolder.getTenantId());
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
        electricityConfig.setIsSelectionExchange(electricityConfigAddAndUpdateQuery.getIsSelectionExchange());
        electricityConfig.setIsEnableSelfOpen(electricityConfigAddAndUpdateQuery.getIsEnableSelfOpen());
        electricityConfig.setIsOpenInsurance(electricityConfigAddAndUpdateQuery.getIsOpenInsurance());
        electricityConfig.setFreeDepositType(electricityConfigAddAndUpdateQuery.getFreeDepositType());
        electricityConfig.setIsOpenCarBatteryBind(electricityConfigAddAndUpdateQuery.getIsOpenCarBatteryBind());
        electricityConfig.setIsOpenCarControl(electricityConfigAddAndUpdateQuery.getIsOpenCarControl());
        electricityConfig.setIsZeroDepositAuditEnabled(electricityConfigAddAndUpdateQuery.getIsZeroDepositAuditEnabled());
        electricityConfig.setIsEnableEsign(electricityConfigAddAndUpdateQuery.getIsEnableEsign());
        electricityConfig.setAllowFreezeWithAssets(electricityConfigAddAndUpdateQuery.getAllowFreezeWithAssets());
        electricityConfig.setChannelTimeLimit(electricityConfigAddAndUpdateQuery.getChannelTimeLimit());
        electricityConfig.setChargeRateType(electricityConfigAddAndUpdateQuery.getChargeRateType());
        electricityConfig.setAlipayCustomer(electricityConfigAddAndUpdateQuery.getAlipayCustomer());
        electricityConfig.setIsComfortExchange(electricityConfigAddAndUpdateQuery.getIsComfortExchange());
        electricityConfig.setPriorityExchangeNorm(electricityConfigAddAndUpdateQuery.getPriorityExchangeNorm());
        electricityConfig.setIsEnableMeiTuanRiderMall(electricityConfigAddAndUpdateQuery.getIsEnableMeiTuanRiderMall());
        electricityConfig.setEleLimit(electricityConfigAddAndUpdateQuery.getEleLimit());
        electricityConfig.setEleLimitCount(electricityConfigAddAndUpdateQuery.getEleLimitCount());
        electricityConfig.setIsEnableFlexibleRenewal(electricityConfigAddAndUpdateQuery.getIsEnableFlexibleRenewal());
        electricityConfig.setIsEnableSeparateDeposit(electricityConfigAddAndUpdateQuery.getIsEnableSeparateDeposit());
        electricityConfig.setIsSwapExchange(electricityConfigAddAndUpdateQuery.getIsSwapExchange());
        electricityConfig.setFreezeAutoReviewDays(electricityConfigAddAndUpdateQuery.getFreezeAutoReviewDays());
        electricityConfig.setPackageFreezeCount(electricityConfigAddAndUpdateQuery.getPackageFreezeCount());
        electricityConfig.setPackageFreezeDays(electricityConfigAddAndUpdateQuery.getPackageFreezeDays());
        electricityConfig.setPackageFreezeDaysWithAssets(electricityConfigAddAndUpdateQuery.getPackageFreezeDaysWithAssets());
        electricityConfig.setExpiredProtectionTime(electricityConfigAddAndUpdateQuery.getExpiredProtectionTime());
        electricityConfig.setIsBindBattery(electricityConfigAddAndUpdateQuery.getIsBindBattery());

        electricityConfig.setAllowOriginalInviter(electricityConfigAddAndUpdateQuery.getAllowOriginalInviter());
        electricityConfig.setLostUserDays(electricityConfigAddAndUpdateQuery.getLostUserDays());
        electricityConfig.setLostUserFirst(electricityConfigAddAndUpdateQuery.getLostUserFirst());

        electricityConfigMapper.update(electricityConfig);
        editElectricityConfigExtra(electricityConfigAddAndUpdateQuery);

        // 清理缓存
        redisService.delete(CacheConstant.CACHE_ELE_SET_CONFIG + TenantContextHolder.getTenantId());
        
        operateRecordUtil.record(oldElectricityConfig, electricityConfig);
        return R.ok();
    }
    
    private void editElectricityConfigExtra(ElectricityConfigAddAndUpdateQuery electricityConfigAddAndUpdateQuery) {
        Integer tenantId = TenantContextHolder.getTenantId();
        ElectricityConfigExtra electricityConfigExtra = electricityConfigExtraService.queryByTenantId(tenantId);
        ElectricityConfigExtra oldElectricityConfigExtra = new ElectricityConfigExtra();
        BeanUtil.copyProperties(electricityConfigExtra, oldElectricityConfigExtra, CopyOptions.create().ignoreNullValue().ignoreError());
        if (Objects.isNull(electricityConfigExtra)) {
            electricityConfigExtraService.insert(
                    ElectricityConfigExtra.builder().tenantId(tenantId).createTime(System.currentTimeMillis()).updateTime(System.currentTimeMillis()).build());
        } else {
            electricityConfigExtra.setAccountDelSwitch(electricityConfigAddAndUpdateQuery.getAccountDelSwitch());
        }

        // 清理缓存
        redisService.delete(CacheConstant.CACHE_ELE_SET_CONFIG_EXTRA + tenantId);

        operateRecordUtil.record(oldElectricityConfigExtra, electricityConfigExtra);
    }

    @Override
    public ElectricityConfig queryFromCacheByTenantId(Integer tenantId) {
        ElectricityConfig cache = redisService.getWithHash(CacheConstant.CACHE_ELE_SET_CONFIG + tenantId, ElectricityConfig.class);
        if (Objects.nonNull(cache)) {
            return cache;
        }
        
        ElectricityConfig electricityConfig = electricityConfigMapper.selectOne(new LambdaQueryWrapper<ElectricityConfig>().eq(ElectricityConfig::getTenantId, tenantId));
        
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
        
        // 根据appId获取租户id
        ElectricityPayParams electricityPayParams = electricityPayParamsService.selectTenantId(appId);
        if (Objects.isNull(electricityPayParams)) {
            log.warn("ELE WARN! not found tenant,appId={}", appId);
            return tenantConfigVO;
        }
        Integer tenantId = electricityPayParams.getTenantId();
        
        // 获取租户配置信息
        ElectricityConfig electricityConfig = this.queryFromCacheByTenantId(tenantId);
        BeanUtils.copyProperties(electricityConfig, tenantConfigVO);
        
        // 获取租户模板id
        List<String> templateConfigList = templateConfigService.queryTemplateIdByTenantIdChannel(tenantId, ChannelEnum.WECHAT.getCode());
        tenantConfigVO.setTemplateConfigList(templateConfigList);
        
        // 获取客服电话
        String servicePhone = userService.selectServicePhone(tenantId);
        tenantConfigVO.setServicePhone(servicePhone);
        tenantConfigVO.setServicePhones(servicePhoneService.listPhones(tenantId));
        tenantConfigVO.setIsMoveFranchisee(ElectricityConfig.MOVE_FRANCHISEE_CLOSE);
        
        return tenantConfigVO;
    }
    
    @Override
    public TenantConfigVO queryTenantConfigByAppId(String appId, String appType) {
        TenantConfigVO tenantConfigVO = new TenantConfigVO();
        
        Integer tenantId = null;
        String channel = null;
        if (TokenConstant.THIRD_AUTH_WX_PRO.equals(appType)) {
            ElectricityPayParams electricityPayParams = electricityPayParamsService.selectTenantId(appId);
            if (Objects.isNull(electricityPayParams)) {
                log.warn("ELE WARN! not found tenant,appId={}", appId);
                return tenantConfigVO;
            }
            
            tenantId = electricityPayParams.getTenantId();
            channel = ChannelEnum.WECHAT.getCode();
        }
        
        if (TokenConstant.THIRD_AUTH_ALI_PAY.equals(appType)) {
            List<AlipayAppConfig> alipayAppConfigs = alipayAppConfigService.queryListByAppId(appId);
            if (CollectionUtils.isEmpty(alipayAppConfigs)) {
                log.warn("ELE WARN! not found alipayAppConfig,appId={}", appId);
                return tenantConfigVO;
            }
            
            tenantId = alipayAppConfigs.stream().findFirst().get().getTenantId();
            channel = ChannelEnum.ALIPAY.getCode();
        }
        
        // 获取租户配置信息
        ElectricityConfig electricityConfig = this.queryFromCacheByTenantId(tenantId);
        BeanUtils.copyProperties(electricityConfig, tenantConfigVO);
        
        // 获取租户模板id
        List<String> templateConfigList = templateConfigService.queryTemplateIdByTenantIdChannel(tenantId, channel);
        tenantConfigVO.setTemplateConfigList(templateConfigList);
        
        // 获取客服电话
        String servicePhone = userService.selectServicePhone(tenantId);
        tenantConfigVO.setServicePhone(servicePhone);
        tenantConfigVO.setServicePhones(servicePhoneService.listPhones(tenantId));
        tenantConfigVO.setIsMoveFranchisee(ElectricityConfig.MOVE_FRANCHISEE_CLOSE);
        
        return tenantConfigVO;
    }
    
    @Slave
    @Override
    public Boolean checkFreezeAutoReviewAndDays(Integer tenantId, Integer days, Long uid, boolean hasAssets, Integer source) throws BizException {
        Boolean autoReviewOrNot = Boolean.TRUE;
        ElectricityConfig electricityConfig = queryFromCacheByTenantId(tenantId);

        if (Objects.isNull(electricityConfig)) {
            throw new BizException("301031", "未找到租户配置信息");
        }

        // 校验是否可以自动审核
        if (Objects.isNull(electricityConfig.getFreezeAutoReviewDays()) || electricityConfig.getFreezeAutoReviewDays() == 0 || electricityConfig.getFreezeAutoReviewDays() < days) {
            log.info("FREEZE AUTO REVIEW CHECK！can not auto review. uid={}", uid);
            autoReviewOrNot = Boolean.FALSE;
        }

        // 校验申请冻结的天数是否合规
        Integer packageFreezeDays = hasAssets ? electricityConfig.getPackageFreezeDaysWithAssets() : electricityConfig.getPackageFreezeDays();
        if (Objects.isNull(packageFreezeDays) || Objects.isNull(electricityConfig.getPackageFreezeCount()) || Objects.equals(electricityConfig.getPackageFreezeCount(), 0)) {
            if (Objects.isNull(days) || days > ElectricityConfig.FREEZE_DAYS_MAX) {
                log.info("FREEZE DAYS CHECK！can not auto review. uid={}", uid);
                throw Objects.equals(source, CheckFreezeDaysSourceEnum.TINY_APP.getCode()) ? new BizException("301033", "超出每次最长天数，请修改")
                        : new BizException("301034", String.format("冻结天数最多为%d天", ElectricityConfig.FREEZE_DAYS_MAX));
            }
        } else {
            if (Objects.isNull(days) || days > packageFreezeDays) {
                throw Objects.equals(source, CheckFreezeDaysSourceEnum.TINY_APP.getCode()) ? new BizException("301033", "超出每次最长天数，请修改")
                        : new BizException("301034", String.format("冻结天数最多为%d天", packageFreezeDays));
            }
        }

        return autoReviewOrNot;
    }

    @Override
    public Triple<Boolean, String, Object> editWxCustomer(ElectricityConfigWxCustomerQuery electricityConfigAddAndUpdateQuery) {
        ElectricityConfig electricityConfig = queryFromCacheByTenantId(TenantContextHolder.getTenantId());
        if (Objects.isNull(electricityConfig)) {
            return Triple.of(false, null, "未找到租户配置信息");
        }
        
        ElectricityConfig updateElectricityConfig = new ElectricityConfig();
        updateElectricityConfig.setId(electricityConfig.getId());
        updateElectricityConfig.setTenantId(TenantContextHolder.getTenantId());
        updateElectricityConfig.setWxCustomer(electricityConfigAddAndUpdateQuery.getEnableWxCustomer());
        updateElectricityConfig.setUpdateTime(System.currentTimeMillis());
        int updateResult = electricityConfigMapper.update(updateElectricityConfig);
        if (updateResult > 0) {
            redisService.delete(CacheConstant.CACHE_ELE_SET_CONFIG + TenantContextHolder.getTenantId());
        }
        return Triple.of(true, null, null);
    }
    
    @Override
    public void updateTenantConfigWxCustomer(Integer status) {
        if (Objects.isNull(TenantContextHolder.getTenantId())) {
            return;
        }
        ElectricityConfig config = electricityConfigMapper.selectElectricityConfigByTenantId(TenantContextHolder.getTenantId());
        ElectricityConfig electricityConfig = new ElectricityConfig();
        electricityConfig.setTenantId(TenantContextHolder.getTenantId());
        electricityConfig.setWxCustomer(status);
        electricityConfig.setUpdateTime(System.currentTimeMillis());
        Integer updateResult = electricityConfigMapper.updateWxCuStatusByTenantId(electricityConfig);
        if (updateResult > 0) {
            if (Objects.isNull(config) || Objects.isNull(config.getWxCustomer()) || !Objects.equals(config.getWxCustomer(), electricityConfig.getWxCustomer())) {
                operateRecordUtil.record(MapUtil.of("wxCustomer", ObjectUtils.defaultIfNull(config.getWxCustomer(),
                                Objects.equals(electricityConfig.getWxCustomer(), YesNoEnum.YES.getCode()) ? YesNoEnum.NO.getCode() : YesNoEnum.YES.getCode())),
                        MapUtil.of("wxCustomer", status));
            }
            redisService.delete(CacheConstant.CACHE_ELE_SET_CONFIG + TenantContextHolder.getTenantId());
        }
    }
    
    @Override
    public ElectricityConfig queryTenantConfigWxCustomer() {
        return electricityConfigMapper.selectElectricityConfigByTenantId(TenantContextHolder.getTenantId());
    }
}
