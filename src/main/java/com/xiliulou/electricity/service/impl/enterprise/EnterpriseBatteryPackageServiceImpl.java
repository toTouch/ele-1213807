package com.xiliulou.electricity.service.impl.enterprise;

import cn.hutool.core.util.ObjectUtil;
import com.google.api.client.util.Lists;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.bo.batteryPackage.UserBatteryMemberCardPackageBO;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.dto.FreeDepositUserDTO;
import com.xiliulou.electricity.entity.BatteryMemberCard;
import com.xiliulou.electricity.entity.EleDepositOrder;
import com.xiliulou.electricity.entity.EleDisableMemberCardRecord;
import com.xiliulou.electricity.entity.EleRefundOrder;
import com.xiliulou.electricity.entity.ElectricityBattery;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.ElectricityConfig;
import com.xiliulou.electricity.entity.ElectricityMemberCardOrder;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.entity.FranchiseeInsurance;
import com.xiliulou.electricity.entity.FreeDepositData;
import com.xiliulou.electricity.entity.FreeDepositOrder;
import com.xiliulou.electricity.entity.InsuranceOrder;
import com.xiliulou.electricity.entity.InsuranceUserInfo;
import com.xiliulou.electricity.entity.MemberCardBatteryType;
import com.xiliulou.electricity.entity.PxzConfig;
import com.xiliulou.electricity.entity.RentBatteryOrder;
import com.xiliulou.electricity.entity.UserBatteryDeposit;
import com.xiliulou.electricity.entity.UserBatteryMemberCard;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.entity.UserOauthBind;
import com.xiliulou.electricity.entity.enterprise.CloudBeanUseRecord;
import com.xiliulou.electricity.entity.enterprise.EnterpriseChannelUser;
import com.xiliulou.electricity.entity.enterprise.EnterpriseChannelUserExit;
import com.xiliulou.electricity.entity.enterprise.EnterpriseInfo;
import com.xiliulou.electricity.enums.BatteryMemberCardBusinessTypeEnum;
import com.xiliulou.electricity.enums.BusinessType;
import com.xiliulou.electricity.enums.PackageTypeEnum;
import com.xiliulou.electricity.enums.enterprise.EnterprisePaymentStatusEnum;
import com.xiliulou.electricity.enums.enterprise.PackageOrderTypeEnum;
import com.xiliulou.electricity.enums.enterprise.RenewalStatusEnum;
import com.xiliulou.electricity.enums.enterprise.UserCostTypeEnum;
import com.xiliulou.electricity.exception.BizException;
import com.xiliulou.electricity.mapper.BatteryMemberCardMapper;
import com.xiliulou.electricity.mapper.enterprise.EnterpriseBatteryPackageMapper;
import com.xiliulou.electricity.mapper.enterprise.EnterpriseChannelUserExitMapper;
import com.xiliulou.electricity.mq.producer.EnterpriseUserCostRecordProducer;
import com.xiliulou.electricity.query.enterprise.EnterpriseChannelUserQuery;
import com.xiliulou.electricity.query.enterprise.EnterpriseFreeDepositQuery;
import com.xiliulou.electricity.query.enterprise.EnterpriseMemberCardQuery;
import com.xiliulou.electricity.query.enterprise.EnterprisePackageOrderQuery;
import com.xiliulou.electricity.query.enterprise.EnterprisePurchaseOrderQuery;
import com.xiliulou.electricity.query.enterprise.EnterpriseRentBatteryOrderQuery;
import com.xiliulou.electricity.queryModel.enterprise.EnterpriseChannelUserExitQueryModel;
import com.xiliulou.electricity.service.BatteryMemberCardService;
import com.xiliulou.electricity.service.BatteryMembercardRefundOrderService;
import com.xiliulou.electricity.service.BatteryModelService;
import com.xiliulou.electricity.service.CouponService;
import com.xiliulou.electricity.service.EleDepositOrderService;
import com.xiliulou.electricity.service.EleDisableMemberCardRecordService;
import com.xiliulou.electricity.service.EleRefundOrderService;
import com.xiliulou.electricity.service.ElectricityBatteryService;
import com.xiliulou.electricity.service.ElectricityCabinetService;
import com.xiliulou.electricity.service.ElectricityConfigService;
import com.xiliulou.electricity.service.ElectricityMemberCardOrderService;
import com.xiliulou.electricity.service.ElectricityPayParamsService;
import com.xiliulou.electricity.service.ElectricityTradeOrderService;
import com.xiliulou.electricity.service.FranchiseeInsuranceService;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.FreeDepositDataService;
import com.xiliulou.electricity.service.FreeDepositOrderService;
import com.xiliulou.electricity.service.InsuranceOrderService;
import com.xiliulou.electricity.service.InsuranceUserInfoService;
import com.xiliulou.electricity.service.MemberCardBatteryTypeService;
import com.xiliulou.electricity.service.PxzConfigService;
import com.xiliulou.electricity.service.ServiceFeeUserInfoService;
import com.xiliulou.electricity.service.UnionTradeOrderService;
import com.xiliulou.electricity.service.UserBatteryDepositService;
import com.xiliulou.electricity.service.UserBatteryMemberCardPackageService;
import com.xiliulou.electricity.service.UserBatteryMemberCardService;
import com.xiliulou.electricity.service.UserBatteryTypeService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.service.UserOauthBindService;
import com.xiliulou.electricity.service.car.CarRentalPackageService;
import com.xiliulou.electricity.service.enterprise.AnotherPayMembercardRecordService;
import com.xiliulou.electricity.service.enterprise.CloudBeanUseRecordService;
import com.xiliulou.electricity.service.enterprise.EnterpriseBatteryPackageService;
import com.xiliulou.electricity.service.enterprise.EnterpriseChannelUserService;
import com.xiliulou.electricity.service.enterprise.EnterpriseInfoService;
import com.xiliulou.electricity.service.enterprise.EnterprisePackageService;
import com.xiliulou.electricity.service.enterprise.EnterpriseUserCostRecordService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.OrderIdUtil;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.BatteryMemberCardAndTypeVO;
import com.xiliulou.electricity.vo.BatteryMemberCardVO;
import com.xiliulou.electricity.vo.EleDepositOrderVO;
import com.xiliulou.electricity.vo.FreeDepositUserInfoVo;
import com.xiliulou.electricity.vo.InsuranceUserInfoVo;
import com.xiliulou.electricity.vo.UserBatteryDepositVO;
import com.xiliulou.electricity.vo.UserBatteryMemberCardInfoVO;
import com.xiliulou.electricity.vo.enterprise.EnterpriseChannelUserVO;
import com.xiliulou.electricity.vo.enterprise.EnterpriseFreezePackageRecordVO;
import com.xiliulou.electricity.vo.enterprise.EnterprisePackageOrderVO;
import com.xiliulou.electricity.vo.enterprise.EnterpriseRefundDepositOrderVO;
import com.xiliulou.electricity.vo.enterprise.EnterpriseRentBatteryOrderVO;
import com.xiliulou.electricity.vo.enterprise.EnterpriseUserCostDetailsVO;
import com.xiliulou.electricity.vo.enterprise.EnterpriseUserPackageDetailsVO;
import com.xiliulou.pay.deposit.paixiaozu.exception.PxzFreeDepositException;
import com.xiliulou.pay.deposit.paixiaozu.pojo.request.PxzCommonRequest;
import com.xiliulou.pay.deposit.paixiaozu.pojo.request.PxzFreeDepositOrderQueryRequest;
import com.xiliulou.pay.deposit.paixiaozu.pojo.request.PxzFreeDepositOrderRequest;
import com.xiliulou.pay.deposit.paixiaozu.pojo.rsp.PxzCommonRsp;
import com.xiliulou.pay.deposit.paixiaozu.pojo.rsp.PxzQueryOrderRsp;
import com.xiliulou.pay.deposit.paixiaozu.service.PxzDepositService;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author BaoYu
 * @description:
 * @date 2023/9/15 13:58
 */
@Service
@Slf4j
public class EnterpriseBatteryPackageServiceImpl implements EnterpriseBatteryPackageService {
    
    @Resource
    EnterpriseBatteryPackageMapper enterpriseBatteryPackageMapper;
    
    @Resource
    ElectricityPayParamsService electricityPayParamsService;
    
    @Resource
    UserOauthBindService userOauthBindService;
    
    @Resource
    ElectricityTradeOrderService electricityTradeOrderService;
    
    @Resource
    EleDisableMemberCardRecordService eleDisableMemberCardRecordService;
    
    @Resource
    EnterpriseChannelUserService enterpriseChannelUserService;
    
    @Resource
    EleDepositOrderService eleDepositOrderService;
    
    @Resource
    UnionTradeOrderService unionTradeOrderService;
    
    @Resource
    FranchiseeInsuranceService franchiseeInsuranceService;
    
    @Resource
    InsuranceOrderService insuranceOrderService;
    
    @Resource
    FreeDepositDataService freeDepositDataService;
    
    @Resource
    PxzConfigService pxzConfigService;
    
    @Resource
    PxzDepositService pxzDepositService;
    
    @Resource
    ElectricityCabinetService electricityCabinetService;
    
    @Resource
    EleRefundOrderService eleRefundOrderService;
    
    @Resource
    BatteryMembercardRefundOrderService batteryMembercardRefundOrderService;
    
    @Resource
    CloudBeanUseRecordService cloudBeanUseRecordService;
    
    @Resource
    ServiceFeeUserInfoService serviceFeeUserInfoService;
    
    @Resource
    ElectricityConfigService electricityConfigService;
    
    @Resource
    ElectricityBatteryService electricityBatteryService;
    
    @Resource
    EnterpriseUserCostRecordProducer enterpriseUserCostRecordProducer;
    
    @Resource
    private BatteryMemberCardMapper batteryMemberCardMapper;
    
    @Resource
    private RedisService redisService;
    
    @Resource
    private UserBatteryMemberCardService userBatteryMemberCardService;
    
    @Resource
    private ElectricityMemberCardOrderService electricityMemberCardOrderService;
    
    @Resource
    private MemberCardBatteryTypeService memberCardBatteryTypeService;
    
    @Resource
    private UserInfoService userInfoService;
    
    @Resource
    private UserBatteryTypeService userBatteryTypeService;
    
    @Resource
    private FranchiseeService franchiseeService;
    
    @Resource
    private CouponService couponService;
    
    @Resource
    private CarRentalPackageService carRentalPackageService;
    
    @Resource
    private BatteryModelService batteryModelService;
    
    @Resource
    private EnterpriseInfoService enterpriseInfoService;
    
    @Resource
    private UserBatteryDepositService userBatteryDepositService;
    
    @Resource
    private BatteryMemberCardService batteryMemberCardService;
    
    @Resource
    private EnterprisePackageService enterprisePackageService;
    
    @Resource
    private UserBatteryMemberCardPackageService userBatteryMemberCardPackageService;
    
    @Resource
    private ElectricityMemberCardOrderService eleMemberCardOrderService;
    
    @Resource
    private InsuranceUserInfoService insuranceUserInfoService;
    
    @Resource
    private AnotherPayMembercardRecordService anotherPayMembercardRecordService;
    
    @Resource
    private FreeDepositOrderService freeDepositOrderService;
    
    @Resource
    private EnterpriseUserCostRecordService enterpriseUserCostRecordService;
    
    @Resource
    private EnterpriseChannelUserExitMapper channelUserExitMapper;
    
    @Deprecated
    @Override
    public Triple<Boolean, String, Object> save(EnterpriseMemberCardQuery query) {
        
        if (Objects.nonNull(this.batteryMemberCardMapper.checkMembercardExist(query.getName(), TenantContextHolder.getTenantId()))) {
            return Triple.of(false, "100104", "套餐名称已存在");
        }
        
        Franchisee franchisee = franchiseeService.queryByIdFromCache(query.getFranchiseeId());
        if (Objects.isNull(franchisee)) {
            return Triple.of(false, "ELECTRICITY.0038", "加盟商不存在");
        }
        
        Triple<Boolean, String, Object> verifyBatteryMemberCardResult = verifyBatteryMemberCardQuery(query, franchisee);
        if (Boolean.FALSE.equals(verifyBatteryMemberCardResult.getLeft())) {
            return verifyBatteryMemberCardResult;
        }
        
        BatteryMemberCard batteryMemberCard = new BatteryMemberCard();
        BeanUtils.copyProperties(query, batteryMemberCard);
        
        //设置企业渠道换电套餐类型
        batteryMemberCard.setBusinessType(BatteryMemberCardBusinessTypeEnum.BUSINESS_TYPE_ENTERPRISE_BATTERY.getCode());
        batteryMemberCard.setDelFlag(BatteryMemberCard.DEL_NORMAL);
        batteryMemberCard.setCreateTime(System.currentTimeMillis());
        batteryMemberCard.setUpdateTime(System.currentTimeMillis());
        batteryMemberCard.setTenantId(TenantContextHolder.getTenantId());
        
        this.batteryMemberCardMapper.insert(batteryMemberCard);
        
        if (Objects.equals(franchisee.getModelType(), Franchisee.NEW_MODEL_TYPE) && CollectionUtils.isNotEmpty(query.getBatteryModels())) {
            memberCardBatteryTypeService.batchInsert(buildMemberCardBatteryTypeList(query.getBatteryModels(), batteryMemberCard.getId()));
        }
        
        return Triple.of(true, null, null);
        
    }
    
