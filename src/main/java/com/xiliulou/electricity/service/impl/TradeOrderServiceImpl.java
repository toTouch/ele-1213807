package com.xiliulou.electricity.service.impl;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.bo.userInfoGroup.UserInfoGroupNamesBO;
import com.xiliulou.electricity.bo.wechat.WechatPayParamsDetails;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.constant.installment.InstallmentConstants;
import com.xiliulou.electricity.entity.BatteryMemberCard;
import com.xiliulou.electricity.entity.BatteryMembercardRefundOrder;
import com.xiliulou.electricity.entity.EleBatteryServiceFeeOrder;
import com.xiliulou.electricity.entity.EleDepositOrder;
import com.xiliulou.electricity.entity.EleRefundOrder;
import com.xiliulou.electricity.entity.ElectricityBattery;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.ElectricityMemberCardOrder;
import com.xiliulou.electricity.entity.ElectricityPayParams;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.entity.FranchiseeInsurance;
import com.xiliulou.electricity.entity.InsuranceOrder;
import com.xiliulou.electricity.entity.ServiceFeeUserInfo;
import com.xiliulou.electricity.entity.UnionPayOrder;
import com.xiliulou.electricity.entity.UnionTradeOrder;
import com.xiliulou.electricity.entity.UserBatteryDeposit;
import com.xiliulou.electricity.entity.UserBatteryMemberCard;
import com.xiliulou.electricity.entity.UserCoupon;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.entity.UserOauthBind;
import com.xiliulou.electricity.entity.car.CarRentalPackageOrderSlippagePo;
import com.xiliulou.electricity.entity.installment.InstallmentRecord;
import com.xiliulou.electricity.enums.BusinessType;
import com.xiliulou.electricity.enums.ServiceFeeEnum;
import com.xiliulou.electricity.query.BatteryMemberCardAndInsuranceQuery;
import com.xiliulou.electricity.query.IntegratedPaymentAdd;
import com.xiliulou.electricity.query.installment.InstallmentPayQuery;
import com.xiliulou.electricity.query.userinfo.userInfoGroup.UserInfoGroupDetailQuery;
import com.xiliulou.electricity.service.BatteryMemberCardOrderCouponService;
import com.xiliulou.electricity.service.BatteryMemberCardService;
import com.xiliulou.electricity.service.BatteryMembercardRefundOrderService;
import com.xiliulou.electricity.service.BatteryModelService;
import com.xiliulou.electricity.service.CouponService;
import com.xiliulou.electricity.service.EleBatteryServiceFeeOrderService;
import com.xiliulou.electricity.service.EleDepositOrderService;
import com.xiliulou.electricity.service.EleRefundOrderService;
import com.xiliulou.electricity.service.ElectricityBatteryService;
import com.xiliulou.electricity.service.ElectricityCabinetService;
import com.xiliulou.electricity.service.ElectricityMemberCardOrderService;
import com.xiliulou.electricity.service.ElectricityMemberCardService;
import com.xiliulou.electricity.service.ElectricityPayParamsService;
import com.xiliulou.electricity.service.FranchiseeInsuranceService;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.InsuranceOrderService;
import com.xiliulou.electricity.service.JoinShareActivityHistoryService;
import com.xiliulou.electricity.service.JoinShareActivityRecordService;
import com.xiliulou.electricity.service.JoinShareMoneyActivityHistoryService;
import com.xiliulou.electricity.service.JoinShareMoneyActivityRecordService;
import com.xiliulou.electricity.service.OldUserActivityService;
import com.xiliulou.electricity.service.ServiceFeeUserInfoService;
import com.xiliulou.electricity.service.ShareActivityRecordService;
import com.xiliulou.electricity.service.ShareMoneyActivityRecordService;
import com.xiliulou.electricity.service.ShareMoneyActivityService;
import com.xiliulou.electricity.service.StoreService;
import com.xiliulou.electricity.service.TradeOrderService;
import com.xiliulou.electricity.service.UnionTradeOrderService;
import com.xiliulou.electricity.service.UserAmountService;
import com.xiliulou.electricity.service.UserBatteryDepositService;
import com.xiliulou.electricity.service.UserBatteryMemberCardService;
import com.xiliulou.electricity.service.UserBatteryTypeService;
import com.xiliulou.electricity.service.UserCouponService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.service.UserOauthBindService;
import com.xiliulou.electricity.service.WechatPayParamsBizService;
import com.xiliulou.electricity.service.car.CarRentalPackageOrderSlippageService;
import com.xiliulou.electricity.service.enterprise.EnterpriseChannelUserService;
import com.xiliulou.electricity.service.installment.InstallmentRecordService;
import com.xiliulou.electricity.service.userinfo.userInfoGroup.UserInfoGroupDetailService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.DateUtils;
import com.xiliulou.electricity.utils.OrderIdUtil;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.pay.weixinv3.dto.WechatJsapiOrderResultDTO;
import com.xiliulou.pay.weixinv3.exception.WechatPayException;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.xiliulou.electricity.constant.CacheConstant.CACHE_INSTALLMENT_CANCEL_SIGN;
import static com.xiliulou.electricity.constant.CacheConstant.CACHE_INSTALLMENT_PAYMENT_LOCK;
import static com.xiliulou.electricity.constant.installment.InstallmentConstants.INSTALLMENT_RECORD_STATUS_INIT;
import static com.xiliulou.electricity.constant.installment.InstallmentConstants.INSTALLMENT_RECORD_STATUS_SIGN;
import static com.xiliulou.electricity.constant.installment.InstallmentConstants.INSTALLMENT_RECORD_STATUS_TERMINATE;
import static com.xiliulou.electricity.constant.installment.InstallmentConstants.INSTALLMENT_RECORD_STATUS_UN_SIGN;

/**
 * 混合支付(UnionTradeOrder)表服务接口
 *
 * @author makejava
 * @since 2022-11-07 14:06:24
 */
@Service("tradeOrderService")
@Slf4j
public class TradeOrderServiceImpl implements TradeOrderService {
    
    @Resource
    private CarRentalPackageOrderSlippageService carRentalPackageOrderSlippageService;
    
    @Autowired
    ElectricityPayParamsService electricityPayParamsService;
    
    @Autowired
    UserOauthBindService userOauthBindService;
    
    @Autowired
    UserInfoService userInfoService;
    
    @Autowired
    FranchiseeService franchiseeService;
    
    @Autowired
    FranchiseeInsuranceService franchiseeInsuranceService;
    
    @Autowired
    EleDepositOrderService eleDepositOrderService;
    
    @Autowired
    InsuranceOrderService insuranceOrderService;
    
    @Autowired
    UnionTradeOrderService unionTradeOrderService;
    
    @Autowired
    UserBatteryMemberCardService userBatteryMemberCardService;
    
    @Autowired
    ServiceFeeUserInfoService serviceFeeUserInfoService;
    
    @Autowired
    ElectricityMemberCardOrderService electricityMemberCardOrderService;
    
    @Autowired
    ElectricityMemberCardService electricityMemberCardService;
    
    @Autowired
    UserCouponService userCouponService;
    
    @Autowired
    CouponService couponService;
    
    @Autowired
    JoinShareActivityRecordService joinShareActivityRecordService;
    
    @Autowired
    OldUserActivityService oldUserActivityService;
    
    @Autowired
    JoinShareActivityHistoryService joinShareActivityHistoryService;
    
    @Autowired
    ShareActivityRecordService shareActivityRecordService;
    
    @Autowired
    JoinShareMoneyActivityRecordService joinShareMoneyActivityRecordService;
    
    @Autowired
    JoinShareMoneyActivityHistoryService joinShareMoneyActivityHistoryService;
    
    @Autowired
    ShareMoneyActivityService shareMoneyActivityService;
    
    @Autowired
    ShareMoneyActivityRecordService shareMoneyActivityRecordService;
    
    @Autowired
    UserAmountService userAmountService;
    
    @Autowired
    RedisService redisService;
    
    @Autowired
    ElectricityCabinetService electricityCabinetService;
    
    @Autowired
    StoreService storeService;
    
    @Autowired
    BatteryModelService batteryModelService;
    
    @Autowired
    BatteryMemberCardOrderCouponService memberCardOrderCouponService;
    
    @Autowired
    BatteryMemberCardService batteryMemberCardService;
    
    @Autowired
    EleRefundOrderService eleRefundOrderService;
    
    @Autowired
    UserBatteryDepositService userBatteryDepositService;
    
    @Autowired
    BatteryMembercardRefundOrderService batteryMembercardRefundOrderService;
    
    @Autowired
    EleBatteryServiceFeeOrderService batteryServiceFeeOrderService;
    
    @Autowired
    ElectricityBatteryService electricityBatteryService;
    
    @Autowired
    UserBatteryTypeService userBatteryTypeService;
    
    @Resource
    EnterpriseChannelUserService enterpriseChannelUserService;
    
    @Autowired
    private UserInfoGroupDetailService userInfoGroupDetailService;
    
    @Resource
    private WechatPayParamsBizService wechatPayParamsBizService;
    
    @Autowired
    private InstallmentRecordService installmentRecordService;
    
    @Autowired
    private ApplicationContext applicationContext;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Triple<Boolean, String, Object> integratedPayment(IntegratedPaymentAdd integratedPaymentAdd, HttpServletRequest request) {
        
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("rentBattery  ERROR! not found user ");
            return Triple.of(false, "ELECTRICITY.0001", "未找到用户");
        }
        
        Integer tenantId = TenantContextHolder.getTenantId();
        
        boolean getLockSuccess = redisService.setNx(CacheConstant.ELE_CACHE_USER_DEPOSIT_LOCK_KEY + SecurityUtils.getUid(), "1", 3 * 1000L, false);
        if (!getLockSuccess) {
            return Triple.of(false, "ELECTRICITY.0034", "操作频繁");
        }
        
