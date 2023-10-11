package com.xiliulou.electricity.service.impl.enterprise;

import cn.hutool.core.util.ObjectUtil;
import com.google.api.client.util.Lists;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.entity.BatteryMemberCard;
import com.xiliulou.electricity.entity.CommonPayOrder;
import com.xiliulou.electricity.entity.EleDepositOrder;
import com.xiliulou.electricity.entity.EleDisableMemberCardRecord;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.ElectricityMemberCardOrder;
import com.xiliulou.electricity.entity.ElectricityPayParams;
import com.xiliulou.electricity.entity.ElectricityTradeOrder;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.entity.FranchiseeInsurance;
import com.xiliulou.electricity.entity.InsuranceOrder;
import com.xiliulou.electricity.entity.InsuranceUserInfo;
import com.xiliulou.electricity.entity.MemberCardBatteryType;
import com.xiliulou.electricity.entity.RentBatteryOrder;
import com.xiliulou.electricity.entity.UnionPayOrder;
import com.xiliulou.electricity.entity.UnionTradeOrder;
import com.xiliulou.electricity.entity.UserBatteryDeposit;
import com.xiliulou.electricity.entity.UserBatteryMemberCard;
import com.xiliulou.electricity.entity.UserBatteryMemberCardPackage;
import com.xiliulou.electricity.entity.UserCoupon;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.entity.UserOauthBind;
import com.xiliulou.electricity.entity.enterprise.EnterpriseInfo;
import com.xiliulou.electricity.entity.enterprise.UserBehaviorRecord;
import com.xiliulou.electricity.enums.BatteryMemberCardBusinessTypeEnum;
import com.xiliulou.electricity.enums.BusinessType;
import com.xiliulou.electricity.enums.enterprise.EnterprisePaymentStatusEnum;
import com.xiliulou.electricity.enums.enterprise.PackageOrderTypeEnum;
import com.xiliulou.electricity.enums.enterprise.UserCostTypeEnum;
import com.xiliulou.electricity.exception.BizException;
import com.xiliulou.electricity.mapper.BatteryMemberCardMapper;
import com.xiliulou.electricity.mapper.enterprise.EnterpriseBatteryPackageMapper;
import com.xiliulou.electricity.query.BatteryMemberCardQuery;
import com.xiliulou.electricity.query.IntegratedPaymentAdd;
import com.xiliulou.electricity.query.enterprise.EnterpriseChannelUserQuery;
import com.xiliulou.electricity.query.enterprise.EnterpriseMemberCardQuery;
import com.xiliulou.electricity.query.enterprise.EnterprisePackageOrderQuery;
import com.xiliulou.electricity.query.enterprise.EnterprisePurchaseOrderQuery;
import com.xiliulou.electricity.query.enterprise.EnterpriseRentBatteryOrderQuery;
import com.xiliulou.electricity.service.BatteryMemberCardService;
import com.xiliulou.electricity.service.BatteryModelService;
import com.xiliulou.electricity.service.CouponService;
import com.xiliulou.electricity.service.EleDepositOrderService;
import com.xiliulou.electricity.service.EleDisableMemberCardRecordService;
import com.xiliulou.electricity.service.ElectricityMemberCardOrderService;
import com.xiliulou.electricity.service.ElectricityPayParamsService;
import com.xiliulou.electricity.service.ElectricityTradeOrderService;
import com.xiliulou.electricity.service.FranchiseeInsuranceService;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.InsuranceOrderService;
import com.xiliulou.electricity.service.InsuranceUserInfoService;
import com.xiliulou.electricity.service.MemberCardBatteryTypeService;
import com.xiliulou.electricity.service.UnionTradeOrderService;
import com.xiliulou.electricity.service.UserBatteryDepositService;
import com.xiliulou.electricity.service.UserBatteryMemberCardPackageService;
import com.xiliulou.electricity.service.UserBatteryMemberCardService;
import com.xiliulou.electricity.service.UserBatteryTypeService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.service.UserOauthBindService;
import com.xiliulou.electricity.service.car.CarRentalPackageService;
import com.xiliulou.electricity.service.enterprise.AnotherPayMembercardRecordService;
import com.xiliulou.electricity.service.enterprise.EnterpriseBatteryPackageService;
import com.xiliulou.electricity.service.enterprise.EnterpriseChannelUserService;
import com.xiliulou.electricity.service.enterprise.EnterpriseInfoService;
import com.xiliulou.electricity.service.enterprise.EnterprisePackageService;
import com.xiliulou.electricity.service.enterprise.UserBehaviorRecordService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.OrderIdUtil;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.BatteryMemberCardAndTypeVO;
import com.xiliulou.electricity.vo.BatteryMemberCardVO;
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
import com.xiliulou.pay.weixinv3.dto.WechatJsapiOrderResultDTO;
import com.xiliulou.pay.weixinv3.exception.WechatPayException;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
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
    
    @Autowired
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
    EleDepositOrderService eleDepositOrderService;
    
    @Resource
    UnionTradeOrderService unionTradeOrderService;
    
    @Resource
    FranchiseeInsuranceService franchiseeInsuranceService;
    
    @Resource
    InsuranceOrderService insuranceOrderService;
    
    @Resource
    UserBehaviorRecordService userBehaviorRecordService;
    
    @Resource
    private ElectricityMemberCardOrderService eleMemberCardOrderService;
    
    @Resource
    private InsuranceUserInfoService insuranceUserInfoService;
    
    @Resource
    private AnotherPayMembercardRecordService anotherPayMembercardRecordService;
    
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
        
        //获取企业对应的加盟商
        EnterpriseInfo enterpriseInfo = enterpriseInfoService.queryByIdFromCache(query.getEnterpriseId());
        if (Objects.isNull(enterpriseInfo)) {
            log.info("not found enterprise record, enterprise id = {}", query.getEnterpriseId());
            return Triple.of(false, "", "当前企业不存在");
        }
        
        BatteryMemberCardQuery batteryMemberCardQuery = BatteryMemberCardQuery.builder().tenantId(TenantContextHolder.getTenantId()).franchiseeId(enterpriseInfo.getFranchiseeId())
                .status(BatteryMemberCard.STATUS_UP).delFlag(BatteryMemberCard.DEL_NORMAL).build();
        
        //待添加套餐的用户
        UserInfo userInfo = userInfoService.queryByUidFromCache(query.getUid());
        if (Objects.isNull(userInfo)) {
            log.error("Not found userInfo for enterprise channel, uid = {}", query.getUid());
            return Triple.of(true, "", Collections.emptyList());
        }
        
        //未缴纳押金
        if (!Objects.equals(UserInfo.BATTERY_DEPOSIT_STATUS_YES, userInfo.getBatteryDepositStatus())) {
            List<BatteryMemberCardVO> list = this.batteryMemberCardMapper.selectMembercardBatteryV(batteryMemberCardQuery);
            if (CollectionUtils.isEmpty(list)) {
                return Triple.of(true, "", Collections.emptyList());
            }
            
            List<String> batteryVs = list.stream().map(BatteryMemberCardVO::getBatteryV).distinct().collect(Collectors.toList());
            return Triple.of(true, "", batteryVs);
        }
        
        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(query.getUid());
        if (Objects.isNull(userBatteryMemberCard) || Objects.equals(NumberConstant.ZERO, userBatteryMemberCard.getCardPayCount())) {
            List<BatteryMemberCardVO> list = batteryMemberCardMapper.selectMembercardBatteryV(batteryMemberCardQuery);
            if (CollectionUtils.isEmpty(list)) {
                return Triple.of(true, "", Collections.emptyList());
            }
            
            List<String> batteryVs = list.stream().map(BatteryMemberCardVO::getBatteryV).distinct().collect(Collectors.toList());
            return Triple.of(true, "", batteryVs);
        }
        
        String batteryType = userBatteryTypeService.selectUserSimpleBatteryType(query.getUid());
        if (StringUtils.isBlank(batteryType)) {
            return Triple.of(true, "", Collections.emptyList());
        }
        
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
            log.error("Not found enterprise for purchase enterprise package ,enterprise id = {}, uid = {}", query.getEnterpriseId(), enterpriseUserId);
            return Triple.of(true, "", Collections.emptyList());
        }
        
        Franchisee franchisee = franchiseeService.queryByIdFromCache(enterpriseInfo.getFranchiseeId());
        if (Objects.isNull(franchisee)) {
            log.error("Not found franchisee for purchase enterprise package ,uid = {}, franchiseeId = {}", enterpriseUserId, query.getFranchiseeId());
            return Triple.of(true, "", Collections.emptyList());
        }
        
        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(enterpriseUserId);
        
        if (Objects.isNull(userBatteryMemberCard) || Objects.isNull(userBatteryMemberCard.getCardPayCount()) || userBatteryMemberCard.getCardPayCount() <= 0) {
            //新租
            query.setRentTypes(Arrays.asList(BatteryMemberCard.RENT_TYPE_NEW, BatteryMemberCard.RENT_TYPE_UNLIMIT));
        } else if (Objects.isNull(userBatteryMemberCard.getMemberCardId()) || Objects.equals(userBatteryMemberCard.getMemberCardId(), NumberConstant.ZERO_L)) {
            //非新租 购买押金套餐
            query.setRentTypes(Arrays.asList(BatteryMemberCard.RENT_TYPE_OLD, BatteryMemberCard.RENT_TYPE_UNLIMIT));
            
            UserBatteryDeposit userBatteryDeposit = userBatteryDepositService.selectByUidFromCache(userBatteryMemberCard.getUid());
            if (Objects.nonNull(userBatteryDeposit)) {
                query.setDeposit(userBatteryDeposit.getBatteryDeposit());
            }
            
        } else {
            //续费
            BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(userBatteryMemberCard.getMemberCardId());
            if (Objects.isNull(batteryMemberCard)) {
                log.error("Not found battery member card for purchase enterprise package, uid = {}, mid = {}", enterpriseUserId, userBatteryMemberCard.getMemberCardId());
                return Triple.of(true, "", Collections.emptyList());
            }
            
            query.setDeposit(batteryMemberCard.getDeposit());
            query.setLimitCount(batteryMemberCard.getLimitCount());
            query.setRentTypes(Arrays.asList(BatteryMemberCard.RENT_TYPE_OLD, BatteryMemberCard.RENT_TYPE_UNLIMIT));
            query.setBatteryV(Objects.equals(franchisee.getModelType(), Franchisee.NEW_MODEL_TYPE) ? userBatteryTypeService.selectUserSimpleBatteryType(enterpriseUserId) : null);
        }
        
        //先获取企业关联套餐信息
        List<Long> packageIds = enterprisePackageService.selectByEnterpriseId(query.getEnterpriseId());
        if(CollectionUtils.isEmpty(packageIds)){
            return Triple.of(true, "", Collections.emptyList());
        }
        query.setPackageIds(packageIds);
        
        List<BatteryMemberCardAndTypeVO> list = this.batteryMemberCardMapper.selectMemberCardsByEnterprise(query);
        if (CollectionUtils.isEmpty(list)) {
            return Triple.of(true, "", Collections.emptyList());
        }
        
        //用户绑定的电池型号串数
        List<String> userBindBatteryType = null;
        if (Objects.equals(franchisee.getModelType(), Franchisee.NEW_MODEL_TYPE)) {
            userBindBatteryType = userBatteryTypeService.selectByUid(enterpriseUserId);
            if (CollectionUtils.isNotEmpty(userBindBatteryType)) {
                userBindBatteryType = userBindBatteryType.stream().map(item -> item.substring(item.lastIndexOf("_") + 1)).collect(Collectors.toList());
            }
        }
        
        List<BatteryMemberCardVO> result = new ArrayList<>();
        for (BatteryMemberCardAndTypeVO item : list) {
            
            if (Objects.equals(franchisee.getModelType(), Franchisee.NEW_MODEL_TYPE)) {
                List<String> number = null;
                if (CollectionUtils.isNotEmpty(item.getBatteryType())) {
                    //套餐电池型号串数 number
                    number = item.getBatteryType().stream().filter(i -> StringUtils.isNotBlank(i.getBatteryType()))
                            .map(e -> e.getBatteryType().substring(e.getBatteryType().lastIndexOf("_") + 1)).collect(Collectors.toList());
                }
                
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
    
    @Slave
    @Override
    public Triple<Boolean, String, Object> queryUserBatteryDeposit(Long uid) {
        UserBatteryDepositVO userBatteryDepositVO = new UserBatteryDepositVO();
        userBatteryDepositVO.setBatteryRentStatus(UserInfo.BATTERY_RENT_STATUS_NO);
        userBatteryDepositVO.setBatteryDepositStatus(UserInfo.BATTERY_DEPOSIT_STATUS_YES);
        userBatteryDepositVO.setBatteryDeposit(BigDecimal.ZERO);
    
        UserInfo userInfo = userInfoService.queryByUidFromCache(SecurityUtils.getUid());
        if (Objects.isNull(userInfo)) {
            log.warn("query deposit warning, not found userInfo,uid = {}", SecurityUtils.getUid());
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
    @Transactional(rollbackFor = Exception.class)
    public Triple<Boolean, String, Object> purchasePackageByEnterpriseUser(EnterprisePackageOrderQuery query, HttpServletRequest request) {
        
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
        
        EnterpriseInfo enterpriseInfo = enterpriseInfoService.queryByIdFromCache(enterpriseId);
        if (Objects.isNull(enterpriseInfo)) {
            log.error("purchase package by enterprise user error, not found enterprise info, enterprise id = {}", enterpriseId);
            return Triple.of(false, "ELECTRICITY.0001", "未找到企业信息");
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
        
        Franchisee franchisee = franchiseeService.queryByIdFromCache(enterpriseInfo.getFranchiseeId());
        if (Objects.isNull(franchisee)) {
            log.warn("purchase package by enterprise user error, uid = {}", uid);
            return Triple.of(false, "ELECTRICITY.0038", "加盟商不存在");
        }
        
        //检查套餐是否属于当前的企业
        List<Long> packageIds = enterprisePackageService.selectByEnterpriseId(query.getEnterpriseId());
        if (Objects.isNull(packageIds) || !packageIds.contains(query.getPackageId())) {
            log.warn("purchase package by enterprise user error, not found packages from packages, uid = {}, package id = {}", user.getUid(), query.getPackageId());
            return Triple.of(false, "ELECTRICITY.0087", "套餐在企业中不存在");
        }
        
        BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(query.getPackageId());
        if (Objects.isNull(batteryMemberCard) || Objects.equals(batteryMemberCard.getStatus(), BatteryMemberCard.STATUS_DOWN)) {
            log.warn("purchase package by enterprise user error, not found batteryMemberCard, uid = {}, package id = {}", user.getUid(), query.getPackageId());
            return Triple.of(false, "ELECTRICITY.0087", "套餐不存在");
        }
        
        Boolean isFirstBuyMemberCard = Boolean.FALSE;
        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(userInfo.getUid());
        if (Objects.isNull(userBatteryMemberCard) || Objects.equals(NumberConstant.ZERO_L, userBatteryMemberCard.getMemberCardId()) || Objects.equals(NumberConstant.ZERO,
                userBatteryMemberCard.getCardPayCount())) {
            isFirstBuyMemberCard = Boolean.TRUE;
        }
        
        Triple<Boolean, String, Object> verifyResult;
        if (Boolean.TRUE.equals(isFirstBuyMemberCard)) {
            verifyResult = handlerFirstBuyBatteryMemberCard(userBatteryMemberCard, batteryMemberCard, userInfo);
        } else {
            verifyResult = handlerNonFirstBuyBatteryMemberCard(userBatteryMemberCard, batteryMemberCard, userInfo, franchisee);
        }
        
        if (Boolean.FALSE.equals(verifyResult.getLeft())) {
            return verifyResult;
        }

       /* Triple<Boolean,Integer, BigDecimal> acquireUserBatteryServiceFeeResult = serviceFeeUserInfoService.acquireUserBatteryServiceFee(userInfo, userBatteryMemberCard, batteryMemberCard, serviceFeeUserInfoService.queryByUidFromCache(userInfo.getUid()));
        if (Boolean.TRUE.equals(acquireUserBatteryServiceFeeResult.getLeft())) {
            log.warn("BATTERY MEMBER ORDER WARN! user exist battery service fee,uid={},mid={}", user.getUid(), query.getMemberId());
            return Triple.of(false,"ELECTRICITY.100000", acquireUserBatteryServiceFeeResult.getRight());
        }

        Triple<Boolean, String, Object> verifyUserBatteryInsuranceResult = verifyUserBatteryInsurance(userInfo, franchisee,batteryMemberCard);
        if (Boolean.FALSE.equals(verifyUserBatteryInsuranceResult.getLeft())) {
            return verifyUserBatteryInsuranceResult;
        }*/
        
        ElectricityPayParams electricityPayParams = electricityPayParamsService.queryFromCache(tenantId);
        if (Objects.isNull(electricityPayParams)) {
            log.error("purchase package by enterprise user error, not found pay params,uid = {}", user.getUid());
            return Triple.of(false, "", "未配置支付参数!");
        }
        
        UserOauthBind userOauthBind = userOauthBindService.queryUserOauthBySysId(user.getUid(), tenantId);
        if (Objects.isNull(userOauthBind) || Objects.isNull(userOauthBind.getThirdId())) {
            log.error("purchase package by enterprise user error, not found userOauthBind,uid={}", user.getUid());
            return Triple.of(false, "", "未找到用户的第三方授权信息!");
        }
        
        BigDecimal payAmount = batteryMemberCard.getRentPrice();
        
        ElectricityMemberCardOrder memberCardOrder = new ElectricityMemberCardOrder();
        memberCardOrder.setOrderId(OrderIdUtil.generateBusinessOrderId(BusinessType.ENTERPRISE_BATTERY_PACKAGE, userInfo.getUid()));
        //设置当前订单类型为企业渠道购买
        memberCardOrder.setOrderType(PackageOrderTypeEnum.PACKAGE_ORDER_TYPE_ENTERPRISE.getCode());
        memberCardOrder.setEnterpriseId(enterpriseId);
        memberCardOrder.setPayType(ElectricityMemberCardOrder.ENTERPRISE_PAYMENT);
        
        memberCardOrder.setStatus(ElectricityMemberCardOrder.STATUS_INIT);
        memberCardOrder.setMemberCardId(batteryMemberCard.getId());
        memberCardOrder.setUid(userInfo.getUid());
        memberCardOrder.setMaxUseCount(batteryMemberCard.getUseCount());
        memberCardOrder.setCardName(batteryMemberCard.getName());
        memberCardOrder.setPayAmount(payAmount);
        memberCardOrder.setUserName(userInfo.getName());
        memberCardOrder.setValidDays(batteryMemberCard.getValidDays());
        memberCardOrder.setTenantId(TenantContextHolder.getTenantId());
        memberCardOrder.setFranchiseeId(franchisee.getId());
        memberCardOrder.setSource(ElectricityMemberCardOrder.SOURCE_NOT_SCAN);
        memberCardOrder.setCreateTime(System.currentTimeMillis());
        memberCardOrder.setUpdateTime(System.currentTimeMillis());
        //memberCardOrder.setSendCouponId(Objects.nonNull(batteryMemberCard.getCouponId()) ? batteryMemberCard.getCouponId().longValue() : null);
        
        enterpriseBatteryPackageMapper.insertMemberCardOrder(memberCardOrder);
    
        //支付0元
        if (memberCardOrder.getPayAmount().compareTo(BigDecimal.valueOf(0.01)) < 0) {
            handlerBatteryMembercardZeroPayment(batteryMemberCard, memberCardOrder, userBatteryMemberCard, userInfo);
            log.info("purchase order by zero payment, uid = {}, package id = {}", userInfo.getUid(), batteryMemberCard.getId());
            return Triple.of(true, null, null);
        }
        
        try {
            CommonPayOrder commonPayOrder = CommonPayOrder.builder().orderId(memberCardOrder.getOrderId()).uid(userInfo.getUid()).payAmount(memberCardOrder.getPayAmount())
                    .orderType(ElectricityTradeOrder.ORDER_TYPE_PURCHASE_ENTERPRISE_PACKAGE).attach(ElectricityTradeOrder.ATTACH_PURCHASE_ENTERPRISE_PACKAGE)
                    .description("企业渠道换电套餐订单购买").tenantId(tenantId).build();
            
            WechatJsapiOrderResultDTO resultDTO = electricityTradeOrderService.commonCreateTradeOrderAndGetPayParams(commonPayOrder, electricityPayParams,
                    userOauthBind.getThirdId(), request);
            log.info("purchase package by enterprise user end, wechat result = {}", JsonUtil.toJson(resultDTO));
            return Triple.of(true, null, resultDTO);
        } catch (WechatPayException e) {
            log.error("purchase package by enterprise user error, wechat v3 order error,uid = {}", user.getUid(), e);
            redisService.delete(CacheConstant.ELE_CACHE_ENTERPRISE_USER_PURCHASE_PACKAGE_LOCK_KEY + user.getUid());
        }
        
        return Triple.of(false, "ELECTRICITY.0099", "企业渠道套餐下单失败");
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Triple<Boolean, String, Object> purchasePackageWithDepositByEnterpriseUser(EnterprisePackageOrderQuery query, HttpServletRequest request) {
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
    
        EnterpriseInfo enterpriseInfo = enterpriseInfoService.queryByIdFromCache(enterpriseId);
        if (Objects.isNull(enterpriseInfo)) {
            log.error("purchase package by enterprise user error, not found enterprise info, enterprise id = {}", enterpriseId);
            return Triple.of(false, "ELECTRICITY.0001", "未找到企业信息");
        }
    
        UserInfo userInfo = userInfoService.queryByUidFromCache(user.getUid());
        if (Objects.isNull(userInfo)) {
            log.warn("BATTERY DEPOSIT WARN! not found user,uid={}", user.getUid());
            return Triple.of(false, "ELECTRICITY.0019", "未找到用户");
        }
    
        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            log.warn("BATTERY DEPOSIT WARN! user is unUsable,uid={}", user.getUid());
            return Triple.of(false, "ELECTRICITY.0024", "用户已被禁用");
        }
    
        if (!Objects.equals(userInfo.getAuthStatus(), UserInfo.AUTH_STATUS_REVIEW_PASSED)) {
            log.warn("BATTERY DEPOSIT WARN! user not auth,uid={}", user.getUid());
            return Triple.of(false, "ELECTRICITY.0041", "未实名认证");
        }
    
        if (Objects.equals(userInfo.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_YES)) {
            log.warn("BATTERY DEPOSIT WARN! user is rent deposit,uid={} ", user.getUid());
            return Triple.of(false, "ELECTRICITY.0049", "已缴纳押金");
        }
    
        ElectricityPayParams electricityPayParams = electricityPayParamsService.queryFromCache(tenantId);
        if (Objects.isNull(electricityPayParams)) {
            log.warn("BATTERY DEPOSIT WARN!not found pay params,uid={}", user.getUid());
            return Triple.of(false, "100307", "未配置支付参数!");
        }
    
        UserOauthBind userOauthBind = userOauthBindService.queryUserOauthBySysId(user.getUid(), tenantId);
        if (Objects.isNull(userOauthBind) || Objects.isNull(userOauthBind.getThirdId())) {
            log.warn("BATTERY DEPOSIT WARN!not found useroauthbind or thirdid is null,uid={}", user.getUid());
            return Triple.of(false, "100308", "未找到用户的第三方授权信息!");
        }
    
        BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(query.getPackageId());
        if (Objects.isNull(batteryMemberCard)) {
            log.warn("BATTERY DEPOSIT WARN!not found batteryMemberCard,uid={},mid={}", userInfo.getUid(), query.getPackageId());
            return Triple.of(false, "ELECTRICITY.00121", "电池套餐不存在");
        }
    
        if(!Objects.equals( BatteryMemberCard.STATUS_UP, batteryMemberCard.getStatus())){
            log.warn("BATTERY DEPOSIT WARN! batteryMemberCard is disable,uid={},mid={}", userInfo.getUid(), query.getPackageId());
            return Triple.of(false, "100275", "电池套餐不可用");
        }
    
        if(Objects.nonNull(userInfo.getFranchiseeId()) && !Objects.equals(userInfo.getFranchiseeId(),NumberConstant.ZERO_L) && !Objects.equals(userInfo.getFranchiseeId(),batteryMemberCard.getFranchiseeId())){
            log.warn("BATTERY DEPOSIT WARN! batteryMemberCard franchiseeId not equals,uid={},mid={}", userInfo.getUid(), query.getPackageId());
            return Triple.of(false, "100349", "用户加盟商与套餐加盟商不一致");
        }
    
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
        
    
        List<String> orderList = new ArrayList<>();
        List<Integer> orderTypeList = new ArrayList<>();
        List<BigDecimal> allPayAmount = new ArrayList<>();
    
        BigDecimal integratedPaAmount = BigDecimal.valueOf(0);
    
    
        ElectricityMemberCardOrder electricityMemberCardOrder = null;
        EleDepositOrder eleDepositOrder = null;
        InsuranceOrder insuranceOrder = null;
    
        //保存押金订单
        if (Boolean.TRUE.equals(generateDepositOrderResult.getLeft()) && Objects.nonNull(generateDepositOrderResult.getRight())) {
            eleDepositOrder = (EleDepositOrder) generateDepositOrderResult.getRight();
            eleDepositOrderService.insert(eleDepositOrder);
        
            orderList.add(eleDepositOrder.getOrderId());
            orderTypeList.add(UnionPayOrder.ORDER_TYPE_DEPOSIT);
            allPayAmount.add(eleDepositOrder.getPayAmount());
            integratedPaAmount = integratedPaAmount.add(eleDepositOrder.getPayAmount());
        }
    
        //保存保险订单
        if (Boolean.TRUE.equals(generateInsuranceOrderResult.getLeft()) && Objects.nonNull(generateInsuranceOrderResult.getRight())) {
            insuranceOrder = (InsuranceOrder) generateInsuranceOrderResult.getRight();
            insuranceOrderService.insert(insuranceOrder);
        
            orderList.add(insuranceOrder.getOrderId());
            orderTypeList.add(UnionPayOrder.ORDER_TYPE_INSURANCE);
            allPayAmount.add(insuranceOrder.getPayAmount());
            integratedPaAmount = integratedPaAmount.add(insuranceOrder.getPayAmount());
        }
    
        //保存套餐订单
        if (Boolean.TRUE.equals(generateMemberCardOrderResult.getLeft()) && Objects.nonNull(generateMemberCardOrderResult.getRight())) {
            electricityMemberCardOrder = (ElectricityMemberCardOrder) generateMemberCardOrderResult.getRight();
            electricityMemberCardOrderService.insert(electricityMemberCardOrder);
    
            orderList.add(electricityMemberCardOrder.getOrderId());
            orderTypeList.add(UnionPayOrder.ORDER_TYPE_ENTERPRISE_PACKAGE);
            allPayAmount.add(electricityMemberCardOrder.getPayAmount());
            integratedPaAmount = integratedPaAmount.add(electricityMemberCardOrder.getPayAmount());
        }
    
        //处理0元支付
        if (integratedPaAmount.compareTo(BigDecimal.valueOf(0.01)) < 0) {
        
            /*Triple<Boolean, String, Object> result = handleTotalAmountZero(userInfo, orderList, orderTypeList);
            if (Boolean.FALSE.equals(result.getLeft())) {
                return result;
            }*/
            
            //更新押金状态
            if(Objects.nonNull(eleDepositOrder)){
                Pair<Boolean, Object> result = unionTradeOrderService.manageDepositOrder(eleDepositOrder.getOrderId(), EleDepositOrder.STATUS_SUCCESS);
                if(Boolean.FALSE.equals(result.getLeft())){
                    //return  Triple.of(false, "100349", result.getRight());
                    throw new BizException("300071", (String) result.getRight());
                }
            }
            
            //更新保险状态
            if(Objects.nonNull(insuranceOrder)){
                Pair<Boolean, Object> result = unionTradeOrderService.manageInsuranceOrder(insuranceOrder.getOrderId(), InsuranceOrder.STATUS_SUCCESS);
                if(Boolean.FALSE.equals(result.getLeft())){
                    throw new BizException("300072", (String) result.getRight());
                }
            }
            
            //更新套餐购买状态
            if(Objects.nonNull(electricityMemberCardOrder)){
                Pair<Boolean, Object> result = unionTradeOrderService.manageEnterpriseMemberCardOrder(electricityMemberCardOrder.getOrderId(), ElectricityMemberCardOrder.STATUS_SUCCESS);
                if(Boolean.FALSE.equals(result.getLeft())){
                    throw new BizException("300073", (String) result.getRight());
                }
    
                //保存骑手购买套餐信息，用于云豆回收业务
                //userBehaviorRecordService.saveUserBehaviorRecord(electricityMemberCardOrder.getUid(), electricityMemberCardOrder.getOrderId(), UserBehaviorRecord.TYPE_PAY_MEMBERCARD, electricityMemberCardOrder.getTenantId());
                anotherPayMembercardRecordService.saveAnotherPayMembercardRecord(electricityMemberCardOrder.getUid(), electricityMemberCardOrder.getOrderId(), electricityMemberCardOrder.getTenantId());
            }
            
            return Triple.of(true, "", null);
        }
    
        //调起支付
        try {
            UnionPayOrder unionPayOrder = UnionPayOrder.builder()
                    .jsonOrderId(JsonUtil.toJson(orderList))
                    .jsonOrderType(JsonUtil.toJson(orderTypeList))
                    .jsonSingleFee(JsonUtil.toJson(allPayAmount))
                    .payAmount(integratedPaAmount)
                    .tenantId(tenantId)
                    .attach(UnionTradeOrder.ATTACH_ENTERPRISE_PACKAGE_DEPOSIT_PAYMENT)
                    .description("企业渠道购买套餐,押金,保险服务")
                    .uid(user.getUid()).build();
            WechatJsapiOrderResultDTO resultDTO =
                    unionTradeOrderService.unionCreateTradeOrderAndGetPayParams(unionPayOrder, electricityPayParams, userOauthBind.getThirdId(), request);
            return Triple.of(true, null, resultDTO);
        } catch (WechatPayException e) {
            log.error("CREATE UNION_INSURANCE_DEPOSIT_ORDER ERROR! wechat v3 order  error! uid={}", user.getUid(), e);
        }
    
        return Triple.of(false, "300073", "企业代付购买套餐支付失败");
    }
    
    private Triple<Boolean, String, Object> generateInsuranceOrder(UserInfo userInfo, Integer insuranceId) {
        
        if (Objects.isNull(insuranceId)) {
            return Triple.of(true, "", null);
        }
        
        //查询保险
        FranchiseeInsurance franchiseeInsurance = franchiseeInsuranceService.queryByIdFromCache(insuranceId);
        
        if (Objects.isNull(franchiseeInsurance) || !Objects.equals(franchiseeInsurance.getInsuranceType() , FranchiseeInsurance.INSURANCE_TYPE_BATTERY)) {
            log.error("CREATE INSURANCE_ORDER ERROR,NOT FOUND MEMBER_CARD BY ID={},uid={}", insuranceId, userInfo.getUid());
            return Triple.of(false, "100305", "未找到保险!");
        }
        if (ObjectUtil.equal(FranchiseeInsurance.STATUS_UN_USABLE, franchiseeInsurance.getStatus())) {
            log.error("CREATE INSURANCE_ORDER ERROR ,MEMBER_CARD IS UN_USABLE ID={},uid={}", insuranceId, userInfo.getUid());
            return Triple.of(false, "100306", "保险已禁用!");
        }
        
        if (Objects.isNull(franchiseeInsurance.getPremium())) {
            log.error("CREATE INSURANCE_ORDER ERROR! payAmount is null ！franchiseeId={},uid={}", insuranceId, userInfo.getUid());
            return Triple.of(false, "100305", "未找到保险");
        }
        
        //生成保险独立订单
        String insuranceOrderId = OrderIdUtil.generateBusinessOrderId(BusinessType.BATTERY_INSURANCE, userInfo.getUid());
        InsuranceOrder insuranceOrder = InsuranceOrder.builder()
                .insuranceId(franchiseeInsurance.getId())
                .insuranceName(franchiseeInsurance.getName())
                .insuranceType(franchiseeInsurance.getInsuranceType())
                .orderId(insuranceOrderId)
                .cid(franchiseeInsurance.getCid())
                .franchiseeId(franchiseeInsurance.getFranchiseeId())
                .isUse(InsuranceOrder.NOT_USE)
                .payAmount(franchiseeInsurance.getPremium())
                .forehead(franchiseeInsurance.getForehead())
                .payType(InsuranceOrder.ONLINE_PAY_TYPE)
                .phone(userInfo.getPhone())
                .status(InsuranceOrder.STATUS_INIT)
               // .storeId(Objects.nonNull(electricityCabinet) ? electricityCabinet.getStoreId() : userInfo.getStoreId())
                .tenantId(userInfo.getTenantId())
                .uid(userInfo.getUid())
                .userName(userInfo.getName())
                .validDays(franchiseeInsurance.getValidDays())
                .createTime(System.currentTimeMillis())
                .updateTime(System.currentTimeMillis()).build();
        
        return Triple.of(true, null, insuranceOrder);
    }
    
    private Triple<Boolean, String, Object> generateDepositOrder(UserInfo userInfo, BatteryMemberCard batteryMemberCard) {
        
        //生成押金独立订单
        String depositOrderId = OrderIdUtil.generateBusinessOrderId(BusinessType.BATTERY_DEPOSIT, userInfo.getUid());
        EleDepositOrder eleDepositOrder = EleDepositOrder.builder()
                .orderId(depositOrderId)
                .uid(userInfo.getUid())
                .phone(userInfo.getPhone())
                .name(userInfo.getName())
                .payAmount(batteryMemberCard.getDeposit())
                .status(EleDepositOrder.STATUS_INIT)
                .createTime(System.currentTimeMillis())
                .updateTime(System.currentTimeMillis())
                .tenantId(userInfo.getTenantId())
                .franchiseeId(batteryMemberCard.getFranchiseeId())
                .payType(EleDepositOrder.ONLINE_PAYMENT)
                .orderType(PackageOrderTypeEnum.PACKAGE_ORDER_TYPE_ENTERPRISE.getCode())
                .mid(batteryMemberCard.getId())
                .modelType(0).build();
        
        return Triple.of(true, null, eleDepositOrder);
    }
    
    private Triple<Boolean, String, Object> generateMemberCardOrder(UserInfo userInfo, BatteryMemberCard batteryMemberCard, EnterprisePackageOrderQuery query, Set<Integer> userCouponIds) {
        
        //查找计算优惠券
        //计算优惠后支付金额
        Triple<Boolean, String, Object> calculatePayAmountResult = electricityMemberCardOrderService.calculatePayAmount(batteryMemberCard.getRentPrice(), userCouponIds);
        if(Boolean.FALSE.equals(calculatePayAmountResult.getLeft())){
            return calculatePayAmountResult;
        }
        BigDecimal payAmount = (BigDecimal) calculatePayAmountResult.getRight();
        
        //支付金额不能为负数
        if (payAmount.compareTo(BigDecimal.valueOf(0.01)) < 0) {
            payAmount = BigDecimal.valueOf(0);
        }
        
        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService
                .selectByUidFromCache(userInfo.getUid());
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
        //设置用户的加盟商为套餐订单的加盟商，因为用户加盟商和套餐所属加盟商一致
        electricityMemberCardOrder.setFranchiseeId(userInfo.getFranchiseeId());
        electricityMemberCardOrder.setPayCount(payCount);
        electricityMemberCardOrder.setSendCouponId(Objects.nonNull(batteryMemberCard.getCouponId()) ? batteryMemberCard.getCouponId().longValue() : null);
        //electricityMemberCardOrder.setRefId(Objects.nonNull(electricityCabinet) ? electricityCabinet.getId().longValue() : null);
        electricityMemberCardOrder.setSource(ElectricityMemberCardOrder.SOURCE_NOT_SCAN);
        //TODO 需要和产品确认门店的归属
        // electricityMemberCardOrder.setStoreId( );
        
        return Triple.of(true, null, electricityMemberCardOrder);
    }
    
    @Slave
    @Override
    public Triple<Boolean, String, Object> queryRiderDetails(EnterpriseMemberCardQuery query) {
        EnterpriseUserPackageDetailsVO enterpriseUserPackageDetailsVO = new EnterpriseUserPackageDetailsVO();
        
        UserInfo userInfo = userInfoService.queryByUidFromCache(query.getUid());
        if (Objects.isNull(userInfo)) {
            log.warn("query rider details failed, not found userInfo,uid = {}", query.getUid());
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
        
        //查询骑手续费方式
        EnterpriseChannelUserVO enterpriseChannelUserVO = enterpriseChannelUserService.selectUserByEnterpriseIdAndUid(query.getEnterpriseId(), query.getUid());
        if (Objects.isNull(enterpriseChannelUserVO)) {
            log.warn("query rider details failed, not found enterprise channel user, uid = {}", query.getUid());
            return Triple.of(true, null, enterpriseUserPackageDetailsVO);
        }
        enterpriseUserPackageDetailsVO.setRenewalStatus(enterpriseChannelUserVO.getRenewalStatus());
        
        UserBatteryDeposit userBatteryDeposit = userBatteryDepositService.selectByUidFromCache(userInfo.getUid());
        if (Objects.isNull(userBatteryDeposit) || StringUtils.isBlank(userBatteryDeposit.getOrderId())) {
            log.warn("query rider details failed, not found userBatteryDeposit,uid = {}", userInfo.getUid());
            return Triple.of(true, null, enterpriseUserPackageDetailsVO);
        }
        
        enterpriseUserPackageDetailsVO.setBatteryDeposit(userBatteryDeposit.getBatteryDeposit());
        
        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(userInfo.getUid());
        if (Objects.isNull(userBatteryMemberCard) || Objects.isNull(userBatteryMemberCard.getMemberCardId()) || Objects.equals(userBatteryMemberCard.getMemberCardId(),
                NumberConstant.ZERO_L)) {
            log.warn("query rider details failed, not found userBatteryMemberCard,uid = {}", userInfo.getUid());
            return Triple.of(true, null, enterpriseUserPackageDetailsVO);
        }
        
        enterpriseUserPackageDetailsVO.setIsExistMemberCard(UserBatteryMemberCardInfoVO.YES);
        enterpriseUserPackageDetailsVO.setMemberCardStatus(userBatteryMemberCard.getMemberCardStatus());
        enterpriseUserPackageDetailsVO.setMemberCardExpireTime(userBatteryMemberCard.getMemberCardExpireTime());
        enterpriseUserPackageDetailsVO.setRemainingNumber(userBatteryMemberCard.getRemainingNumber());
        enterpriseUserPackageDetailsVO.setMemberCardId(userBatteryMemberCard.getMemberCardId());
        
        BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(userBatteryMemberCard.getMemberCardId());
        if (Objects.isNull(batteryMemberCard)) {
            log.warn("query rider details failed, not found batteryMemberCard,uid = {},mid = {}", userInfo.getUid(), userBatteryMemberCard.getMemberCardId());
            return Triple.of(true, null, enterpriseUserPackageDetailsVO);
        }
        if (Objects.equals(batteryMemberCard.getRentUnit(), BatteryMemberCard.RENT_UNIT_DAY)) {
            enterpriseUserPackageDetailsVO.setValidDays(userBatteryMemberCard.getMemberCardExpireTime() > System.currentTimeMillis() ? (int) Math.ceil(
                    (userBatteryMemberCard.getMemberCardExpireTime() - System.currentTimeMillis()) / 24 / 60 / 60 / 1000.0) : 0);
        } else {
            enterpriseUserPackageDetailsVO.setValidDays(userBatteryMemberCard.getMemberCardExpireTime() > System.currentTimeMillis() ? (int) Math.ceil(
                    (userBatteryMemberCard.getMemberCardExpireTime() - System.currentTimeMillis()) / 60 / 1000.0) : 0);
        }
        
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
        if(Objects.nonNull(insuranceUserInfo)) {
            BeanUtils.copyProperties(insuranceUserInfo, insuranceUserInfoVo);
        }
        enterpriseUserPackageDetailsVO.setInsuranceUserInfoVo(insuranceUserInfoVo);
        
        //查询当前用户是否存在最新的冻结订单信息
        EleDisableMemberCardRecord eleDisableMemberCardRecord = eleDisableMemberCardRecordService.queryCreateTimeMaxEleDisableMemberCardRecord(SecurityUtils.getUid(),
                TenantContextHolder.getTenantId());
        if (Objects.nonNull(eleDisableMemberCardRecord) && UserBatteryMemberCard.MEMBER_CARD_DISABLE_REVIEW_REFUSE.equals(eleDisableMemberCardRecord.getStatus())) {
            enterpriseUserPackageDetailsVO.setRejectReason(eleDisableMemberCardRecord.getErrMsg());
        }
        
        
        return Triple.of(true, null, enterpriseUserPackageDetailsVO);
        
    }
    
    @Slave
    @Override
    public Triple<Boolean, String, Object> queryCostDetails(EnterprisePackageOrderQuery query) {
        
        log.info("query cost details start, enterprise id = {}, channel user id = {}", query.getEnterpriseId(), query.getUid());
        Long channelUserId = query.getUid();
        
        List<EnterpriseUserCostDetailsVO> enterpriseUserCostDetailsVOList = Lists.newArrayList();
        
        //1. 查询骑手购买套餐信息, 支付成功的记录
        //userBatteryMemberCardPackageService.selectByUid(channelUserId);
        List<EnterprisePackageOrderVO> enterprisePackageOrderVOList = enterpriseBatteryPackageMapper.queryBatteryPackageOrder(query);
        for(EnterprisePackageOrderVO enterprisePackageOrderVO : enterprisePackageOrderVOList){
            EnterpriseUserCostDetailsVO enterpriseUserCostDetailsVO = new EnterpriseUserCostDetailsVO();
            enterpriseUserCostDetailsVO.setCostType(UserCostTypeEnum.COST_TYPE_PURCHASE_PACKAGE.getCode());
            enterpriseUserCostDetailsVO.setPackageId(enterprisePackageOrderVO.getPackageId());
            enterpriseUserCostDetailsVO.setPackageName(enterprisePackageOrderVO.getPackageName());
            enterpriseUserCostDetailsVO.setPayAmount(enterprisePackageOrderVO.getPayAmount());
            enterpriseUserCostDetailsVO.setDepositAmount(enterprisePackageOrderVO.getBatteryDeposit());
            enterpriseUserCostDetailsVO.setOperationTime(enterprisePackageOrderVO.getCreateTime());
            enterpriseUserCostDetailsVOList.add(enterpriseUserCostDetailsVO);
        }
        
        //2. 查询骑手租退电池信息, 状态分别为租电池成功取走, 还电池成功
        EnterpriseRentBatteryOrderQuery enterpriseRentBatteryOrderQuery = new EnterpriseRentBatteryOrderQuery();
        enterpriseRentBatteryOrderQuery.setEnterpriseId(query.getEnterpriseId());
        enterpriseRentBatteryOrderQuery.setUid(query.getUid());
        enterpriseRentBatteryOrderQuery.setBeginTime(query.getBeginTime());
        enterpriseRentBatteryOrderQuery.setEndTime(query.getEndTime());
    
        List<EnterpriseRentBatteryOrderVO> enterpriseRentBatteryOrderVOList = enterpriseBatteryPackageMapper.queryRentBatteryOrder(enterpriseRentBatteryOrderQuery);
        
        for(EnterpriseRentBatteryOrderVO enterpriseRentBatteryOrderVO : enterpriseRentBatteryOrderVOList){
            EnterpriseUserCostDetailsVO enterpriseUserCostDetailsVO = new EnterpriseUserCostDetailsVO();
            enterpriseUserCostDetailsVO.setOrderNo(enterpriseRentBatteryOrderVO.getOrderNo());
            enterpriseUserCostDetailsVO.setDepositAmount(enterpriseRentBatteryOrderVO.getBatteryDeposit());
            enterpriseUserCostDetailsVO.setOperationTime(enterpriseRentBatteryOrderVO.getCreateTime());
            if(RentBatteryOrder.RENT_BATTERY_TAKE_SUCCESS.equals(enterpriseRentBatteryOrderVO.getStatus())){
                enterpriseUserCostDetailsVO.setCostType(UserCostTypeEnum.COST_TYPE_RENT_BATTERY.getCode());
                enterpriseUserCostDetailsVOList.add(enterpriseUserCostDetailsVO);
            }else if(RentBatteryOrder.RETURN_BATTERY_CHECK_SUCCESS.equals(enterpriseRentBatteryOrderVO.getStatus())){
                enterpriseUserCostDetailsVO.setCostType(UserCostTypeEnum.COST_TYPE_RETURN_BATTERY.getCode());
                enterpriseUserCostDetailsVOList.add(enterpriseUserCostDetailsVO);
            }
        }
        
        //3. 查询骑手套餐冻结信息, 审核通过的记录
        List<EnterpriseFreezePackageRecordVO> enterpriseFreezePackageRecordVOList = enterpriseBatteryPackageMapper.queryBatteryFreezeOrder(query);
        
        for(EnterpriseFreezePackageRecordVO enterpriseFreezePackageRecordVO : enterpriseFreezePackageRecordVOList){
            EnterpriseUserCostDetailsVO enterpriseUserCostDetailsVO = new EnterpriseUserCostDetailsVO();
            enterpriseUserCostDetailsVO.setCostType(UserCostTypeEnum.COST_TYPE_FREEZE_PACKAGE.getCode());
            enterpriseUserCostDetailsVO.setPackageId(enterpriseFreezePackageRecordVO.getPackageId());
            enterpriseUserCostDetailsVO.setPackageName(enterpriseFreezePackageRecordVO.getPackageName());
            enterpriseUserCostDetailsVO.setOperationTime(enterpriseFreezePackageRecordVO.getCreateTime());
            enterpriseUserCostDetailsVOList.add(enterpriseUserCostDetailsVO);
        }
        
        //4. 查询骑手退押信息, 押金退款成功的记录
        List<EnterpriseRefundDepositOrderVO> enterpriseRefundDepositOrderVOList = enterpriseBatteryPackageMapper.queryBatteryDepositOrder(query);
        for(EnterpriseRefundDepositOrderVO enterpriseRefundDepositOrderVO : enterpriseRefundDepositOrderVOList){
            EnterpriseUserCostDetailsVO enterpriseUserCostDetailsVO = new EnterpriseUserCostDetailsVO();
            enterpriseUserCostDetailsVO.setCostType(UserCostTypeEnum.COST_TYPE_REFUND_DEPOSIT.getCode());
            enterpriseUserCostDetailsVO.setPackageId(enterpriseRefundDepositOrderVO.getPackageId());
            //根据套餐ID, 查询套餐信息
            BatteryMemberCard userBindBatteryMemberCard = batteryMemberCardService.queryByIdFromCache(enterpriseRefundDepositOrderVO.getPackageId());
            if(Objects.nonNull(userBindBatteryMemberCard)){
                enterpriseUserCostDetailsVO.setPackageName(userBindBatteryMemberCard.getName());
            }
            enterpriseUserCostDetailsVO.setPayAmount(enterpriseRefundDepositOrderVO.getPayAmount());
            enterpriseUserCostDetailsVO.setDepositAmount(enterpriseRefundDepositOrderVO.getRefundAmount());
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
        if(EnterprisePaymentStatusEnum.PAYMENT_TYPE_EXPIRED.getCode().equals(query.getPaymentStatus())){
            enterprisePackageOrderVOList = enterpriseBatteryPackageMapper.queryExpiredPackageOrder(query);
            assignmentForPurchasedPackage(enterprisePackageOrderVOList);
            
        } else if(EnterprisePaymentStatusEnum.PAYMENT_TYPE_SUCCESS.getCode().equals(query.getPaymentStatus())){
            enterprisePackageOrderVOList = enterpriseBatteryPackageMapper.queryPaidPackageOrder(query);
            assignmentForPurchasedPackage(enterprisePackageOrderVOList);
            
        } else if(EnterprisePaymentStatusEnum.PAYMENT_TYPE_NO_PAY.getCode().equals(query.getPaymentStatus())){
            enterprisePackageOrderVOList = enterpriseBatteryPackageMapper.queryUnpaidPackageOrder(query);
        }
        
        return Triple.of(true, null, enterprisePackageOrderVOList);
    }
    
    private void assignmentForPurchasedPackage(List<EnterprisePackageOrderVO> enterprisePackageOrderVOList){
        
        for(EnterprisePackageOrderVO enterprisePackageOrderVO : enterprisePackageOrderVOList){
        
            //设置套餐信息
            BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(enterprisePackageOrderVO.getPackageId());
            if(Objects.nonNull(batteryMemberCard)){
                enterprisePackageOrderVO.setPackageName(batteryMemberCard.getName());
                enterprisePackageOrderVO.setPayAmount(batteryMemberCard.getRentPrice());
            }
        
            //设置押金
            UserBatteryDeposit userBatteryDeposit = userBatteryDepositService.selectByUidFromCache(enterprisePackageOrderVO.getUid());
            if(Objects.nonNull(userBatteryDeposit)){
                enterprisePackageOrderVO.setBatteryDeposit(userBatteryDeposit.getBatteryDeposit());
            }
        
            //设置用户电池型号
            enterprisePackageOrderVO.setUserBatterySimpleType(userBatteryTypeService.selectUserSimpleBatteryType(enterprisePackageOrderVO.getUid()));
            
            //设置套餐购买后企业代付时间
            ElectricityMemberCardOrder electricityMemberCardOrder = eleMemberCardOrderService.selectByOrderNo(enterprisePackageOrderVO.getOrderNo());
            if(Objects.nonNull(electricityMemberCardOrder)){
                enterprisePackageOrderVO.setPaymentTime(electricityMemberCardOrder.getCreateTime());
            }
        
            //TODO 设置可回收云豆信息
        
        }
    }
    
    private Triple<Boolean, String, Object> handlerFirstBuyBatteryMemberCard(UserBatteryMemberCard userBatteryMemberCard, BatteryMemberCard batteryMemberCard, UserInfo userInfo) {
        if (Objects.nonNull(userBatteryMemberCard) && Objects.equals(userBatteryMemberCard.getMemberCardId(), UserBatteryMemberCard.SEND_REMAINING_NUMBER)) {
            log.warn("purchase package by enterprise user, not allow buy this package, uid = {}", userInfo.getUid());
            return Triple.of(false, "100274", "赠送套餐不允许续费");
        }
        
        if (!(Objects.equals(BatteryMemberCard.RENT_TYPE_NEW, batteryMemberCard.getRentType()) || Objects.equals(BatteryMemberCard.RENT_TYPE_UNLIMIT,
                batteryMemberCard.getRentType()))) {
            log.warn("purchase package by enterprise user, new batteryMemberCard not available,uid={},mid={}", userInfo.getUid(), batteryMemberCard.getId());
            return Triple.of(false, "100275", "换电套餐不可用");
        }
        
        UserBatteryDeposit userBatteryDeposit = userBatteryDepositService.selectByUidFromCache(userInfo.getUid());
        if (Objects.isNull(userBatteryDeposit)) {
            log.warn("purchase package by enterprise user, not found userBatteryDeposit,uid={}", userInfo.getUid());
            return Triple.of(false, "100247", "用户信息不存在");
        }
        
        boolean flag = batteryMemberCard.getDeposit().compareTo(userBatteryDeposit.getBatteryDeposit()) == 0;
        if (!flag) {
            log.warn("purchase package by enterprise user, batteryMemberCard deposit not equals user battery deposit,uid={},mid={}", userInfo.getUid(), batteryMemberCard.getId());
            return Triple.of(false, "100277", "换电套餐押金不一致");
        }
        
        return Triple.of(true, null, null);
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
    
    @Transactional(rollbackFor = Exception.class)
    public void handlerBatteryMembercardZeroPayment(BatteryMemberCard batteryMemberCard, ElectricityMemberCardOrder memberCardOrder, UserBatteryMemberCard userBatteryMemberCard,
            UserInfo userInfo) {
        int payCount = electricityMemberCardOrderService.queryMaxPayCount(userBatteryMemberCard);
        //用户未绑定套餐
        if (Objects.isNull(userBatteryMemberCard) || Objects.isNull(userBatteryMemberCard.getMemberCardId()) || Objects.equals(userBatteryMemberCard.getMemberCardId(),
                NumberConstant.ZERO_L)) {
            UserBatteryMemberCard userBatteryMemberCardUpdate = new UserBatteryMemberCard();
            userBatteryMemberCardUpdate.setUid(userInfo.getUid());
            userBatteryMemberCardUpdate.setMemberCardId(batteryMemberCard.getId());
            userBatteryMemberCardUpdate.setOrderId(memberCardOrder.getOrderId());
            userBatteryMemberCardUpdate.setMemberCardExpireTime(
                    System.currentTimeMillis() + batteryMemberCardService.transformBatteryMembercardEffectiveTime(batteryMemberCard, memberCardOrder));
            userBatteryMemberCardUpdate.setOrderExpireTime(
                    System.currentTimeMillis() + batteryMemberCardService.transformBatteryMembercardEffectiveTime(batteryMemberCard, memberCardOrder));
            userBatteryMemberCardUpdate.setOrderEffectiveTime(System.currentTimeMillis());
            userBatteryMemberCardUpdate.setOrderRemainingNumber(memberCardOrder.getMaxUseCount());
            userBatteryMemberCardUpdate.setRemainingNumber(memberCardOrder.getMaxUseCount());
            userBatteryMemberCardUpdate.setMemberCardStatus(UserBatteryMemberCard.MEMBER_CARD_NOT_DISABLE);
            userBatteryMemberCardUpdate.setDisableMemberCardTime(null);
            userBatteryMemberCardUpdate.setDelFlag(UserBatteryMemberCard.DEL_NORMAL);
            //套餐购买次数加一
            userBatteryMemberCardUpdate.setCardPayCount(payCount + 1);
            userBatteryMemberCardUpdate.setCreateTime(System.currentTimeMillis());
            userBatteryMemberCardUpdate.setUpdateTime(System.currentTimeMillis());
            userBatteryMemberCardService.insert(userBatteryMemberCardUpdate);
            
        } else {
            //用户已绑定套餐
            UserBatteryMemberCardPackage userBatteryMemberCardPackage = new UserBatteryMemberCardPackage();
            userBatteryMemberCardPackage.setUid(userInfo.getUid());
            userBatteryMemberCardPackage.setMemberCardId(memberCardOrder.getMemberCardId());
            userBatteryMemberCardPackage.setOrderId(memberCardOrder.getOrderId());
            userBatteryMemberCardPackage.setRemainingNumber(batteryMemberCard.getUseCount());
            userBatteryMemberCardPackage.setMemberCardExpireTime(batteryMemberCardService.transformBatteryMembercardEffectiveTime(batteryMemberCard, memberCardOrder));
            userBatteryMemberCardPackage.setTenantId(userInfo.getTenantId());
            userBatteryMemberCardPackage.setCreateTime(System.currentTimeMillis());
            userBatteryMemberCardPackage.setUpdateTime(System.currentTimeMillis());
            userBatteryMemberCardPackageService.insert(userBatteryMemberCardPackage);
            
            UserBatteryMemberCard userBatteryMemberCardUpdate = new UserBatteryMemberCard();
            userBatteryMemberCardUpdate.setUid(userInfo.getUid());
            userBatteryMemberCardUpdate.setMemberCardExpireTime(
                    userBatteryMemberCard.getMemberCardExpireTime() + batteryMemberCardService.transformBatteryMembercardEffectiveTime(batteryMemberCard, memberCardOrder));
            userBatteryMemberCardUpdate.setRemainingNumber(userBatteryMemberCard.getRemainingNumber() + memberCardOrder.getMaxUseCount());
            userBatteryMemberCardUpdate.setCardPayCount(payCount + 1);
            userBatteryMemberCardUpdate.setUpdateTime(System.currentTimeMillis());
            userBatteryMemberCardService.updateByUid(userBatteryMemberCardUpdate);
            
            //获取用户电池型号
            List<String> userBatteryTypes = acquireUserBatteryType(userBatteryTypeService.selectByUid(userInfo.getUid()),
                    memberCardBatteryTypeService.selectBatteryTypeByMid(batteryMemberCard.getId()));
            if (CollectionUtils.isNotEmpty(userBatteryTypes)) {
                //更新用户电池型号
                userBatteryTypeService.deleteByUid(userInfo.getUid());
                userBatteryTypeService.batchInsert(userBatteryTypeService.buildUserBatteryType(userBatteryTypes, userInfo));
            }
        }
        
        //暂无活动
        /*ActivityProcessDTO activityProcessDTO = new ActivityProcessDTO();
        activityProcessDTO.setOrderNo(memberCardOrder.getOrderId());
        activityProcessDTO.setType(PackageTypeEnum.PACKAGE_TYPE_BATTERY.getCode());
        activityProcessDTO.setActivityType(ActivityEnum.INVITATION_CRITERIA_BUY_PACKAGE.getCode());
        activityProcessDTO.setTraceId(IdUtil.simpleUUID());
        activityService.asyncProcessActivity(activityProcessDTO);*/
        
        //electricityMemberCardOrderService.sendUserCoupon(batteryMemberCard, memberCardOrder);
        
        //套餐购买次数加一
        UserInfo userInfoUpdate = new UserInfo();
        userInfoUpdate.setUid(userInfo.getUid());
        
        userInfoUpdate.setPayCount(userInfo.getPayCount() + 1);
        userInfoUpdate.setUpdateTime(System.currentTimeMillis());
        userInfoService.updateByUid(userInfoUpdate);
        
        ElectricityMemberCardOrder memberCardOrderUpdate = new ElectricityMemberCardOrder();
        memberCardOrderUpdate.setId(memberCardOrder.getId());
        memberCardOrderUpdate.setStatus(ElectricityMemberCardOrder.STATUS_SUCCESS);
        memberCardOrderUpdate.setUpdateTime(System.currentTimeMillis());
        memberCardOrderUpdate.setPayCount(payCount + 1);
        
        electricityMemberCardOrderService.updateByID(memberCardOrderUpdate);
    
        //保存骑手购买套餐信息，用于云豆回收业务
        //userBehaviorRecordService.saveUserBehaviorRecord(memberCardOrder.getUid(), memberCardOrder.getOrderId(), UserBehaviorRecord.TYPE_PAY_MEMBERCARD, memberCardOrder.getTenantId());
        anotherPayMembercardRecordService.saveAnotherPayMembercardRecord(memberCardOrder.getUid(), memberCardOrder.getOrderId(),memberCardOrder.getTenantId());
    
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