    @Slave
    @Override
    public Triple<Boolean, String, Object> queryBatterV(EnterpriseChannelUserQuery query) {
        log.info("query battery v flow start, enterprise id = {}", query.getEnterpriseId());
        //获取当前企业信息
        EnterpriseInfo enterpriseInfo = enterpriseInfoService.queryByIdFromCache(query.getEnterpriseId());
        if (Objects.isNull(enterpriseInfo)) {
            log.info("not found enterprise record, enterprise id = {}", query.getEnterpriseId());
            return Triple.of(false, "", "当前企业不存在");
        }
        List<Long> packageIds = enterprisePackageService.selectByEnterpriseId(query.getEnterpriseId());
        if (CollectionUtils.isEmpty(packageIds)) {
            log.info("not found enterprise package record, enterprise id = {}", query.getEnterpriseId());
            return Triple.of(false, "", "当前企业套餐不存在");
        }
        
        //查询当前骑手自主续费开关是否关闭，若开启，则无法购买套餐
        EnterpriseChannelUserVO enterpriseChannelUserVO = enterpriseChannelUserService.queryEnterpriseChannelUser(query.getUid());
        if (Objects.isNull(enterpriseChannelUserVO)) {
            log.error("Not found enterprise channel user for query battery V, enterprise id = {}, uid = {}", query.getEnterpriseId(), query.getUid());
            return Triple.of(false, "", "企业骑手信息不存在");
        }
        
        if (RenewalStatusEnum.RENEWAL_STATUS_BY_SELF.getCode().equals(enterpriseChannelUserVO.getRenewalStatus())) {
            log.error("Not found enterprise channel user for query battery V, enterprise id = {}, uid = {}", query.getEnterpriseId(), query.getUid());
            return Triple.of(false, "", "骑手已开启自主续费功能，无法代付套餐，建议您前往骑手详情页进行相关设置");
        }
        
        //根据企业加盟商获取套餐信息
        /*BatteryMemberCardQuery batteryMemberCardQuery = BatteryMemberCardQuery.builder().tenantId(TenantContextHolder.getTenantId()).franchiseeId(enterpriseInfo.getFranchiseeId())
                .status(BatteryMemberCard.STATUS_UP).delFlag(BatteryMemberCard.DEL_NORMAL).build();*/
        
        EnterpriseMemberCardQuery batteryMemberCardQuery = new EnterpriseMemberCardQuery();
        batteryMemberCardQuery.setTenantId(TenantContextHolder.getTenantId());
        batteryMemberCardQuery.setFranchiseeId(enterpriseInfo.getFranchiseeId());
        batteryMemberCardQuery.setStatus(BatteryMemberCard.STATUS_UP);
        batteryMemberCardQuery.setDelFlag(BatteryMemberCard.DEL_NORMAL);
        batteryMemberCardQuery.setPackageIds(packageIds);
        
        //待添加套餐的用户
        UserInfo userInfo = userInfoService.queryByUidFromCache(query.getUid());
        if (Objects.isNull(userInfo)) {
            log.error("Not found userInfo for enterprise channel, uid = {}", query.getUid());
            return Triple.of(true, "", Collections.emptyList());
        }
        
        //未缴纳押金
        if (!Objects.equals(UserInfo.BATTERY_DEPOSIT_STATUS_YES, userInfo.getBatteryDepositStatus())) {
            List<BatteryMemberCardVO> list = batteryMemberCardMapper.selectMembercardBatteryVByEnterprise(batteryMemberCardQuery);
            //List<BatteryMemberCardVO> list = this.batteryMemberCardMapper.selectMembercardBatteryV(batteryMemberCardQuery);
            if (CollectionUtils.isEmpty(list)) {
                return Triple.of(true, "", Collections.emptyList());
            }
            
            List<String> batteryVs = list.stream().map(BatteryMemberCardVO::getBatteryV).distinct().collect(Collectors.toList());
            log.info("query battery v without deposit, batteryVs = {}", batteryVs);
            if (CollectionUtils.isEmpty(batteryVs) || batteryVs.stream().allMatch(item -> Objects.isNull(item))) {
                return Triple.of(true, "", Collections.emptyList());
            }
            batteryVs = batteryVs.stream().filter(Objects::nonNull).collect(Collectors.toList());
            return Triple.of(true, "", batteryVs);
        }
        
        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(query.getUid());
        if (Objects.isNull(userBatteryMemberCard) || Objects.equals(NumberConstant.ZERO, userBatteryMemberCard.getCardPayCount()) || Objects.equals(userBatteryMemberCard.getMemberCardId(), NumberConstant.ZERO_L)) {
            List<BatteryMemberCardVO> list = batteryMemberCardMapper.selectMembercardBatteryVByEnterprise(batteryMemberCardQuery);
            //List<BatteryMemberCardVO> list = batteryMemberCardMapper.selectMembercardBatteryV(batteryMemberCardQuery);
            if (CollectionUtils.isEmpty(list)) {
                return Triple.of(true, "", Collections.emptyList());
            }
            
            List<String> batteryVs = list.stream().map(BatteryMemberCardVO::getBatteryV).distinct().collect(Collectors.toList());
            log.info("query battery v with user battery member card, batteryVs = {}", batteryVs);
            if (CollectionUtils.isEmpty(batteryVs) || batteryVs.stream().allMatch(item -> Objects.isNull(item))) {
                return Triple.of(true, "", Collections.emptyList());
            }
            batteryVs = batteryVs.stream().filter(Objects::nonNull).collect(Collectors.toList());
            return Triple.of(true, "", batteryVs);
        }
        
        String batteryType = userBatteryTypeService.selectUserSimpleBatteryType(query.getUid());
        if (StringUtils.isEmpty(batteryType) || StringUtils.isBlank(batteryType)) {
            return Triple.of(true, "", Collections.emptyList());
        }
        log.info("query battery v flow end, batteryType = {}", batteryType);
        return Triple.of(true, "", Collections.singletonList(batteryType));
        
    }
    
    
    /**
     * 根据电池型号查询企业渠道套餐信息
     *
     * @param query
     * @return
     */
    @Slave
    @Override
    public Triple<Boolean, String, Object> queryPackagesByBatteryV(EnterpriseMemberCardQuery query) {
        
        Long enterpriseUserId = query.getUid();
        EnterpriseInfo enterpriseInfo = enterpriseInfoService.queryByIdFromCache(query.getEnterpriseId());
        
        if (Objects.isNull(enterpriseInfo)) {
            log.error("Not found enterprise for query package by battery V, enterprise id = {}, uid = {}", query.getEnterpriseId(), enterpriseUserId);
            return Triple.of(true, "", Collections.emptyList());
        }
        
        //查询当前骑手自主续费开关是否关闭，若开启，则无法购买套餐
        EnterpriseChannelUserVO enterpriseChannelUserVO = enterpriseChannelUserService.queryEnterpriseChannelUser(enterpriseUserId);
        if (Objects.isNull(enterpriseChannelUserVO)) {
            log.error("Not found enterprise channel user for query package by battery V, enterprise id = {}, uid = {}", query.getEnterpriseId(), enterpriseUserId);
            return Triple.of(false, "300064", "企业骑手信息不存在");
        }
        
        if (RenewalStatusEnum.RENEWAL_STATUS_BY_SELF.getCode().equals(enterpriseChannelUserVO.getRenewalStatus())) {
            log.error("Not found enterprise channel user for query package by battery V, enterprise id = {}, uid = {}", query.getEnterpriseId(), enterpriseUserId);
            return Triple.of(false, "300063", "骑手已开启自主续费功能，无法代付套餐，建议您前往骑手详情页进行相关设置");
        }
        
        Franchisee franchisee = franchiseeService.queryByIdFromCache(enterpriseInfo.getFranchiseeId());
        if (Objects.isNull(franchisee)) {
            log.error("Not found franchisee for query package by battery V, uid = {}, franchiseeId = {}", enterpriseUserId, query.getFranchiseeId());
            return Triple.of(true, "", Collections.emptyList());
        }
        
        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(query.getUid());
        UserBatteryDeposit userBatteryDeposit = userBatteryDepositService.selectByUidFromCache(query.getUid());
        UserInfo userInfo = userInfoService.queryByUidFromCache(query.getUid());
        
        
        if (Objects.isNull(userBatteryMemberCard) || Objects.isNull(userBatteryMemberCard.getCardPayCount()) || userBatteryMemberCard.getCardPayCount() <= 0) {
            //新租
            query.setRentTypes(Arrays.asList(BatteryMemberCard.RENT_TYPE_NEW, BatteryMemberCard.RENT_TYPE_UNLIMIT));
            query.setFreeDeposite(Objects.nonNull(userBatteryDeposit)&& Objects.equals( userInfo.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_YES) && Objects.equals(userBatteryDeposit.getDepositType(), UserBatteryDeposit.DEPOSIT_TYPE_FREE) ? BatteryMemberCard.YES: null);
            
            
            //为了兼容免押后购买套餐
            if (Objects.nonNull(userBatteryDeposit) && Objects.equals(userBatteryDeposit.getDepositType(), UserBatteryDeposit.DEPOSIT_TYPE_FREE) && UserInfo.BATTERY_DEPOSIT_STATUS_YES.equals(userInfo.getBatteryDepositStatus())) {
                boolean isMember = false;
                
                if (Objects.equals(userBatteryDeposit.getDepositModifyFlag(), UserBatteryDeposit.DEPOSIT_MODIFY_YES)) {
                    query.setDeposit(userBatteryDeposit.getBeforeModifyDeposit());
                } else {
                    query.setDeposit(userBatteryDeposit.getBatteryDeposit());
                }
                
                EleDepositOrder eleDepositOrder = eleDepositOrderService.queryByOrderId(userBatteryDeposit.getOrderId());
                if (Objects.nonNull(eleDepositOrder)) {
                    isMember = Objects.equals(eleDepositOrder.getOrderType(), PackageOrderTypeEnum.PACKAGE_ORDER_TYPE_NORMAL.getCode());
                }
                
                if (isMember) {
                    // 如果是会员用户则查询的套餐为续租以后的企业套餐对应的押金及限次类型的套餐
                    query.setDeposit(null);
                    query.setLimitCount(null);
                    query.setFreeDeposite(null);
                }
            }
            
        } else if (Objects.isNull(userBatteryMemberCard.getMemberCardId()) || Objects.equals(userBatteryMemberCard.getMemberCardId(), NumberConstant.ZERO_L)) {
            //非新租 购买押金套餐
            query.setRentTypes(Arrays.asList(BatteryMemberCard.RENT_TYPE_OLD, BatteryMemberCard.RENT_TYPE_UNLIMIT));
            query.setFreeDeposite(Objects.nonNull(userBatteryDeposit) && Objects.equals( userInfo.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_YES) && Objects.equals(userBatteryDeposit.getDepositType(), UserBatteryDeposit.DEPOSIT_TYPE_FREE) ? BatteryMemberCard.YES: null);
            
            boolean isMember = false;
            
            if (Objects.nonNull(userBatteryDeposit) && UserInfo.BATTERY_DEPOSIT_STATUS_YES.equals(userInfo.getBatteryDepositStatus())) {
                query.setDeposit(userBatteryDeposit.getBatteryDeposit());
                
                EleDepositOrder eleDepositOrder = eleDepositOrderService.queryByOrderId(userBatteryDeposit.getOrderId());
                if (Objects.nonNull(eleDepositOrder)) {
                    isMember = Objects.equals(eleDepositOrder.getOrderType(), PackageOrderTypeEnum.PACKAGE_ORDER_TYPE_NORMAL.getCode());
                }
            }
            
            if (isMember) {
                // 如果是会员用户则查询的套餐为续租以后的企业套餐对应的押金及限次类型的套餐
                query.setDeposit(null);
                query.setLimitCount(null);
                query.setFreeDeposite(null);
            }
            
        } else {
            //续费
            BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(userBatteryMemberCard.getMemberCardId());
            if (Objects.isNull(batteryMemberCard)) {
                log.error("Not found battery member card for query package by battery V, uid = {}, mid = {}", enterpriseUserId, userBatteryMemberCard.getMemberCardId());
                return Triple.of(true, "", Collections.emptyList());
            }
            
            boolean isMember = false;
            
            if (Objects.nonNull(userBatteryDeposit)) {
                EleDepositOrder eleDepositOrder = eleDepositOrderService.queryByOrderId(userBatteryDeposit.getOrderId());
                isMember = Objects.equals(eleDepositOrder.getOrderType(), PackageOrderTypeEnum.PACKAGE_ORDER_TYPE_NORMAL.getCode());
            }
            
            UserBatteryMemberCardPackageBO userBatteryMemberCardPackageBO = userBatteryMemberCardPackageService.queryEnterprisePackageByUid(query.getUid());
            
            query.setFreeDeposite(Objects.nonNull(userBatteryDeposit) && Objects.equals( userInfo.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_YES) && Objects.equals(userBatteryDeposit.getDepositType(), UserBatteryDeposit.DEPOSIT_TYPE_FREE) ? BatteryMemberCard.YES: null);
            
            // 企业用户并且用户押金为企业代付  押金未换电套餐的押金 但是已经购买了
            if (!isMember) {
                query.setDeposit(batteryMemberCard.getDeposit());
                query.setLimitCount(batteryMemberCard.getLimitCount());
            } else {
                // 如果是会员用户且当前的生效的套餐类型为企业套餐类型则现在生效的企业套餐对应的押金和限次进行查询
                if (Objects.equals(batteryMemberCard.getBusinessType(), BatteryMemberCard.BUSINESS_TYPE_ENTERPRISE)) {
                    query.setDeposit(batteryMemberCard.getDeposit());
                    query.setLimitCount(batteryMemberCard.getLimitCount());
                    
                } else if (Objects.nonNull(userBatteryMemberCardPackageBO)) {
                    // 如果是会员用户则查询的套餐为续租以后的企业套餐对应的押金及限次类型的套餐
                    query.setDeposit(userBatteryMemberCardPackageBO.getDeposit());
                    query.setLimitCount(userBatteryMemberCardPackageBO.getLimitCount());
                } else {
                    // 会员用户加入站点以后第一次代付 不受限制
                    query.setDeposit(null);
                    query.setLimitCount(null);
                    query.setFreeDeposite(null);
                }
            }
            query.setRentTypes(Arrays.asList(BatteryMemberCard.RENT_TYPE_OLD, BatteryMemberCard.RENT_TYPE_UNLIMIT));
            query.setBatteryV(Objects.equals(franchisee.getModelType(), Franchisee.NEW_MODEL_TYPE) ? userBatteryTypeService.selectUserSimpleBatteryType(enterpriseUserId) : null);
        }
        
        //先获取企业关联套餐信息
        List<Long> packageIds = enterprisePackageService.selectByEnterpriseId(query.getEnterpriseId());
        if (CollectionUtils.isEmpty(packageIds)) {
            return Triple.of(true, "", Collections.emptyList());
        }
        query.setPackageIds(packageIds);
        
        List<BatteryMemberCardAndTypeVO> list = this.batteryMemberCardMapper.selectMemberCardsByEnterprise(query);
        if (CollectionUtils.isEmpty(list)) {
            return Triple.of(true, "", Collections.emptyList());
        }
        
        log.info("get battery packages, packages = {}", JsonUtil.toJson(list));
        
        //用户绑定的电池型号串数
        List<String> userBindBatteryType = null;
        if (Objects.equals(franchisee.getModelType(), Franchisee.NEW_MODEL_TYPE)) {
            userBindBatteryType = userBatteryTypeService.selectByUid(enterpriseUserId);
            if (CollectionUtils.isNotEmpty(userBindBatteryType)) {
                userBindBatteryType = userBindBatteryType.stream().map(item -> item.substring(item.lastIndexOf("_") + 1)).collect(Collectors.toList());
            }
        }
        log.info("user bind battery type, battery type = {}", userBindBatteryType);
        List<BatteryMemberCardVO> result = new ArrayList<>();
        for (BatteryMemberCardAndTypeVO item : list) {
            
            if (Objects.equals(franchisee.getModelType(), Franchisee.NEW_MODEL_TYPE)) {
                List<String> number = null;
                if (CollectionUtils.isNotEmpty(item.getBatteryType())) {
                    //套餐电池型号串数 number
                    number = item.getBatteryType().stream().filter(i -> StringUtils.isNotBlank(i.getBatteryType()))
                            .map(e -> e.getBatteryType().substring(e.getBatteryType().lastIndexOf("_") + 1)).collect(Collectors.toList());
                }
                
                log.info("get battery type number, battery type number = {} ", number);
                
                if (CollectionUtils.isNotEmpty(userBindBatteryType)) {
                    if (!(CollectionUtils.isNotEmpty(number) && CollectionUtils.containsAll(number, userBindBatteryType))) {
                        continue;
                    }
                }
            }
            
            BatteryMemberCardVO batteryMemberCardVO = new BatteryMemberCardVO();
            BeanUtils.copyProperties(item, batteryMemberCardVO);

            /*if (Objects.nonNull(item.getCouponId())) {
                Coupon coupon = couponService.queryByIdFromCache(item.getCouponId());
                batteryMemberCardVO.setCouponName(Objects.isNull(coupon) ? "" : coupon.getName());
                batteryMemberCardVO.setAmount(Objects.isNull(coupon) ? null : coupon.getAmount());
            }*/
            
            result.add(batteryMemberCardVO);
        }
        
        return Triple.of(true, "", result);
    }
    
    @Override
    public Triple<Boolean, String, Object> queryByPackageId(EnterpriseMemberCardQuery query) {
        
        return null;
    }
    
    @Override
    public Triple<Boolean, String, Object> queryRiderDepositAndPackage(Long uid) {
        
        UserBatteryMemberCardInfoVO userBatteryMemberCardInfoVO = new UserBatteryMemberCardInfoVO();
        
        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
        if (Objects.isNull(userInfo)) {
            log.warn("query rider deposit and package failed, not found userInfo,uid = {}", uid);
            return Triple.of(true, null, userBatteryMemberCardInfoVO);
        }
        
        Franchisee franchisee = franchiseeService.queryByIdFromCache(userInfo.getFranchiseeId());
        userBatteryMemberCardInfoVO.setModelType(Objects.isNull(franchisee) ? null : franchisee.getModelType());
        userBatteryMemberCardInfoVO.setBatteryRentStatus(userInfo.getBatteryRentStatus());
        userBatteryMemberCardInfoVO.setBatteryDepositStatus(userInfo.getBatteryDepositStatus());
        userBatteryMemberCardInfoVO.setFranchiseeId(userInfo.getFranchiseeId());
        userBatteryMemberCardInfoVO.setStoreId(userInfo.getStoreId());
        userBatteryMemberCardInfoVO.setIsExistMemberCard(UserBatteryMemberCardInfoVO.NO);
        
        UserBatteryDeposit userBatteryDeposit = userBatteryDepositService.selectByUidFromCache(userInfo.getUid());
        if (Objects.isNull(userBatteryDeposit) || StringUtils.isBlank(userBatteryDeposit.getOrderId())) {
            log.warn("query rider deposit and package failed, not found userBatteryDeposit,uid={}", userInfo.getUid());
            return Triple.of(true, null, userBatteryMemberCardInfoVO);
        }
        
        userBatteryMemberCardInfoVO.setBatteryDeposit(userBatteryDeposit.getBatteryDeposit());
        
        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(userInfo.getUid());
        if (Objects.isNull(userBatteryMemberCard) || Objects.isNull(userBatteryMemberCard.getMemberCardId()) || Objects.equals(userBatteryMemberCard.getMemberCardId(),
                NumberConstant.ZERO_L)) {
            log.warn("query rider deposit and package failed, not found userBatteryMemberCard,uid={}", userInfo.getUid());
            return Triple.of(true, null, userBatteryMemberCardInfoVO);
        }
        
        userBatteryMemberCardInfoVO.setIsExistMemberCard(UserBatteryMemberCardInfoVO.YES);
        userBatteryMemberCardInfoVO.setMemberCardStatus(userBatteryMemberCard.getMemberCardStatus());
        userBatteryMemberCardInfoVO.setMemberCardExpireTime(userBatteryMemberCard.getMemberCardExpireTime());
        userBatteryMemberCardInfoVO.setRemainingNumber(userBatteryMemberCard.getRemainingNumber());
        userBatteryMemberCardInfoVO.setMemberCardId(userBatteryMemberCard.getMemberCardId());
        
        BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(userBatteryMemberCard.getMemberCardId());
        if (Objects.isNull(batteryMemberCard)) {
            log.warn("query rider deposit and package failed, not found batteryMemberCard,uid={},mid={}", userInfo.getUid(), userBatteryMemberCard.getMemberCardId());
            return Triple.of(true, null, userBatteryMemberCardInfoVO);
        }
        if (Objects.equals(batteryMemberCard.getRentUnit(), BatteryMemberCard.RENT_UNIT_DAY)) {
            userBatteryMemberCardInfoVO.setValidDays(userBatteryMemberCard.getMemberCardExpireTime() > System.currentTimeMillis() ? (int) Math.ceil(
                    (userBatteryMemberCard.getMemberCardExpireTime() - System.currentTimeMillis()) / 24 / 60 / 60 / 1000.0) : 0);
        } else {
            userBatteryMemberCardInfoVO.setValidDays(userBatteryMemberCard.getMemberCardExpireTime() > System.currentTimeMillis() ? (int) Math.ceil(
                    (userBatteryMemberCard.getMemberCardExpireTime() - System.currentTimeMillis()) / 60 / 1000.0) : 0);
        }
        
        //套餐订单金额
        ElectricityMemberCardOrder electricityMemberCardOrder = eleMemberCardOrderService.selectByOrderNo(userBatteryMemberCard.getOrderId());
        userBatteryMemberCardInfoVO.setBatteryMembercardPayAmount(Objects.isNull(electricityMemberCardOrder) ? null : electricityMemberCardOrder.getPayAmount());
        userBatteryMemberCardInfoVO.setMemberCardPayTime(Objects.isNull(electricityMemberCardOrder) ? null : electricityMemberCardOrder.getCreateTime());
        userBatteryMemberCardInfoVO.setMemberCardName(batteryMemberCard.getName());
        userBatteryMemberCardInfoVO.setRentUnit(batteryMemberCard.getRentUnit());
        userBatteryMemberCardInfoVO.setLimitCount(batteryMemberCard.getLimitCount());
        
        //用户电池型号
        userBatteryMemberCardInfoVO.setUserBatterySimpleType(userBatteryTypeService.selectUserSimpleBatteryType(userInfo.getUid()));
        
        //查询当前用户是否存在最新的冻结订单信息
        EleDisableMemberCardRecord eleDisableMemberCardRecord = eleDisableMemberCardRecordService.queryCreateTimeMaxEleDisableMemberCardRecord(SecurityUtils.getUid(),
                TenantContextHolder.getTenantId());
        if (Objects.nonNull(eleDisableMemberCardRecord) && UserBatteryMemberCard.MEMBER_CARD_DISABLE_REVIEW_REFUSE.equals(eleDisableMemberCardRecord.getStatus())) {
            userBatteryMemberCardInfoVO.setRejectReason(eleDisableMemberCardRecord.getErrMsg());
        }
        
        return Triple.of(true, null, userBatteryMemberCardInfoVO);
        
    }
    
    @Slave
    @Override
    public Triple<Boolean, String, Object> queryUserBatteryDeposit(Long uid) {
        UserBatteryDepositVO userBatteryDepositVO = new UserBatteryDepositVO();
        userBatteryDepositVO.setBatteryRentStatus(UserInfo.BATTERY_RENT_STATUS_NO);
        userBatteryDepositVO.setBatteryDepositStatus(UserInfo.BATTERY_DEPOSIT_STATUS_YES);
        userBatteryDepositVO.setBatteryDeposit(BigDecimal.ZERO);
    
        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
        if (Objects.isNull(userInfo)) {
            log.warn("query deposit warning, not found userInfo,uid = {}", uid);
            return Triple.of(true, "", userBatteryDepositVO);
            
        }
        userBatteryDepositVO.setBatteryRentStatus(userInfo.getBatteryRentStatus());
        userBatteryDepositVO.setBatteryDepositStatus(userInfo.getBatteryDepositStatus());
        
        UserBatteryDeposit userBatteryDeposit = userBatteryDepositService.selectByUidFromCache(userInfo.getUid());
        if (Objects.isNull(userBatteryDeposit)) {
            log.warn("query deposit warning, not found userBatteryDeposit,uid = {}", userInfo.getUid());
            return Triple.of(true, "", userBatteryDepositVO);
        }
        
        userBatteryDepositVO.setBatteryDeposit(userBatteryDeposit.getBatteryDeposit());
        userBatteryDepositVO.setDepositType(userBatteryDeposit.getDepositType());
        
        return Triple.of(true, "", userBatteryDepositVO);
        
    }
    