        try {
            
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
            
            // 检查是否为自主续费状态
            Boolean userRenewalStatus = enterpriseChannelUserService.checkRenewalStatusByUid(user.getUid());
            if (!userRenewalStatus) {
                log.warn("BATTERY MEMBER ORDER WARN! user renewal status is false, uid={}, mid={}", user.getUid(), integratedPaymentAdd.getMemberCardId());
                return Triple.of(false, "000088", "您已是渠道用户，请联系对应站点购买套餐");
            }
            
            if (Objects.equals(userInfo.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_YES)) {
                log.warn("BATTERY DEPOSIT WARN! user is rent deposit,uid={} ", user.getUid());
                return Triple.of(false, "ELECTRICITY.0049", "已缴纳押金");
            }
            
            ElectricityPayParams electricityPayParams = electricityPayParamsService.queryCacheByTenantIdAndFranchiseeId(tenantId, integratedPaymentAdd.getFranchiseeId());
            if (Objects.isNull(electricityPayParams)) {
                log.warn("BATTERY DEPOSIT WARN!not found pay params,uid={}", user.getUid());
                return Triple.of(false, "100307", "未配置支付参数!");
            }
            
            UserOauthBind userOauthBind = userOauthBindService.queryUserOauthBySysId(user.getUid(), tenantId);
            if (Objects.isNull(userOauthBind) || Objects.isNull(userOauthBind.getThirdId())) {
                log.warn("BATTERY DEPOSIT WARN!not found useroauthbind or thirdid is null,uid={}", user.getUid());
                return Triple.of(false, "100308", "未找到用户的第三方授权信息!");
            }
            
            BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(integratedPaymentAdd.getMemberCardId());
            if (Objects.isNull(batteryMemberCard)) {
                log.warn("BATTERY DEPOSIT WARN!not found batteryMemberCard,uid={},mid={}", user.getUid(), integratedPaymentAdd.getMemberCardId());
                return Triple.of(false, "ELECTRICITY.00121", "电池套餐不存在");
            }
            
            if (!Objects.equals(BatteryMemberCard.STATUS_UP, batteryMemberCard.getStatus())) {
                log.warn("BATTERY DEPOSIT WARN! batteryMemberCard is disable,uid={},mid={}", user.getUid(), integratedPaymentAdd.getMemberCardId());
                return Triple.of(false, "100275", "电池套餐不可用");
            }
            
            if (Objects.nonNull(userInfo.getFranchiseeId()) && !Objects.equals(userInfo.getFranchiseeId(), NumberConstant.ZERO_L) && !Objects.equals(userInfo.getFranchiseeId(),
                    batteryMemberCard.getFranchiseeId())) {
                log.warn("BATTERY DEPOSIT WARN! batteryMemberCard franchiseeId not equals,uid={},mid={}", user.getUid(), integratedPaymentAdd.getMemberCardId());
                return Triple.of(false, "100349", "用户加盟商与套餐加盟商不一致");
            }
            
            // 判断套餐用户分组和用户的用户分组是否匹配
            List<UserInfoGroupNamesBO> userInfoGroupNamesBOs = userInfoGroupDetailService.listGroupByUid(
                    UserInfoGroupDetailQuery.builder().uid(userInfo.getUid()).tenantId(TenantContextHolder.getTenantId()).build());
            
            if (CollectionUtils.isNotEmpty(userInfoGroupNamesBOs)) {
                if (Objects.equals(batteryMemberCard.getGroupType(), BatteryMemberCard.GROUP_TYPE_SYSTEM)) {
                    return Triple.of(false, "100318", "您浏览的套餐已下架，请看看其他的吧");
                }
                
                List<Long> userGroupIds = userInfoGroupNamesBOs.stream().map(UserInfoGroupNamesBO::getGroupId).collect(Collectors.toList());
                userGroupIds.retainAll(JsonUtil.fromJsonArray(batteryMemberCard.getUserInfoGroupIds(), Long.class));
                if (CollectionUtils.isEmpty(userGroupIds)) {
                    return Triple.of(false, "100318", "您浏览的套餐已下架，请看看其他的吧");
                }
            } else {
                if (Objects.equals(batteryMemberCard.getGroupType(), BatteryMemberCard.GROUP_TYPE_USER)) {
                    return Triple.of(false, "100318", "您浏览的套餐已下架，请看看其他的吧");
                }
            }
            
            // 判断套餐租赁状态，用户为老用户，套餐类型为新租，则不支持购买
            if (userInfo.getPayCount() > 0 && BatteryMemberCard.RENT_TYPE_NEW.equals(batteryMemberCard.getRentType())) {
                log.warn("INTEGRATED PAYMENT WARN! The rent type of current package is a new rental package, uid={}, mid={}", userInfo.getUid(),
                        integratedPaymentAdd.getMemberCardId());
                return Triple.of(false, "100376", "已是平台老用户，无法购买新租类型套餐，请刷新页面重试");
            }
            
            // 获取扫码柜机
            ElectricityCabinet electricityCabinet = null;
            if (StringUtils.isNotBlank(integratedPaymentAdd.getProductKey()) && StringUtils.isNotBlank(integratedPaymentAdd.getDeviceName())) {
                electricityCabinet = electricityCabinetService.queryFromCacheByProductAndDeviceName(integratedPaymentAdd.getProductKey(), integratedPaymentAdd.getDeviceName());
            }
            
            if (Objects.nonNull(electricityCabinet) && !Objects.equals(electricityCabinet.getFranchiseeId(), NumberConstant.ZERO_L) && Objects.nonNull(
                    electricityCabinet.getFranchiseeId()) && !Objects.equals(electricityCabinet.getFranchiseeId(), batteryMemberCard.getFranchiseeId())) {
                log.warn("BATTERY DEPOSIT WARN! batteryMemberCard franchiseeId not equals electricityCabinet,eid={},mid={}", electricityCabinet.getId(),
                        integratedPaymentAdd.getMemberCardId());
                return Triple.of(false, "100375", "柜机加盟商与套餐加盟商不一致,请删除小程序后重新进入");
            }
            
            // 押金订单
            Triple<Boolean, String, Object> generateDepositOrderResult = generateDepositOrder(userInfo, batteryMemberCard, electricityCabinet, electricityPayParams);
            if (Boolean.FALSE.equals(generateDepositOrderResult.getLeft())) {
                return generateDepositOrderResult;
            }
            
            // 套餐订单
            Set<Integer> userCouponIds = electricityMemberCardOrderService.generateUserCouponIds(integratedPaymentAdd.getUserCouponId(), integratedPaymentAdd.getUserCouponIds());
            Triple<Boolean, String, Object> generateMemberCardOrderResult = generateMemberCardOrder(userInfo, batteryMemberCard, integratedPaymentAdd, userCouponIds,
                    electricityCabinet, electricityPayParams);
            if (Boolean.FALSE.equals(generateMemberCardOrderResult.getLeft())) {
                return generateMemberCardOrderResult;
            }
            
            // 保险订单
            Triple<Boolean, String, Object> generateInsuranceOrderResult = generateInsuranceOrder(userInfo, integratedPaymentAdd.getInsuranceId(), electricityCabinet,
                    electricityPayParams);
            if (Boolean.FALSE.equals(generateInsuranceOrderResult.getLeft())) {
                return generateInsuranceOrderResult;
            }
            
            List<String> orderList = new ArrayList<>();
            List<Integer> orderTypeList = new ArrayList<>();
            List<BigDecimal> allPayAmount = new ArrayList<>();
            
            BigDecimal integratedPaAmount = BigDecimal.valueOf(0);
            
            // 保存押金订单
            if (Boolean.TRUE.equals(generateDepositOrderResult.getLeft()) && Objects.nonNull(generateDepositOrderResult.getRight())) {
                EleDepositOrder eleDepositOrder = (EleDepositOrder) generateDepositOrderResult.getRight();
                eleDepositOrderService.insert(eleDepositOrder);
                
                orderList.add(eleDepositOrder.getOrderId());
                orderTypeList.add(UnionPayOrder.ORDER_TYPE_DEPOSIT);
                allPayAmount.add(eleDepositOrder.getPayAmount());
                integratedPaAmount = integratedPaAmount.add(eleDepositOrder.getPayAmount());
            }
            
            // 保存套餐订单
            if (Boolean.TRUE.equals(generateMemberCardOrderResult.getLeft()) && Objects.nonNull(generateMemberCardOrderResult.getRight())) {
                ElectricityMemberCardOrder electricityMemberCardOrder = (ElectricityMemberCardOrder) generateMemberCardOrderResult.getRight();
                electricityMemberCardOrderService.insert(electricityMemberCardOrder);
                
                orderList.add(electricityMemberCardOrder.getOrderId());
                orderTypeList.add(UnionPayOrder.ORDER_TYPE_MEMBER_CARD);
                allPayAmount.add(electricityMemberCardOrder.getPayAmount());
                integratedPaAmount = integratedPaAmount.add(electricityMemberCardOrder.getPayAmount());
                
                if (CollectionUtils.isNotEmpty(userCouponIds)) {
                    // 保存订单所使用的优惠券
                    memberCardOrderCouponService.batchInsert(electricityMemberCardOrderService.buildMemberCardOrderCoupon(electricityMemberCardOrder.getOrderId(), userCouponIds));
                    // 修改优惠券状态为核销中
                    userCouponService.batchUpdateUserCoupon(
                            electricityMemberCardOrderService.buildUserCouponList(userCouponIds, UserCoupon.STATUS_IS_BEING_VERIFICATION, electricityMemberCardOrder.getOrderId()));
                }
            }
            
            // 保存保险订单
            if (Boolean.TRUE.equals(generateInsuranceOrderResult.getLeft()) && Objects.nonNull(generateInsuranceOrderResult.getRight())) {
                InsuranceOrder insuranceOrder = (InsuranceOrder) generateInsuranceOrderResult.getRight();
                insuranceOrderService.insert(insuranceOrder);
                
                orderList.add(insuranceOrder.getOrderId());
                orderTypeList.add(UnionPayOrder.ORDER_TYPE_INSURANCE);
                allPayAmount.add(insuranceOrder.getPayAmount());
                integratedPaAmount = integratedPaAmount.add(insuranceOrder.getPayAmount());
            }
            
            // 处理0元问题
            if (integratedPaAmount.compareTo(BigDecimal.valueOf(0.01)) < 0) {
                
                Triple<Boolean, String, Object> result = handleTotalAmountZero(userInfo, orderList, orderTypeList, null);
                if (Boolean.FALSE.equals(result.getLeft())) {
                    return result;
                }
                
                return Triple.of(true, "", null);
            }
            
            // 非0元查询详情用于调起支付，查询详情会因为证书问题报错，置于0元处理前会干扰其逻辑
            WechatPayParamsDetails wechatPayParamsDetails = wechatPayParamsBizService.getDetailsByIdTenantIdAndFranchiseeId(tenantId, integratedPaymentAdd.getFranchiseeId());
            
            // 调起支付
            UnionPayOrder unionPayOrder = UnionPayOrder.builder().jsonOrderId(JsonUtil.toJson(orderList)).jsonOrderType(JsonUtil.toJson(orderTypeList))
                    .jsonSingleFee(JsonUtil.toJson(allPayAmount)).payAmount(integratedPaAmount).tenantId(tenantId).attach(UnionTradeOrder.ATTACH_INTEGRATED_PAYMENT)
                    .description("租电租金（含押金）").uid(user.getUid()).build();
            WechatJsapiOrderResultDTO resultDTO = unionTradeOrderService.unionCreateTradeOrderAndGetPayParams(unionPayOrder, wechatPayParamsDetails, userOauthBind.getThirdId(),
                    request, null);
            return Triple.of(true, null, resultDTO);
            
            // 友好提示，对用户端不展示错误信息
        } catch (Exception e) {
            log.error("CREATE UNION_INSURANCE_DEPOSIT_ORDER ERROR! wechat v3 order  error! uid={}", user.getUid(), e);
        } finally {
            //            redisService.delete(CacheConstant.ELE_CACHE_USER_DEPOSIT_LOCK_KEY + user.getUid());
        }
        
