package com.xiliulou.electricity.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.EleEsignConstant;
import com.xiliulou.electricity.dto.FranchiseeBatteryModelDTO;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.mapper.ElectricityConfigMapper;
import com.xiliulou.electricity.query.ElectricityConfigAddAndUpdateQuery;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.TenantConfigVO;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
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
    
        //实名审核方式若为人脸核身
        if(Objects.equals(electricityConfigAddAndUpdateQuery.getIsManualReview(),ElectricityConfig.FACE_REVIEW)){
            //是否购买资源包
            FaceRecognizeData faceRecognizeData = faceRecognizeDataService.selectByTenantId(TenantContextHolder.getTenantId());
            if(Objects.isNull(faceRecognizeData)){
                return R.fail("100334", "未购买人脸核身资源包，请联系管理员");
            }
        
            //资源包是否可用
            if(faceRecognizeData.getFaceRecognizeCapacity()<=0){
                return R.fail("100335", "人脸核身资源包余额不足，请充值");
            }
        }

        String franchiseeMoveDetail = null;
        //若开启了迁移加盟商
        if (Objects.equals(electricityConfigAddAndUpdateQuery.getIsMoveFranchisee(), ElectricityConfig.MOVE_FRANCHISEE_OPEN)) {
            if (Objects.isNull(electricityConfigAddAndUpdateQuery.getFranchiseeMoveInfo()) || Objects.isNull(electricityConfigAddAndUpdateQuery.getFranchiseeMoveInfo().getBatteryModel())) {
                return R.fail("ELECTRICITY.0007", "加盟商迁移信息不能为空");
            }

            FranchiseeMoveInfo franchiseeMoveInfoQuery = electricityConfigAddAndUpdateQuery.getFranchiseeMoveInfo();

            Franchisee oldFranchisee = franchiseeService.queryByIdFromCache(franchiseeMoveInfoQuery.getFromFranchiseeId());
            Franchisee newFranchisee = franchiseeService.queryByIdFromCache(franchiseeMoveInfoQuery.getToFranchiseeId());

            Triple<Boolean, String, Object> verifyFranchiseeResult = verifyFranchisee(oldFranchisee, newFranchisee, franchiseeMoveInfoQuery);
            if (!verifyFranchiseeResult.getLeft()) {
                return R.fail(verifyFranchiseeResult.getMiddle(), (String) verifyFranchiseeResult.getRight());
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

            //将旧加盟商下的车辆型号迁移到新加盟商下
            electricityCarModelService.moveCarModel(franchiseeMoveInfo);
        }

        //若开启免押
        if (Objects.nonNull(electricityConfigAddAndUpdateQuery.getFreeDepositType()) && !Objects.equals(electricityConfigAddAndUpdateQuery.getFreeDepositType(), ElectricityConfig.FREE_DEPOSIT_TYPE_DEFAULT)) {
            //检查免押配置
            PxzConfig pxzConfig = pxzConfigService.queryByTenantIdFromCache(TenantContextHolder.getTenantId());
            if (Objects.isNull(pxzConfig) || StringUtils.isBlank(pxzConfig.getAesKey()) || StringUtils.isBlank(pxzConfig.getMerchantCode())) {
                return R.fail("100400", "免押功能未配置相关信息");
            }
        }

        //若开启签名功能，需要检查签名相关信息是否已经配置
        if(Objects.equals(electricityConfigAddAndUpdateQuery.getIsEnableEsign(), EleEsignConstant.ESIGN_ENABLE)){
            EleEsignConfig eleEsignConfig = eleEsignConfigService.selectLatestByTenantId(TenantContextHolder.getTenantId());
            if (Objects.isNull(eleEsignConfig) || StringUtils.isBlank(eleEsignConfig.getAppId()) || StringUtils.isBlank(eleEsignConfig.getAppSecret())){
                return R.fail("100500", "电子签名功能未配置相关信息,请检查");
            }
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
            electricityConfig.setFreeDepositType(electricityConfigAddAndUpdateQuery.getFreeDepositType());
            electricityConfig.setIsMoveFranchisee(electricityConfigAddAndUpdateQuery.getIsMoveFranchisee());
            electricityConfig.setFranchiseeMoveInfo(franchiseeMoveDetail);
            electricityConfig.setIsOpenCarBatteryBind(electricityConfigAddAndUpdateQuery.getIsOpenCarBatteryBind());
            electricityConfig.setIsOpenCarControl(electricityConfigAddAndUpdateQuery.getIsOpenCarControl());
            electricityConfig.setIsZeroDepositAuditEnabled(electricityConfigAddAndUpdateQuery.getIsZeroDepositAuditEnabled());
            electricityConfig.setIsEnableEsign(electricityConfigAddAndUpdateQuery.getIsEnableEsign());
            electricityConfig.setAllowRentEle(electricityConfigAddAndUpdateQuery.getAllowRentEle());
            electricityConfig.setAllowReturnEle(electricityConfigAddAndUpdateQuery.getAllowReturnEle());
            electricityConfigMapper.insert(electricityConfig);
            return R.ok();
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
        electricityConfig.setIsEnableSelfOpen(electricityConfigAddAndUpdateQuery.getIsEnableSelfOpen());
        electricityConfig.setIsEnableReturnBoxCheck(electricityConfigAddAndUpdateQuery.getIsEnableReturnBoxCheck());
        electricityConfig.setIsOpenInsurance(electricityConfigAddAndUpdateQuery.getIsOpenInsurance());
        electricityConfig.setFreeDepositType(electricityConfigAddAndUpdateQuery.getFreeDepositType());
        electricityConfig.setIsMoveFranchisee(electricityConfigAddAndUpdateQuery.getIsMoveFranchisee());
        electricityConfig.setFranchiseeMoveInfo(franchiseeMoveDetail);
        electricityConfig.setIsOpenCarBatteryBind(electricityConfigAddAndUpdateQuery.getIsOpenCarBatteryBind());
        electricityConfig.setIsOpenCarControl(electricityConfigAddAndUpdateQuery.getIsOpenCarControl());
        electricityConfig.setIsZeroDepositAuditEnabled(electricityConfigAddAndUpdateQuery.getIsZeroDepositAuditEnabled());
        electricityConfig.setIsEnableEsign(electricityConfigAddAndUpdateQuery.getIsEnableEsign());
        electricityConfig.setAllowRentEle(electricityConfigAddAndUpdateQuery.getAllowRentEle());
        electricityConfig.setAllowReturnEle(electricityConfigAddAndUpdateQuery.getAllowReturnEle());
        int updateResult = electricityConfigMapper.update(electricityConfig);
        if (updateResult > 0) {
            redisService.delete(CacheConstant.CACHE_ELE_SET_CONFIG + TenantContextHolder.getTenantId());
        }

        return R.ok();
    }

    private Triple<Boolean, String, Object> verifyFranchisee(Franchisee oldFranchisee, Franchisee newFranchisee, FranchiseeMoveInfo franchiseeMoveInfoQuery) {
        //旧加盟商校验
        if (Objects.isNull(oldFranchisee)) {
            log.error("ELE ERROR! not found old franchisee,franchiseeId={}", franchiseeMoveInfoQuery.getFromFranchiseeId());
            return Triple.of(false, "ELECTRICITY.0038", "旧加盟商不存在");
        }
        if (Objects.equals(oldFranchisee.getModelType(), Franchisee.NEW_MODEL_TYPE)) {
            log.error("ELE ERROR! old franchisee not allow new MODEL TYPE,franchiseeId={}", franchiseeMoveInfoQuery.getFromFranchiseeId());
            return Triple.of(false, "100350", "旧加盟商不能为多型号");
        }

        //新加盟商校验
        if (Objects.isNull(newFranchisee)) {
            log.error("ELE ERROR! not found new franchisee,franchiseeId={}", franchiseeMoveInfoQuery.getToFranchiseeId());
            return Triple.of(false, "ELECTRICITY.0038", "新加盟商不存在");
        }
        if (Objects.equals(newFranchisee.getModelType(), Franchisee.OLD_MODEL_TYPE)) {
            log.error("ELE ERROR! new franchisee not allow new MODEL TYPE,franchiseeId={}", franchiseeMoveInfoQuery.getToFranchiseeId());
            return Triple.of(false, "100351", "新加盟商不能为单型号");
        }

        List<FranchiseeBatteryModelDTO> franchiseeBatteryModels = JsonUtil.fromJsonArray(newFranchisee.getModelBatteryDeposit(), FranchiseeBatteryModelDTO.class);
        if (CollectionUtils.isEmpty(franchiseeBatteryModels)) {
            log.error("ELE ERROR!not found newFranchiseeBatteryModelDTO,franchinseeId={}", newFranchisee.getId());
            return Triple.of(false, "100355", "加盟商电池型号信息不存在");
        }

        FranchiseeBatteryModelDTO franchiseeBatteryModelDTO = franchiseeBatteryModels.stream().filter(item -> Objects.equals(item.getModel(), franchiseeMoveInfoQuery.getBatteryModel())).findFirst().orElse(null);
        if (Objects.isNull(franchiseeBatteryModelDTO)) {
            log.error("ELE ERROR!franchiseeBatteryModelDTO is null,franchinseeId={}", newFranchisee.getId());
            return Triple.of(false, "100355", "加盟商电池型号信息不存在");
        }

        //加盟商押金校验
        if (oldFranchisee.getBatteryDeposit().compareTo(franchiseeBatteryModelDTO.getBatteryDeposit()) != 0) {
            log.error("ELE ERROR! oldFranchisee batteryDeposit not equals newFranchisee,oldFranchinseeId={},newFranchinseeId={}", newFranchisee.getId(), oldFranchisee.getId());
            return Triple.of(false, "100363", "新加盟商与旧加盟商押金不一致");
        }

        //加盟商电池服务费开关校验
        if (!Objects.equals(oldFranchisee.getIsOpenServiceFee(), newFranchisee.getIsOpenServiceFee())) {
            log.error("ELE ERROR! IsOpenServiceFee new franchisee not equals old franchisee,newfranchiseeId={},oldfranchiseeId={}", newFranchisee.getId(), oldFranchisee.getId());
            return Triple.of(false, "100367", "新加盟商与旧加盟商电池服务费开关不一致");
        }

        //加盟商电池服务费校验
        if (Objects.equals(oldFranchisee.getIsOpenServiceFee(), Franchisee.OPEN_SERVICE_FEE)
                && Objects.equals(newFranchisee.getIsOpenServiceFee(), Franchisee.OPEN_SERVICE_FEE)
                && oldFranchisee.getBatteryServiceFee().compareTo(franchiseeBatteryModelDTO.getBatteryServiceFee()) != 0) {
            log.error("ELE ERROR! oldFranchisee batteryServiceFee not equals newFranchisee,oldFranchinseeId={},newFranchinseeId={}", newFranchisee.getId(), oldFranchisee.getId());
            return Triple.of(false, "100364", "新加盟商与旧加盟商电池服务费不一致");
        }

        return Triple.of(true, "", null);
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