    @Override
    public Triple<Boolean, String, Object> checkUserFreeBatteryDepositStatus(Long uid) {
        
        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
        if (Objects.isNull(userInfo)) {
            log.error("check user deposit status error, not found user info,uid={}", uid);
            return Triple.of(false, "ELECTRICITY.0001", "未能查到用户信息");
        }
        
        UserBatteryDeposit userBatteryDeposit = userBatteryDepositService.selectByUidFromCache(userInfo.getUid());
        if (Objects.isNull(userBatteryDeposit)) {
            log.error("check user deposit status error, not found userBatteryDeposit,uid={}", uid);
            return Triple.of(true, "", "");
        }
        
        FreeDepositOrder freeDepositOrder = freeDepositOrderService.selectByOrderId(userBatteryDeposit.getOrderId());
        if (Objects.isNull(freeDepositOrder)) {
            log.error("check user deposit status error, not found freeDepositOrder,uid={},orderId={}", uid, userBatteryDeposit.getOrderId());
            return Triple.of(false, "100403", "免押订单不存在");
        }
        
        //如果已冻结  直接返回
        FreeDepositUserInfoVo freeDepositUserInfoVo = new FreeDepositUserInfoVo();
        if (Objects.equals(freeDepositOrder.getAuthStatus(), FreeDepositOrder.AUTH_FROZEN)) {
            freeDepositUserInfoVo.setApplyBatteryDepositTime(userBatteryDeposit.getApplyDepositTime());
            freeDepositUserInfoVo.setBatteryDepositAuthStatus(freeDepositOrder.getAuthStatus());
            return Triple.of(true, null, freeDepositUserInfoVo);
        }
        
        //        ElectricityConfig electricityConfig = electricityConfigService.queryFromCacheByTenantId(TenantContextHolder.getTenantId());
        //        if (!(Objects.equals(electricityConfig.getFreeDepositType(), ElectricityConfig.FREE_DEPOSIT_TYPE_BATTERY) || Objects.equals(electricityConfig.getFreeDepositType(), ElectricityConfig.FREE_DEPOSIT_TYPE_ALL))) {
        //            return Triple.of(false, "100418", "押金免押功能未开启,请联系客服处理");
        //        }
        
        PxzConfig pxzConfig = pxzConfigService.queryByTenantIdFromCache(TenantContextHolder.getTenantId());
        if (Objects.isNull(pxzConfig) || StringUtils.isBlank(pxzConfig.getAesKey()) || StringUtils.isBlank(pxzConfig.getMerchantCode())) {
            log.error("check user deposit status error, not found pxzConfig,uid={}", uid);
            return Triple.of(false, "100400", "免押功能未配置相关信息,请联系客服处理");
        }
        
        EleDepositOrder eleDepositOrder = eleDepositOrderService.queryByOrderId(userBatteryDeposit.getOrderId());
        if (Objects.isNull(eleDepositOrder)) {
            log.error("check user deposit status error, not found eleDepositOrder! uid={},orderId={}", uid, userBatteryDeposit.getOrderId());
            return Triple.of(false, "ELECTRICITY.0015", "未找到订单");
        }
        
        PxzCommonRequest<PxzFreeDepositOrderQueryRequest> query = new PxzCommonRequest<>();
        query.setAesSecret(pxzConfig.getAesKey());
        query.setDateTime(System.currentTimeMillis());
        query.setSessionId(userBatteryDeposit.getOrderId());
        query.setMerchantCode(pxzConfig.getMerchantCode());
        
        PxzFreeDepositOrderQueryRequest request = new PxzFreeDepositOrderQueryRequest();
        request.setTransId(freeDepositOrder.getOrderId());
        query.setData(request);
        
        PxzCommonRsp<PxzQueryOrderRsp> pxzQueryOrderRsp = null;
        try {
            pxzQueryOrderRsp = pxzDepositService.queryFreeDepositOrder(query);
        } catch (PxzFreeDepositException e) {
            log.error("query Pxz error, freeDepositOrderQuery fail! uid={},orderId={}", uid, userBatteryDeposit.getOrderId(), e);
            return Triple.of(false, "100402", "免押查询失败！");
        }
        
        if (Objects.isNull(pxzQueryOrderRsp)) {
            log.error("query Pxz error, freeDepositOrderQuery fail! pxzQueryOrderRsp is null! uid={},orderId={}", uid, userBatteryDeposit.getOrderId());
            return Triple.of(false, "100402", "免押查询失败！");
        }
        
        if (!pxzQueryOrderRsp.isSuccess()) {
            return Triple.of(false, "100402", pxzQueryOrderRsp.getRespDesc());
        }
        
        PxzQueryOrderRsp queryOrderRspData = pxzQueryOrderRsp.getData();
        if (Objects.isNull(queryOrderRspData)) {
            log.error("query Pxz error, freeDepositOrderQuery fail! queryOrderRspData is null! uid={},orderId={}", uid, userBatteryDeposit.getOrderId());
            return Triple.of(false, "100402", "免押查询失败！");
        }
        
        //更新免押订单状态
        FreeDepositOrder freeDepositOrderUpdate = new FreeDepositOrder();
        freeDepositOrderUpdate.setId(freeDepositOrder.getId());
        freeDepositOrderUpdate.setAuthNo(queryOrderRspData.getAuthNo());
        freeDepositOrderUpdate.setAuthStatus(queryOrderRspData.getAuthStatus());
        freeDepositOrderUpdate.setUpdateTime(System.currentTimeMillis());
        freeDepositOrderService.update(freeDepositOrderUpdate);
        
        //冻结成功
        if (Objects.equals(queryOrderRspData.getAuthStatus(), FreeDepositOrder.AUTH_FROZEN)) {
            
            //扣减免押次数
            freeDepositDataService.deductionFreeDepositCapacity(TenantContextHolder.getTenantId(), 1);
            
            //更新押金订单状态
            EleDepositOrder eleDepositOrderUpdate = new EleDepositOrder();
            eleDepositOrderUpdate.setId(eleDepositOrder.getId());
            eleDepositOrderUpdate.setStatus(EleDepositOrder.STATUS_SUCCESS);
            eleDepositOrderUpdate.setUpdateTime(System.currentTimeMillis());
            eleDepositOrderService.update(eleDepositOrderUpdate);
            
            //绑定加盟商、更新押金状态
            UserInfo userInfoUpdate = new UserInfo();
            userInfoUpdate.setUid(uid);
            userInfoUpdate.setFranchiseeId(eleDepositOrder.getFranchiseeId());
            userInfoUpdate.setStoreId(eleDepositOrder.getStoreId());
            userInfoUpdate.setBatteryDepositStatus(UserInfo.BATTERY_DEPOSIT_STATUS_YES);
            userInfoUpdate.setUpdateTime(System.currentTimeMillis());
            userInfoService.updateByUid(userInfoUpdate);
            
            //绑定电池型号
           /* List<String> batteryTypeList = memberCardBatteryTypeService.selectBatteryTypeByMid(eleDepositOrder.getMid());
            if (org.apache.commons.collections.CollectionUtils.isNotEmpty(batteryTypeList)) {
                userBatteryTypeService.batchInsert(userBatteryTypeService.buildUserBatteryType(batteryTypeList, userInfo));
            }*/
        }
        
        freeDepositUserInfoVo.setApplyBatteryDepositTime(userBatteryDeposit.getApplyDepositTime());
        freeDepositUserInfoVo.setBatteryDepositAuthStatus(queryOrderRspData.getAuthStatus());
        
        return Triple.of(true, null, freeDepositUserInfoVo);
        
    }
    
    @Override
    public Triple<Boolean, String, Object> freeBatteryDeposit(EnterpriseFreeDepositQuery freeQuery) {
        
        UserInfo userInfo = userInfoService.queryByUidFromCache(freeQuery.getUid());
        if (Objects.isNull(userInfo)) {
            log.error("Free battery deposit error, not found user info! uid={}", freeQuery.getUid());
            return Triple.of(false, "ELECTRICITY.0001", "未能查到用户信息");
        }
        
        //获取租户免押次数
        FreeDepositData freeDepositData = freeDepositDataService.selectByTenantId(TenantContextHolder.getTenantId());
        if (Objects.isNull(freeDepositData)) {
            log.error("Free battery deposit error, freeDepositData is null,uid={}", freeQuery.getUid());
            return Triple.of(false, "100404", "免押次数未充值，请联系管理员");
        }
        
        if (freeDepositData.getFreeDepositCapacity() <= NumberConstant.ZERO) {
            log.error("Free battery deposit error, freeDepositCapacity already run out,uid={}", freeQuery.getUid());
            return Triple.of(false, "100405", "免押次数已用完，请联系管理员");
        }
        
        PxzConfig pxzConfig = pxzConfigService.queryByTenantIdFromCache(TenantContextHolder.getTenantId());
        if (Objects.isNull(pxzConfig) || StringUtils.isBlank(pxzConfig.getAesKey()) || StringUtils.isBlank(pxzConfig.getMerchantCode())) {
            return Triple.of(false, "100400", "免押功能未配置相关信息！请联系客服处理");
        }
        
        Triple<Boolean, String, Object> checkUserCanFreeDepositResult = checkUserCanFreeBatteryDeposit(freeQuery.getUid(), userInfo);
        if (Boolean.FALSE.equals(checkUserCanFreeDepositResult.getLeft())) {
            return checkUserCanFreeDepositResult;
        }
        
        FreeDepositUserDTO freeDepositUserDTO = FreeDepositUserDTO.builder()
                .uid(userInfo.getUid())
                .realName(freeQuery.getRealName())
                .phoneNumber(freeQuery.getPhoneNumber())
                .idCard(freeQuery.getIdCard())
                .tenantId(TenantContextHolder.getTenantId())
                .packageId(freeQuery.getMembercardId())
                .packageType(PackageTypeEnum.PACKAGE_TYPE_BATTERY.getCode())
                .build();
        
        //检查用户是否已经进行过免押操作，且已免押成功
        Triple<Boolean, String, Object> useFreeDepositStatusResult = freeDepositOrderService.checkFreeDepositStatusFromPxz(freeDepositUserDTO, pxzConfig);
        if (Boolean.FALSE.equals(useFreeDepositStatusResult.getLeft())) {
            return useFreeDepositStatusResult;
        }
        
        //查看缓存中的免押链接信息是否还存在，若存在，并且本次免押传入的用户名称和身份证与上次相同，则获取缓存数据并返回
        boolean freeOrderCacheResult = redisService.hasKey(CacheConstant.ELE_CACHE_ENTERPRISE_BATTERY_FREE_DEPOSIT_ORDER_GENERATE_LOCK_KEY + userInfo.getUid());
        if (Objects.isNull(useFreeDepositStatusResult.getRight()) && freeOrderCacheResult) {
            String result = UriUtils.decode(redisService.get(CacheConstant.ELE_CACHE_ENTERPRISE_BATTERY_FREE_DEPOSIT_ORDER_GENERATE_LOCK_KEY + userInfo.getUid()), StandardCharsets.UTF_8);
            result = JsonUtil.fromJson(result, String.class);
            log.info("found the free order result for enterprise from cache. uid = {}, result = {}", userInfo.getUid(), result);
            return Triple.of(true, null, result);
        }
        
        Triple<Boolean, String, Object> generateDepositOrderResult = generateBatteryDepositOrder(userInfo, freeQuery);
        if (Boolean.FALSE.equals(generateDepositOrderResult.getLeft())) {
            return generateDepositOrderResult;
        }
        EleDepositOrder eleDepositOrder = (EleDepositOrder) generateDepositOrderResult.getRight();
        
        FreeDepositOrder freeDepositOrder = FreeDepositOrder.builder().uid(freeQuery.getUid()).authStatus(FreeDepositOrder.AUTH_PENDING_FREEZE).idCard(freeQuery.getIdCard())
                .orderId(eleDepositOrder.getOrderId()).phone(freeQuery.getPhoneNumber()).realName(freeQuery.getRealName()).createTime(System.currentTimeMillis())
                .updateTime(System.currentTimeMillis()).payStatus(FreeDepositOrder.PAY_STATUS_INIT).storeId(eleDepositOrder.getStoreId())
                .franchiseeId(eleDepositOrder.getFranchiseeId()).tenantId(TenantContextHolder.getTenantId()).transAmt(eleDepositOrder.getPayAmount().doubleValue())
                .type(FreeDepositOrder.TYPE_ZHIFUBAO).depositType(FreeDepositOrder.DEPOSIT_TYPE_BATTERY).build();
        
        PxzCommonRequest<PxzFreeDepositOrderRequest> query = new PxzCommonRequest<>();
        query.setAesSecret(pxzConfig.getAesKey());
        query.setDateTime(System.currentTimeMillis());
        query.setSessionId(freeDepositOrder.getOrderId());
        query.setMerchantCode(pxzConfig.getMerchantCode());
        
        PxzFreeDepositOrderRequest request = new PxzFreeDepositOrderRequest();
        request.setPhone(freeQuery.getPhoneNumber());
        request.setSubject("企业渠道用户电池免押");
        request.setRealName(freeQuery.getRealName());
        request.setIdNumber(freeQuery.getIdCard());
        request.setTransId(freeDepositOrder.getOrderId());
        request.setTransAmt(BigDecimal.valueOf(freeDepositOrder.getTransAmt()).multiply(BigDecimal.valueOf(100)).intValue());
        query.setData(request);
        
        PxzCommonRsp<String> callPxzRsp = null;
        try {
            callPxzRsp = pxzDepositService.freeDepositOrder(query);
        } catch (Exception e) {
            log.error("Pxz error fot free battery deposit, freeDepositOrder fail! uid={},orderId={}", freeQuery.getUid(), freeDepositOrder.getOrderId(), e);
            return Triple.of(false, "3000745", "请填写免押骑手用户名/身份证号码/手机号");
        }
        
        if (Objects.isNull(callPxzRsp)) {
            log.error("Pxz error fot free battery deposit, freeDepositOrder fail! rsp is null! uid={},orderId={}", freeQuery.getUid(), freeDepositOrder.getOrderId());
            return Triple.of(false, "30007456", "免押调用失败！");
        }
        
        if (!callPxzRsp.isSuccess()) {
            return Triple.of(false, "3000747", callPxzRsp.getRespDesc());
        }
        
        freeDepositOrderService.insert(freeDepositOrder);
        eleDepositOrderService.insert(eleDepositOrder);
        
        //绑定免押订单
        UserBatteryDeposit userBatteryDeposit = new UserBatteryDeposit();
        userBatteryDeposit.setOrderId(eleDepositOrder.getOrderId());
        userBatteryDeposit.setUid(freeQuery.getUid());
        userBatteryDeposit.setDid(eleDepositOrder.getMid());
        userBatteryDeposit.setBatteryDeposit(eleDepositOrder.getPayAmount());
        userBatteryDeposit.setDelFlag(UserBatteryDeposit.DEL_NORMAL);
        userBatteryDeposit.setDepositType(UserBatteryDeposit.DEPOSIT_TYPE_FREE);
        userBatteryDeposit.setApplyDepositTime(System.currentTimeMillis());
        userBatteryDeposit.setCreateTime(System.currentTimeMillis());
        userBatteryDeposit.setUpdateTime(System.currentTimeMillis());
        userBatteryDepositService.insertOrUpdate(userBatteryDeposit);
        
        log.info("generate free deposit data from pxz for enterprise battery package, data = {}", callPxzRsp);
        //保存pxz返回的免押链接信息，5分钟之内不会生成新码
        redisService.saveWithString(CacheConstant.ELE_CACHE_ENTERPRISE_BATTERY_FREE_DEPOSIT_ORDER_GENERATE_LOCK_KEY + userInfo.getUid(), UriUtils.encode(callPxzRsp.getData(), StandardCharsets.UTF_8), 300 * 1000L, false);
        
        return Triple.of(true, null, callPxzRsp.getData());
        
    }
    
    private Triple<Boolean, String, Object> checkUserCanFreeBatteryDeposit(Long uid, UserInfo userInfo) {
        //        ElectricityConfig electricityConfig = electricityConfigService.queryFromCacheByTenantId(TenantContextHolder.getTenantId());
        //        if (!(Objects.equals(electricityConfig.getFreeDepositType(), ElectricityConfig.FREE_DEPOSIT_TYPE_BATTERY) || Objects.equals(electricityConfig.getFreeDepositType(), ElectricityConfig.FREE_DEPOSIT_TYPE_ALL))) {
        //            return Triple.of(false, "100418", "押金免押功能未开启,请联系客服处理");
        //        }
        
        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            log.error("Free battery deposit error, user is disable,uid={}", userInfo.getUid());
            return Triple.of(false, "ELECTRICITY.0024", "用户已被禁用");
        }
        
        if (!Objects.equals(userInfo.getAuthStatus(), UserInfo.AUTH_STATUS_REVIEW_PASSED)) {
            log.error("Free battery deposit error, user not auth,uid={}", uid);
            return Triple.of(false, "ELECTRICITY.0041", "未实名认证");
        }
        