        return Triple.of(false, "PAY_TRANSFER.0019", "支付未成功，请联系客服处理");
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Triple<Boolean, String, Object> payMemberCardAndInsurance(BatteryMemberCardAndInsuranceQuery query, HttpServletRequest request) {
        Integer tenantId = TenantContextHolder.getTenantId();
        
        boolean getLockSuccess = redisService.setNx(CacheConstant.ELE_CACHE_USER_BATTERY_MEMBER_CARD_LOCK_KEY + SecurityUtils.getUid(), "1", 3 * 1000L, false);
        if (!getLockSuccess) {
            return Triple.of(false, "ELECTRICITY.0034", "操作频繁");
        }
        
        UserInfo userInfo = userInfoService.queryByUidFromCache(SecurityUtils.getUid());
        if (Objects.isNull(userInfo)) {
            log.warn("BATTERY DEPOSIT WARN! not found user,uid={}", SecurityUtils.getUid());
            return Triple.of(false, "ELECTRICITY.0019", "未找到用户");
        }
        
        try {
            if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
                log.warn("BATTERY DEPOSIT WARN! user is unUsable,uid={}", userInfo.getUid());
                return Triple.of(false, "ELECTRICITY.0024", "用户已被禁用");
            }
            
            if (!Objects.equals(userInfo.getAuthStatus(), UserInfo.AUTH_STATUS_REVIEW_PASSED)) {
                log.warn("BATTERY DEPOSIT WARN! user not auth,uid={}", userInfo.getUid());
                return Triple.of(false, "ELECTRICITY.0041", "未实名认证");
            }
            
            // 检查是否为自主续费状态
            Boolean userRenewalStatus = enterpriseChannelUserService.checkRenewalStatusByUid(userInfo.getUid());
            if (!userRenewalStatus) {
                log.warn("BATTERY MEMBER ORDER WARN! user renewal status is false, uid={}, mid={}", userInfo.getUid(), query.getMemberId());
                return Triple.of(false, "000088", "您已是渠道用户，请联系对应站点购买套餐");
            }
            
            if (!Objects.equals(userInfo.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_YES)) {
                log.warn("BATTERY DEPOSIT WARN! user not pay deposit,uid={} ", userInfo.getUid());
                return Triple.of(false, "ELECTRICITY.0049", "未缴纳押金");
            }
            
            BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(query.getMemberId());
            if (Objects.isNull(batteryMemberCard)) {
                log.warn("BATTERY DEPOSIT WARN!not found batteryMemberCard,uid={},mid={}", userInfo.getUid(), query.getMemberId());
                return Triple.of(false, "ELECTRICITY.00121", "电池套餐不存在");
            }
            
            ElectricityPayParams electricityPayParams = electricityPayParamsService.queryCacheByTenantIdAndFranchiseeId(tenantId, batteryMemberCard.getFranchiseeId());
            if (Objects.isNull(electricityPayParams)) {
                log.warn("BATTERY DEPOSIT WARN!not found pay params,uid={}", userInfo.getUid());
                return Triple.of(false, "100307", "未配置支付参数!");
            }
            
            UserOauthBind userOauthBind = userOauthBindService.queryUserOauthBySysId(userInfo.getUid(), tenantId);
            if (Objects.isNull(userOauthBind) || Objects.isNull(userOauthBind.getThirdId())) {
                log.warn("BATTERY DEPOSIT WARN!not found useroauthbind or thirdid is null,uid={}", userInfo.getUid());
                return Triple.of(false, "100308", "未找到用户的第三方授权信息!");
            }
            
            UserBatteryDeposit userBatteryDeposit = userBatteryDepositService.selectByUidFromCache(userInfo.getUid());
            if (Objects.isNull(userBatteryDeposit)) {
                log.warn("BATTERY MEMBERCARD REFUND WARN! not found userBatteryDeposit,uid={}", userInfo.getUid());
                return Triple.of(false, "ELECTRICITY.0001", "用户信息不存在");
            }
            
            // 是否有正在进行中的退押
            Integer refundCount = eleRefundOrderService.queryCountByOrderId(userBatteryDeposit.getOrderId(), EleRefundOrder.BATTERY_DEPOSIT_REFUND_ORDER);
            if (refundCount > 0) {
                log.warn("ELE DEPOSIT WARN! have refunding order,uid={}", userInfo.getUid());
                return Triple.of(false, "ELECTRICITY.0047", "电池押金退款中");
            }
            
            // 判断套餐用户分组和用户的用户分组是否匹配
            List<UserInfoGroupNamesBO> userInfoGroupNamesBOs = userInfoGroupDetailService.listGroupByUid(
                    UserInfoGroupDetailQuery.builder().uid(SecurityUtils.getUid()).tenantId(TenantContextHolder.getTenantId()).build());
            
            if (CollectionUtils.isNotEmpty(userInfoGroupNamesBOs)) {
                if (Objects.equals(batteryMemberCard.getGroupType(), BatteryMemberCard.GROUP_TYPE_SYSTEM)) {
                    return Triple.of(false, "100318", "您浏览的套餐已下架，请看看其他的吧");
                }
                
                List<Long> userGroupIds = userInfoGroupNamesBOs.stream().map(UserInfoGroupNamesBO::getGroupId).collect(Collectors.toList());
                userGroupIds.retainAll(JsonUtil.fromJsonArray(batteryMemberCard.getUserInfoGroupIds(), Long.class));
                if (CollectionUtils.isEmpty(userGroupIds)) {
                    return Triple.of(false, "100318", "您浏览的套餐已下架，请看看其他的吧");
                }
            } else {
                if (Objects.equals(batteryMemberCard.getGroupType(), BatteryMemberCard.GROUP_TYPE_USER)) {
                    return Triple.of(false, "100318", "您浏览的套餐已下架，请看看其他的吧");
                }
            }
            
            if (!Objects.equals(BatteryMemberCard.STATUS_UP, batteryMemberCard.getStatus())) {
                log.warn("BATTERY DEPOSIT WARN! batteryMemberCard is disable,uid={},mid={}", userInfo.getUid(), query.getMemberId());
                return Triple.of(false, "100275", "电池套餐不可用");
            }
            
            // 判断套餐租赁状态，用户为老用户，套餐类型为新租，则不支持购买
            if (userInfo.getPayCount() > 0 && BatteryMemberCard.RENT_TYPE_NEW.equals(batteryMemberCard.getRentType())) {
                log.warn("PAY MEMBER CARD AND INSURANCE WARN! The rent type of current package is a new rental package, uid={}, mid={}", userInfo.getUid(), query.getMemberId());
                return Triple.of(false, "100376", "已是平台老用户，无法购买新租类型套餐，请刷新页面重试");
            }
            
            List<BatteryMembercardRefundOrder> batteryMembercardRefundOrders = batteryMembercardRefundOrderService.selectRefundingOrderByUid(userInfo.getUid());
            if (CollectionUtils.isNotEmpty(batteryMembercardRefundOrders)) {
                log.warn("BATTERY DEPOSIT WARN! battery membercard refund review,uid={}", userInfo.getUid());
                return Triple.of(false, "100018", "套餐租金退款审核中");
            }
            
            if (Objects.nonNull(userInfo.getFranchiseeId()) && !Objects.equals(userInfo.getFranchiseeId(), NumberConstant.ZERO_L) && !Objects.equals(userInfo.getFranchiseeId(),
                    batteryMemberCard.getFranchiseeId())) {
                log.warn("BATTERY DEPOSIT WARN! batteryMemberCard franchiseeId not equals,uid={},mid={}", userInfo.getUid(), query.getMemberId());
                return Triple.of(false, "100349", "用户加盟商与套餐加盟商不一致");
            }
            
            // 获取扫码柜机
            ElectricityCabinet electricityCabinet = null;
            if (StringUtils.isNotBlank(query.getProductKey()) && StringUtils.isNotBlank(query.getDeviceName())) {
                electricityCabinet = electricityCabinetService.queryFromCacheByProductAndDeviceName(query.getProductKey(), query.getDeviceName());
            }
            
            // 套餐订单
            Triple<Boolean, String, Object> generateMemberCardOrderResult = generateMemberCardOrder(userInfo, batteryMemberCard, query, electricityCabinet, electricityPayParams);
            if (Boolean.FALSE.equals(generateMemberCardOrderResult.getLeft())) {
                return generateMemberCardOrderResult;
            }
            
            // 保险订单
            Triple<Boolean, String, Object> generateInsuranceOrderResult = generateInsuranceOrder(userInfo, query.getInsuranceId(), electricityCabinet, electricityPayParams);
            if (Boolean.FALSE.equals(generateInsuranceOrderResult.getLeft())) {
                return generateInsuranceOrderResult;
            }
            
            List<String> orderList = new ArrayList<>();
            List<Integer> orderTypeList = new ArrayList<>();
            List<BigDecimal> allPayAmount = new ArrayList<>();
            
            BigDecimal integratedPaAmount = BigDecimal.valueOf(0);
            
            // 保存套餐订单
            if (Boolean.TRUE.equals(generateMemberCardOrderResult.getLeft()) && Objects.nonNull(generateMemberCardOrderResult.getRight())) {
                ElectricityMemberCardOrder electricityMemberCardOrder = (ElectricityMemberCardOrder) generateMemberCardOrderResult.getRight();
                electricityMemberCardOrderService.insert(electricityMemberCardOrder);
                
                orderList.add(electricityMemberCardOrder.getOrderId());
                orderTypeList.add(UnionPayOrder.ORDER_TYPE_MEMBER_CARD);
                allPayAmount.add(electricityMemberCardOrder.getPayAmount());
                integratedPaAmount = integratedPaAmount.add(electricityMemberCardOrder.getPayAmount());
                
                if (CollectionUtils.isNotEmpty(query.getUserCouponIds())) {
                    // 保存订单所使用的优惠券
                    memberCardOrderCouponService.batchInsert(
                            electricityMemberCardOrderService.buildMemberCardOrderCoupon(electricityMemberCardOrder.getOrderId(), query.getUserCouponIds()));
                    // 修改优惠券状态为核销中
                    userCouponService.batchUpdateUserCoupon(electricityMemberCardOrderService.buildUserCouponList(query.getUserCouponIds(), UserCoupon.STATUS_IS_BEING_VERIFICATION,
                            electricityMemberCardOrder.getOrderId()));
                }
            }
            
            // 保存保险订单
            if (Boolean.TRUE.equals(generateInsuranceOrderResult.getLeft()) && Objects.nonNull(generateInsuranceOrderResult.getRight())) {
                InsuranceOrder insuranceOrder = (InsuranceOrder) generateInsuranceOrderResult.getRight();
                insuranceOrderService.insert(insuranceOrder);
                
                orderList.add(insuranceOrder.getOrderId());
                orderTypeList.add(UnionPayOrder.ORDER_TYPE_INSURANCE);
                allPayAmount.add(insuranceOrder.getPayAmount());
                integratedPaAmount = integratedPaAmount.add(insuranceOrder.getPayAmount());
            }
            
            // 处理0元问题
            if (integratedPaAmount.compareTo(BigDecimal.valueOf(0.01)) < 0) {
                
                Triple<Boolean, String, Object> result = handleTotalAmountZero(userInfo, orderList, orderTypeList, null);
                if (Boolean.FALSE.equals(result.getLeft())) {
                    return result;
                }
                
                return Triple.of(true, "", null);
            }
            
            // 非0元查询详情用于调起支付，查询详情会因为证书问题报错，置于0元处理前会干扰其逻辑
            WechatPayParamsDetails wechatPayParamsDetails = wechatPayParamsBizService.getDetailsByIdTenantIdAndFranchiseeId(tenantId, batteryMemberCard.getFranchiseeId());
            
            // 调起支付
            UnionPayOrder unionPayOrder = UnionPayOrder.builder().jsonOrderId(JsonUtil.toJson(orderList)).jsonOrderType(JsonUtil.toJson(orderTypeList))
                    .jsonSingleFee(JsonUtil.toJson(allPayAmount)).payAmount(integratedPaAmount).tenantId(tenantId).attach(UnionTradeOrder.ATTACH_MEMBERCARD_INSURANCE)
                    .description("租电套餐").uid(userInfo.getUid()).build();
            WechatJsapiOrderResultDTO resultDTO = unionTradeOrderService.unionCreateTradeOrderAndGetPayParams(unionPayOrder, wechatPayParamsDetails, userOauthBind.getThirdId(),
                    request, null);
            return Triple.of(true, null, resultDTO);
            
            // 友好提示，对用户端不展示错误信息
        } catch (Exception e) {
            log.error("CREATE UNION_INSURANCE_DEPOSIT_ORDER ERROR! wechat v3 order  error! uid={}", userInfo.getUid(), e);
        } finally {
            //            redisService.delete(CacheConstant.ELE_CACHE_USER_BATTERY_MEMBER_CARD_LOCK_KEY + SecurityUtils.getUid());
        }
        
