package com.xiliulou.electricity.service.impl;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.entity.car.CarRentalPackageOrderSlippagePo;
import com.xiliulou.electricity.enums.BusinessType;
import com.xiliulou.electricity.enums.ServiceFeeEnum;
import com.xiliulou.electricity.query.BatteryMemberCardAndInsuranceQuery;
import com.xiliulou.electricity.query.IntegratedPaymentAdd;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.service.car.CarRentalPackageOrderSlippageService;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

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

            BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(integratedPaymentAdd.getMemberCardId());
            if (Objects.isNull(batteryMemberCard)) {
                log.warn("BATTERY DEPOSIT WARN!not found batteryMemberCard,uid={},mid={}", user.getUid(), integratedPaymentAdd.getMemberCardId());
                return Triple.of(false, "ELECTRICITY.00121", "电池套餐不存在");
            }

            if(!Objects.equals( BatteryMemberCard.STATUS_UP, batteryMemberCard.getStatus())){
                log.warn("BATTERY DEPOSIT WARN! batteryMemberCard is disable,uid={},mid={}", user.getUid(), integratedPaymentAdd.getMemberCardId());
                return Triple.of(false, "100275", "电池套餐不可用");
            }

            if(Objects.nonNull(userInfo.getFranchiseeId()) && !Objects.equals(userInfo.getFranchiseeId(),NumberConstant.ZERO_L) && !Objects.equals(userInfo.getFranchiseeId(),batteryMemberCard.getFranchiseeId())){
                log.warn("BATTERY DEPOSIT WARN! batteryMemberCard franchiseeId not equals,uid={},mid={}", user.getUid(), integratedPaymentAdd.getMemberCardId());
                return Triple.of(false, "100349", "用户加盟商与套餐加盟商不一致");
            }

            //获取扫码柜机
            ElectricityCabinet electricityCabinet = null;
            if (StringUtils.isNotBlank(integratedPaymentAdd.getProductKey()) && StringUtils.isNotBlank(integratedPaymentAdd.getDeviceName())) {
                electricityCabinet = electricityCabinetService.queryFromCacheByProductAndDeviceName(integratedPaymentAdd.getProductKey(), integratedPaymentAdd.getDeviceName());
            }

            //押金订单
            Triple<Boolean, String, Object> generateDepositOrderResult = generateDepositOrder(userInfo, batteryMemberCard, electricityCabinet);
            if (Boolean.FALSE.equals(generateDepositOrderResult.getLeft())) {
                return generateDepositOrderResult;
            }

            //套餐订单
            Set<Integer> userCouponIds = electricityMemberCardOrderService.generateUserCouponIds(integratedPaymentAdd.getUserCouponId(), integratedPaymentAdd.getUserCouponIds());
            Triple<Boolean, String, Object> generateMemberCardOrderResult = generateMemberCardOrder(userInfo, batteryMemberCard, integratedPaymentAdd, userCouponIds,electricityCabinet);
            if (Boolean.FALSE.equals(generateMemberCardOrderResult.getLeft())) {
                return generateMemberCardOrderResult;
            }

            //保险订单
            Triple<Boolean, String, Object> generateInsuranceOrderResult = generateInsuranceOrder(userInfo, integratedPaymentAdd.getInsuranceId(), electricityCabinet);
            if (Boolean.FALSE.equals(generateInsuranceOrderResult.getLeft())) {
                return generateInsuranceOrderResult;
            }

            List<String> orderList = new ArrayList<>();
            List<Integer> orderTypeList = new ArrayList<>();
            List<BigDecimal> allPayAmount = new ArrayList<>();

            BigDecimal integratedPaAmount = BigDecimal.valueOf(0);

            //保存押金订单
            if (Boolean.TRUE.equals(generateDepositOrderResult.getLeft()) && Objects.nonNull(generateDepositOrderResult.getRight())) {
                EleDepositOrder eleDepositOrder = (EleDepositOrder) generateDepositOrderResult.getRight();
                eleDepositOrderService.insert(eleDepositOrder);

                orderList.add(eleDepositOrder.getOrderId());
                orderTypeList.add(UnionPayOrder.ORDER_TYPE_DEPOSIT);
                allPayAmount.add(eleDepositOrder.getPayAmount());
                integratedPaAmount = integratedPaAmount.add(eleDepositOrder.getPayAmount());
            }

            //保存套餐订单
            if (Boolean.TRUE.equals(generateMemberCardOrderResult.getLeft()) && Objects.nonNull(generateMemberCardOrderResult.getRight())) {
                ElectricityMemberCardOrder electricityMemberCardOrder = (ElectricityMemberCardOrder) generateMemberCardOrderResult.getRight();
                electricityMemberCardOrderService.insert(electricityMemberCardOrder);

                orderList.add(electricityMemberCardOrder.getOrderId());
                orderTypeList.add(UnionPayOrder.ORDER_TYPE_MEMBER_CARD);
                allPayAmount.add(electricityMemberCardOrder.getPayAmount());
                integratedPaAmount = integratedPaAmount.add(electricityMemberCardOrder.getPayAmount());

                if (CollectionUtils.isNotEmpty(userCouponIds)) {
                    //保存订单所使用的优惠券
                    memberCardOrderCouponService.batchInsert(electricityMemberCardOrderService.buildMemberCardOrderCoupon(electricityMemberCardOrder.getOrderId(), userCouponIds));
                    //修改优惠券状态为核销中
                    userCouponService.batchUpdateUserCoupon(electricityMemberCardOrderService.buildUserCouponList(userCouponIds, UserCoupon.STATUS_IS_BEING_VERIFICATION, electricityMemberCardOrder.getOrderId()));
                }
            }

            //保存保险订单
            if (Boolean.TRUE.equals(generateInsuranceOrderResult.getLeft()) && Objects.nonNull(generateInsuranceOrderResult.getRight())) {
                InsuranceOrder insuranceOrder = (InsuranceOrder) generateInsuranceOrderResult.getRight();
                insuranceOrderService.insert(insuranceOrder);

                orderList.add(insuranceOrder.getOrderId());
                orderTypeList.add(UnionPayOrder.ORDER_TYPE_INSURANCE);
                allPayAmount.add(insuranceOrder.getPayAmount());
                integratedPaAmount = integratedPaAmount.add(insuranceOrder.getPayAmount());
            }


            //处理0元问题
            if (integratedPaAmount.compareTo(BigDecimal.valueOf(0.01)) < 0) {

                Triple<Boolean, String, Object> result = handleTotalAmountZero(userInfo, orderList, orderTypeList);
                if (Boolean.FALSE.equals(result.getLeft())) {
                    return result;
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
                        .attach(UnionTradeOrder.ATTACH_INTEGRATED_PAYMENT)
                        .description("租电押金")
                        .uid(user.getUid()).build();
                WechatJsapiOrderResultDTO resultDTO =
                        unionTradeOrderService.unionCreateTradeOrderAndGetPayParams(unionPayOrder, electricityPayParams, userOauthBind.getThirdId(), request);
                return Triple.of(true, null, resultDTO);
            } catch (WechatPayException e) {
                log.error("CREATE UNION_INSURANCE_DEPOSIT_ORDER ERROR! wechat v3 order  error! uid={}", user.getUid(), e);
            }
        } finally {
            redisService.delete(CacheConstant.ELE_CACHE_USER_DEPOSIT_LOCK_KEY + user.getUid());
        }

        return Triple.of(false, "ELECTRICITY.0099", "租电押金支付失败");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Triple<Boolean, String, Object> payMemberCardAndInsurance(BatteryMemberCardAndInsuranceQuery query, HttpServletRequest request) {
        Integer tenantId = TenantContextHolder.getTenantId();

        boolean getLockSuccess = redisService.setNx(CacheConstant.ELE_CACHE_USER_BATTERY_MEMBER_CARD_LOCK_KEY + SecurityUtils.getUid(), "1", 3 * 1000L, false);
        if (!getLockSuccess) {
            return Triple.of(false, "ELECTRICITY.0034", "操作频繁");
        }

        try {
            UserInfo userInfo = userInfoService.queryByUidFromCache(SecurityUtils.getUid());
            if (Objects.isNull(userInfo)) {
                log.warn("BATTERY DEPOSIT WARN! not found user,uid={}", SecurityUtils.getUid());
                return Triple.of(false, "ELECTRICITY.0019", "未找到用户");
            }

            if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
                log.warn("BATTERY DEPOSIT WARN! user is unUsable,uid={}", userInfo.getUid());
                return Triple.of(false, "ELECTRICITY.0024", "用户已被禁用");
            }

            if (!Objects.equals(userInfo.getAuthStatus(), UserInfo.AUTH_STATUS_REVIEW_PASSED)) {
                log.warn("BATTERY DEPOSIT WARN! user not auth,uid={}", userInfo.getUid());
                return Triple.of(false, "ELECTRICITY.0041", "未实名认证");
            }

            if (!Objects.equals(userInfo.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_YES)) {
                log.warn("BATTERY DEPOSIT WARN! user not pay deposit,uid={} ", userInfo.getUid());
                return Triple.of(false, "ELECTRICITY.0049", "未缴纳押金");
            }

            ElectricityPayParams electricityPayParams = electricityPayParamsService.queryFromCache(tenantId);
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
            if(Objects.isNull(userBatteryDeposit)){
                log.warn("BATTERY MEMBERCARD REFUND WARN! not found userBatteryDeposit,uid={}", userInfo.getUid());
                return Triple.of(false, "ELECTRICITY.0001", "用户信息不存在");
            }

            //是否有正在进行中的退押
            Integer refundCount = eleRefundOrderService.queryCountByOrderId(userBatteryDeposit.getOrderId(), EleRefundOrder.BATTERY_DEPOSIT_REFUND_ORDER);
            if (refundCount > 0) {
                log.warn("ELE DEPOSIT WARN! have refunding order,uid={}", userInfo.getUid());
                return Triple.of(false,"ELECTRICITY.0047", "电池押金退款中");
            }

            BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(query.getMemberId());
            if (Objects.isNull(batteryMemberCard)) {
                log.warn("BATTERY DEPOSIT WARN!not found batteryMemberCard,uid={},mid={}", userInfo.getUid(), query.getMemberId());
                return Triple.of(false, "ELECTRICITY.00121", "电池套餐不存在");
            }

            if(!Objects.equals( BatteryMemberCard.STATUS_UP, batteryMemberCard.getStatus())){
                log.warn("BATTERY DEPOSIT WARN! batteryMemberCard is disable,uid={},mid={}", userInfo.getUid(), query.getMemberId());
                return Triple.of(false, "100275", "电池套餐不可用");
            }

            List<BatteryMembercardRefundOrder> batteryMembercardRefundOrders = batteryMembercardRefundOrderService.selectRefundingOrderByUid(userInfo.getUid());
            if(CollectionUtils.isNotEmpty(batteryMembercardRefundOrders)){
                log.warn("BATTERY DEPOSIT WARN! battery membercard refund review,uid={}", userInfo.getUid());
                return Triple.of(false,"100018", "套餐租金退款审核中");
            }

            if(Objects.nonNull(userInfo.getFranchiseeId()) && !Objects.equals(userInfo.getFranchiseeId(),NumberConstant.ZERO_L) && !Objects.equals(userInfo.getFranchiseeId(),batteryMemberCard.getFranchiseeId())){
                log.warn("BATTERY DEPOSIT WARN! batteryMemberCard franchiseeId not equals,uid={},mid={}", userInfo.getUid(), query.getMemberId());
                return Triple.of(false, "100349", "用户加盟商与套餐加盟商不一致");
            }

            //获取扫码柜机
            ElectricityCabinet electricityCabinet = null;
            if (StringUtils.isNotBlank(query.getProductKey()) && StringUtils.isNotBlank(query.getDeviceName())) {
                electricityCabinet = electricityCabinetService.queryFromCacheByProductAndDeviceName(query.getProductKey(), query.getDeviceName());
            }

            //套餐订单
            Triple<Boolean, String, Object> generateMemberCardOrderResult = generateMemberCardOrder(userInfo, batteryMemberCard, query, electricityCabinet);
            if (Boolean.FALSE.equals(generateMemberCardOrderResult.getLeft())) {
                return generateMemberCardOrderResult;
            }

            //保险订单
            Triple<Boolean, String, Object> generateInsuranceOrderResult = generateInsuranceOrder(userInfo, query.getInsuranceId(), electricityCabinet);
            if (Boolean.FALSE.equals(generateInsuranceOrderResult.getLeft())) {
                return generateInsuranceOrderResult;
            }

            List<String> orderList = new ArrayList<>();
            List<Integer> orderTypeList = new ArrayList<>();
            List<BigDecimal> allPayAmount = new ArrayList<>();

            BigDecimal integratedPaAmount = BigDecimal.valueOf(0);

            //保存套餐订单
            if (Boolean.TRUE.equals(generateMemberCardOrderResult.getLeft()) && Objects.nonNull(generateMemberCardOrderResult.getRight())) {
                ElectricityMemberCardOrder electricityMemberCardOrder = (ElectricityMemberCardOrder) generateMemberCardOrderResult.getRight();
                electricityMemberCardOrderService.insert(electricityMemberCardOrder);

                orderList.add(electricityMemberCardOrder.getOrderId());
                orderTypeList.add(UnionPayOrder.ORDER_TYPE_MEMBER_CARD);
                allPayAmount.add(electricityMemberCardOrder.getPayAmount());
                integratedPaAmount = integratedPaAmount.add(electricityMemberCardOrder.getPayAmount());

                if (CollectionUtils.isNotEmpty(query.getUserCouponIds())) {
                    //保存订单所使用的优惠券
                    memberCardOrderCouponService.batchInsert(electricityMemberCardOrderService.buildMemberCardOrderCoupon(electricityMemberCardOrder.getOrderId(), query.getUserCouponIds()));
                    //修改优惠券状态为核销中
                    userCouponService.batchUpdateUserCoupon(electricityMemberCardOrderService.buildUserCouponList(query.getUserCouponIds(), UserCoupon.STATUS_IS_BEING_VERIFICATION, electricityMemberCardOrder.getOrderId()));
                }
            }

            //保存保险订单
            if (Boolean.TRUE.equals(generateInsuranceOrderResult.getLeft()) && Objects.nonNull(generateInsuranceOrderResult.getRight())) {
                InsuranceOrder insuranceOrder = (InsuranceOrder) generateInsuranceOrderResult.getRight();
                insuranceOrderService.insert(insuranceOrder);

                orderList.add(insuranceOrder.getOrderId());
                orderTypeList.add(UnionPayOrder.ORDER_TYPE_INSURANCE);
                allPayAmount.add(insuranceOrder.getPayAmount());
                integratedPaAmount = integratedPaAmount.add(insuranceOrder.getPayAmount());
            }


            //处理0元问题
            if (integratedPaAmount.compareTo(BigDecimal.valueOf(0.01)) < 0) {

                Triple<Boolean, String, Object> result = handleTotalAmountZero(userInfo, orderList, orderTypeList);
                if (Boolean.FALSE.equals(result.getLeft())) {
                    return result;
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
                        .attach(UnionTradeOrder.ATTACH_MEMBERCARD_INSURANCE)
                        .description("租电套餐")
                        .uid(userInfo.getUid()).build();
                WechatJsapiOrderResultDTO resultDTO = unionTradeOrderService.unionCreateTradeOrderAndGetPayParams(unionPayOrder, electricityPayParams, userOauthBind.getThirdId(), request);
                return Triple.of(true, null, resultDTO);
            } catch (WechatPayException e) {
                log.error("CREATE UNION_INSURANCE_DEPOSIT_ORDER ERROR! wechat v3 order  error! uid={}", userInfo.getUid(), e);
            }
        } finally {
            redisService.delete(CacheConstant.ELE_CACHE_USER_BATTERY_MEMBER_CARD_LOCK_KEY + SecurityUtils.getUid());
        }

        return Triple.of(false, "ELECTRICITY.0099", "租电套餐支付失败");
    }

    /**
     * 处理混合支付总金额为0的场景
     *
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Triple<Boolean, String, Object> handleTotalAmountZero(UserInfo userInfo, List<String> orderList, List<Integer> orderTypeList) {
        if (CollectionUtils.isEmpty(orderList) || CollectionUtils.isEmpty(orderTypeList)) {
            log.error("ELE UNION BATTERY DEPOSIT ORDER ERROR! orderList is empty,uid={}", userInfo.getUid());
            return Triple.of(false, "000001", "系统异常");
        }

        //遍历订单类型
        for (Integer orderType : orderTypeList) {

            //电池押金
            if (Objects.equals(orderType, UnionPayOrder.ORDER_TYPE_DEPOSIT)) {
                int index = orderTypeList.indexOf(UnionPayOrder.ORDER_TYPE_DEPOSIT);
                if (index < 0) {
                    log.error("ELE UNION DEPOSIT ORDER ERROR! not found orderType,uid={}", userInfo.getUid());
                    return Triple.of(false, "ELECTRICITY.0099", "租电池押金退款订单类型不存!");
                }

                String depositOrderId = orderList.get(index);

                unionTradeOrderService.manageDepositOrder(depositOrderId, EleDepositOrder.STATUS_SUCCESS);
            }

            //电池套餐
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

            //电池保险
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

        boolean getLockSuccess = redisService.setNx(CacheConstant.ELE_CACHE_SERVICE_FEE_LOCK_KEY + SecurityUtils.getUid(), "1", 3 * 1000L, false);
        if (!getLockSuccess) {
            return Triple.of(false, "ELECTRICITY.0034", "操作频繁");
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

            if (Objects.equals(userInfo.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_YES)) {
                log.warn("SERVICE FEE WARN! user is rent deposit,uid={} ", user.getUid());
                return Triple.of(false, "ELECTRICITY.0049", "已缴纳押金");
            }

            ElectricityPayParams electricityPayParams = electricityPayParamsService.queryFromCache(tenantId);
            if (Objects.isNull(electricityPayParams)) {
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

            //获取电池滞纳金
            Triple<Boolean, String, Object> handleBatteryServiceFeeResult = handleBatteryServiceFee(userInfo, orderList, orderTypeList, allPayAmountList);
            if(Boolean.FALSE.equals(handleBatteryServiceFeeResult.getLeft())){
                return handleBatteryServiceFeeResult;
            }

            // 处理租车套餐的滞纳金
            handCarSlippage(userInfo, orderList, orderTypeList, allPayAmountList);

            if(CollectionUtils.isEmpty(allPayAmountList)){
                log.warn("SERVICE FEE WARN!allPayAmountList is empty,uid={}", user.getUid());
                return Triple.of(false, "000001", "滞纳金为空!");
            }

            //总滞纳金
            allPayAmountList.forEach(totalPayAmount::add);

            if (totalPayAmount.compareTo(BigDecimal.valueOf(0.01)) < 0) {
                log.warn("SERVICE FEE WARN!not found useroauthbind or thirdid is null,uid={}", user.getUid());
                return Triple.of(false, "000001", "滞纳金不合法!");
            }

            //调起支付
            try {
                UnionPayOrder unionPayOrder = UnionPayOrder.builder()
                        .jsonOrderId(JsonUtil.toJson(orderList))
                        .jsonOrderType(JsonUtil.toJson(orderTypeList))
                        .jsonSingleFee(JsonUtil.toJson(allPayAmountList))
                        .payAmount(totalPayAmount)
                        .tenantId(tenantId)
                        .attach(UnionTradeOrder.ATTACH_SERVUCE_FEE)
                        .description("滞纳金")
                        .uid(user.getUid()).build();
                WechatJsapiOrderResultDTO resultDTO =
                        unionTradeOrderService.unionCreateTradeOrderAndGetPayParams(unionPayOrder, electricityPayParams, userOauthBind.getThirdId(), request);
                return Triple.of(true, null, resultDTO);
            } catch (WechatPayException e) {
                log.error("CREATE UNION SERVICE FEE ERROR! wechat v3 order error! uid={}", user.getUid(), e);
            }
        } finally {
            redisService.delete(CacheConstant.ELE_CACHE_SERVICE_FEE_LOCK_KEY + user.getUid());
        }

        return Triple.of(false, "ELECTRICITY.0099", "滞纳金支付失败");
    }

    /**
     * 处理车辆的滞纳金
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
            long now = System.currentTimeMillis();
            for (CarRentalPackageOrderSlippagePo slippageEntity: slippageEntityList) {
                // 结束时间，不为空
                if (ObjectUtils.isNotEmpty(slippageEntity.getLateFeeEndTime())) {
                    now = slippageEntity.getLateFeeEndTime();
                }

                // 时间比对
                long lateFeeStartTime = slippageEntity.getLateFeeStartTime();

                // 没有滞纳金产生
                if (lateFeeStartTime > now) {
                    continue;
                }
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

    private Triple<Boolean, String, Object> handleBatteryServiceFee(UserInfo userInfo, List<String> orderList, List<Integer> orderTypeList, List<BigDecimal> allPayAmount) {
        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(userInfo.getUid());
        if (Objects.isNull(userBatteryMemberCard) || Objects.isNull(userBatteryMemberCard.getMemberCardExpireTime()) || Objects.isNull(userBatteryMemberCard.getRemainingNumber())) {
            log.warn("SERVICE FEE WARN! user haven't memberCard uid={}", userInfo.getUid());
            return Triple.of(false,"100210", "用户未开通套餐");
        }

        Franchisee franchisee = franchiseeService.queryByIdFromCache(userInfo.getFranchiseeId());
        if (Objects.isNull(franchisee)) {
            log.warn("SERVICE FEE WARN! not found user UID={}", userInfo.getUid());
            return Triple.of(false,"ELECTRICITY.0038", "未找到加盟商");
        }

        BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(userBatteryMemberCard.getMemberCardId());
        if (Objects.isNull(batteryMemberCard)) {
            log.warn("SERVICE FEE WARN! memberCard  is not exit,uid={},memberCardId={}", userInfo.getUid(), userBatteryMemberCard.getMemberCardId());
            return Triple.of(false,"ELECTRICITY.00121", "套餐不存在");
        }

        ServiceFeeUserInfo serviceFeeUserInfo = serviceFeeUserInfoService.queryByUidFromCache(userInfo.getUid());
        if (Objects.isNull(serviceFeeUserInfo)) {
            log.warn("SERVICE FEE WARN! not found user,uid={}", userInfo.getUid());
            return Triple.of(false,"100247", "用户信息不存在");
        }

        Triple<Boolean,Integer,BigDecimal> acquireDisableMembercardServiceFeeResult = serviceFeeUserInfoService.acquireDisableMembercardServiceFee(userInfo, userBatteryMemberCard, batteryMemberCard);
        Triple<Boolean,Integer,BigDecimal> acquireExpireMembercardServiceFeeResult = serviceFeeUserInfoService.acquireExpireMembercardServiceFee(userInfo, userBatteryMemberCard, batteryMemberCard, serviceFeeUserInfo);

        if (Boolean.FALSE.equals(acquireDisableMembercardServiceFeeResult.getLeft()) && Boolean.FALSE.equals(acquireExpireMembercardServiceFeeResult.getLeft())) {
            log.warn("SERVICE FEE WARN! user not exist battery service fee,uid={}", userInfo.getUid());
            return Triple.of(true, null, null);
        }

        BigDecimal totalServiceFee=BigDecimal.ZERO;

        //暂停套餐电池服务费
        if(Boolean.TRUE.equals(acquireDisableMembercardServiceFeeResult.getLeft())){
            EleBatteryServiceFeeOrder eleBatteryServiceFeeOrder = batteryServiceFeeOrderService.selectByOrderNo(serviceFeeUserInfo.getPauseOrderNo());
            if(Objects.isNull(eleBatteryServiceFeeOrder)){
                log.warn("SERVICE FEE WARN! not found disableMembercard eleBatteryServiceFeeOrder,uid={}", userInfo.getUid());
                return Triple.of(false,"ELECTRICITY.0015", "滞纳金订单不存在");
            }

            orderList.add(eleBatteryServiceFeeOrder.getOrderId());
            orderTypeList.add(ServiceFeeEnum.BATTERY_PAUSE.getCode());
            allPayAmount.add(acquireDisableMembercardServiceFeeResult.getRight());
            totalServiceFee.add(acquireDisableMembercardServiceFeeResult.getRight());
        }

        //套餐过期电池服务费
        if(Boolean.TRUE.equals(acquireExpireMembercardServiceFeeResult.getLeft())){
            EleBatteryServiceFeeOrder eleBatteryServiceFeeOrder = batteryServiceFeeOrderService.selectByOrderNo(serviceFeeUserInfo.getExpireOrderNo());
            if(Objects.isNull(eleBatteryServiceFeeOrder)){
                log.warn("SERVICE FEE WARN! not found disableMembercard eleBatteryServiceFeeOrder,uid={}", userInfo.getUid());
                return Triple.of(false,"ELECTRICITY.0015", "滞纳金订单不存在");
            }

            orderList.add(eleBatteryServiceFeeOrder.getOrderId());
            orderTypeList.add(ServiceFeeEnum.BATTERY_EXPIRE.getCode());
            allPayAmount.add(acquireExpireMembercardServiceFeeResult.getRight());
            totalServiceFee.add(acquireExpireMembercardServiceFeeResult.getRight());
        }

        if (totalServiceFee.compareTo(BigDecimal.valueOf(0.01)) < 0) {
            log.warn("SERVICE FEE WARN! service fee illegal,uid={}", userInfo.getUid());
            return Triple.of(false,"ELECTRICITY.100000", "电池服务费不合法");
        }

        return Triple.of(true, null, null);
    }

    private Triple<Boolean, String, Object> generateDepositOrder(UserInfo userInfo, BatteryMemberCard batteryMemberCard, ElectricityCabinet electricityCabinet) {

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
                .storeId(Objects.nonNull(electricityCabinet) ? electricityCabinet.getStoreId() : userInfo.getStoreId())
                .mid(batteryMemberCard.getId())
                .modelType(0).build();

        return Triple.of(true, null, eleDepositOrder);
    }

    private Triple<Boolean, String, Object> generateMemberCardOrder(UserInfo userInfo, BatteryMemberCard batteryMemberCard, IntegratedPaymentAdd integratedPaymentAdd, Set<Integer> userCouponIds, ElectricityCabinet electricityCabinet) {

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
        electricityMemberCardOrder.setFranchiseeId(integratedPaymentAdd.getFranchiseeId());
        electricityMemberCardOrder.setPayCount(payCount);
        electricityMemberCardOrder.setSendCouponId(Objects.nonNull(batteryMemberCard.getCouponId()) ? batteryMemberCard.getCouponId().longValue() : null);
        electricityMemberCardOrder.setRefId(Objects.nonNull(electricityCabinet) ? electricityCabinet.getId().longValue() : null);
        electricityMemberCardOrder.setSource(Objects.nonNull(electricityCabinet) ? ElectricityMemberCardOrder.SOURCE_SCAN : ElectricityMemberCardOrder.SOURCE_NOT_SCAN);
        electricityMemberCardOrder.setStoreId(Objects.nonNull(electricityCabinet) ? electricityCabinet.getStoreId() : userInfo.getStoreId());

        return Triple.of(true, null, electricityMemberCardOrder);
    }

    public Triple<Boolean, String, Object> generateMemberCardOrder(UserInfo userInfo, BatteryMemberCard batteryMemberCard, BatteryMemberCardAndInsuranceQuery query, ElectricityCabinet electricityCabinet) {

        //查找计算优惠券
        //计算优惠后支付金额
        Triple<Boolean, String, Object> calculatePayAmountResult = electricityMemberCardOrderService.calculatePayAmount(batteryMemberCard.getRentPrice(), query.getUserCouponIds());
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

        return Triple.of(true, null, electricityMemberCardOrder);
    }

    private Triple<Boolean, String, Object> generateInsuranceOrder(UserInfo userInfo, Integer insuranceId, ElectricityCabinet electricityCabinet) {

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
                .storeId(Objects.nonNull(electricityCabinet) ? electricityCabinet.getStoreId() : userInfo.getStoreId())
                .tenantId(userInfo.getTenantId())
                .uid(userInfo.getUid())
                .userName(userInfo.getName())
                .validDays(franchiseeInsurance.getValidDays())
                .createTime(System.currentTimeMillis())
                .updateTime(System.currentTimeMillis()).build();

        return Triple.of(true, null, insuranceOrder);
    }


    private String generateDepositOrderId(Long uid) {
        return String.valueOf(System.currentTimeMillis()).substring(2) + uid +
                RandomUtil.randomNumbers(6);
    }

    private String generateInsuranceOrderId(Long uid) {
        return String.valueOf(System.currentTimeMillis()).substring(0, 6) + uid +
                RandomUtil.randomNumbers(4);
    }
}