        if (Objects.equals(userInfo.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_YES)) {
            return Triple.of(false, "ELECTRICITY.0049", "电池押金已经缴纳，无需重复缴纳");
        }
        return Triple.of(true, null, null);
    }
    
    private Triple<Boolean, String, Object> generateBatteryDepositOrder(UserInfo userInfo, EnterpriseFreeDepositQuery freeQuery) {
        ElectricityCabinet electricityCabinet = electricityCabinetService.queryFromCacheByProductAndDeviceName(freeQuery.getProductKey(), freeQuery.getDeviceName());
        
        BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(freeQuery.getMembercardId());
        if (Objects.isNull(batteryMemberCard)) {
            log.warn("Free battery deposit warn, not found batteryMemberCard,mid={},uid={}", freeQuery.getMembercardId(), userInfo.getUid());
            return Triple.of(false, "ELECTRICITY.00121", "电池套餐不存在");
        }
        
        if (batteryMemberCard.getDeposit().compareTo(BigDecimal.valueOf(0.01)) < 0) {
            return Triple.of(false, "100299", "免押金额不合法");
        }
        
        //生成押金独立订单
        String depositOrderId = OrderIdUtil.generateBusinessOrderId(BusinessType.BATTERY_DEPOSIT, userInfo.getUid());
        EleDepositOrder eleDepositOrder = EleDepositOrder.builder().orderId(depositOrderId).uid(userInfo.getUid()).phone(userInfo.getPhone()).name(userInfo.getName())
                .payAmount(batteryMemberCard.getDeposit()).status(EleDepositOrder.STATUS_INIT).createTime(System.currentTimeMillis()).updateTime(System.currentTimeMillis())
                .tenantId(userInfo.getTenantId()).franchiseeId(batteryMemberCard.getFranchiseeId()).payType(EleDepositOrder.FREE_DEPOSIT_PAYMENT)
                .orderType(PackageOrderTypeEnum.PACKAGE_ORDER_TYPE_ENTERPRISE.getCode())
                //.storeId(Objects.nonNull(electricityCabinet)?electricityCabinet.getStoreId():userInfo.getStoreId())
                .modelType(0).mid(freeQuery.getMembercardId()).batteryType(null).build();
        
        return Triple.of(true, null, eleDepositOrder);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Triple<Boolean, String, Object> purchasePackageByEnterpriseUser(EnterprisePackageOrderQuery query) {
        
        Integer tenantId = TenantContextHolder.getTenantId();
        //TokenUser user = SecurityUtils.getUserInfo();
        Long enterpriseId = query.getEnterpriseId();
        Long uid = query.getUid();
        
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("current user for enterprise not found");
            return Triple.of(false, "ELECTRICITY.0001", "未找到企业站长");
        }
        
        if (!redisService.setNx(CacheConstant.ELE_CACHE_ENTERPRISE_USER_PURCHASE_PACKAGE_LOCK_KEY + uid, "1", 3 * 1000L, false)) {
            return Triple.of(false, "ELECTRICITY.0034", "操作频繁");
        }
        
        EnterpriseUserPackageDetailsVO enterpriseUserPackageDetailsVO = new EnterpriseUserPackageDetailsVO();
        
        try {
            EnterpriseInfo enterpriseInfo = enterpriseInfoService.queryByIdFromDB(enterpriseId);
            if (Objects.isNull(enterpriseInfo)) {
                log.error("purchase package by enterprise user error, not found enterprise info, enterprise id = {}", enterpriseId);
                return Triple.of(false, "ELECTRICITY.0001", "未找到企业信息");
            }
            query.setFranchiseeId(enterpriseInfo.getFranchiseeId());
            
            //检查骑手信息是否属于当前企业
            EnterpriseChannelUserVO enterpriseChannelUser = enterpriseChannelUserService.selectUserByEnterpriseIdAndUid(enterpriseId, uid);
            if (Objects.isNull(enterpriseChannelUser)) {
                log.error("purchase package by enterprise user error, not found enterprise channel user, enterprise id = {}, uid = {}", enterpriseId, uid);
                return Triple.of(false, "ELECTRICITY.0001", "骑手未归属于该企业");
            }
            
            //检查骑手自主续费开关
            if (RenewalStatusEnum.RENEWAL_STATUS_BY_SELF.getCode().equals(enterpriseChannelUser.getRenewalStatus())) {
                log.error("purchase package by enterprise user error, enterprise channel user renew status by self, enterprise id = {}, uid = {}", enterpriseId, uid);
                return Triple.of(false, "300063", "骑手已开启自主续费功能，无法代付套餐，建议您前往骑手详情页进行相关设置");
            }
            
            //检查套餐是否属于当前的企业
            List<Long> packageIds = enterprisePackageService.selectByEnterpriseId(query.getEnterpriseId());
            if (Objects.isNull(packageIds) || !packageIds.contains(query.getPackageId())) {
                log.warn("purchase package by enterprise user warn, not found packages from packages, uid = {}, package id = {}", user.getUid(), query.getPackageId());
                return Triple.of(false, "300069", "当前企业套餐不存在");
            }
            
            UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
            if (Objects.isNull(userInfo)) {
                log.error("purchase package by enterprise user error, not found user, uid = {}", uid);
                return Triple.of(false, "ELECTRICITY.0001", "未找到用户");
            }
            
            if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
                log.warn("purchase package by enterprise user error, user is unUsable, uid = {}", uid);
                return Triple.of(false, "ELECTRICITY.0024", "用户已被禁用");
            }
            
            if (!Objects.equals(userInfo.getAuthStatus(), UserInfo.AUTH_STATUS_REVIEW_PASSED)) {
                log.warn("purchase package by enterprise user error, uid = {}", uid);
                return Triple.of(false, "ELECTRICITY.0041", "未实名认证");
            }
            
            if (!Objects.equals(userInfo.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_YES)) {
                log.warn("purchase package by enterprise user error, not pay deposit, uid = {}", uid);
                return Triple.of(false, "ELECTRICITY.0042", "未缴纳押金");
            }
    
           /* ElectricityPayParams electricityPayParams = electricityPayParamsService.queryFromCache(tenantId);
            if (Objects.isNull(electricityPayParams)) {
                log.warn("purchase package by enterprise user error, not found pay params,uid={}", userInfo.getUid());
                return Triple.of(false, "100307", "未配置支付参数!");
            }*/
            
            UserOauthBind userOauthBind = userOauthBindService.queryByUidAndTenantAndSource(userInfo.getUid(), tenantId,UserOauthBind.SOURCE_WX_PRO);
            if (Objects.isNull(userOauthBind) || Objects.isNull(userOauthBind.getThirdId())) {
                log.warn("purchase package by enterprise user error, not found user oauth bind or third id is null,uid={}", userInfo.getUid());
                return Triple.of(false, "100308", "未找到用户的第三方授权信息!");
            }
            
            UserBatteryDeposit userBatteryDeposit = userBatteryDepositService.selectByUidFromCache(userInfo.getUid());
            if (Objects.isNull(userBatteryDeposit)) {
                log.warn("purchase package by enterprise user error, not found userBatteryDeposit,uid={}", userInfo.getUid());
                return Triple.of(false, "ELECTRICITY.0001", "用户信息不存在");
            }
            
            //是否有正在进行中的退押
            Integer refundCount = eleRefundOrderService.queryCountByOrderId(userBatteryDeposit.getOrderId(), EleRefundOrder.BATTERY_DEPOSIT_REFUND_ORDER);
            if (refundCount > 0) {
                log.warn("purchase package by enterprise user error, have refunding order,uid={}", userInfo.getUid());
                return Triple.of(false,"120317", "该用户退押审核中，无法代付，请联系用户处理后操作");
            }
            
            BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(query.getPackageId());
            if (Objects.isNull(batteryMemberCard) || Objects.equals(batteryMemberCard.getStatus(), BatteryMemberCard.STATUS_DOWN)) {
                log.warn("purchase package by enterprise user error, not found batteryMemberCard, uid = {}, package id = {}", uid, query.getPackageId());
                return Triple.of(false, "ELECTRICITY.0087", "套餐不存在");
            }
            
            if (!Objects.equals(BatteryMemberCard.STATUS_UP, batteryMemberCard.getStatus())) {
                log.warn("purchase package by enterprise user error, batteryMemberCard is disable,uid={},mid={}", userInfo.getUid(), query.getPackageId());
                return Triple.of(false, "100275", "电池套餐不可用");
            }
    
           /* if(Objects.nonNull(userInfo.getFranchiseeId()) && !Objects.equals(userInfo.getFranchiseeId(),NumberConstant.ZERO_L) && !Objects.equals(userInfo.getFranchiseeId(),batteryMemberCard.getFranchiseeId())){
                log.warn("BATTERY DEPOSIT WARN! batteryMemberCard franchiseeId not equals,uid={},mid={}", userInfo.getUid(), query.getPackageId());
                return Triple.of(false, "100349", "用户加盟商与套餐加盟商不一致");
            }*/
            
            //判断是否存在滞纳金
            UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(userInfo.getUid());
            Triple<Boolean, Integer, BigDecimal> acquireUserBatteryServiceFeeResult = serviceFeeUserInfoService.acquireUserBatteryServiceFee(userInfo, userBatteryMemberCard,
                    batteryMemberCard, serviceFeeUserInfoService.queryByUidFromCache(userInfo.getUid()));
            if (Boolean.TRUE.equals(acquireUserBatteryServiceFeeResult.getLeft())) {
                log.warn("purchase package by enterprise user error, user exist battery service fee,uid={},mid={}", userInfo.getUid(), query.getPackageId());
                return Triple.of(false, "300084", "该用户未缴纳滞纳金，无法代付，请联系用户处理后操作");
            }
            
            if (Objects.equals(UserBatteryMemberCard.MEMBER_CARD_DISABLE, userBatteryMemberCard.getMemberCardStatus())) {
                log.warn("purchase package by enterprise user error, user package was freeze, uid={}, mid={}", userInfo.getUid(), query.getPackageId());
                return Triple.of(false, "300070", "该用户套餐已冻结，无法代付，请联系用户处理后操作");
            }
            
            if (Objects.equals(UserBatteryMemberCard.MEMBER_CARD_DISABLE_REVIEW, userBatteryMemberCard.getMemberCardStatus())) {
                log.warn("purchase package by enterprise user error, user package freeze waiting approve, uid={}, mid={}", userInfo.getUid(), query.getPackageId());
                return Triple.of(false, "300071", "该用户套餐冻结审核中，无法代付，请联系用户处理后操作");
            }
            
            //续租操作时，已经有了电池信息，查询用户当前关联的电池型号
            String batteryType = userBatteryTypeService.selectUserSimpleBatteryType(userInfo.getUid());
            
            //是否开启购买保险（是进入）
            ElectricityConfig electricityConfig = electricityConfigService.queryFromCacheByTenantId(tenantId);
            if (Objects.nonNull(electricityConfig) && Objects.equals(electricityConfig.getIsOpenInsurance(), ElectricityConfig.ENABLE_INSURANCE)) {
                //保险是否强制购买（是进入）
                FranchiseeInsurance franchiseeInsurance = franchiseeInsuranceService.queryByFranchiseeId(userInfo.getFranchiseeId(), batteryType, userInfo.getTenantId());
                long now = System.currentTimeMillis();
                if (Objects.nonNull(franchiseeInsurance) && Objects.equals(franchiseeInsurance.getIsConstraint(), FranchiseeInsurance.CONSTRAINT_FORCE)) {
                    //先判断当前用户是否已经购买保险, 用户是否没有保险信息或已过期（是进入）
                    InsuranceUserInfo insuranceUserInfo = insuranceUserInfoService.queryByUidFromCache(userInfo.getUid());
                    if (Objects.isNull(insuranceUserInfo)
                            || Objects.equals(insuranceUserInfo.getIsUse(), InsuranceUserInfo.IS_USE)
                            || insuranceUserInfo.getInsuranceExpireTime() < now) {
                        log.error("purchase package by enterprise user error! not pay insurance! uid={} ", userInfo.getUid());
                        return  Triple.of(false,"100309", "未购买保险或保险已过期");
                    }
                }
            }
            
            //套餐订单
            Triple<Boolean, String, Object> generateMemberCardOrderResult = generateMemberCardOrder(userInfo, batteryMemberCard, query, null);
            if (Boolean.FALSE.equals(generateMemberCardOrderResult.getLeft())) {
                return generateMemberCardOrderResult;
            }
            
            //保险订单
            Triple<Boolean, String, Object> generateInsuranceOrderResult = generateInsuranceOrder(userInfo, query.getInsuranceId());
            if (Boolean.FALSE.equals(generateInsuranceOrderResult.getLeft())) {
                return generateInsuranceOrderResult;
            }
            
            BigDecimal integratedPaAmount = BigDecimal.valueOf(0);
            
            ElectricityMemberCardOrder electricityMemberCardOrder = null;
            InsuranceOrder insuranceOrder = null;
            
            //保存套餐订单
            if (Boolean.TRUE.equals(generateMemberCardOrderResult.getLeft()) && Objects.nonNull(generateMemberCardOrderResult.getRight())) {
                electricityMemberCardOrder = (ElectricityMemberCardOrder) generateMemberCardOrderResult.getRight();
                electricityMemberCardOrderService.insert(electricityMemberCardOrder);
                
                integratedPaAmount = integratedPaAmount.add(electricityMemberCardOrder.getPayAmount());
            }
            
            //保存保险订单
            if (Boolean.TRUE.equals(generateInsuranceOrderResult.getLeft()) && Objects.nonNull(generateInsuranceOrderResult.getRight())) {
                insuranceOrder = (InsuranceOrder) generateInsuranceOrderResult.getRight();
                
                //设置保险关联购买订单号
                insuranceOrder.setSourceOrderNo(electricityMemberCardOrder.getOrderId());
                insuranceOrderService.insert(insuranceOrder);
                
                integratedPaAmount = integratedPaAmount.add(insuranceOrder.getPayAmount());
            }
            
            //判断企业云豆数量是否满足代付金额
            if (enterpriseInfo.getTotalBeanAmount().compareTo(integratedPaAmount) < 0) {
                log.error("purchase Package with free deposit error, not enough total bean amount, enterprise id = {}", enterpriseInfo.getId());
                throw new BizException("300079", "云豆数量不足，请先充值");
            }
            
            //更新保险状态
            if (Objects.nonNull(insuranceOrder)) {
                Pair<Boolean, Object> result = unionTradeOrderService.manageInsuranceOrder(insuranceOrder.getOrderId(), InsuranceOrder.STATUS_SUCCESS);
                if (Boolean.FALSE.equals(result.getLeft())) {
                    throw new BizException("300072", (String) result.getRight());
                }
            }
            
            //更新套餐购买状态
            if (Objects.nonNull(electricityMemberCardOrder)) {
                Pair<Boolean, Object> result = unionTradeOrderService.manageEnterpriseMemberCardOrder(electricityMemberCardOrder.getOrderId(),
                        ElectricityMemberCardOrder.STATUS_SUCCESS);
                if (Boolean.FALSE.equals(result.getLeft())) {
                    throw new BizException("300073", (String) result.getRight());
                }
            }
            
            BigDecimal totalBeanAmount = enterpriseInfo.getTotalBeanAmount();
            totalBeanAmount = totalBeanAmount.subtract(integratedPaAmount);
            
            enterpriseInfoService.subtractCloudBean(enterpriseInfo.getId(), integratedPaAmount, System.currentTimeMillis());
            
            CloudBeanUseRecord cloudBeanUseRecord = new CloudBeanUseRecord();
            cloudBeanUseRecord.setEnterpriseId(enterpriseInfo.getId());
            cloudBeanUseRecord.setUid(userInfo.getUid());
            cloudBeanUseRecord.setType(CloudBeanUseRecord.TYPE_PAY_MEMBERCARD);
            cloudBeanUseRecord.setBeanAmount(integratedPaAmount);
            cloudBeanUseRecord.setRemainingBeanAmount(totalBeanAmount);
            cloudBeanUseRecord.setPackageId(electricityMemberCardOrder.getMemberCardId());
            cloudBeanUseRecord.setFranchiseeId(enterpriseInfo.getFranchiseeId());
            cloudBeanUseRecord.setRef(electricityMemberCardOrder.getOrderId());
            cloudBeanUseRecord.setTenantId(enterpriseInfo.getTenantId());
            cloudBeanUseRecord.setCreateTime(System.currentTimeMillis());
            cloudBeanUseRecord.setUpdateTime(System.currentTimeMillis());
            cloudBeanUseRecordService.insert(cloudBeanUseRecord);
            
            //记录企业代付订单信息
            /*EnterpriseUserCostRecordDTO enterpriseUserCostRecordDTO = new EnterpriseUserCostRecordDTO();
            enterpriseUserCostRecordDTO.setUid(userInfo.getUid());
            enterpriseUserCostRecordDTO.setEnterpriseId(enterpriseId);
            enterpriseUserCostRecordDTO.setOrderId(electricityMemberCardOrder.getOrderId());
            enterpriseUserCostRecordDTO.setPackageId(batteryMemberCard.getId());
            enterpriseUserCostRecordDTO.setPackageName(batteryMemberCard.getName());
            enterpriseUserCostRecordDTO.setCostType(UserCostTypeEnum.COST_TYPE_PURCHASE_PACKAGE.getCode());
            enterpriseUserCostRecordDTO.setTenantId(tenantId.longValue());
            enterpriseUserCostRecordDTO.setCreateTime(electricityMemberCardOrder.getCreateTime());
            enterpriseUserCostRecordDTO.setUpdateTime(System.currentTimeMillis());
            enterpriseUserCostRecordDTO.setTraceId(UUID.randomUUID().toString().replaceAll("-", ""));
            
            EnterpriseUserCostRecordRemarkVO enterpriseUserCostRecordRemarkVO = new EnterpriseUserCostRecordRemarkVO();
            enterpriseUserCostRecordRemarkVO.setPayAmount(electricityMemberCardOrder.getPayAmount());
            //enterpriseUserCostRecordRemarkVO.setDepositAmount(eleDepositOrder.getPayAmount());
            if (Objects.nonNull(insuranceOrder)) {
                enterpriseUserCostRecordRemarkVO.setInsuranceAmount(insuranceOrder.getPayAmount());
            }
            enterpriseUserCostRecordDTO.setRemark(JsonUtil.toJson(enterpriseUserCostRecordRemarkVO));
            String message = JsonUtil.toJson(enterpriseUserCostRecordDTO);*/
            
            //MQ处理企业代付订单信息
            log.info("Async save enterprise user cost record for renewal package. order no = {}, package id = {}", electricityMemberCardOrder.getOrderId(), electricityMemberCardOrder.getMemberCardId());
            //enterpriseUserCostRecordProducer.sendAsyncMessage(message);
            BigDecimal insuranceAmount = null;
            if(Objects.nonNull(insuranceOrder)){
                insuranceAmount = insuranceOrder.getPayAmount();
            }
            enterpriseUserCostRecordService.asyncSaveUserCostRecordForPurchasePackage(electricityMemberCardOrder, null, insuranceAmount);
            
            enterpriseUserPackageDetailsVO.setUid(userInfo.getUid());
            enterpriseUserPackageDetailsVO.setName(userInfo.getName());
            enterpriseUserPackageDetailsVO.setPhone(userInfo.getPhone());
            enterpriseUserPackageDetailsVO.setOrderId(electricityMemberCardOrder.getOrderId());
            enterpriseUserPackageDetailsVO.setMemberCardName(batteryMemberCard.getName());
            //enterpriseUserPackageDetailsVO.setBatteryDeposit(BigDecimal.ZERO);
            
            UserBatteryMemberCard userBatteryMemberCardInfo = userBatteryMemberCardService.selectByUidFromDB(userInfo.getUid());
            enterpriseUserPackageDetailsVO.setMemberCardExpireTime(userBatteryMemberCardInfo.getMemberCardExpireTime());
            
            //查询用户保险信息
            InsuranceUserInfoVo insuranceUserInfoVo = insuranceUserInfoService.selectUserInsuranceDetailByUidAndType(userInfo.getUid(), FranchiseeInsurance.INSURANCE_TYPE_BATTERY);
            enterpriseUserPackageDetailsVO.setInsuranceUserInfoVo(insuranceUserInfoVo);
            
        } catch (BizException e) {
            log.error("renewal package by enterprise user error, uid = {}, ex = {}", uid, e);
            throw new BizException(e.getErrCode(), e.getMessage());
        } finally {
            redisService.delete(CacheConstant.ELE_CACHE_ENTERPRISE_USER_PURCHASE_PACKAGE_LOCK_KEY + uid);
        }
        
        return Triple.of(true, "", enterpriseUserPackageDetailsVO);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Triple<Boolean, String, Object> purchasePackageWithDepositByEnterpriseUser(EnterprisePackageOrderQuery query) {
        Integer tenantId = TenantContextHolder.getTenantId();
        Long enterpriseId = query.getEnterpriseId();
        Long uid = query.getUid();
        
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("current user for enterprise not found");
            return Triple.of(false, "ELECTRICITY.0001", "未找到企业站长");
        }
        
        if (!redisService.setNx(CacheConstant.ELE_CACHE_ENTERPRISE_USER_PURCHASE_PACKAGE_WITH_DEPOSIT_LOCK_KEY + uid, "1", 3 * 1000L, false)) {
            return Triple.of(false, "ELECTRICITY.0034", "操作频繁");
        }
        
        EnterpriseUserPackageDetailsVO enterpriseUserPackageDetailsVO = new EnterpriseUserPackageDetailsVO();
        
        try {
            EnterpriseInfo enterpriseInfo = enterpriseInfoService.queryByIdFromDB(enterpriseId);
            if (Objects.isNull(enterpriseInfo)) {
                log.error("purchase package with deposit by enterprise user error, not found enterprise info, enterprise id = {}", enterpriseId);
                return Triple.of(false, "ELECTRICITY.0001", "未找到企业信息");
            }
            //设置企业关联的加盟商
            query.setFranchiseeId(enterpriseInfo.getFranchiseeId());
            
            //检查骑手信息是否属于当前企业
            EnterpriseChannelUserVO enterpriseChannelUser = enterpriseChannelUserService.selectUserByEnterpriseIdAndUid(enterpriseId, uid);
            if (Objects.isNull(enterpriseChannelUser)) {
                log.error("purchase package with deposit by enterprise user error, not found enterprise channel user, enterprise id = {}, uid = {}", enterpriseId, uid);
                return Triple.of(false, "ELECTRICITY.0001", "骑手未归属于该企业");
            }
            
            //检查骑手自主续费开关
            if (RenewalStatusEnum.RENEWAL_STATUS_BY_SELF.getCode().equals(enterpriseChannelUser.getRenewalStatus())) {
                log.error("purchase package with deposit by enterprise user error, enterprise channel user renew status by self, enterprise id = {}, uid = {}", enterpriseId, uid);
                return Triple.of(false, "300063", "骑手已开启自主续费功能，无法代付套餐，建议您前往骑手详情页进行相关设置");
            }
            
            UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
            if (Objects.isNull(userInfo)) {
                log.warn("purchase package with deposit by enterprise user warn, not found user,uid={}", userInfo.getUid());
                return Triple.of(false, "ELECTRICITY.0019", "未找到用户");
            }
            
            if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
                log.warn("purchase package with deposit by enterprise user warn, user is unUsable,uid={}", userInfo.getUid());
                return Triple.of(false, "ELECTRICITY.0024", "用户已被禁用");
            }
            
            if (!Objects.equals(userInfo.getAuthStatus(), UserInfo.AUTH_STATUS_REVIEW_PASSED)) {
                log.warn("purchase package with deposit by enterprise user warn, user not auth,uid={}", userInfo.getUid());
                return Triple.of(false, "ELECTRICITY.0041", "未实名认证");
            }
            
            if (Objects.equals(userInfo.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_YES)) {
                log.warn("purchase package with deposit by enterprise user warn, user is rent deposit,uid={} ", userInfo.getUid());
                return Triple.of(false, "ELECTRICITY.0049", "已缴纳押金");
            }
            
            UserOauthBind userOauthBind = userOauthBindService.queryByUidAndTenantAndSource(userInfo.getUid(), tenantId,UserOauthBind.SOURCE_WX_PRO);
            if (Objects.isNull(userOauthBind) || Objects.isNull(userOauthBind.getThirdId())) {
                log.warn("purchase package with deposit by enterprise user warn, not found user oauth bind or third id is null,uid={}", userInfo.getUid());
                return Triple.of(false, "100308", "未找到用户的第三方授权信息!");
            }
            
            BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(query.getPackageId());
            if (Objects.isNull(batteryMemberCard)) {
                log.warn("purchase package with deposit by enterprise user warn, not found batteryMemberCard,uid={},mid={}", userInfo.getUid(), query.getPackageId());
                return Triple.of(false, "ELECTRICITY.00121", "电池套餐不存在");
            }
            
            if (!Objects.equals(BatteryMemberCard.STATUS_UP, batteryMemberCard.getStatus())) {
                log.warn("purchase package with deposit by enterprise user warn, batteryMemberCard is disable,uid={},mid={}", userInfo.getUid(), query.getPackageId());
                return Triple.of(false, "100275", "电池套餐不可用");
            }
            
            //检查套餐是否属于当前的企业
            List<Long> packageIds = enterprisePackageService.selectByEnterpriseId(query.getEnterpriseId());
            if (Objects.isNull(packageIds) || !packageIds.contains(query.getPackageId())) {
                log.warn("purchase package by enterprise user warn, not found packages from packages, uid = {}, package id = {}", userInfo.getUid(), query.getPackageId());
                return Triple.of(false, "300069", "当前企业套餐不存在");
            }
    
            /*if(Objects.nonNull(userInfo.getFranchiseeId()) && !Objects.equals(userInfo.getFranchiseeId(),NumberConstant.ZERO_L) && !Objects.equals(userInfo.getFranchiseeId(),batteryMemberCard.getFranchiseeId())){
                log.warn("purchase package with deposit by enterprise user warn, batteryMemberCard franchiseeId not equals,uid={},mid={}", userInfo.getUid(), query.getPackageId());
                return Triple.of(false, "100349", "用户加盟商与套餐加盟商不一致");
            }*/
            
            //判断是否存在滞纳金
            UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(userInfo.getUid());
            Triple<Boolean, Integer, BigDecimal> acquireUserBatteryServiceFeeResult = serviceFeeUserInfoService.acquireUserBatteryServiceFee(userInfo, userBatteryMemberCard,
                    batteryMemberCard, serviceFeeUserInfoService.queryByUidFromCache(userInfo.getUid()));
            if (Boolean.TRUE.equals(acquireUserBatteryServiceFeeResult.getLeft())) {
                log.warn("purchase package by enterprise user error, user exist battery service fee,uid={},mid={}", userInfo.getUid(), query.getPackageId());
                return Triple.of(false, "300084", "该用户未缴纳滞纳金，无法代付，请联系用户处理后操作");
            }
            
            if(Objects.nonNull(userBatteryMemberCard)){
                if (Objects.equals(UserBatteryMemberCard.MEMBER_CARD_DISABLE, userBatteryMemberCard.getMemberCardStatus())) {
                    log.warn("purchase package by enterprise user error, user package was freeze, uid={}, mid={}", userInfo.getUid(), query.getPackageId());
                    return Triple.of(false, "300070", "该用户套餐已冻结，无法代付，请联系用户处理后操作");
                }
                
                if (Objects.equals(UserBatteryMemberCard.MEMBER_CARD_DISABLE_REVIEW, userBatteryMemberCard.getMemberCardStatus())) {
                    log.warn("purchase package by enterprise user error, user package freeze waiting approve, uid={}, mid={}", userInfo.getUid(), query.getPackageId());
                    return Triple.of(false, "300071", "该用户套餐冻结审核中，无法代付，请联系用户处理后操作");
                }
            }
            
            //是否开启购买保险（是进入）
            /*ElectricityConfig electricityConfig = electricityConfigService.queryFromCacheByTenantId(tenantId);
            if (Objects.nonNull(electricityConfig) && Objects.equals(electricityConfig.getIsOpenInsurance(), ElectricityConfig.ENABLE_INSURANCE)) {
                //保险ID是否有传入
                if(Objects.isNull(query.getInsuranceId())){
                    return Triple.of(false,"100309", "请先选择购买保险");
                }
            }*/
            
            //押金订单
            Triple<Boolean, String, Object> generateDepositOrderResult = generateDepositOrder(userInfo, batteryMemberCard);
            if (Boolean.FALSE.equals(generateDepositOrderResult.getLeft())) {
                return generateDepositOrderResult;
            }
            
            //套餐订单
            //Set<Integer> userCouponIds = electricityMemberCardOrderService.generateUserCouponIds(integratedPaymentAdd.getUserCouponId(), integratedPaymentAdd.getUserCouponIds());
            Triple<Boolean, String, Object> generateMemberCardOrderResult = generateMemberCardOrder(userInfo, batteryMemberCard, query, null);
            if (Boolean.FALSE.equals(generateMemberCardOrderResult.getLeft())) {
                return generateMemberCardOrderResult;
            }
            
            //保险订单
            Triple<Boolean, String, Object> generateInsuranceOrderResult = generateInsuranceOrder(userInfo, query.getInsuranceId());
            if (Boolean.FALSE.equals(generateInsuranceOrderResult.getLeft())) {
                return generateInsuranceOrderResult;
            }
            
            BigDecimal integratedPaAmount = BigDecimal.valueOf(0);
            
            ElectricityMemberCardOrder electricityMemberCardOrder = null;
            EleDepositOrder eleDepositOrder = null;
            InsuranceOrder insuranceOrder = null;
            
            //保存套餐订单
            if (Boolean.TRUE.equals(generateMemberCardOrderResult.getLeft()) && Objects.nonNull(generateMemberCardOrderResult.getRight())) {
                electricityMemberCardOrder = (ElectricityMemberCardOrder) generateMemberCardOrderResult.getRight();
                electricityMemberCardOrderService.insert(electricityMemberCardOrder);
                
                integratedPaAmount = integratedPaAmount.add(electricityMemberCardOrder.getPayAmount());
            }
            
            //保存押金订单
            if (Boolean.TRUE.equals(generateDepositOrderResult.getLeft()) && Objects.nonNull(generateDepositOrderResult.getRight())) {
                eleDepositOrder = (EleDepositOrder) generateDepositOrderResult.getRight();
                
                //设置押金关联套餐购买订单号
                eleDepositOrder.setSourceOrderNo(electricityMemberCardOrder.getOrderId());
                eleDepositOrderService.insert(eleDepositOrder);
                
                integratedPaAmount = integratedPaAmount.add(eleDepositOrder.getPayAmount());
            }
            
            //保存保险订单
            if (Boolean.TRUE.equals(generateInsuranceOrderResult.getLeft()) && Objects.nonNull(generateInsuranceOrderResult.getRight())) {
                insuranceOrder = (InsuranceOrder) generateInsuranceOrderResult.getRight();
                
                //设置保险关联套餐购买订单号
                insuranceOrder.setSourceOrderNo(electricityMemberCardOrder.getOrderId());
                insuranceOrderService.insert(insuranceOrder);
                
                integratedPaAmount = integratedPaAmount.add(insuranceOrder.getPayAmount());
            }
            
            //判断企业云豆数量是否满足代付金额
            if (enterpriseInfo.getTotalBeanAmount().compareTo(integratedPaAmount) < 0) {
                log.error("purchase Package with free deposit error, not enough total bean amount, enterprise id = {}", enterpriseInfo.getId());
                throw new BizException("300079", "云豆数量不足，请先充值");
            }
            
            //更新押金状态
            if (Objects.nonNull(eleDepositOrder)) {
                Pair<Boolean, Object> result = unionTradeOrderService.manageDepositOrder(eleDepositOrder.getOrderId(), EleDepositOrder.STATUS_SUCCESS);
                if (Boolean.FALSE.equals(result.getLeft())) {
                    //return  Triple.of(false, "100349", result.getRight());
                    throw new BizException("300071", (String) result.getRight());
                }
            }
            
            //更新保险状态
            if (Objects.nonNull(insuranceOrder)) {
                Pair<Boolean, Object> result = unionTradeOrderService.manageInsuranceOrder(insuranceOrder.getOrderId(), InsuranceOrder.STATUS_SUCCESS);
                if (Boolean.FALSE.equals(result.getLeft())) {
                    throw new BizException("300072", (String) result.getRight());
                }
            }
            
            //更新套餐购买状态
            if (Objects.nonNull(electricityMemberCardOrder)) {
                Pair<Boolean, Object> result = unionTradeOrderService.manageEnterpriseMemberCardOrder(electricityMemberCardOrder.getOrderId(),
                        ElectricityMemberCardOrder.STATUS_SUCCESS);
                if (Boolean.FALSE.equals(result.getLeft())) {
                    throw new BizException("300073", (String) result.getRight());
                }
            }
            
            //扣减企业云豆数量
            BigDecimal totalBeanAmount = enterpriseInfo.getTotalBeanAmount();
            totalBeanAmount = totalBeanAmount.subtract(integratedPaAmount);
            
            enterpriseInfoService.subtractCloudBean(enterpriseInfo.getId(), integratedPaAmount, System.currentTimeMillis());
            
            //添加云豆使用记录
            CloudBeanUseRecord cloudBeanUseRecord = new CloudBeanUseRecord();
            cloudBeanUseRecord.setEnterpriseId(enterpriseInfo.getId());
            cloudBeanUseRecord.setUid(userInfo.getUid());
            cloudBeanUseRecord.setType(CloudBeanUseRecord.TYPE_PAY_MEMBERCARD);
            cloudBeanUseRecord.setBeanAmount(integratedPaAmount);
            cloudBeanUseRecord.setRemainingBeanAmount(totalBeanAmount);
            cloudBeanUseRecord.setPackageId(electricityMemberCardOrder.getMemberCardId());
            cloudBeanUseRecord.setFranchiseeId(enterpriseInfo.getFranchiseeId());
            cloudBeanUseRecord.setRef(electricityMemberCardOrder.getOrderId());
            cloudBeanUseRecord.setTenantId(enterpriseInfo.getTenantId());
            cloudBeanUseRecord.setCreateTime(System.currentTimeMillis());
            cloudBeanUseRecord.setUpdateTime(System.currentTimeMillis());
            cloudBeanUseRecordService.insert(cloudBeanUseRecord);
            
            //记录企业代付订单信息
           /* EnterpriseUserCostRecordDTO enterpriseUserCostRecordDTO = new EnterpriseUserCostRecordDTO();
            enterpriseUserCostRecordDTO.setUid(userInfo.getUid());
            enterpriseUserCostRecordDTO.setEnterpriseId(enterpriseId);
            enterpriseUserCostRecordDTO.setOrderId(electricityMemberCardOrder.getOrderId());
            enterpriseUserCostRecordDTO.setPackageId(batteryMemberCard.getId());
            enterpriseUserCostRecordDTO.setPackageName(batteryMemberCard.getName());
            enterpriseUserCostRecordDTO.setCostType(UserCostTypeEnum.COST_TYPE_PURCHASE_PACKAGE.getCode());
            enterpriseUserCostRecordDTO.setTenantId(tenantId.longValue());
            enterpriseUserCostRecordDTO.setCreateTime(electricityMemberCardOrder.getCreateTime());
            enterpriseUserCostRecordDTO.setUpdateTime(System.currentTimeMillis());
            enterpriseUserCostRecordDTO.setTraceId(UUID.randomUUID().toString().replaceAll("-", ""));
            
            EnterpriseUserCostRecordRemarkVO enterpriseUserCostRecordRemarkVO = new EnterpriseUserCostRecordRemarkVO();
            enterpriseUserCostRecordRemarkVO.setPayAmount(electricityMemberCardOrder.getPayAmount());
            enterpriseUserCostRecordRemarkVO.setDepositAmount(eleDepositOrder.getPayAmount());
            if (Objects.nonNull(insuranceOrder)) {
                enterpriseUserCostRecordRemarkVO.setInsuranceAmount(insuranceOrder.getPayAmount());
            }
            enterpriseUserCostRecordDTO.setRemark(JsonUtil.toJson(enterpriseUserCostRecordRemarkVO));
            String message = JsonUtil.toJson(enterpriseUserCostRecordDTO);*/
            
            //MQ处理企业代付订单信息
            log.info("Async save enterprise user cost record for purchase package with deposit. order id = {}", electricityMemberCardOrder.getOrderId());
            //enterpriseUserCostRecordProducer.sendAsyncMessage(message);
            BigDecimal insuranceAmount = null;
            if(Objects.nonNull(insuranceOrder)){
                insuranceAmount = insuranceOrder.getPayAmount();
            }
            
            enterpriseUserCostRecordService.asyncSaveUserCostRecordForPurchasePackage(electricityMemberCardOrder, eleDepositOrder.getPayAmount(), insuranceAmount);
            
            //构造前端页面套餐购买成功后显示信息
            enterpriseUserPackageDetailsVO.setUid(userInfo.getUid());
            enterpriseUserPackageDetailsVO.setName(userInfo.getName());
            enterpriseUserPackageDetailsVO.setPhone(userInfo.getPhone());
            enterpriseUserPackageDetailsVO.setOrderId(electricityMemberCardOrder.getOrderId());
            enterpriseUserPackageDetailsVO.setMemberCardName(batteryMemberCard.getName());
            enterpriseUserPackageDetailsVO.setBatteryDeposit(eleDepositOrder.getPayAmount());
            enterpriseUserPackageDetailsVO.setDepositType(UserBatteryDeposit.DEPOSIT_TYPE_DEFAULT);
            
            UserBatteryMemberCard userBatteryMemberCardInfo = userBatteryMemberCardService.selectByUidFromDB(userInfo.getUid());
            enterpriseUserPackageDetailsVO.setMemberCardExpireTime(userBatteryMemberCardInfo.getMemberCardExpireTime());
            
            //查询用户保险信息
            InsuranceUserInfoVo insuranceUserInfoVo = insuranceUserInfoService.selectUserInsuranceDetailByUidAndType(userInfo.getUid(), FranchiseeInsurance.INSURANCE_TYPE_BATTERY);
            enterpriseUserPackageDetailsVO.setInsuranceUserInfoVo(insuranceUserInfoVo);
            
        } catch (BizException e) {
            log.error("purchase package with deposit by enterprise user error, uid = {},ex = {}", uid, e);
            throw new BizException(e.getErrCode(), e.getMessage());
            
        } finally {
            redisService.delete(CacheConstant.ELE_CACHE_ENTERPRISE_USER_PURCHASE_PACKAGE_WITH_DEPOSIT_LOCK_KEY + uid);
        }
        
        return Triple.of(true, "", enterpriseUserPackageDetailsVO);
        
    }
    
    @Override
    public Triple<Boolean, String, Object> purchasePackageWithFreeDeposit(EnterprisePackageOrderQuery query) {
        Integer tenantId = TenantContextHolder.getTenantId();
        Long enterpriseId = query.getEnterpriseId();
        Long uid = query.getUid();
        if (Objects.isNull(uid)) {
            return Triple.of(false, "ELECTRICITY.0001", "未找到用户");
        }
        
        if (!redisService.setNx(CacheConstant.ELE_CACHE_ENTERPRISE_USER_PURCHASE_PACKAGE_WITHOUT_DEPOSIT_LOCK_KEY + uid, "1", 3 * 1000L, false)) {
            return Triple.of(false, "ELECTRICITY.0034", "操作频繁");
        }
        
        EnterpriseUserPackageDetailsVO enterpriseUserPackageDetailsVO = new EnterpriseUserPackageDetailsVO();
        try {
            EnterpriseInfo enterpriseInfo = enterpriseInfoService.queryByIdFromDB(enterpriseId);
            if (Objects.isNull(enterpriseInfo)) {
                log.error("purchase package with deposit by enterprise user error, not found enterprise info, enterprise id = {}", enterpriseId);
                return Triple.of(false, "ELECTRICITY.0001", "未找到企业信息");
            }
            //设置企业关联的加盟商
            query.setFranchiseeId(enterpriseInfo.getFranchiseeId());
            
            //检查骑手信息是否属于当前企业
            EnterpriseChannelUserVO enterpriseChannelUser = enterpriseChannelUserService.selectUserByEnterpriseIdAndUid(enterpriseId, uid);
            if (Objects.isNull(enterpriseChannelUser)) {
                log.error("purchase package with deposit by enterprise user error, not found enterprise channel user, enterprise id = {}, uid = {}", enterpriseId, uid);
                return Triple.of(false, "ELECTRICITY.0001", "骑手未归属于该企业");
            }
            
            //检查骑手自主续费开关
            if (RenewalStatusEnum.RENEWAL_STATUS_BY_SELF.getCode().equals(enterpriseChannelUser.getRenewalStatus())) {
                log.error("purchase package with deposit by enterprise user error, Not found enterprise channel user, enterprise id = {}, uid = {}", enterpriseId, uid);
                return Triple.of(false, "300063", "骑手已开启自主续费功能，无法代付套餐，建议您前往骑手详情页进行相关设置");
            }
            
            UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
            if (Objects.isNull(userInfo)) {
                log.error("purchase Package with free deposit error, not found user info,uid={}", uid);
                return Triple.of(false, "ELECTRICITY.0001", "未能查到用户信息");
            }
            
            if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
                log.error("purchase Package with free deposit error, not found userInfo,uid={}", uid);
                return Triple.of(false, "ELECTRICITY.0024", "用户已被禁用");
            }
            
            if (!Objects.equals(userInfo.getAuthStatus(), UserInfo.AUTH_STATUS_REVIEW_PASSED)) {
                log.error("purchase Package with free deposit error, user not auth,uid={}", uid);
                return Triple.of(false, "ELECTRICITY.0041", "未实名认证");
            }
    
           /* ElectricityPayParams electricityPayParams = electricityPayParamsService.queryFromCache(tenantId);
            if (Objects.isNull(electricityPayParams)) {
                log.error("purchase Package with free deposit error, not found electricityPayParams,uid={}", uid);
                return Triple.of(false, "100234", "未配置支付参数!");
            }*/
            
            UserOauthBind userOauthBind = userOauthBindService.queryByUidAndTenantAndSource(uid, tenantId,UserOauthBind.SOURCE_WX_PRO);
            if (Objects.isNull(userOauthBind) || Objects.isNull(userOauthBind.getThirdId())) {
                log.error("purchase Package with free deposit error, not found userOauthBind,uid={}", uid);
                return Triple.of(false, "100235", "未找到用户的第三方授权信息!");
            }
            
            UserBatteryDeposit userBatteryDeposit = userBatteryDepositService.selectByUidFromCache(userInfo.getUid());
            if (Objects.isNull(userBatteryDeposit)) {
                log.error("purchase Package with free deposit error, not found userBatteryDeposit,uid={}", uid);
                return Triple.of(false, "100247", "用户信息不存在");
            }
            
            FreeDepositOrder freeDepositOrder = freeDepositOrderService.selectByOrderId(userBatteryDeposit.getOrderId());
            if (Objects.isNull(freeDepositOrder) || !Objects.equals(freeDepositOrder.getAuthStatus(), FreeDepositOrder.AUTH_FROZEN)) {
                log.error("purchase Package with free deposit error, freeDepositOrder is anomaly,uid={}", uid);
                return Triple.of(false, "100402", "免押失败！");
            }
            
            //获取押金订单
            EleDepositOrder eleDepositOrder = eleDepositOrderService.queryByOrderId(userBatteryDeposit.getOrderId());
            if (Objects.isNull(eleDepositOrder)) {
                log.error("purchase Package with free deposit error, not found eleDepositOrder! uid={},orderId={}", uid, userBatteryDeposit.getOrderId());
                return Triple.of(false, "ELECTRICITY.0015", "未找到订单");
            }
            
            BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(query.getPackageId().longValue());
            if (Objects.isNull(batteryMemberCard)) {
                log.warn("purchase Package with free deposit warning, not found batteryMemberCard,uid={},mid={}", userInfo.getUid(), query.getPackageId());
                return Triple.of(false, "ELECTRICITY.00121", "电池套餐不存在");
            }
            
            if (!Objects.equals(BatteryMemberCard.STATUS_UP, batteryMemberCard.getStatus())) {
                log.warn("purchase Package with free deposit warning, batteryMemberCard is disable,uid={},mid={}", userInfo.getUid(), query.getPackageId());
                return Triple.of(false, "100275", "电池套餐不可用");
            }
            
            //判断套餐是否为免押套餐
            if(!Objects.equals( batteryMemberCard.getFreeDeposite(), BatteryMemberCard.YES)){
                log.warn("FREE DEPOSIT WARN! batteryMemberCard is illegal,uid={},mid={}", userInfo.getUid(), query.getPackageId());
                return Triple.of(false, "100483", "电池套餐不合法");
            }
            
            if(Objects.nonNull(userInfo.getFranchiseeId()) && !Objects.equals(userInfo.getFranchiseeId(),NumberConstant.ZERO_L) && !Objects.equals(userInfo.getFranchiseeId(), batteryMemberCard.getFranchiseeId())){
                log.warn("BATTERY DEPOSIT WARN! batteryMemberCard franchiseeId not equals,uid={},mid={}", userInfo.getUid(), query.getPackageId());
                return Triple.of(false, "100349", "用户加盟商与套餐加盟商不一致");
            }
            
            //检查套餐是否属于当前的企业
            List<Long> packageIds = enterprisePackageService.selectByEnterpriseId(query.getEnterpriseId());
            if (Objects.isNull(packageIds) || !packageIds.contains(query.getPackageId())) {
                log.warn("purchase package by enterprise user warn, not found packages from packages, uid = {}, package id = {}", userInfo.getUid(), query.getPackageId());
                return Triple.of(false, "300069", "当前企业套餐不存在");
            }
            
            //是否有正在进行中的退押
            Integer refundCount = eleRefundOrderService.queryCountByOrderId(userBatteryDeposit.getOrderId(), EleRefundOrder.BATTERY_DEPOSIT_REFUND_ORDER);
            if (refundCount > 0) {
                log.warn("purchase Package with free deposit warning, have refunding order,uid={}", userInfo.getUid());
                return Triple.of(false,"120317", "该用户退押审核中，无法代付，请联系用户处理后操作");
            }
            
            //判断是否存在滞纳金
            UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(userInfo.getUid());
            Triple<Boolean, Integer, BigDecimal> acquireUserBatteryServiceFeeResult = serviceFeeUserInfoService.acquireUserBatteryServiceFee(userInfo, userBatteryMemberCard,
                    batteryMemberCard, serviceFeeUserInfoService.queryByUidFromCache(userInfo.getUid()));
            if (Boolean.TRUE.equals(acquireUserBatteryServiceFeeResult.getLeft())) {
                log.warn("purchase package by enterprise user error, user exist battery service fee,uid={},mid={}", userInfo.getUid(), query.getPackageId());
                return Triple.of(false, "300084", "该用户未缴纳滞纳金，无法代付，请联系用户处理后操作");
            }
            
            //如果会员表存在信息，则用户并非第一次购买套餐，需要检查是否存在冻结的状况
            if(Objects.nonNull(userBatteryMemberCard)){
                if (Objects.equals(UserBatteryMemberCard.MEMBER_CARD_DISABLE, userBatteryMemberCard.getMemberCardStatus())) {
                    log.warn("purchase package by enterprise user error, user package was freeze, uid={}, mid={}", userInfo.getUid(), query.getPackageId());
                    return Triple.of(false, "300070", "该用户套餐已冻结，无法代付，请联系用户处理后操作");
                }
                
                if (Objects.equals(UserBatteryMemberCard.MEMBER_CARD_DISABLE_REVIEW, userBatteryMemberCard.getMemberCardStatus())) {
                    log.warn("purchase package by enterprise user error, user package freeze waiting approve, uid={}, mid={}", userInfo.getUid(), query.getPackageId());
                    return Triple.of(false, "300071", "该用户套餐冻结审核中，无法代付，请联系用户处理后操作");
                }
            }
            
            //是否开启购买保险（是进入）
            /*ElectricityConfig electricityConfig = electricityConfigService.queryFromCacheByTenantId(tenantId);
            if (Objects.nonNull(electricityConfig) && Objects.equals(electricityConfig.getIsOpenInsurance(), ElectricityConfig.ENABLE_INSURANCE)) {
                //保险ID是否有传入
                if(Objects.isNull(query.getInsuranceId())){
                    return Triple.of(false,"100309", "请先选择购买保险");
                }
            }*/
            
            //套餐订单
            Triple<Boolean, String, Object> rentBatteryMemberCardTriple = generateMemberCardOrder(userInfo, batteryMemberCard, query, null);
            if (Boolean.FALSE.equals(rentBatteryMemberCardTriple.getLeft())) {
                return rentBatteryMemberCardTriple;
            }
            
            //保险订单
            Triple<Boolean, String, Object> rentBatteryInsuranceTriple = generateInsuranceOrder(userInfo, query.getInsuranceId());
            if (Boolean.FALSE.equals(rentBatteryInsuranceTriple.getLeft())) {
                return rentBatteryInsuranceTriple;
            }
            
            BigDecimal totalPayAmount = BigDecimal.valueOf(0);
            
            ElectricityMemberCardOrder electricityMemberCardOrder = null;
            InsuranceOrder insuranceOrder = null;
            
            //保存套餐订单
            if (Objects.nonNull(rentBatteryMemberCardTriple.getRight())) {
                electricityMemberCardOrder = (ElectricityMemberCardOrder) (rentBatteryMemberCardTriple.getRight());
                electricityMemberCardOrderService.insert(electricityMemberCardOrder);
                
                totalPayAmount = totalPayAmount.add(electricityMemberCardOrder.getPayAmount());
            }
            
            //保存保险订单
            if (Objects.nonNull(rentBatteryInsuranceTriple.getRight())) {
                insuranceOrder = (InsuranceOrder) rentBatteryInsuranceTriple.getRight();
                //设置保险关联购买订单号
                insuranceOrder.setSourceOrderNo(electricityMemberCardOrder.getOrderId());
                insuranceOrderService.insert(insuranceOrder);
                
                totalPayAmount = totalPayAmount.add(insuranceOrder.getPayAmount());
            }
            
            //判断企业云豆数量是否满足待支付云豆数
            if (enterpriseInfo.getTotalBeanAmount().compareTo(totalPayAmount) < 0) {
                log.error("purchase Package with free deposit error, not enough total bean amount, enterprise id = {}", enterpriseInfo.getId());
                throw new BizException("300079", "云豆数量不足，请先充值");
            }
            
            //更新保险状态
            if (Objects.nonNull(insuranceOrder)) {
                Pair<Boolean, Object> result = unionTradeOrderService.manageInsuranceOrder(insuranceOrder.getOrderId(), InsuranceOrder.STATUS_SUCCESS);
                if (Boolean.FALSE.equals(result.getLeft())) {
                    throw new BizException("300072", (String) result.getRight());
                }
            }
            
            //更新套餐购买状态
            if (Objects.nonNull(electricityMemberCardOrder)) {
                Pair<Boolean, Object> result = unionTradeOrderService.manageEnterpriseMemberCardOrder(electricityMemberCardOrder.getOrderId(),
                        ElectricityMemberCardOrder.STATUS_SUCCESS);
                if (Boolean.FALSE.equals(result.getLeft())) {
                    throw new BizException("300073", (String) result.getRight());
                }
            }
            
            BigDecimal totalBeanAmount = enterpriseInfo.getTotalBeanAmount();
            totalBeanAmount = totalBeanAmount.subtract(totalPayAmount);
            
            enterpriseInfoService.subtractCloudBean(enterpriseInfo.getId(), totalPayAmount, System.currentTimeMillis());
            
            
            CloudBeanUseRecord cloudBeanUseRecord = new CloudBeanUseRecord();
            cloudBeanUseRecord.setEnterpriseId(enterpriseInfo.getId());
            cloudBeanUseRecord.setUid(userInfo.getUid());
            cloudBeanUseRecord.setType(CloudBeanUseRecord.TYPE_PAY_MEMBERCARD);
            cloudBeanUseRecord.setBeanAmount(totalPayAmount);
            cloudBeanUseRecord.setRemainingBeanAmount(totalBeanAmount);
            cloudBeanUseRecord.setPackageId(electricityMemberCardOrder.getMemberCardId());
            cloudBeanUseRecord.setFranchiseeId(enterpriseInfo.getFranchiseeId());
            cloudBeanUseRecord.setRef(electricityMemberCardOrder.getOrderId());
            cloudBeanUseRecord.setTenantId(enterpriseInfo.getTenantId());
            cloudBeanUseRecord.setCreateTime(System.currentTimeMillis());
            cloudBeanUseRecord.setUpdateTime(System.currentTimeMillis());
            cloudBeanUseRecordService.insert(cloudBeanUseRecord);
            
            //记录企业代付订单信息
            /*EnterpriseUserCostRecordDTO enterpriseUserCostRecordDTO = new EnterpriseUserCostRecordDTO();
            enterpriseUserCostRecordDTO.setUid(userInfo.getUid());
            enterpriseUserCostRecordDTO.setEnterpriseId(enterpriseId);
            enterpriseUserCostRecordDTO.setOrderId(electricityMemberCardOrder.getOrderId());
            enterpriseUserCostRecordDTO.setPackageId(batteryMemberCard.getId());
            enterpriseUserCostRecordDTO.setPackageName(batteryMemberCard.getName());
            enterpriseUserCostRecordDTO.setCostType(UserCostTypeEnum.COST_TYPE_PURCHASE_PACKAGE.getCode());
            enterpriseUserCostRecordDTO.setTenantId(tenantId.longValue());
            enterpriseUserCostRecordDTO.setCreateTime(electricityMemberCardOrder.getCreateTime());
            enterpriseUserCostRecordDTO.setUpdateTime(System.currentTimeMillis());
            enterpriseUserCostRecordDTO.setTraceId(UUID.randomUUID().toString().replaceAll("-", ""));
            
            EnterpriseUserCostRecordRemarkVO enterpriseUserCostRecordRemarkVO = new EnterpriseUserCostRecordRemarkVO();
            enterpriseUserCostRecordRemarkVO.setPayAmount(electricityMemberCardOrder.getPayAmount());
            enterpriseUserCostRecordRemarkVO.setDepositAmount(batteryMemberCard.getDeposit());
            if (Objects.nonNull(insuranceOrder)) {
                enterpriseUserCostRecordRemarkVO.setInsuranceAmount(insuranceOrder.getPayAmount());
            }
            enterpriseUserCostRecordDTO.setRemark(JsonUtil.toJson(enterpriseUserCostRecordRemarkVO));
            String message = JsonUtil.toJson(enterpriseUserCostRecordDTO);*/
            //MQ处理企业代付订单信息
            log.info("Async save enterprise user cost record for purchase package with free deposit. order no = {}, member card id = {}", electricityMemberCardOrder.getOrderId(), electricityMemberCardOrder.getMemberCardId());
            //enterpriseUserCostRecordProducer.sendAsyncMessage(message);
            
            BigDecimal insuranceAmount = null;
            if(Objects.nonNull(insuranceOrder)){
                insuranceAmount = insuranceOrder.getPayAmount();
            }
            enterpriseUserCostRecordService.asyncSaveUserCostRecordForPurchasePackage(electricityMemberCardOrder, batteryMemberCard.getDeposit(), insuranceAmount);
            
            enterpriseUserPackageDetailsVO.setUid(userInfo.getUid());
            enterpriseUserPackageDetailsVO.setName(userInfo.getName());
            enterpriseUserPackageDetailsVO.setPhone(userInfo.getPhone());
            enterpriseUserPackageDetailsVO.setOrderId(electricityMemberCardOrder.getOrderId());
            enterpriseUserPackageDetailsVO.setMemberCardName(batteryMemberCard.getName());
            enterpriseUserPackageDetailsVO.setBatteryDeposit(batteryMemberCard.getDeposit());
            enterpriseUserPackageDetailsVO.setDepositType(UserBatteryDeposit.DEPOSIT_TYPE_FREE);
            
            UserBatteryMemberCard userBatteryMemberCardInfo = userBatteryMemberCardService.selectByUidFromDB(userInfo.getUid());
            enterpriseUserPackageDetailsVO.setMemberCardExpireTime(userBatteryMemberCardInfo.getMemberCardExpireTime());
            
            //查询用户保险信息
            InsuranceUserInfoVo insuranceUserInfoVo = insuranceUserInfoService.selectUserInsuranceDetailByUidAndType(userInfo.getUid(), FranchiseeInsurance.INSURANCE_TYPE_BATTERY);
            /*InsuranceUserInfo insuranceUserInfo = insuranceUserInfoService.selectByUidAndTypeFromCache(userInfo.getUid(), FranchiseeInsurance.INSURANCE_TYPE_BATTERY);
            InsuranceUserInfoVo insuranceUserInfoVo = new InsuranceUserInfoVo();
            if(Objects.nonNull(insuranceUserInfo)) {
                BeanUtils.copyProperties(insuranceUserInfo, insuranceUserInfoVo);
            }*/
            enterpriseUserPackageDetailsVO.setInsuranceUserInfoVo(insuranceUserInfoVo);
            
        } catch (BizException e) {
            log.error("purchase package without deposit by enterprise user error, uid = {}, ex = {}", uid, e);
            throw new BizException(e.getErrCode(), e.getMessage());
            
        } finally {
            redisService.delete(CacheConstant.ELE_CACHE_ENTERPRISE_USER_PURCHASE_PACKAGE_WITHOUT_DEPOSIT_LOCK_KEY + uid);
        }
        
        return Triple.of(true, "", enterpriseUserPackageDetailsVO);
    }
    
    private Triple<Boolean, String, Object> generateInsuranceOrder(UserInfo userInfo, Integer insuranceId) {
        
        if (Objects.isNull(insuranceId)) {
            return Triple.of(true, "", null);
        }
        
        //查询保险
        FranchiseeInsurance franchiseeInsurance = franchiseeInsuranceService.queryByIdFromCache(insuranceId);
        
        if (Objects.isNull(franchiseeInsurance) || !Objects.equals(franchiseeInsurance.getInsuranceType(), FranchiseeInsurance.INSURANCE_TYPE_BATTERY)) {
            log.error("generate insurance order error, not found member_card by id={},uid={}", insuranceId, userInfo.getUid());
            return Triple.of(false, "100305", "未找到保险!");
        }
        if (ObjectUtil.equal(FranchiseeInsurance.STATUS_UN_USABLE, franchiseeInsurance.getStatus())) {
            log.error("generate insurance order error, member_card is un_usable id={},uid={}", insuranceId, userInfo.getUid());
            return Triple.of(false, "100306", "保险已禁用!");
        }
        
        if (Objects.isNull(franchiseeInsurance.getPremium())) {
            log.error("generate insurance order error, payAmount is null ！franchiseeId={},uid={}", insuranceId, userInfo.getUid());
            return Triple.of(false, "100305", "未找到保险");
        }
        
        //生成保险独立订单
        String insuranceOrderId = OrderIdUtil.generateBusinessOrderId(BusinessType.BATTERY_INSURANCE, userInfo.getUid());
        InsuranceOrder insuranceOrder = InsuranceOrder.builder().insuranceId(franchiseeInsurance.getId()).insuranceName(franchiseeInsurance.getName())
                .insuranceType(franchiseeInsurance.getInsuranceType()).orderId(insuranceOrderId).cid(franchiseeInsurance.getCid())
                .franchiseeId(franchiseeInsurance.getFranchiseeId()).isUse(InsuranceOrder.NOT_USE).payAmount(franchiseeInsurance.getPremium())
                .forehead(franchiseeInsurance.getForehead()).payType(InsuranceOrder.ONLINE_PAY_TYPE).phone(userInfo.getPhone()).status(InsuranceOrder.STATUS_INIT)
                // .storeId(Objects.nonNull(electricityCabinet) ? electricityCabinet.getStoreId() : userInfo.getStoreId())
                .tenantId(userInfo.getTenantId()).uid(userInfo.getUid()).userName(userInfo.getName()).validDays(franchiseeInsurance.getValidDays())
                .createTime(System.currentTimeMillis()).updateTime(System.currentTimeMillis())
                .simpleBatteryType(franchiseeInsurance.getSimpleBatteryType()).build();
        
        return Triple.of(true, null, insuranceOrder);
    }
    
    private Triple<Boolean, String, Object> generateDepositOrder(UserInfo userInfo, BatteryMemberCard batteryMemberCard) {
        
        //生成押金独立订单
        String depositOrderId = OrderIdUtil.generateBusinessOrderId(BusinessType.BATTERY_DEPOSIT, userInfo.getUid());
        EleDepositOrder eleDepositOrder = EleDepositOrder.builder().orderId(depositOrderId).uid(userInfo.getUid()).phone(userInfo.getPhone()).name(userInfo.getName())
                .payAmount(batteryMemberCard.getDeposit()).status(EleDepositOrder.STATUS_INIT).createTime(System.currentTimeMillis()).updateTime(System.currentTimeMillis())
                .tenantId(userInfo.getTenantId()).franchiseeId(batteryMemberCard.getFranchiseeId()).payType(EleDepositOrder.ONLINE_PAYMENT)
                .orderType(PackageOrderTypeEnum.PACKAGE_ORDER_TYPE_ENTERPRISE.getCode()).mid(batteryMemberCard.getId()).modelType(0).build();
        
        return Triple.of(true, null, eleDepositOrder);
    }
    
    private Triple<Boolean, String, Object> generateMemberCardOrder(UserInfo userInfo, BatteryMemberCard batteryMemberCard, EnterprisePackageOrderQuery query,
            Set<Integer> userCouponIds) {
        
        //查找计算优惠券
        //计算优惠后支付金额
        Triple<Boolean, String, Object> calculatePayAmountResult = electricityMemberCardOrderService.calculatePayAmount(batteryMemberCard.getRentPrice(), userCouponIds);
        if (Boolean.FALSE.equals(calculatePayAmountResult.getLeft())) {
            return calculatePayAmountResult;
        }
        BigDecimal payAmount = (BigDecimal) calculatePayAmountResult.getRight();
        
        //支付金额不能为负数
        if (payAmount.compareTo(BigDecimal.valueOf(0.01)) < 0) {
            payAmount = BigDecimal.valueOf(0);
        }
        
        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(userInfo.getUid());
        Integer payCount = electricityMemberCardOrderService.queryMaxPayCount(userBatteryMemberCard);
        
        ElectricityMemberCardOrder electricityMemberCardOrder = new ElectricityMemberCardOrder();
        electricityMemberCardOrder.setOrderId(OrderIdUtil.generateBusinessOrderId(BusinessType.ENTERPRISE_BATTERY_PACKAGE, userInfo.getUid()));
        //设置当前订单类型为企业渠道购买
        electricityMemberCardOrder.setOrderType(PackageOrderTypeEnum.PACKAGE_ORDER_TYPE_ENTERPRISE.getCode());
        electricityMemberCardOrder.setEnterpriseId(query.getEnterpriseId());
        electricityMemberCardOrder.setPayType(ElectricityMemberCardOrder.ENTERPRISE_PAYMENT);
        
        electricityMemberCardOrder.setCreateTime(System.currentTimeMillis());
        electricityMemberCardOrder.setUpdateTime(System.currentTimeMillis());
        electricityMemberCardOrder.setStatus(ElectricityMemberCardOrder.STATUS_INIT);
        electricityMemberCardOrder.setMemberCardId(query.getPackageId());
        electricityMemberCardOrder.setUid(userInfo.getUid());
        electricityMemberCardOrder.setMaxUseCount(batteryMemberCard.getUseCount());
        electricityMemberCardOrder.setCardName(batteryMemberCard.getName());
        electricityMemberCardOrder.setPayAmount(payAmount);
        electricityMemberCardOrder.setUserName(userInfo.getName());
        electricityMemberCardOrder.setValidDays(batteryMemberCard.getValidDays());
        electricityMemberCardOrder.setTenantId(batteryMemberCard.getTenantId());
        //设置企业的加盟商为套餐订单的加盟商
        electricityMemberCardOrder.setFranchiseeId(query.getFranchiseeId());
        electricityMemberCardOrder.setPayCount(payCount);
        electricityMemberCardOrder.setSendCouponId(Objects.nonNull(batteryMemberCard.getCouponId()) ? batteryMemberCard.getCouponId().longValue() : null);
        //electricityMemberCardOrder.setRefId(Objects.nonNull(electricityCabinet) ? electricityCabinet.getId().longValue() : null);
        electricityMemberCardOrder.setSource(ElectricityMemberCardOrder.SOURCE_NOT_SCAN);
        //企业套餐没有门店信息
        // electricityMemberCardOrder.setStoreId( );
        electricityMemberCardOrder.setCouponIds(batteryMemberCard.getCouponIds());
        
        return Triple.of(true, null, electricityMemberCardOrder);
    }
    
    @Deprecated
    @Slave
    @Override
    public Triple<Boolean, String, Object> queryRiderDetails(EnterpriseMemberCardQuery query) {
        EnterpriseUserPackageDetailsVO enterpriseUserPackageDetailsVO = new EnterpriseUserPackageDetailsVO();
        
        UserInfo userInfo = userInfoService.queryByUidFromCache(query.getUid());
        if (Objects.isNull(userInfo)) {
            log.warn("query rider details info, not found userInfo,uid = {}", query.getUid());
            return Triple.of(true, null, enterpriseUserPackageDetailsVO);
        }
        
        Franchisee franchisee = franchiseeService.queryByIdFromCache(userInfo.getFranchiseeId());
        
        enterpriseUserPackageDetailsVO.setModelType(Objects.isNull(franchisee) ? null : franchisee.getModelType());
        enterpriseUserPackageDetailsVO.setBatteryRentStatus(userInfo.getBatteryRentStatus());
        enterpriseUserPackageDetailsVO.setBatteryDepositStatus(userInfo.getBatteryDepositStatus());
        enterpriseUserPackageDetailsVO.setFranchiseeId(userInfo.getFranchiseeId());
        enterpriseUserPackageDetailsVO.setStoreId(userInfo.getStoreId());
        enterpriseUserPackageDetailsVO.setIsExistMemberCard(UserBatteryMemberCardInfoVO.NO);
        
        //设置骑手个人信息
        enterpriseUserPackageDetailsVO.setUid(userInfo.getUid());
        enterpriseUserPackageDetailsVO.setName(userInfo.getName());
        enterpriseUserPackageDetailsVO.setPhone(userInfo.getPhone());
        enterpriseUserPackageDetailsVO.setIdNumber(userInfo.getIdNumber());
        
        //查询骑手续费方式
        EnterpriseChannelUserVO enterpriseChannelUserVO = enterpriseChannelUserService.selectUserByEnterpriseIdAndUid(query.getEnterpriseId(), query.getUid());
        log.info("query enterprise channel user, enterprise id = {}, uid = {}", query.getEnterpriseId(), query.getUid());
        if (Objects.isNull(enterpriseChannelUserVO)) {
            log.warn("query rider details info, not found enterprise channel user, uid = {}", query.getUid());
            return Triple.of(true, null, enterpriseUserPackageDetailsVO);
        }
        enterpriseUserPackageDetailsVO.setRenewalStatus(enterpriseChannelUserVO.getRenewalStatus());
        
        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(userInfo.getUid());
        if (Objects.isNull(userBatteryMemberCard) || Objects.isNull(userBatteryMemberCard.getMemberCardId()) || Objects.equals(userBatteryMemberCard.getMemberCardId(),
                NumberConstant.ZERO_L)) {
            log.warn("query rider details failed, not found userBatteryMemberCard,uid = {}", userInfo.getUid());
            return Triple.of(true, null, enterpriseUserPackageDetailsVO);
        }
        
        BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(userBatteryMemberCard.getMemberCardId());
        if (Objects.isNull(batteryMemberCard)) {
            log.warn("query rider details failed, not found batteryMemberCard,uid = {},mid = {}", userInfo.getUid(), userBatteryMemberCard.getMemberCardId());
            return Triple.of(true, null, enterpriseUserPackageDetailsVO);
        }
        
        if (NumberConstant.ZERO_L.equals(userBatteryMemberCard.getMemberCardId()) || !BatteryMemberCardBusinessTypeEnum.BUSINESS_TYPE_ENTERPRISE_BATTERY.getCode()
                .equals(batteryMemberCard.getBusinessType())) {
            ElectricityMemberCardOrder electricityMemberCardOrder = enterpriseBatteryPackageMapper.selectLatestEnterpriseOrderByUid(query.getUid());
            if (Objects.isNull(electricityMemberCardOrder)) {
                return Triple.of(true, null, enterpriseUserPackageDetailsVO);
            }
            
            BatteryMemberCard batteryPackage = batteryMemberCardService.queryByIdFromCache(electricityMemberCardOrder.getMemberCardId());
            
            enterpriseUserPackageDetailsVO.setOrderId(electricityMemberCardOrder.getOrderId());
            enterpriseUserPackageDetailsVO.setMemberCardExpireTime(null);
            enterpriseUserPackageDetailsVO.setMemberCardId(batteryPackage.getId());
            enterpriseUserPackageDetailsVO.setMemberCardName(batteryPackage.getName());
            enterpriseUserPackageDetailsVO.setBatteryMembercardPayAmount(Objects.isNull(electricityMemberCardOrder) ? null : electricityMemberCardOrder.getPayAmount());
            enterpriseUserPackageDetailsVO.setMemberCardPayTime(Objects.isNull(electricityMemberCardOrder) ? null : electricityMemberCardOrder.getCreateTime());
            
            //获取关联押金信息
            EleDepositOrderVO eleDepositOrderVO = eleDepositOrderService.queryByUidAndSourceOrderNo(query.getUid(), electricityMemberCardOrder.getOrderId());
            if (Objects.nonNull(eleDepositOrderVO)) {
                enterpriseUserPackageDetailsVO.setBatteryDeposit(eleDepositOrderVO.getPayAmount());
                enterpriseUserPackageDetailsVO.setDepositType(UserBatteryDeposit.DEPOSIT_TYPE_DEFAULT);
            } else {
                //免押设置
                // enterprisePackageOrderVO.setBatteryDeposit(BigDecimal.ZERO);
                enterpriseUserPackageDetailsVO.setDepositType(UserBatteryDeposit.DEPOSIT_TYPE_FREE);
            }
            //此时用户无绑定电池信息
        } else {
            
            enterpriseUserPackageDetailsVO.setIsExistMemberCard(UserBatteryMemberCardInfoVO.YES);
            enterpriseUserPackageDetailsVO.setMemberCardStatus(userBatteryMemberCard.getMemberCardStatus());
            enterpriseUserPackageDetailsVO.setMemberCardExpireTime(userBatteryMemberCard.getMemberCardExpireTime());
            enterpriseUserPackageDetailsVO.setRemainingNumber(userBatteryMemberCard.getRemainingNumber());
            enterpriseUserPackageDetailsVO.setMemberCardId(userBatteryMemberCard.getMemberCardId());
            
            if (Objects.equals(batteryMemberCard.getRentUnit(), BatteryMemberCard.RENT_UNIT_DAY)) {
                enterpriseUserPackageDetailsVO.setValidDays(userBatteryMemberCard.getMemberCardExpireTime() > System.currentTimeMillis() ? (int) Math.ceil(
                        (userBatteryMemberCard.getMemberCardExpireTime() - System.currentTimeMillis()) / 24 / 60 / 60 / 1000.0) : 0);
            } else {
                enterpriseUserPackageDetailsVO.setValidDays(userBatteryMemberCard.getMemberCardExpireTime() > System.currentTimeMillis() ? (int) Math.ceil(
                        (userBatteryMemberCard.getMemberCardExpireTime() - System.currentTimeMillis()) / 60 / 1000.0) : 0);
            }
            
            //查询用户押金状况
            UserBatteryDeposit userBatteryDeposit = userBatteryDepositService.selectByUidFromCache(userInfo.getUid());
            if (Objects.isNull(userBatteryDeposit) || StringUtils.isBlank(userBatteryDeposit.getOrderId())) {
                log.warn("query rider details failed, not found userBatteryDeposit,uid = {}", userInfo.getUid());
                return Triple.of(true, null, enterpriseUserPackageDetailsVO);
            }
            
            enterpriseUserPackageDetailsVO.setBatteryDeposit(userBatteryDeposit.getBatteryDeposit());
            enterpriseUserPackageDetailsVO.setDepositType(userBatteryDeposit.getDepositType());
            
            //套餐订单金额
            ElectricityMemberCardOrder electricityMemberCardOrder = electricityMemberCardOrderService.selectByOrderNo(userBatteryMemberCard.getOrderId());
            enterpriseUserPackageDetailsVO.setBatteryMembercardPayAmount(Objects.isNull(electricityMemberCardOrder) ? null : electricityMemberCardOrder.getPayAmount());
            enterpriseUserPackageDetailsVO.setMemberCardPayTime(Objects.isNull(electricityMemberCardOrder) ? null : electricityMemberCardOrder.getCreateTime());
            enterpriseUserPackageDetailsVO.setMemberCardName(batteryMemberCard.getName());
            enterpriseUserPackageDetailsVO.setRentUnit(batteryMemberCard.getRentUnit());
            enterpriseUserPackageDetailsVO.setLimitCount(batteryMemberCard.getLimitCount());
            
            //用户电池型号
            enterpriseUserPackageDetailsVO.setUserBatterySimpleType(userBatteryTypeService.selectUserSimpleBatteryType(userInfo.getUid()));
            
            //查询用户保险信息
            InsuranceUserInfo insuranceUserInfo = insuranceUserInfoService.selectByUidAndTypeFromCache(userInfo.getUid(), FranchiseeInsurance.INSURANCE_TYPE_BATTERY);
            InsuranceUserInfoVo insuranceUserInfoVo = new InsuranceUserInfoVo();
            if (Objects.nonNull(insuranceUserInfo)) {
                BeanUtils.copyProperties(insuranceUserInfo, insuranceUserInfoVo);
            }
            enterpriseUserPackageDetailsVO.setInsuranceUserInfoVo(insuranceUserInfoVo);
            
        }
        
        return Triple.of(true, null, enterpriseUserPackageDetailsVO);
    }
    
    @Slave
    @Override
    public Triple<Boolean, String, Object> queryDepositInfo(EnterpriseMemberCardQuery query) {
        
        EnterpriseUserPackageDetailsVO enterpriseUserPackageDetailsVO = new EnterpriseUserPackageDetailsVO();
        
        UserInfo userInfo = userInfoService.queryByUidFromCache(query.getUid());
        if (Objects.isNull(userInfo)) {
            log.warn("query user deposit info failed, not found userInfo,uid = {}", query.getUid());
            return Triple.of(true, null, enterpriseUserPackageDetailsVO);
        }
        
        Franchisee franchisee = franchiseeService.queryByIdFromCache(userInfo.getFranchiseeId());
        
        enterpriseUserPackageDetailsVO.setModelType(Objects.isNull(franchisee) ? null : franchisee.getModelType());
        enterpriseUserPackageDetailsVO.setBatteryRentStatus(userInfo.getBatteryRentStatus());
        enterpriseUserPackageDetailsVO.setBatteryDepositStatus(userInfo.getBatteryDepositStatus());
        enterpriseUserPackageDetailsVO.setFranchiseeId(userInfo.getFranchiseeId());
        enterpriseUserPackageDetailsVO.setStoreId(userInfo.getStoreId());
        enterpriseUserPackageDetailsVO.setIsExistMemberCard(UserBatteryMemberCardInfoVO.NO);
        
        //设置骑手个人信息
        enterpriseUserPackageDetailsVO.setUid(userInfo.getUid());
        enterpriseUserPackageDetailsVO.setName(userInfo.getName());
        enterpriseUserPackageDetailsVO.setPhone(userInfo.getPhone());
        enterpriseUserPackageDetailsVO.setIdNumber(userInfo.getIdNumber());
        
        //查询用户保险信息
        InsuranceUserInfo insuranceUserInfo = insuranceUserInfoService.selectByUidAndTypeFromCache(userInfo.getUid(), FranchiseeInsurance.INSURANCE_TYPE_BATTERY);
        InsuranceUserInfoVo insuranceUserInfoVo = new InsuranceUserInfoVo();
        if (Objects.nonNull(insuranceUserInfo)) {
            BeanUtils.copyProperties(insuranceUserInfo, insuranceUserInfoVo);
        }
        enterpriseUserPackageDetailsVO.setInsuranceUserInfoVo(insuranceUserInfoVo);
        
        //用户电池型号
        enterpriseUserPackageDetailsVO.setUserBatterySimpleType(userBatteryTypeService.selectUserSimpleBatteryType(userInfo.getUid()));
        
        //查询骑手续费方式
        EnterpriseChannelUserVO enterpriseChannelUserVO = enterpriseChannelUserService.selectUserByEnterpriseIdAndUid(query.getEnterpriseId(), query.getUid());
        log.info("query user deposit info, enterprise id = {}, uid = {}", query.getEnterpriseId(), query.getUid());
        if (Objects.isNull(enterpriseChannelUserVO)) {
            log.warn("query user deposit info failed, not found enterprise channel user, uid = {}", query.getUid());
            return Triple.of(true, null, enterpriseUserPackageDetailsVO);
        }
        enterpriseUserPackageDetailsVO.setRenewalStatus(enterpriseChannelUserVO.getRenewalStatus());
        
        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(userInfo.getUid());
        if (Objects.isNull(userBatteryMemberCard) || Objects.isNull(userBatteryMemberCard.getMemberCardId()) || Objects.equals(userBatteryMemberCard.getMemberCardId(),
                NumberConstant.ZERO_L)) {
            log.warn("query user deposit info failed, not found userBatteryMemberCard,uid = {}", userInfo.getUid());
            return Triple.of(true, null, enterpriseUserPackageDetailsVO);
        }
        
        BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(userBatteryMemberCard.getMemberCardId());
        if (Objects.isNull(batteryMemberCard)) {
            log.warn("query user deposit info failed, not found batteryMemberCard,uid = {},mid = {}", userInfo.getUid(), userBatteryMemberCard.getMemberCardId());
            return Triple.of(true, null, enterpriseUserPackageDetailsVO);
        }
        
        //判断当前套餐是否为企业套餐
        if (!BatteryMemberCardBusinessTypeEnum.BUSINESS_TYPE_ENTERPRISE_BATTERY.getCode().equals(batteryMemberCard.getBusinessType())) {
            log.warn("query user deposit info failed, current package is not belong enterprise package,uid = {},mid = {}", userInfo.getUid(),
                    userBatteryMemberCard.getMemberCardId());
            return Triple.of(true, null, enterpriseUserPackageDetailsVO);
        }
        
        enterpriseUserPackageDetailsVO.setIsExistMemberCard(UserBatteryMemberCardInfoVO.YES);
        enterpriseUserPackageDetailsVO.setMemberCardStatus(userBatteryMemberCard.getMemberCardStatus());
        enterpriseUserPackageDetailsVO.setMemberCardExpireTime(userBatteryMemberCard.getMemberCardExpireTime());
        enterpriseUserPackageDetailsVO.setRemainingNumber(userBatteryMemberCard.getRemainingNumber());
        enterpriseUserPackageDetailsVO.setMemberCardId(userBatteryMemberCard.getMemberCardId());
        
        if (Objects.equals(batteryMemberCard.getRentUnit(), BatteryMemberCard.RENT_UNIT_DAY)) {
            enterpriseUserPackageDetailsVO.setValidDays(userBatteryMemberCard.getMemberCardExpireTime() > System.currentTimeMillis() ? (int) Math.ceil(
                    (userBatteryMemberCard.getMemberCardExpireTime() - System.currentTimeMillis()) / 24 / 60 / 60 / 1000.0) : 0);
        } else {
            enterpriseUserPackageDetailsVO.setValidDays(userBatteryMemberCard.getMemberCardExpireTime() > System.currentTimeMillis() ? (int) Math.ceil(
                    (userBatteryMemberCard.getMemberCardExpireTime() - System.currentTimeMillis()) / 60 / 1000.0) : 0);
        }
        
        //查询用户押金状况
        UserBatteryDeposit userBatteryDeposit = userBatteryDepositService.selectByUidFromCache(userInfo.getUid());
        if (Objects.isNull(userBatteryDeposit) || StringUtils.isBlank(userBatteryDeposit.getOrderId())) {
            log.warn("query user deposit info failed, not found userBatteryDeposit,uid = {}", userInfo.getUid());
            return Triple.of(true, null, enterpriseUserPackageDetailsVO);
        }
        
        enterpriseUserPackageDetailsVO.setBatteryDeposit(userBatteryDeposit.getBatteryDeposit());
        enterpriseUserPackageDetailsVO.setDepositType(userBatteryDeposit.getDepositType());
        
        //套餐订单金额
        ElectricityMemberCardOrder electricityMemberCardOrder = electricityMemberCardOrderService.selectByOrderNo(userBatteryMemberCard.getOrderId());
        enterpriseUserPackageDetailsVO.setBatteryMembercardPayAmount(Objects.isNull(electricityMemberCardOrder) ? null : electricityMemberCardOrder.getPayAmount());
        enterpriseUserPackageDetailsVO.setMemberCardPayTime(Objects.isNull(electricityMemberCardOrder) ? null : electricityMemberCardOrder.getCreateTime());
        enterpriseUserPackageDetailsVO.setMemberCardName(batteryMemberCard.getName());
        enterpriseUserPackageDetailsVO.setRentUnit(batteryMemberCard.getRentUnit());
        enterpriseUserPackageDetailsVO.setLimitCount(batteryMemberCard.getLimitCount());
        
        return Triple.of(true, null, enterpriseUserPackageDetailsVO);
    }
    
    @Deprecated
    @Slave
    @Override
    public Triple<Boolean, String, Object> queryCostDetails(EnterprisePackageOrderQuery query) {
        
        log.info("query cost details start, enterprise id = {}, channel user id = {}", query.getEnterpriseId(), query.getUid());
        Long channelUserId = query.getUid();
        
        List<EnterpriseUserCostDetailsVO> enterpriseUserCostDetailsVOList = Lists.newArrayList();
        
        //1. 查询骑手购买套餐信息, 支付成功的记录
        //userBatteryMemberCardPackageService.selectByUid(channelUserId);
        List<EnterprisePackageOrderVO> enterprisePackageOrderVOList = enterpriseBatteryPackageMapper.queryBatteryPackageOrder(query);
        for (EnterprisePackageOrderVO enterprisePackageOrderVO : enterprisePackageOrderVOList) {
            EnterpriseUserCostDetailsVO enterpriseUserCostDetailsVO = new EnterpriseUserCostDetailsVO();
            enterpriseUserCostDetailsVO.setCostType(UserCostTypeEnum.COST_TYPE_PURCHASE_PACKAGE.getCode());
            enterpriseUserCostDetailsVO.setOrderNo(enterprisePackageOrderVO.getOrderNo());
            enterpriseUserCostDetailsVO.setPackageId(enterprisePackageOrderVO.getPackageId());
            enterpriseUserCostDetailsVO.setPackageName(enterprisePackageOrderVO.getPackageName());
            enterpriseUserCostDetailsVO.setPayAmount(enterprisePackageOrderVO.getPayAmount());
            //enterpriseUserCostDetailsVO.setDepositAmount(enterprisePackageOrderVO.getBatteryDeposit());
            enterpriseUserCostDetailsVO.setOperationTime(enterprisePackageOrderVO.getCreateTime());
            
            //查询购买订单关联保险金额
            InsuranceOrder insuranceOrder = insuranceOrderService.selectBySourceOrderNoAndType(enterprisePackageOrderVO.getOrderNo(), FranchiseeInsurance.INSURANCE_TYPE_BATTERY);
            if (Objects.nonNull(insuranceOrder)) {
                enterpriseUserCostDetailsVO.setInsuranceAmount(insuranceOrder.getPayAmount());
            }
            
            //查询购买订单关联押金金额
            EleDepositOrderVO eleDepositOrderVO = eleDepositOrderService.queryByUidAndSourceOrderNo(channelUserId, enterprisePackageOrderVO.getOrderNo());
            if (Objects.nonNull(eleDepositOrderVO)) {
                enterpriseUserCostDetailsVO.setDepositAmount(eleDepositOrderVO.getPayAmount());
            }
            
            enterpriseUserCostDetailsVOList.add(enterpriseUserCostDetailsVO);
        }
        
        //2. 查询骑手租退电池信息, 状态分别为租电池成功取走, 还电池成功
        EnterpriseRentBatteryOrderQuery enterpriseRentBatteryOrderQuery = new EnterpriseRentBatteryOrderQuery();
        enterpriseRentBatteryOrderQuery.setEnterpriseId(query.getEnterpriseId());
        enterpriseRentBatteryOrderQuery.setUid(query.getUid());
        enterpriseRentBatteryOrderQuery.setBeginTime(query.getBeginTime());
        enterpriseRentBatteryOrderQuery.setEndTime(query.getEndTime());
        
        List<EnterpriseRentBatteryOrderVO> enterpriseRentBatteryOrderVOList = enterpriseBatteryPackageMapper.queryRentBatteryOrder(enterpriseRentBatteryOrderQuery);
        
        for (EnterpriseRentBatteryOrderVO enterpriseRentBatteryOrderVO : enterpriseRentBatteryOrderVOList) {
            EnterpriseUserCostDetailsVO enterpriseUserCostDetailsVO = new EnterpriseUserCostDetailsVO();
            enterpriseUserCostDetailsVO.setOrderNo(enterpriseRentBatteryOrderVO.getOrderNo());
            enterpriseUserCostDetailsVO.setDepositAmount(enterpriseRentBatteryOrderVO.getBatteryDeposit());
            enterpriseUserCostDetailsVO.setOperationTime(enterpriseRentBatteryOrderVO.getCreateTime());
            if (RentBatteryOrder.RENT_BATTERY_TAKE_SUCCESS.equals(enterpriseRentBatteryOrderVO.getStatus())) {
                enterpriseUserCostDetailsVO.setCostType(UserCostTypeEnum.COST_TYPE_RENT_BATTERY.getCode());
                enterpriseUserCostDetailsVOList.add(enterpriseUserCostDetailsVO);
            } else if (RentBatteryOrder.RETURN_BATTERY_CHECK_SUCCESS.equals(enterpriseRentBatteryOrderVO.getStatus())) {
                enterpriseUserCostDetailsVO.setCostType(UserCostTypeEnum.COST_TYPE_RETURN_BATTERY.getCode());
                enterpriseUserCostDetailsVOList.add(enterpriseUserCostDetailsVO);
            }
        }
        
        //3. 查询骑手套餐冻结信息, 审核通过的记录
        List<EnterpriseFreezePackageRecordVO> enterpriseFreezePackageRecordVOList = enterpriseBatteryPackageMapper.queryBatteryFreezeOrder(query);
        
        for (EnterpriseFreezePackageRecordVO enterpriseFreezePackageRecordVO : enterpriseFreezePackageRecordVOList) {
            EnterpriseUserCostDetailsVO enterpriseUserCostDetailsVO = new EnterpriseUserCostDetailsVO();
            enterpriseUserCostDetailsVO.setCostType(UserCostTypeEnum.COST_TYPE_FREEZE_PACKAGE.getCode());
            enterpriseUserCostDetailsVO.setPackageId(enterpriseFreezePackageRecordVO.getPackageId());
            enterpriseUserCostDetailsVO.setPackageName(enterpriseFreezePackageRecordVO.getPackageName());
            enterpriseUserCostDetailsVO.setOperationTime(enterpriseFreezePackageRecordVO.getFreezePackageTime());
            enterpriseUserCostDetailsVOList.add(enterpriseUserCostDetailsVO);
        }
        
        //4. 查询骑手退押信息, 押金退款成功的记录
        List<EnterpriseRefundDepositOrderVO> enterpriseRefundDepositOrderVOList = enterpriseBatteryPackageMapper.queryBatteryDepositOrder(query);
        for (EnterpriseRefundDepositOrderVO enterpriseRefundDepositOrderVO : enterpriseRefundDepositOrderVOList) {
            EnterpriseUserCostDetailsVO enterpriseUserCostDetailsVO = new EnterpriseUserCostDetailsVO();
            enterpriseUserCostDetailsVO.setCostType(UserCostTypeEnum.COST_TYPE_REFUND_DEPOSIT.getCode());
            enterpriseUserCostDetailsVO.setOrderNo(enterpriseRefundDepositOrderVO.getOrderNo());
            enterpriseUserCostDetailsVO.setPackageId(enterpriseRefundDepositOrderVO.getPackageId());
            //根据套餐ID, 查询套餐信息
            BatteryMemberCard userBindBatteryMemberCard = batteryMemberCardService.queryByIdFromCache(enterpriseRefundDepositOrderVO.getPackageId());
            if (Objects.nonNull(userBindBatteryMemberCard)) {
                enterpriseUserCostDetailsVO.setPackageName(userBindBatteryMemberCard.getName());
            }
            enterpriseUserCostDetailsVO.setPayAmount(enterpriseRefundDepositOrderVO.getPayAmount());
            enterpriseUserCostDetailsVO.setDepositAmount(enterpriseRefundDepositOrderVO.getRefundAmount());
            enterpriseUserCostDetailsVO.setOperationTime(enterpriseRefundDepositOrderVO.getCreateTime());
            enterpriseUserCostDetailsVOList.add(enterpriseUserCostDetailsVO);
        }
        
        //5. 查询骑手套餐启用记录，冻结后被启用
        List<EnterpriseFreezePackageRecordVO> enterpriseEnableFreezePackageRecordVOList = enterpriseBatteryPackageMapper.queryEnableFreezeOrder(query);
        for (EnterpriseFreezePackageRecordVO enterpriseFreezePackageRecordVO : enterpriseEnableFreezePackageRecordVOList) {
            
            EnterpriseUserCostDetailsVO enterpriseUserCostDetailsVO = new EnterpriseUserCostDetailsVO();
            enterpriseUserCostDetailsVO.setCostType(UserCostTypeEnum.COST_TYPE_ENABLE_PACKAGE.getCode());
            enterpriseUserCostDetailsVO.setPackageId(enterpriseFreezePackageRecordVO.getPackageId());
            enterpriseUserCostDetailsVO.setPackageName(enterpriseFreezePackageRecordVO.getPackageName());
            enterpriseUserCostDetailsVO.setOperationTime(enterpriseFreezePackageRecordVO.getEnablePackageTime());
            enterpriseUserCostDetailsVOList.add(enterpriseUserCostDetailsVO);
        }
        
        //按照时间降序排序
        enterpriseUserCostDetailsVOList.sort(Comparator.comparing(EnterpriseUserCostDetailsVO::getOperationTime).reversed());
        log.info("query cost details end, enterprise id = {}, channel user id = {}", query.getEnterpriseId(), query.getUid());
        
        return Triple.of(true, null, enterpriseUserCostDetailsVOList);
    }
    
    @Slave
    @Override
    public Triple<Boolean, String, Object> queryPurchasedPackageOrders(EnterprisePurchaseOrderQuery query) {
        List<EnterprisePackageOrderVO> enterprisePackageOrderVOList = Lists.newArrayList();
        if (EnterprisePaymentStatusEnum.PAYMENT_TYPE_EXPIRED.getCode().equals(query.getPaymentStatus())) {
            enterprisePackageOrderVOList = enterpriseBatteryPackageMapper.queryExpiredPackageOrder(query);
            assignmentForExpiredPackage(enterprisePackageOrderVOList);
            
        } else if (EnterprisePaymentStatusEnum.PAYMENT_TYPE_SUCCESS.getCode().equals(query.getPaymentStatus())) {
            enterprisePackageOrderVOList = enterpriseBatteryPackageMapper.queryPaidPackageOrder(query);
            assignmentForPurchasedPackage(enterprisePackageOrderVOList);
            
        } else if (EnterprisePaymentStatusEnum.PAYMENT_TYPE_NO_PAY.getCode().equals(query.getPaymentStatus())) {
            enterprisePackageOrderVOList = enterpriseBatteryPackageMapper.queryUnpaidPackageOrder(query);
        }
        
        if (ObjectUtils.isNotEmpty(enterprisePackageOrderVOList)) {
            List<Long> uidList = enterprisePackageOrderVOList.stream().map(EnterprisePackageOrderVO::getUid).collect(Collectors.toList());
            List<Integer> typeList = new ArrayList<>();
            typeList.add(EnterpriseChannelUserExit.TYPE_INIT);
            typeList.add(EnterpriseChannelUserExit.TYPE_FAIL);
            EnterpriseChannelUserExitQueryModel queryModel = EnterpriseChannelUserExitQueryModel.builder().uidList(uidList).typeList(typeList).build();
            List<EnterpriseChannelUserExit> channelUserList = channelUserExitMapper.list(queryModel);
            Map<Long, EnterpriseChannelUserExit> exitMap = new HashMap<>();
            if (ObjectUtils.isNotEmpty(channelUserList)) {
                exitMap = channelUserList.stream()
                        .collect(Collectors.groupingBy(EnterpriseChannelUserExit::getUid, Collectors.collectingAndThen(Collectors.toList(), e -> e.get(0))));
            }
            
            for (EnterprisePackageOrderVO item : enterprisePackageOrderVOList) {
                if (ObjectUtils.isNotEmpty(exitMap.get(item.getUid()))) {
                    item.setRenewalStatusExit(EnterprisePackageOrderVO.RENEWAL_STATUS_EXIT_YES);
                } else {
                    item.setRenewalStatusExit(EnterprisePackageOrderVO.RENEWAL_STATUS_EXIT_NO);
                }
            }
        }
        return Triple.of(true, null, enterprisePackageOrderVOList);
    }
    
    @Slave
    @Override
    public Triple<Boolean, String, Object> selectFranchiseeByEnterpriseId(Long enterpriseId) {
        
        EnterpriseInfo enterpriseInfo = enterpriseInfoService.queryByIdFromCache(enterpriseId);
        if (Objects.isNull(enterpriseInfo)) {
            log.error("query enterprise info failed by query franchisee, enterpriseId = {}", enterpriseId);
            return Triple.of(false, "300065", "企业信息不存在");
        }
        Franchisee franchisee = franchiseeService.queryByIdFromCache(enterpriseInfo.getFranchiseeId());
        if (Objects.isNull(franchisee) || !Objects.equals(franchisee.getTenantId(), TenantContextHolder.getTenantId())) {
            return Triple.of(false, "300066", "加盟商不存在");
        }
        
        return Triple.of(true, null, franchisee);
        
    }
    
    private void assignmentForExpiredPackage(List<EnterprisePackageOrderVO> enterprisePackageOrderVOList) {
        for (EnterprisePackageOrderVO enterprisePackageOrderVO : enterprisePackageOrderVOList) {
            //查询在用套餐信息
            BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(enterprisePackageOrderVO.getPackageId());
            //如果当前用户绑定的套餐被解绑或者当前套餐不是企业套餐，则获取最近一笔的企业套餐购买订单
            //重新获取套餐信息，并设置套餐过期时间为空
            if (NumberConstant.ZERO_L.equals(enterprisePackageOrderVO.getPackageId()) || !BatteryMemberCardBusinessTypeEnum.BUSINESS_TYPE_ENTERPRISE_BATTERY.getCode()
                    .equals(batteryMemberCard.getBusinessType())) {
                ElectricityMemberCardOrder electricityMemberCardOrder = enterpriseBatteryPackageMapper.selectLatestEnterpriseOrderByUid(enterprisePackageOrderVO.getUid());
                if (Objects.isNull(electricityMemberCardOrder)) {
                    continue;
                }
                
                BatteryMemberCard batteryPackage = batteryMemberCardService.queryByIdFromCache(electricityMemberCardOrder.getMemberCardId());
                enterprisePackageOrderVO.setOrderNo(electricityMemberCardOrder.getOrderId());
                enterprisePackageOrderVO.setPackageId(batteryPackage.getId());
                enterprisePackageOrderVO.setPackageName(batteryPackage.getName());
                enterprisePackageOrderVO.setPackageExpiredTime(null);
                enterprisePackageOrderVO.setPayAmount(batteryPackage.getRentPrice());
                enterprisePackageOrderVO.setBatteryDeposit(batteryPackage.getDeposit());
                
                //获取关联押金订单信息
                EleDepositOrderVO eleDepositOrderVO = eleDepositOrderService.queryByUidAndSourceOrderNo(enterprisePackageOrderVO.getUid(), electricityMemberCardOrder.getOrderId());
                if (Objects.nonNull(eleDepositOrderVO)) {
                    enterprisePackageOrderVO.setDepositType(UserBatteryDeposit.DEPOSIT_TYPE_DEFAULT);
                } else {
                    //免押，页面显示为0
                    enterprisePackageOrderVO.setDepositType(UserBatteryDeposit.DEPOSIT_TYPE_FREE);
                }
                
                //设置企业代付时间
                enterprisePackageOrderVO.setPaymentTime(electricityMemberCardOrder.getCreateTime());
                
            } else {
                enterprisePackageOrderVO.setPackageName(batteryMemberCard.getName());
                enterprisePackageOrderVO.setPayAmount(batteryMemberCard.getRentPrice());
                
                //设置押金
                UserBatteryDeposit userBatteryDeposit = userBatteryDepositService.selectByUidFromCache(enterprisePackageOrderVO.getUid());
                if (Objects.nonNull(userBatteryDeposit)) {
                    enterprisePackageOrderVO.setBatteryDeposit(userBatteryDeposit.getBatteryDeposit());
                    enterprisePackageOrderVO.setDepositType(userBatteryDeposit.getDepositType());
                }
                
                //设置套餐购买后企业代付时间
                ElectricityMemberCardOrder electricityMemberCardOrder = eleMemberCardOrderService.selectByOrderNo(enterprisePackageOrderVO.getOrderNo());
                if (Objects.nonNull(electricityMemberCardOrder)) {
                    enterprisePackageOrderVO.setPaymentTime(electricityMemberCardOrder.getCreateTime());
                }
                
            }
            
            //查看此时用户有无绑定电池信息，若存在续租的线上套餐，则存在电池信息
            //设置用户电池伏数
            enterprisePackageOrderVO.setUserBatterySimpleType(userBatteryTypeService.selectUserSimpleBatteryType(enterprisePackageOrderVO.getUid()));
            
            //设置电池编码
            ElectricityBattery electricityBattery = electricityBatteryService.queryByUid(enterprisePackageOrderVO.getUid());
            if (Objects.nonNull(electricityBattery)) {
                enterprisePackageOrderVO.setBatterySn(electricityBattery.getSn());
            }
            
            //设置可回收云豆信息
            EnterpriseChannelUser enterpriseChannelUser = enterpriseChannelUserService.selectByUid(enterprisePackageOrderVO.getUid());
            if (Objects.nonNull(enterpriseChannelUser)) {
                enterprisePackageOrderVO.setCloudBeanStatus(enterpriseChannelUser.getCloudBeanStatus());
            }
            
            //查询当前用户是否已租电池
            UserInfo userInfo = userInfoService.queryByUidFromCache(enterprisePackageOrderVO.getUid());
            if(Objects.nonNull(userInfo) && UserInfo.BATTERY_RENT_STATUS_YES.equals(userInfo.getBatteryRentStatus())){
                enterprisePackageOrderVO.setBatteryRentStatus(UserInfo.BATTERY_RENT_STATUS_YES);
            }else{
                enterprisePackageOrderVO.setBatteryRentStatus(UserInfo.BATTERY_RENT_STATUS_NO);
            }
            
        }
    }
    
    private void assignmentForPurchasedPackage(List<EnterprisePackageOrderVO> enterprisePackageOrderVOList) {
        
        for (EnterprisePackageOrderVO enterprisePackageOrderVO : enterprisePackageOrderVOList) {
            
            //设置套餐信息
            BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(enterprisePackageOrderVO.getPackageId());
            if (Objects.nonNull(batteryMemberCard)) {
                enterprisePackageOrderVO.setPackageName(batteryMemberCard.getName());
                enterprisePackageOrderVO.setPayAmount(batteryMemberCard.getRentPrice());
            }
            
            //设置押金
            UserBatteryDeposit userBatteryDeposit = userBatteryDepositService.selectByUidFromCache(enterprisePackageOrderVO.getUid());
            if (Objects.nonNull(userBatteryDeposit)) {
                enterprisePackageOrderVO.setBatteryDeposit(userBatteryDeposit.getBatteryDeposit());
                enterprisePackageOrderVO.setDepositType(userBatteryDeposit.getDepositType());
            }
            
            //设置用户电池伏数
            enterprisePackageOrderVO.setUserBatterySimpleType(userBatteryTypeService.selectUserSimpleBatteryType(enterprisePackageOrderVO.getUid()));
            
            //设置电池编码
            ElectricityBattery electricityBattery = electricityBatteryService.queryByUid(enterprisePackageOrderVO.getUid());
            if (Objects.nonNull(electricityBattery)) {
                enterprisePackageOrderVO.setBatterySn(electricityBattery.getSn());
            }
            
            //设置套餐购买后企业代付时间
            ElectricityMemberCardOrder electricityMemberCardOrder = eleMemberCardOrderService.selectByOrderNo(enterprisePackageOrderVO.getOrderNo());
            if (Objects.nonNull(electricityMemberCardOrder)) {
                enterprisePackageOrderVO.setPaymentTime(electricityMemberCardOrder.getCreateTime());
            }
            
            //设置可回收云豆信息
            EnterpriseChannelUser enterpriseChannelUser = enterpriseChannelUserService.selectByUid(enterprisePackageOrderVO.getUid());
            if (Objects.nonNull(enterpriseChannelUser)) {
                enterprisePackageOrderVO.setCloudBeanStatus(enterpriseChannelUser.getCloudBeanStatus());
            }
            
            //查询当前用户是否已租电池
            UserInfo userInfo = userInfoService.queryByUidFromCache(enterprisePackageOrderVO.getUid());
            if(Objects.nonNull(userInfo) && UserInfo.BATTERY_RENT_STATUS_YES.equals(userInfo.getBatteryRentStatus())){
                enterprisePackageOrderVO.setBatteryRentStatus(UserInfo.BATTERY_RENT_STATUS_YES);
            }else{
                enterprisePackageOrderVO.setBatteryRentStatus(UserInfo.BATTERY_RENT_STATUS_NO);
            }
        }
    }
    
    private Triple<Boolean, String, Object> handlerNonFirstBuyBatteryMemberCard(UserBatteryMemberCard userBatteryMemberCard, BatteryMemberCard batteryMemberCard, UserInfo userInfo,
            Franchisee franchisee) {
        if (Objects.isNull(userBatteryMemberCard)) {
            log.warn("purchase package by enterprise user, not found userBatteryMemberCard,uid={}", userInfo.getUid());
            return Triple.of(false, "100247", "用户信息不存在");
        }
        
        if (Objects.equals(userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_DISABLE)) {
            log.warn("purchase package by enterprise user, userBatteryMemberCard is disable,uid={}", userInfo.getUid());
            return Triple.of(false, "100241", "当前套餐暂停中，请先启用套餐");
        }
        
        if (!(Objects.equals(BatteryMemberCard.RENT_TYPE_OLD, batteryMemberCard.getRentType()) || Objects.equals(BatteryMemberCard.RENT_TYPE_UNLIMIT,
                batteryMemberCard.getRentType()))) {
            log.warn("purchase package by enterprise user, new batteryMemberCard not available,uid={},mid={}", userInfo.getUid(), batteryMemberCard.getId());
            return Triple.of(false, "100275", "换电套餐不可用");
        }
        
        BatteryMemberCard userBindBatteryMemberCard = batteryMemberCardService.queryByIdFromCache(userBatteryMemberCard.getMemberCardId());
        if (Objects.isNull(userBindBatteryMemberCard)) {
            log.warn("purchase package by enterprise user, userBindBatteryMemberCard is null,uid={}", userBatteryMemberCard.getUid());
            return Triple.of(false, "ELECTRICITY.0087", "套餐不存在");
        }
        
        if (!Objects.equals(userBindBatteryMemberCard.getLimitCount(), batteryMemberCard.getLimitCount())) {
            log.warn("purchase package by enterprise user, batteryMemberCard limitCount inconformity,uid={},mid={}", userBatteryMemberCard.getUid(), batteryMemberCard.getId());
            return Triple.of(false, "100276", "换电套餐类型不一致");
        }
        
        boolean flag = batteryMemberCard.getDeposit().compareTo(userBindBatteryMemberCard.getDeposit()) == 0;
        if (!flag) {
            log.warn("purchase package by enterprise user, batteryMemberCard deposit inconformity,uid={},mid={}", userBatteryMemberCard.getUid(), batteryMemberCard.getId());
            return Triple.of(false, "100277", "换电套餐押金不一致");
        }
        
        if (Objects.equals(Franchisee.OLD_MODEL_TYPE, franchisee.getModelType())) {
            return Triple.of(true, null, null);
        }
        
        List<String> oldMembercardBatteryTypeList = memberCardBatteryTypeService.selectBatteryTypeByMid(userBindBatteryMemberCard.getId());
        if (CollectionUtils.isEmpty(oldMembercardBatteryTypeList)) {
            log.warn("purchase package by enterprise user, old batteryMemberCard batteryType illegal,uid={},mid={}", userBatteryMemberCard.getUid(),
                    userBindBatteryMemberCard.getId());
            return Triple.of(false, "100279", "换电套餐电池型号不存在");
        }
        
        List<String> newMembercardBatteryTypeList = memberCardBatteryTypeService.selectBatteryTypeByMid(batteryMemberCard.getId());
        if (CollectionUtils.isEmpty(newMembercardBatteryTypeList)) {
            log.warn("purchase package by enterprise user, new batteryMemberCard batteryType illegal,uid={},mid={}", userBatteryMemberCard.getUid(), batteryMemberCard.getId());
            return Triple.of(false, "100279", "换电套餐电池型号不存在");
        }
        
        if (!CollectionUtils.containsAll(newMembercardBatteryTypeList, oldMembercardBatteryTypeList)) {
            log.warn("purchase package by enterprise user, batteryType illegal,uid={},mid={}", userBatteryMemberCard.getUid(), batteryMemberCard.getId());
            return Triple.of(false, "100278", "换电套餐电池型号不一致");
        }
        
        return Triple.of(true, null, null);
    }
    
    private List<String> acquireUserBatteryType(List<String> userBatteryTypeList, List<String> membercardBatteryTypeList) {
        if (CollectionUtils.isEmpty(membercardBatteryTypeList)) {
            return userBatteryTypeList;
        }
        
        if (CollectionUtils.isEmpty(userBatteryTypeList)) {
            return Collections.emptyList();
        }
        
        Set<String> result = new HashSet<>();
        result.addAll(userBatteryTypeList);
        result.addAll(userBatteryTypeList);
        
        return new ArrayList<>(result);
    }
    
    private Triple<Boolean, String, Object> verifyBatteryMemberCardQuery(EnterpriseMemberCardQuery query, Franchisee franchisee) {
        
        if (Objects.equals(franchisee.getModelType(), Franchisee.OLD_MODEL_TYPE)) {
            return Triple.of(true, null, null);
        }
        
        List<String> list = query.getBatteryModels().stream().map(item -> item.substring(item.indexOf("_") + 1).substring(0, item.substring(item.indexOf("_") + 1).indexOf("_")))
                .distinct().collect(Collectors.toList());
        if (CollectionUtils.isEmpty(list) || list.size() != 1) {
            return Triple.of(false, "100273", "套餐电池型号电压不一致");
        }
        
        return Triple.of(true, null, null);
    }
    
    private List<MemberCardBatteryType> buildMemberCardBatteryTypeList(List<String> batteryModels, Long mid) {
        
        List<MemberCardBatteryType> memberCardBatteryTypeList = Lists.newArrayList();
        
        for (String batteryModel : batteryModels) {
            MemberCardBatteryType memberCardBatteryType = new MemberCardBatteryType();
            memberCardBatteryType.setBatteryType(batteryModel);
            memberCardBatteryType.setBatteryV(
                    batteryModel.substring(batteryModel.indexOf("_") + 1).substring(0, batteryModel.substring(batteryModel.indexOf("_") + 1).indexOf("_")));
            memberCardBatteryType.setMid(mid);
            memberCardBatteryType.setTenantId(TenantContextHolder.getTenantId());
            memberCardBatteryType.setCreateTime(System.currentTimeMillis());
            memberCardBatteryType.setUpdateTime(System.currentTimeMillis());
            
            memberCardBatteryTypeList.add(memberCardBatteryType);
        }
        
        return memberCardBatteryTypeList;
    }
    
}
