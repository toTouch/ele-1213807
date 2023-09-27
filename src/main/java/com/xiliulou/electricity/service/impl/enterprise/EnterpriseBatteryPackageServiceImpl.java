package com.xiliulou.electricity.service.impl.enterprise;

import com.google.api.client.util.Lists;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.entity.BatteryMemberCard;
import com.xiliulou.electricity.entity.CommonPayOrder;
import com.xiliulou.electricity.entity.EleDisableMemberCardRecord;
import com.xiliulou.electricity.entity.ElectricityMemberCardOrder;
import com.xiliulou.electricity.entity.ElectricityPayParams;
import com.xiliulou.electricity.entity.ElectricityTradeOrder;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.entity.MemberCardBatteryType;
import com.xiliulou.electricity.entity.UserBatteryDeposit;
import com.xiliulou.electricity.entity.UserBatteryMemberCard;
import com.xiliulou.electricity.entity.UserBatteryMemberCardPackage;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.entity.UserOauthBind;
import com.xiliulou.electricity.entity.enterprise.EnterpriseInfo;
import com.xiliulou.electricity.enums.BatteryMemberCardBusinessTypeEnum;
import com.xiliulou.electricity.enums.BusinessType;
import com.xiliulou.electricity.enums.enterprise.PackageOrderTypeEnum;
import com.xiliulou.electricity.mapper.BatteryMemberCardMapper;
import com.xiliulou.electricity.mapper.enterprise.EnterpriseBatteryPackageMapper;
import com.xiliulou.electricity.query.BatteryMemberCardQuery;
import com.xiliulou.electricity.query.enterprise.EnterpriseChannelUserQuery;
import com.xiliulou.electricity.query.enterprise.EnterpriseMemberCardQuery;
import com.xiliulou.electricity.query.enterprise.EnterprisePackageOrderQuery;
import com.xiliulou.electricity.query.enterprise.EnterpriseRentBatteryOrderQuery;
import com.xiliulou.electricity.service.BatteryMemberCardService;
import com.xiliulou.electricity.service.BatteryModelService;
import com.xiliulou.electricity.service.CouponService;
import com.xiliulou.electricity.service.EleDisableMemberCardRecordService;
import com.xiliulou.electricity.service.ElectricityMemberCardOrderService;
import com.xiliulou.electricity.service.ElectricityPayParamsService;
import com.xiliulou.electricity.service.ElectricityTradeOrderService;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.MemberCardBatteryTypeService;
import com.xiliulou.electricity.service.UserBatteryDepositService;
import com.xiliulou.electricity.service.UserBatteryMemberCardPackageService;
import com.xiliulou.electricity.service.UserBatteryMemberCardService;
import com.xiliulou.electricity.service.UserBatteryTypeService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.service.UserOauthBindService;
import com.xiliulou.electricity.service.car.CarRentalPackageService;
import com.xiliulou.electricity.service.enterprise.EnterpriseBatteryPackageService;
import com.xiliulou.electricity.service.enterprise.EnterpriseChannelUserService;
import com.xiliulou.electricity.service.enterprise.EnterpriseInfoService;
import com.xiliulou.electricity.service.enterprise.EnterprisePackageService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.OrderIdUtil;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.BatteryMemberCardAndTypeVO;
import com.xiliulou.electricity.vo.BatteryMemberCardVO;
import com.xiliulou.electricity.vo.UserBatteryMemberCardInfoVO;
import com.xiliulou.electricity.vo.enterprise.EnterpriseChannelUserVO;
import com.xiliulou.electricity.vo.enterprise.EnterprisePackageOrderVO;
import com.xiliulou.electricity.vo.enterprise.EnterpriseRentBatteryOrderVO;
import com.xiliulou.electricity.vo.enterprise.EnterpriseUserPackageDetailsVO;
import com.xiliulou.pay.weixinv3.dto.WechatJsapiOrderResultDTO;
import com.xiliulou.pay.weixinv3.exception.WechatPayException;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
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
        List<Long> packageIds = enterprisePackageService.selectByEnterpriseId(query.getId());
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
        
        memberCardOrder.setStatus(ElectricityMemberCardOrder.STATUS_INIT);
        memberCardOrder.setMemberCardId(batteryMemberCard.getId());
        memberCardOrder.setUid(userInfo.getUid());
        memberCardOrder.setMaxUseCount(batteryMemberCard.getUseCount());
        memberCardOrder.setCardName(batteryMemberCard.getName());
        memberCardOrder.setPayAmount(payAmount);
        memberCardOrder.setPayType(ElectricityMemberCardOrder.ENTERPRISE_PAYMENT);
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
        
        //查询当前用户是否存在最新的冻结订单信息
        EleDisableMemberCardRecord eleDisableMemberCardRecord = eleDisableMemberCardRecordService.queryCreateTimeMaxEleDisableMemberCardRecord(SecurityUtils.getUid(),
                TenantContextHolder.getTenantId());
        if (Objects.nonNull(eleDisableMemberCardRecord) && UserBatteryMemberCard.MEMBER_CARD_DISABLE_REVIEW_REFUSE.equals(eleDisableMemberCardRecord.getStatus())) {
            enterpriseUserPackageDetailsVO.setRejectReason(eleDisableMemberCardRecord.getErrMsg());
        }
        
        return Triple.of(true, null, enterpriseUserPackageDetailsVO);
        
    }
    
    @Override
    public Triple<Boolean, String, Object> queryCostDetails(EnterprisePackageOrderQuery query) {
        
        Long channelUserId = query.getUid();
        
        //1. 查询骑手购买套餐信息
        //userBatteryMemberCardPackageService.selectByUid(channelUserId);
        List<EnterprisePackageOrderVO> enterprisePackageOrderVOList = enterpriseBatteryPackageMapper.queryBatteryPackageOrder(query);
        
        //2. 查询骑手租退电池信息
        EnterpriseRentBatteryOrderQuery enterpriseRentBatteryOrderQuery = new EnterpriseRentBatteryOrderQuery();
        enterpriseRentBatteryOrderQuery.setEnterpriseId(query.getEnterpriseId());
        enterpriseRentBatteryOrderQuery.setUid(query.getUid());
        enterpriseRentBatteryOrderQuery.setBeginTime(query.getBeginTime());
        enterpriseRentBatteryOrderQuery.setEndTime(query.getEndTime());
    
        List<EnterpriseRentBatteryOrderVO> enterpriseRentBatteryOrderVOList = enterpriseBatteryPackageMapper.queryRentBatteryOrder(enterpriseRentBatteryOrderQuery);
        
        
        //3. 查询骑手套餐冻结信息
        
        
        
        
        //4. 查询骑手退押信息
        
        
        
        return null;
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