        return Triple.of(false, "PAY_TRANSFER.0019", "支付未成功，请联系客服处理");
    }
    
    /**
     * 处理混合支付总金额为0的场景
     *
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Triple<Boolean, String, Object> handleTotalAmountZero(UserInfo userInfo, List<String> orderList, List<Integer> orderTypeList, InstallmentRecord installmentRecord) {
        if (Objects.nonNull(installmentRecord)) {
            InstallmentRecord installmentRecordUpdate = new InstallmentRecord();
            installmentRecordUpdate.setId(installmentRecord.getId());
            installmentRecordUpdate.setStatus(INSTALLMENT_RECORD_STATUS_INIT);
            installmentRecordUpdate.setUpdateTime(System.currentTimeMillis());
            installmentRecordService.update(installmentRecordUpdate);
        }
        
        if (CollectionUtils.isEmpty(orderList) || CollectionUtils.isEmpty(orderTypeList)) {
            log.error("ELE UNION BATTERY DEPOSIT ORDER ERROR! orderList is empty,uid={}", userInfo.getUid());
            return Objects.isNull(installmentRecord) ? Triple.of(false, "000001", "系统异常") : Triple.of(true, null, null);
        }
        
        // 遍历订单类型
        for (Integer orderType : orderTypeList) {
            
            // 电池押金
            if (Objects.equals(orderType, UnionPayOrder.ORDER_TYPE_DEPOSIT)) {
                int index = orderTypeList.indexOf(UnionPayOrder.ORDER_TYPE_DEPOSIT);
                if (index < 0) {
                    log.error("ELE UNION DEPOSIT ORDER ERROR! not found orderType,uid={}", userInfo.getUid());
                    return Triple.of(false, "ELECTRICITY.0099", "租电池押金退款订单类型不存!");
                }
                
                String depositOrderId = orderList.get(index);
                
                unionTradeOrderService.manageDepositOrder(depositOrderId, EleDepositOrder.STATUS_SUCCESS);
            }
            
            // 电池套餐
            if (Objects.equals(orderType, UnionPayOrder.ORDER_TYPE_MEMBER_CARD)) {
                int index = orderTypeList.indexOf(UnionPayOrder.ORDER_TYPE_MEMBER_CARD);
                if (index < 0) {
                    log.error("ELE UNION MEMBERCARD ORDER ERROR! not found orderType,uid={}", userInfo.getUid());
                    return Triple.of(false, "ELECTRICITY.0099", "租电池押金退款订单类型不存!");
                }
                
                String memberCardOrderId = orderList.get(index);
                //                unionTradeOrderService.manageMemberCardOrder(memberCardOrderId, ElectricityMemberCardOrder.STATUS_SUCCESS);
                unionTradeOrderService.manageMemberCardOrderV2(memberCardOrderId, ElectricityMemberCardOrder.STATUS_SUCCESS);
            }
            
            // 电池保险
            if (Objects.equals(orderType, UnionPayOrder.ORDER_TYPE_INSURANCE)) {
                int index = orderTypeList.indexOf(UnionPayOrder.ORDER_TYPE_INSURANCE);
                if (index < 0) {
                    log.error("ELE UNION INSURANCE ORDER ERROR! not found orderType,uid={}", userInfo.getUid());
                    return Triple.of(false, "ELECTRICITY.0099", "租电池押金退款订单类型不存!");
                }
                
                String insuranceOrderId = orderList.get(index);
                unionTradeOrderService.manageInsuranceOrder(insuranceOrderId, InsuranceOrder.STATUS_SUCCESS);
            }
        }
        
        return Triple.of(true, "", "");
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Triple<Boolean, String, Object> payServiceFee(HttpServletRequest request) {
        
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("SERVICE FEE ERROR! not found user ");
            return Triple.of(false, "ELECTRICITY.0001", "未找到用户");
        }
        
        Integer tenantId = TenantContextHolder.getTenantId();
        
        boolean getLockSuccess = redisService.setNx(CacheConstant.ELE_CACHE_SERVICE_FEE_LOCK_KEY + SecurityUtils.getUid(), "1", 60 * 1000L, false);
        if (!getLockSuccess) {
            return Triple.of(false, "ELECTRICITY.0034", "操作频繁，请1分钟后重试");
        }
        
        try {
            
            UserInfo userInfo = userInfoService.queryByUidFromCache(user.getUid());
            if (Objects.isNull(userInfo)) {
                log.warn("SERVICE FEE WARN! not found user,uid={}", user.getUid());
                return Triple.of(false, "ELECTRICITY.0019", "未找到用户");
            }
            
            if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
                log.warn("SERVICE FEE WARN! user is unUsable,uid={}", user.getUid());
                return Triple.of(false, "ELECTRICITY.0024", "用户已被禁用");
            }
            
            if (!Objects.equals(userInfo.getAuthStatus(), UserInfo.AUTH_STATUS_REVIEW_PASSED)) {
                log.warn("SERVICE FEE WARN! user not auth,uid={}", user.getUid());
                return Triple.of(false, "ELECTRICITY.0041", "未实名认证");
            }
            
            WechatPayParamsDetails wechatPayParamsDetails = wechatPayParamsBizService.getDetailsByIdTenantIdAndFranchiseeId(tenantId, userInfo.getFranchiseeId());
            if (Objects.isNull(wechatPayParamsDetails)) {
                log.warn("SERVICE FEE WARN!not found pay params,uid={}", user.getUid());
                return Triple.of(false, "100307", "未配置支付参数!");
            }
            
            UserOauthBind userOauthBind = userOauthBindService.queryUserOauthBySysId(user.getUid(), tenantId);
            if (Objects.isNull(userOauthBind) || Objects.isNull(userOauthBind.getThirdId())) {
                log.warn("SERVICE FEE WARN!not found useroauthbind or thirdid is null,uid={}", user.getUid());
                return Triple.of(false, "100308", "未找到用户的第三方授权信息!");
            }
            
            List<String> orderList = new ArrayList<>();
            List<Integer> orderTypeList = new ArrayList<>();
            List<BigDecimal> allPayAmountList = new ArrayList<>();
            
            BigDecimal totalPayAmount = BigDecimal.valueOf(0);
            
            // 获取电池滞纳金
            handleBatteryServiceFee(userInfo, orderList, orderTypeList, allPayAmountList, wechatPayParamsDetails);
            
            // 处理租车套餐的滞纳金
            handCarSlippage(userInfo, orderList, orderTypeList, allPayAmountList);
            
            if (CollectionUtils.isEmpty(allPayAmountList)) {
                log.warn("SERVICE FEE WARN!allPayAmountList is empty,uid={}", user.getUid());
                return Triple.of(false, "000001", "滞纳金为空!");
            }
            
            // 总滞纳金
            totalPayAmount = allPayAmountList.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
            
            if (totalPayAmount.compareTo(BigDecimal.valueOf(0.01)) < 0) {
                log.warn("payServiceFee failed. totalPayAmount is {}", totalPayAmount);
                return Triple.of(false, "000001", "滞纳金不合法!");
            }
            
            // 调起支付
            UnionPayOrder unionPayOrder = UnionPayOrder.builder().jsonOrderId(JsonUtil.toJson(orderList)).jsonOrderType(JsonUtil.toJson(orderTypeList))
                    .jsonSingleFee(JsonUtil.toJson(allPayAmountList)).payAmount(totalPayAmount).tenantId(tenantId).attach(UnionTradeOrder.ATTACH_SERVUCE_FEE).description("滞纳金")
                    .uid(user.getUid()).build();
            WechatJsapiOrderResultDTO resultDTO = unionTradeOrderService.unionCreateTradeOrderAndGetPayParams(unionPayOrder, wechatPayParamsDetails, userOauthBind.getThirdId(),
                    request, null);
            return Triple.of(true, null, resultDTO);
            
            // 友好提示，对用户端不展示错误信息
        } catch (Exception e) {
            log.error("CREATE UNION SERVICE FEE ERROR! wechat v3 order error! uid={}", user.getUid(), e);
        } finally {
            /// 因缓存锁删除过快，导致重复发起支付。因此注释掉该功能
            // redisService.delete(CacheConstant.ELE_CACHE_SERVICE_FEE_LOCK_KEY + user.getUid());
        }
        
        return Triple.of(false, "PAY_TRANSFER.0019", "支付未成功，请联系客服处理");
    }
    
    @Override
    public R<Object> installmentPayment(InstallmentPayQuery query, HttpServletRequest request) {
        Long uid = SecurityUtils.getUid();
        Integer tenantId = TenantContextHolder.getTenantId();
        
        boolean getLockSuccess = redisService.setNx(String.format(CACHE_INSTALLMENT_PAYMENT_LOCK, uid), "1", 3 * 1000L, false);
        if (!getLockSuccess) {
            return R.fail("ELECTRICITY.0034", "操作频繁");
        }
        
        try {
            UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
            if (Objects.isNull(userInfo)) {
                log.warn("INSTALLMENT PAY WARN! not found user,uid={}", uid);
                return R.fail("ELECTRICITY.0019", "未找到用户");
            }
            
            InstallmentRecord installmentRecord = installmentRecordService.queryRecordWithStatusForUser(uid,
                    Arrays.asList(INSTALLMENT_RECORD_STATUS_INIT, INSTALLMENT_RECORD_STATUS_UN_SIGN, INSTALLMENT_RECORD_STATUS_SIGN, INSTALLMENT_RECORD_STATUS_TERMINATE));
            if (Objects.nonNull(installmentRecord)) {
                return R.fail("301008", "当前有进行中的分期签约，完成或取消当前分期签约后方可续签分期套餐");
            }
            
            if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
                log.warn("INSTALLMENT PAY WARN! user is unUsable,uid={}", uid);
                return R.fail("ELECTRICITY.0024", "用户已被禁用");
            }
            
            if (!Objects.equals(userInfo.getAuthStatus(), UserInfo.AUTH_STATUS_REVIEW_PASSED)) {
                log.warn("INSTALLMENT PAY WARN! user not auth,uid={}", uid);
                return R.fail("ELECTRICITY.0041", "未实名认证");
            }
            
            // 检查是否为自主续费状态
            Boolean userRenewalStatus = enterpriseChannelUserService.checkRenewalStatusByUid(uid);
            if (!userRenewalStatus) {
                log.warn("INSTALLMENT PAY WARN! user renewal status is false, uid={}, mid={}", uid, query.getPackageId());
                return R.fail("000088", "您已是渠道用户，请联系对应站点购买套餐");
            }
            
            ElectricityPayParams electricityPayParams = electricityPayParamsService.queryCacheByTenantIdAndFranchiseeId(tenantId, query.getFranchiseeId());
            if (Objects.isNull(electricityPayParams)) {
                log.warn("INSTALLMENT PAY WARN! not found pay params,uid={}", uid);
                return R.fail("100307", "未配置支付参数!");
            }
            
            UserOauthBind userOauthBind = userOauthBindService.queryUserOauthBySysId(uid, tenantId);
            if (Objects.isNull(userOauthBind) || Objects.isNull(userOauthBind.getThirdId())) {
                log.warn("INSTALLMENT PAY WARN! not found user oauth bind or third id is null,uid={}", uid);
                return R.fail("100308", "未找到用户的第三方授权信息!");
            }
            
            // 换电与租车-车电一体两种处理均使用以下三个对象接收对应处理的结果
            Triple<Boolean, String, Object> saveOrderAndPayResult = null;
            Triple<Boolean, String, Object> insuranceOrderTriple = null;
            Triple<Boolean, String, Object> eleDepositOrderTriple = null;
            Triple<Boolean, String, InstallmentRecord> installmentRecordTriple = null;
            // 分换电与租车做响应的处理
            if (Objects.equals(query.getPackageType(), InstallmentConstants.PACKAGE_TYPE_BATTERY)) {
                // 购买换电套餐
                BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(query.getPackageId());
                if (Objects.isNull(batteryMemberCard)) {
                    log.warn("INSTALLMENT PAY WARN! not found batteryMemberCard,uid={},mid={}", uid, query.getPackageId());
                    return R.fail("ELECTRICITY.00121", "电池套餐不存在");
                }
                
                if (!Objects.equals(BatteryMemberCard.STATUS_UP, batteryMemberCard.getStatus())) {
                    log.warn("INSTALLMENT PAY WARN! batteryMemberCard is disable,uid={},mid={}", uid, query.getPackageId());
                    return R.fail("100275", "电池套餐不可用");
                }
                
                if (Objects.nonNull(userInfo.getFranchiseeId()) && !Objects.equals(userInfo.getFranchiseeId(), NumberConstant.ZERO_L) && !Objects.equals(userInfo.getFranchiseeId(),
                        batteryMemberCard.getFranchiseeId())) {
                    log.warn("INSTALLMENT PAY WARN! batteryMemberCard franchiseeId not equals,uid={},mid={}", uid, query.getPackageId());
                    return R.fail("100349", "用户加盟商与套餐加盟商不一致");
                }
                
                // 校验用户与套餐的分组是否一致
                Triple<Boolean, String, String> checkMemberCardGroup = userInfoService.checkMemberCardGroup(userInfo, batteryMemberCard);
                if (!checkMemberCardGroup.getLeft()) {
                    return R.fail(checkMemberCardGroup.getMiddle(), checkMemberCardGroup.getRight());
                }
                
                // 获取扫码柜机
                ElectricityCabinet electricityCabinet = null;
                if (StringUtils.isNotBlank(query.getProductKey()) && StringUtils.isNotBlank(query.getDeviceName())) {
                    electricityCabinet = electricityCabinetService.queryFromCacheByProductAndDeviceName(query.getProductKey(), query.getDeviceName());
                }
                
                if (Objects.nonNull(electricityCabinet) && !Objects.equals(electricityCabinet.getFranchiseeId(), NumberConstant.ZERO_L) && Objects.nonNull(
                        electricityCabinet.getFranchiseeId()) && !Objects.equals(electricityCabinet.getFranchiseeId(), batteryMemberCard.getFranchiseeId())) {
                    log.warn("INSTALLMENT PAY WARN! batteryMemberCard franchiseeId not equals electricityCabinet,eid={},mid={}", electricityCabinet.getId(),
                            batteryMemberCard.getId());
                    return R.fail("100375", "柜机加盟商与套餐加盟商不一致,请删除小程序后重新进入");
                }
                
                // 生成押金订单
                EleDepositOrder eleDepositOrder = null;
                if (!UserInfo.BATTERY_DEPOSIT_STATUS_YES.equals(userInfo.getBatteryDepositStatus())) {
                    eleDepositOrderTriple = eleDepositOrderService.generateDepositOrder(userInfo, batteryMemberCard, electricityCabinet, electricityPayParams);
                    
                    if (Objects.isNull(eleDepositOrderTriple) || Boolean.FALSE.equals(eleDepositOrderTriple.getLeft())) {
                        log.info("INSTALLMENT PAY INFO! generate eleDepositOrder record fail, uid={}", uid);
                        return R.fail("301001", "购买分期套餐失败，请联系管理员");
                    }
                    eleDepositOrder = (EleDepositOrder) eleDepositOrderTriple.getRight();
                }
                
                // 生成保险订单
                InsuranceOrder insuranceOrder = null;
                if (Objects.nonNull(query.getInsuranceId())) {
                    insuranceOrderTriple = insuranceOrderService.generateInsuranceOrder(userInfo, query.getInsuranceId(), electricityCabinet, electricityPayParams);
                    
                    if (Objects.isNull(insuranceOrderTriple) || Boolean.FALSE.equals(insuranceOrderTriple.getLeft())) {
                        log.info("INSTALLMENT PAY INFO! generate insuranceOrder record fail, uid={}", uid);
                        return R.fail("301001", "购买分期套餐失败，请联系管理员");
                    }
                    insuranceOrder = (InsuranceOrder) insuranceOrderTriple.getRight();
                }
                
                // 生成分期签约记录
                installmentRecordTriple = installmentRecordService.generateInstallmentRecord(query, batteryMemberCard, null, userInfo);
                if (Objects.isNull(installmentRecordTriple) || Boolean.FALSE.equals(installmentRecordTriple.getLeft()) || Objects.isNull(installmentRecordTriple.getRight())) {
                    log.info("INSTALLMENT PAY INFO! generate installment record fail, uid={}", uid);
                    return R.fail("301001", "购买分期套餐失败，请联系管理员");
                }
                
                // 生成一期子套餐订单
                Triple<Boolean, String, ElectricityMemberCardOrder> memberCardOrderTriple = electricityMemberCardOrderService.generateInstallmentMemberCardOrder(userInfo,
                        batteryMemberCard, electricityCabinet, installmentRecordTriple.getRight());
                if (Objects.isNull(memberCardOrderTriple) || Boolean.FALSE.equals(memberCardOrderTriple.getLeft()) || Objects.isNull(memberCardOrderTriple.getRight())) {
                    log.info("INSTALLMENT PAY INFO! generate memberCardOrder fail, uid={}", uid);
                    return R.fail("301001", "购买分期套餐失败，请联系管理员");
                }
                
                // 保存相关订单并调起支付
                saveOrderAndPayResult = applicationContext.getBean(TradeOrderServiceImpl.class)
                        .saveOrderAndPay(eleDepositOrder, insuranceOrder, installmentRecordTriple.getRight(), memberCardOrderTriple.getRight(), batteryMemberCard, userOauthBind,
                                userInfo, request);
                
                // TODO SJP 自动取消签约时间目前设置5分钟，上线时设置三天后的当前时刻减去2分钟
                double score = (double) Instant.now().plus(5, ChronoUnit.MINUTES).minus(2, ChronoUnit.MINUTES).toEpochMilli();
                redisService.zsetAddString(CACHE_INSTALLMENT_CANCEL_SIGN, installmentRecordTriple.getRight().getExternalAgreementNo(), score);
                log.info("取消签约定时任务调试，存入请求签约号1，externalAgreementNo={}，score={}", installmentRecordTriple.getRight().getExternalAgreementNo(), score);
                
            }// 购买租车、车电一体套餐在此处扩展else代码块
            
            // 根据支付调用结果返回
            if (Objects.nonNull(saveOrderAndPayResult)) {
                return saveOrderAndPayResult.getLeft() ? R.ok(saveOrderAndPayResult.getRight()) : R.fail("301001", "购买分期套餐失败，请联系管理员");
            }
        } catch (Exception e) {
            log.error("INSTALLMENT PAY ERROR! uid={}", uid, e);
        }
        
        return R.fail("301001", "购买分期套餐失败，请联系管理员");
    }
    
    public Triple<Boolean, String, Object> saveOrderAndPay(EleDepositOrder eleDepositOrder, InsuranceOrder insuranceOrder, InstallmentRecord installmentRecord,
            ElectricityMemberCardOrder memberCardOrder, BatteryMemberCard batteryMemberCard, UserOauthBind userOauthBind, UserInfo userInfo, HttpServletRequest request)
            throws WechatPayException {
        List<String> orderList = new ArrayList<>();
        List<Integer> orderTypeList = new ArrayList<>();
        List<BigDecimal> payAmountList = new ArrayList<>();
        
        BigDecimal totalAmount = BigDecimal.valueOf(0);
        
        if (Objects.nonNull(eleDepositOrder)) {
            // 保存押金订单
            eleDepositOrderService.insert(eleDepositOrder);
            
            orderList.add(eleDepositOrder.getOrderId());
            orderTypeList.add(UnionPayOrder.ORDER_TYPE_DEPOSIT);
            payAmountList.add(eleDepositOrder.getPayAmount());
            totalAmount = totalAmount.add(eleDepositOrder.getPayAmount());
        }
        
        if (Objects.nonNull(insuranceOrder)) {
            // 保存保险订单
            insuranceOrderService.insert(insuranceOrder);
            
            orderList.add(insuranceOrder.getOrderId());
            orderTypeList.add(UnionPayOrder.ORDER_TYPE_INSURANCE);
            payAmountList.add(insuranceOrder.getPayAmount());
            totalAmount = totalAmount.add(insuranceOrder.getPayAmount());
        }
        
        // 保存签约记录
        installmentRecordService.insert(installmentRecord);
        
        // 保存一期套餐订单
        electricityMemberCardOrderService.insert(memberCardOrder);
        
        // 计算服务费并设置ElectricityTradeOrder的相关数据
        if (batteryMemberCard.getInstallmentServiceFee().compareTo(BigDecimal.valueOf(0.01)) >= 0) {
            orderList.add(OrderIdUtil.generateBusinessOrderId(BusinessType.INSTALLMENT_SERVICE_FEE, userInfo.getUid()));
            orderTypeList.add(UnionPayOrder.ORDER_TYPE_INSTALLMENT_SERVICE_FEE);
            payAmountList.add(batteryMemberCard.getInstallmentServiceFee());
            totalAmount = totalAmount.add(batteryMemberCard.getInstallmentServiceFee());
        }
        
        // 处理0元问题
        if (totalAmount.compareTo(BigDecimal.valueOf(0.01)) < 0) {
            Triple<Boolean, String, Object> result = handleTotalAmountZero(userInfo, orderList, orderTypeList, installmentRecord);
            if (Boolean.FALSE.equals(result.getLeft())) {
                return result;
            }
            return Triple.of(true, "", null);
        }
        
        // 非0元查询详情用于调起支付，查询详情会因为证书问题报错，置于0元处理前会干扰其逻辑
        WechatPayParamsDetails wechatPayParamsDetails = wechatPayParamsBizService.getDetailsByIdTenantIdAndFranchiseeId(userInfo.getTenantId(),
                batteryMemberCard.getFranchiseeId());
        
        // 调起支付
        UnionPayOrder unionPayOrder = UnionPayOrder.builder().jsonOrderId(JsonUtil.toJson(orderList)).jsonOrderType(JsonUtil.toJson(orderTypeList))
                .jsonSingleFee(JsonUtil.toJson(payAmountList)).payAmount(totalAmount).tenantId(userInfo.getTenantId()).attach(UnionTradeOrder.ATTACH_INSTALLMENT)
                .description("购买分期套餐").uid(userInfo.getUid()).build();
        WechatJsapiOrderResultDTO resultDTO = unionTradeOrderService.unionCreateTradeOrderAndGetPayParams(unionPayOrder, wechatPayParamsDetails, userOauthBind.getThirdId(),
                request, installmentRecord.getExternalAgreementNo());
        return Triple.of(true, null, resultDTO);
    }
    
    /**
     * 处理车辆的滞纳金
     *
     * @param userInfo
     * @param orderList
     * @param orderTypeList
     * @param allPayAmountList
     */
    private void handCarSlippage(UserInfo userInfo, List<String> orderList, List<Integer> orderTypeList, List<BigDecimal> allPayAmountList) {
        Integer tenantId = userInfo.getTenantId();
        Long uid = userInfo.getUid();
        // 车辆滞纳金相关逻辑
        List<CarRentalPackageOrderSlippagePo> slippageEntityList = carRentalPackageOrderSlippageService.selectUnPayByByUid(tenantId, uid);
        if (!CollectionUtils.isEmpty(slippageEntityList)) {
            for (CarRentalPackageOrderSlippagePo slippageEntity : slippageEntityList) {
                long now = System.currentTimeMillis();
                // 结束时间，不为空
                if (ObjectUtils.isNotEmpty(slippageEntity.getLateFeeEndTime())) {
                    now = slippageEntity.getLateFeeEndTime();
                }
                
                // 时间比对
                long lateFeeStartTime = slippageEntity.getLateFeeStartTime();
                
                // 转换天
                long diffDay = DateUtils.diffDay(lateFeeStartTime, now);
                // 计算滞纳金金额
                BigDecimal amount = NumberUtil.mul(diffDay, slippageEntity.getLateFee());
                
                // 赋值计算
                orderList.add(slippageEntity.getOrderNo());
                orderTypeList.add(ServiceFeeEnum.CAR_SLIPPAGE.getCode());
                allPayAmountList.add(amount);
            }
        }
    }
    
    private Triple<Boolean, String, Object> handleBatteryServiceFee(UserInfo userInfo, List<String> orderList, List<Integer> orderTypeList, List<BigDecimal> allPayAmount,
            WechatPayParamsDetails wechatPayParamsDetails) {
        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(userInfo.getUid());
        if (Objects.isNull(userBatteryMemberCard) || Objects.isNull(userBatteryMemberCard.getMemberCardExpireTime()) || Objects.isNull(
                userBatteryMemberCard.getRemainingNumber())) {
            log.warn("SERVICE FEE WARN! user haven't memberCard uid={}", userInfo.getUid());
            return Triple.of(false, "100210", "用户未开通套餐");
        }
        
        Franchisee franchisee = franchiseeService.queryByIdFromCache(userInfo.getFranchiseeId());
        if (Objects.isNull(franchisee)) {
            log.warn("SERVICE FEE WARN! not found user UID={}", userInfo.getUid());
            return Triple.of(false, "ELECTRICITY.0038", "未找到加盟商");
        }
        
        BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(userBatteryMemberCard.getMemberCardId());
        if (Objects.isNull(batteryMemberCard)) {
            log.warn("SERVICE FEE WARN! memberCard  is not exit,uid={},memberCardId={}", userInfo.getUid(), userBatteryMemberCard.getMemberCardId());
            return Triple.of(false, "ELECTRICITY.00121", "套餐不存在");
        }
        
        ServiceFeeUserInfo serviceFeeUserInfo = serviceFeeUserInfoService.queryByUidFromCache(userInfo.getUid());
        if (Objects.isNull(serviceFeeUserInfo)) {
            log.warn("SERVICE FEE WARN! not found user,uid={}", userInfo.getUid());
            return Triple.of(false, "100247", "用户信息不存在");
        }
        
        // 套餐过期电池服务费
        if (Objects.equals(userInfo.getBatteryRentStatus(), UserInfo.BATTERY_RENT_STATUS_YES) && (
                Objects.equals(userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_NOT_DISABLE) || Objects.equals(
                        userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_DISABLE_REVIEW_REFUSE))
                && userBatteryMemberCard.getMemberCardExpireTime() + 24 * 60 * 60 * 1000L < System.currentTimeMillis()) {
            // 1.获取滞纳金订单
            EleBatteryServiceFeeOrder eleBatteryServiceFeeOrder;
            if (StringUtils.isBlank(serviceFeeUserInfo.getExpireOrderNo())) {// 兼容2.0版本小程序
                // 用户绑定的电池型号
                List<String> userBatteryTypes = userBatteryTypeService.selectByUid(userInfo.getUid());
                Set<String> batteryTypeSet = null;
                if (CollectionUtils.isNotEmpty(userBatteryTypes)) {
                    batteryTypeSet = new HashSet<>(userBatteryTypes);
                }
                
                // 生成滞纳金订单
                ElectricityBattery electricityBattery = electricityBatteryService.queryByUid(userInfo.getUid());
                eleBatteryServiceFeeOrder = EleBatteryServiceFeeOrder.builder().orderId(OrderIdUtil.generateBusinessOrderId(BusinessType.BATTERY_STAGNATE, userInfo.getUid()))
                        .uid(userInfo.getUid()).phone(userInfo.getPhone()).name(userInfo.getName()).payAmount(BigDecimal.ZERO).status(EleDepositOrder.STATUS_INIT)
                        .batteryServiceFeeGenerateTime(userBatteryMemberCard.getMemberCardExpireTime()).createTime(System.currentTimeMillis())
                        .updateTime(System.currentTimeMillis()).tenantId(userInfo.getTenantId()).source(EleBatteryServiceFeeOrder.MEMBER_CARD_OVERDUE)
                        .franchiseeId(franchisee.getId()).storeId(userInfo.getStoreId()).modelType(franchisee.getModelType())
                        .batteryType(CollectionUtils.isEmpty(batteryTypeSet) ? "" : JsonUtil.toJson(batteryTypeSet))
                        .sn(Objects.isNull(electricityBattery) ? "" : electricityBattery.getSn()).batteryServiceFee(batteryMemberCard.getServiceCharge())
                        .paramFranchiseeId(wechatPayParamsDetails.getFranchiseeId()).wechatMerchantId(wechatPayParamsDetails.getWechatMerchantId()).build();
                batteryServiceFeeOrderService.insert(eleBatteryServiceFeeOrder);
                
                // 将滞纳金订单与用户绑定
                ServiceFeeUserInfo serviceFeeUserInfoUpdate = new ServiceFeeUserInfo();
                serviceFeeUserInfoUpdate.setUid(userInfo.getUid());
                serviceFeeUserInfoUpdate.setExpireOrderNo(eleBatteryServiceFeeOrder.getOrderId());
                serviceFeeUserInfoUpdate.setUpdateTime(System.currentTimeMillis());
                serviceFeeUserInfoService.updateByUid(serviceFeeUserInfoUpdate);
            } else {
                eleBatteryServiceFeeOrder = batteryServiceFeeOrderService.selectByOrderNo(serviceFeeUserInfo.getExpireOrderNo());
                if (Objects.isNull(eleBatteryServiceFeeOrder)) {
                    log.warn("SERVICE FEE WARN! not found disableMembercard eleBatteryServiceFeeOrder,uid={}", userInfo.getUid());
                    return Triple.of(false, "ELECTRICITY.0015", "滞纳金订单不存在");
                }
            }
            
            // 2.计算套餐过期电池服务费
            BigDecimal expireBatteryServiceFee = BigDecimal.ZERO;
            // 是否存在套餐过期电池服务费
            if (System.currentTimeMillis() - (userBatteryMemberCard.getMemberCardExpireTime() + 24 * 60 * 60 * 1000L) > 0) {
                int batteryMemebercardExpireDays = (int) Math.ceil(
                        (System.currentTimeMillis() - (userBatteryMemberCard.getMemberCardExpireTime() + 24 * 60 * 60 * 1000L)) / 1000.0 / 60 / 60 / 24);
                expireBatteryServiceFee = batteryMemberCard.getServiceCharge().multiply(BigDecimal.valueOf(batteryMemebercardExpireDays));
                log.info("BATTERY SERVICE FEE INFO!user exist expire fee,uid={},fee={}", userInfo.getUid(), expireBatteryServiceFee.doubleValue());
            }
            
            // 更新滞纳金订单金额
            EleBatteryServiceFeeOrder eleBatteryServiceFeeOrderUpdate = new EleBatteryServiceFeeOrder();
            eleBatteryServiceFeeOrderUpdate.setId(eleBatteryServiceFeeOrder.getId());
            eleBatteryServiceFeeOrderUpdate.setPayAmount(expireBatteryServiceFee);
            eleBatteryServiceFeeOrderUpdate.setUpdateTime(System.currentTimeMillis());
            eleBatteryServiceFeeOrderUpdate.setParamFranchiseeId(wechatPayParamsDetails.getFranchiseeId());
            eleBatteryServiceFeeOrderUpdate.setWechatMerchantId(wechatPayParamsDetails.getWechatMerchantId());
            batteryServiceFeeOrderService.update(eleBatteryServiceFeeOrderUpdate);
            
            orderList.add(eleBatteryServiceFeeOrder.getOrderId());
            orderTypeList.add(ServiceFeeEnum.BATTERY_EXPIRE.getCode());
            allPayAmount.add(expireBatteryServiceFee);
            
            log.info("BATTERY SERVICE FEE INFO!user exist battery expire fee,uid={},fee={}", userInfo.getUid(), expireBatteryServiceFee.doubleValue());
        }
        
        // 暂停套餐电池服务费
        if (Objects.equals(userInfo.getBatteryRentStatus(), UserInfo.BATTERY_RENT_STATUS_YES) && StringUtils.isNotBlank(serviceFeeUserInfo.getDisableMemberCardNo())) {
            // 获取滞纳金订单
            EleBatteryServiceFeeOrder eleBatteryServiceFeeOrder = null;
            
            BigDecimal pauseBatteryServiceFee = BigDecimal.ZERO;
            
            // 1旧版小程序  停卡，不存在滞纳金订单
            if (Objects.equals(userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_DISABLE) && StringUtils.isBlank(
                    serviceFeeUserInfo.getPauseOrderNo())) {
                // 计算暂停套餐电池服务费
                int batteryMembercardDisableDays = (int) Math.ceil((System.currentTimeMillis() - userBatteryMemberCard.getDisableMemberCardTime()) / 1000.0 / 60 / 60 / 24);
                pauseBatteryServiceFee = batteryMemberCard.getServiceCharge().multiply(BigDecimal.valueOf(batteryMembercardDisableDays));
                log.info("BATTERY SERVICE FEE INFO!user exist pause fee,uid={},fee={}", userInfo.getUid(), pauseBatteryServiceFee.doubleValue());
                
                // 生成滞纳金订单
                ElectricityBattery electricityBattery = electricityBatteryService.queryByUid(userInfo.getUid());
                
                List<String> userBatteryTypes = userBatteryTypeService.selectByUid(userInfo.getUid());
                Set<String> batteryTypeSet = null;
                if (CollectionUtils.isNotEmpty(userBatteryTypes)) {
                    batteryTypeSet = new HashSet<>(userBatteryTypes);
                }
                
                eleBatteryServiceFeeOrder = EleBatteryServiceFeeOrder.builder().orderId(OrderIdUtil.generateBusinessOrderId(BusinessType.BATTERY_STAGNATE, userInfo.getUid()))
                        .uid(userInfo.getUid()).phone(userInfo.getPhone()).name(userInfo.getName()).payAmount(pauseBatteryServiceFee).status(EleDepositOrder.STATUS_INIT)
                        .createTime(System.currentTimeMillis()).updateTime(System.currentTimeMillis()).tenantId(userInfo.getTenantId())
                        .source(EleBatteryServiceFeeOrder.MEMBER_CARD_OVERDUE).franchiseeId(franchisee.getId()).storeId(userInfo.getStoreId()).modelType(franchisee.getModelType())
                        .batteryType(CollectionUtils.isEmpty(batteryTypeSet) ? "" : JsonUtil.toJson(batteryTypeSet))
                        .sn(Objects.isNull(electricityBattery) ? "" : electricityBattery.getSn()).batteryServiceFee(batteryMemberCard.getServiceCharge())
                        .paramFranchiseeId(wechatPayParamsDetails.getFranchiseeId()).wechatMerchantId(wechatPayParamsDetails.getWechatMerchantId()).build();
                batteryServiceFeeOrderService.insert(eleBatteryServiceFeeOrder);
                
                // 将滞纳金订单与用户绑定
                ServiceFeeUserInfo serviceFeeUserInfoUpdate = new ServiceFeeUserInfo();
                serviceFeeUserInfoUpdate.setUid(userInfo.getUid());
                serviceFeeUserInfoUpdate.setPauseOrderNo(eleBatteryServiceFeeOrder.getOrderId());
                serviceFeeUserInfoUpdate.setUpdateTime(System.currentTimeMillis());
                serviceFeeUserInfoService.updateByUid(serviceFeeUserInfoUpdate);
            }
            
            // 2 新版小程序  停卡，存在滞纳金订单
            if (Objects.equals(userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_DISABLE) && StringUtils.isNotBlank(
                    serviceFeeUserInfo.getPauseOrderNo())) {
                // 计算暂停套餐电池服务费
                int batteryMembercardDisableDays = (int) Math.ceil((System.currentTimeMillis() - userBatteryMemberCard.getDisableMemberCardTime()) / 1000.0 / 60 / 60 / 24);
                pauseBatteryServiceFee = batteryMemberCard.getServiceCharge().multiply(BigDecimal.valueOf(batteryMembercardDisableDays));
                log.info("BATTERY SERVICE FEE INFO!user exist pause fee,uid={},fee={}", userInfo.getUid(), pauseBatteryServiceFee.doubleValue());
                
                eleBatteryServiceFeeOrder = batteryServiceFeeOrderService.selectByOrderNo(serviceFeeUserInfo.getPauseOrderNo());
                if (Objects.isNull(eleBatteryServiceFeeOrder)) {
                    log.warn("SERVICE FEE WARN! not found disableMembercard eleBatteryServiceFeeOrder,uid={}", userInfo.getUid());
                    return Triple.of(false, "ELECTRICITY.0015", "滞纳金订单不存在");
                }
                
                EleBatteryServiceFeeOrder eleBatteryServiceFeeOrderUpdate = new EleBatteryServiceFeeOrder();
                eleBatteryServiceFeeOrderUpdate.setId(eleBatteryServiceFeeOrder.getId());
                eleBatteryServiceFeeOrderUpdate.setPayAmount(pauseBatteryServiceFee);
                eleBatteryServiceFeeOrderUpdate.setUpdateTime(System.currentTimeMillis());
                eleBatteryServiceFeeOrderUpdate.setParamFranchiseeId(wechatPayParamsDetails.getFranchiseeId());
                eleBatteryServiceFeeOrderUpdate.setWechatMerchantId(wechatPayParamsDetails.getWechatMerchantId());
                batteryServiceFeeOrderService.update(eleBatteryServiceFeeOrderUpdate);
            }
            
            // 3 新版小程序 套餐已启用  存在滞纳金订单
            if (Objects.equals(userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_NOT_DISABLE) && StringUtils.isNotBlank(
                    serviceFeeUserInfo.getPauseOrderNo())) {
                // 获取滞纳金订单
                eleBatteryServiceFeeOrder = batteryServiceFeeOrderService.selectByOrderNo(serviceFeeUserInfo.getPauseOrderNo());
                if (Objects.isNull(eleBatteryServiceFeeOrder)) {
                    log.warn("SERVICE FEE WARN! not found disableMembercard eleBatteryServiceFeeOrder,uid={}", userInfo.getUid());
                    return Triple.of(false, "ELECTRICITY.0015", "滞纳金订单不存在");
                }
                
                pauseBatteryServiceFee = eleBatteryServiceFeeOrder.getPayAmount();
                
                eleBatteryServiceFeeOrder.setParamFranchiseeId(wechatPayParamsDetails.getFranchiseeId());
                eleBatteryServiceFeeOrder.setWechatMerchantId(wechatPayParamsDetails.getWechatMerchantId());
                batteryServiceFeeOrderService.update(eleBatteryServiceFeeOrder);
                log.info("BATTERY SERVICE FEE INFO!user exist pauseBatteryServiceFee,uid={},fee={}", userInfo.getUid(), pauseBatteryServiceFee.doubleValue());
            }
            
            if (Objects.nonNull(eleBatteryServiceFeeOrder)) {
                orderList.add(eleBatteryServiceFeeOrder.getOrderId());
                orderTypeList.add(ServiceFeeEnum.BATTERY_PAUSE.getCode());
                allPayAmount.add(pauseBatteryServiceFee);
            }
        }
        
        return Triple.of(true, null, null);
    }
    
    private Triple<Boolean, String, Object> generateDepositOrder(UserInfo userInfo, BatteryMemberCard batteryMemberCard, ElectricityCabinet electricityCabinet,
            ElectricityPayParams electricityPayParams) {
        
        // 生成押金独立订单
        String depositOrderId = OrderIdUtil.generateBusinessOrderId(BusinessType.BATTERY_DEPOSIT, userInfo.getUid());
        EleDepositOrder eleDepositOrder = EleDepositOrder.builder().orderId(depositOrderId).uid(userInfo.getUid()).phone(userInfo.getPhone()).name(userInfo.getName())
                .payAmount(batteryMemberCard.getDeposit()).status(EleDepositOrder.STATUS_INIT).createTime(System.currentTimeMillis()).updateTime(System.currentTimeMillis())
                .tenantId(userInfo.getTenantId()).franchiseeId(batteryMemberCard.getFranchiseeId()).payType(EleDepositOrder.ONLINE_PAYMENT)
                .storeId(Objects.nonNull(electricityCabinet) ? electricityCabinet.getStoreId() : userInfo.getStoreId()).mid(batteryMemberCard.getId()).modelType(0)
                .paramFranchiseeId(electricityPayParams.getFranchiseeId()).wechatMerchantId(electricityPayParams.getWechatMerchantId()).build();
        
        return Triple.of(true, null, eleDepositOrder);
    }
    
    private Triple<Boolean, String, Object> generateMemberCardOrder(UserInfo userInfo, BatteryMemberCard batteryMemberCard, IntegratedPaymentAdd integratedPaymentAdd,
            Set<Integer> userCouponIds, ElectricityCabinet electricityCabinet, ElectricityPayParams electricityPayParams) {
        
        // 多加盟商版本增加：加盟商一致性校验
        // 查找计算优惠券
        // 计算优惠后支付金额
        Triple<Boolean, String, Object> calculatePayAmountResult = electricityMemberCardOrderService.calculatePayAmount(batteryMemberCard, userCouponIds);
        if (Boolean.FALSE.equals(calculatePayAmountResult.getLeft())) {
            return calculatePayAmountResult;
        }
        BigDecimal payAmount = (BigDecimal) calculatePayAmountResult.getRight();
        
        // 支付金额不能为负数
        if (payAmount.compareTo(BigDecimal.valueOf(0.01)) < 0) {
            payAmount = BigDecimal.valueOf(0);
        }
        
        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(userInfo.getUid());
        Integer payCount = electricityMemberCardOrderService.queryMaxPayCount(userBatteryMemberCard);
        
        ElectricityMemberCardOrder electricityMemberCardOrder = new ElectricityMemberCardOrder();
        electricityMemberCardOrder.setOrderId(OrderIdUtil.generateBusinessOrderId(BusinessType.BATTERY_MEMBERCARD, userInfo.getUid()));
        electricityMemberCardOrder.setCreateTime(System.currentTimeMillis());
        electricityMemberCardOrder.setUpdateTime(System.currentTimeMillis());
        electricityMemberCardOrder.setStatus(ElectricityMemberCardOrder.STATUS_INIT);
        electricityMemberCardOrder.setMemberCardId(integratedPaymentAdd.getMemberCardId());
        electricityMemberCardOrder.setUid(userInfo.getUid());
        electricityMemberCardOrder.setMaxUseCount(batteryMemberCard.getUseCount());
        electricityMemberCardOrder.setCardName(batteryMemberCard.getName());
        electricityMemberCardOrder.setPayAmount(payAmount);
        electricityMemberCardOrder.setUserName(userInfo.getName());
        electricityMemberCardOrder.setValidDays(batteryMemberCard.getValidDays());
        electricityMemberCardOrder.setTenantId(batteryMemberCard.getTenantId());
        electricityMemberCardOrder.setFranchiseeId(batteryMemberCard.getFranchiseeId());
        electricityMemberCardOrder.setPayCount(payCount);
        electricityMemberCardOrder.setSendCouponId(Objects.nonNull(batteryMemberCard.getCouponId()) ? batteryMemberCard.getCouponId().longValue() : null);
        electricityMemberCardOrder.setRefId(Objects.nonNull(electricityCabinet) ? electricityCabinet.getId().longValue() : null);
        electricityMemberCardOrder.setSource(Objects.nonNull(electricityCabinet) ? ElectricityMemberCardOrder.SOURCE_SCAN : ElectricityMemberCardOrder.SOURCE_NOT_SCAN);
        electricityMemberCardOrder.setStoreId(Objects.nonNull(electricityCabinet) ? electricityCabinet.getStoreId() : userInfo.getStoreId());
        electricityMemberCardOrder.setCouponIds(batteryMemberCard.getCouponIds());
        electricityMemberCardOrder.setParamFranchiseeId(electricityPayParams.getFranchiseeId());
        electricityMemberCardOrder.setWechatMerchantId(electricityPayParams.getWechatMerchantId());
        
        return Triple.of(true, null, electricityMemberCardOrder);
    }
    
    public Triple<Boolean, String, Object> generateMemberCardOrder(UserInfo userInfo, BatteryMemberCard batteryMemberCard, BatteryMemberCardAndInsuranceQuery query,
            ElectricityCabinet electricityCabinet, ElectricityPayParams electricityPayParams) {
        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(userInfo.getUid());
        
        Triple<Boolean, Integer, BigDecimal> acquireUserBatteryServiceFeeResult = serviceFeeUserInfoService.acquireUserBatteryServiceFee(userInfo, userBatteryMemberCard,
                batteryMemberCard, serviceFeeUserInfoService.queryByUidFromCache(userInfo.getUid()));
        if (Boolean.TRUE.equals(acquireUserBatteryServiceFeeResult.getLeft())) {
            log.warn("BATTERY MEMBER ORDER WARN! user exist battery service fee,uid={},mid={}", userInfo.getUid(), query.getMemberId());
            return Triple.of(false, "ELECTRICITY.100000", acquireUserBatteryServiceFeeResult.getRight());
        }
        
        // 多加盟商版本增加：加盟商一致性校验
        // 查找计算优惠券
        // 计算优惠后支付金额
        Triple<Boolean, String, Object> calculatePayAmountResult = electricityMemberCardOrderService.calculatePayAmount(batteryMemberCard, query.getUserCouponIds());
        if (Boolean.FALSE.equals(calculatePayAmountResult.getLeft())) {
            return calculatePayAmountResult;
        }
        BigDecimal payAmount = (BigDecimal) calculatePayAmountResult.getRight();
        
        // 支付金额不能为负数
        if (payAmount.compareTo(BigDecimal.valueOf(0.01)) < 0) {
            payAmount = BigDecimal.valueOf(0);
        }
        
        Integer payCount = electricityMemberCardOrderService.queryMaxPayCount(userBatteryMemberCard);
        
        ElectricityMemberCardOrder electricityMemberCardOrder = new ElectricityMemberCardOrder();
        electricityMemberCardOrder.setOrderId(OrderIdUtil.generateBusinessOrderId(BusinessType.BATTERY_MEMBERCARD, userInfo.getUid()));
        electricityMemberCardOrder.setCreateTime(System.currentTimeMillis());
        electricityMemberCardOrder.setUpdateTime(System.currentTimeMillis());
        electricityMemberCardOrder.setStatus(ElectricityMemberCardOrder.STATUS_INIT);
        electricityMemberCardOrder.setMemberCardId(query.getMemberId());
        electricityMemberCardOrder.setUid(userInfo.getUid());
        electricityMemberCardOrder.setMaxUseCount(batteryMemberCard.getUseCount());
        electricityMemberCardOrder.setCardName(batteryMemberCard.getName());
        electricityMemberCardOrder.setPayAmount(payAmount);
        electricityMemberCardOrder.setUserName(userInfo.getName());
        electricityMemberCardOrder.setValidDays(batteryMemberCard.getValidDays());
        electricityMemberCardOrder.setTenantId(batteryMemberCard.getTenantId());
        electricityMemberCardOrder.setFranchiseeId(userInfo.getFranchiseeId());
        electricityMemberCardOrder.setPayCount(payCount);
        electricityMemberCardOrder.setSendCouponId(Objects.nonNull(batteryMemberCard.getCouponId()) ? batteryMemberCard.getCouponId().longValue() : null);
        electricityMemberCardOrder.setRefId(Objects.nonNull(electricityCabinet) ? electricityCabinet.getId().longValue() : null);
        electricityMemberCardOrder.setSource(Objects.nonNull(electricityCabinet) ? ElectricityMemberCardOrder.SOURCE_SCAN : ElectricityMemberCardOrder.SOURCE_NOT_SCAN);
        electricityMemberCardOrder.setStoreId(Objects.nonNull(electricityCabinet) ? electricityCabinet.getStoreId() : userInfo.getStoreId());
        electricityMemberCardOrder.setCouponIds(batteryMemberCard.getCouponIds());
        electricityMemberCardOrder.setParamFranchiseeId(electricityPayParams.getFranchiseeId());
        electricityMemberCardOrder.setWechatMerchantId(electricityPayParams.getWechatMerchantId());
        
        return Triple.of(true, null, electricityMemberCardOrder);
    }
    
    private Triple<Boolean, String, Object> generateInsuranceOrder(UserInfo userInfo, Integer insuranceId, ElectricityCabinet electricityCabinet,
            ElectricityPayParams electricityPayParams) {
        
        if (Objects.isNull(insuranceId)) {
            return Triple.of(true, "", null);
        }
        
        // 查询保险
        FranchiseeInsurance franchiseeInsurance = franchiseeInsuranceService.queryByIdFromCache(insuranceId);
        
        if (Objects.isNull(franchiseeInsurance) || !Objects.equals(franchiseeInsurance.getInsuranceType(), FranchiseeInsurance.INSURANCE_TYPE_BATTERY)) {
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
        
        // 生成保险独立订单
        String insuranceOrderId = OrderIdUtil.generateBusinessOrderId(BusinessType.BATTERY_INSURANCE, userInfo.getUid());
        InsuranceOrder insuranceOrder = InsuranceOrder.builder().insuranceId(franchiseeInsurance.getId()).insuranceName(franchiseeInsurance.getName())
                .insuranceType(franchiseeInsurance.getInsuranceType()).orderId(insuranceOrderId).cid(franchiseeInsurance.getCid())
                .franchiseeId(franchiseeInsurance.getFranchiseeId()).isUse(InsuranceOrder.NOT_USE).payAmount(franchiseeInsurance.getPremium())
                .forehead(franchiseeInsurance.getForehead()).payType(InsuranceOrder.ONLINE_PAY_TYPE).phone(userInfo.getPhone()).status(InsuranceOrder.STATUS_INIT)
                .storeId(Objects.nonNull(electricityCabinet) ? electricityCabinet.getStoreId() : userInfo.getStoreId()).tenantId(userInfo.getTenantId()).uid(userInfo.getUid())
                .userName(userInfo.getName()).validDays(franchiseeInsurance.getValidDays()).createTime(System.currentTimeMillis()).updateTime(System.currentTimeMillis())
                .simpleBatteryType(franchiseeInsurance.getSimpleBatteryType()).paramFranchiseeId(electricityPayParams.getFranchiseeId())
                .wechatMerchantId(electricityPayParams.getWechatMerchantId()).build();
        
        return Triple.of(true, null, insuranceOrder);
    }
    
}
