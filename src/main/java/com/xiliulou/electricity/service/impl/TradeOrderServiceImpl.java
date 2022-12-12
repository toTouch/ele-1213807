package com.xiliulou.electricity.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.BatteryConstant;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.enums.BusinessType;
import com.xiliulou.electricity.mapper.FaqMapper;
import com.xiliulou.electricity.query.FaqQuery;
import com.xiliulou.electricity.query.IntegratedPaymentAdd;
import com.xiliulou.electricity.query.UnionTradeOrderAdd;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.OrderIdUtil;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.pay.weixinv3.dto.WechatJsapiOrderResultDTO;
import com.xiliulou.pay.weixinv3.exception.WechatPayException;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.OrderUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 混合支付(UnionTradeOrder)表服务接口
 *
 * @author makejava
 * @since 2022-11-07 14:06:24
 */
@Service("tradeOrderService")
@Slf4j
public class TradeOrderServiceImpl implements TradeOrderService {

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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R createOrder(UnionTradeOrderAdd unionTradeOrderAdd, HttpServletRequest request) {

        //用户
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("rentBattery  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        //租户
        Integer tenantId = TenantContextHolder.getTenantId();

        //支付相关
        ElectricityPayParams electricityPayParams = electricityPayParamsService.queryFromCache(tenantId);
        if (Objects.isNull(electricityPayParams)) {
            log.error("CREATE INSURANCE_ORDER ERROR ,NOT FOUND PAY_PARAMS");
            return R.failMsg("未配置支付参数!");
        }

        UserOauthBind userOauthBind = userOauthBindService.queryUserOauthBySysId(user.getUid(), tenantId);
        if (Objects.isNull(userOauthBind) || Objects.isNull(userOauthBind.getThirdId())) {
            log.error("CREATE INSURANCE_ORDER ERROR ,NOT FOUND USEROAUTHBIND OR THIRDID IS NULL UID={}", user.getUid());
            return R.failMsg("未找到用户的第三方授权信息!");
        }

        //用户
        UserInfo userInfo = userInfoService.queryByUidFromCache(user.getUid());
        if (Objects.isNull(userInfo)) {
            log.error("CREATE INSURANCE_ORDER ERROR! not found user,uid={} ", user.getUid());
            return R.fail("ELECTRICITY.0019", "未找到用户");
        }

        //用户是否可用
        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            log.error("CREATE INSURANCE_ORDER ERROR! user is unUsable! uid={} ", user.getUid());
            return R.fail("ELECTRICITY.0024", "用户已被禁用");
        }

        //未实名认证
        if (!Objects.equals(userInfo.getAuthStatus(), UserInfo.AUTH_STATUS_REVIEW_PASSED)) {
            log.error("CREATE INSURANCE_ORDER ERROR! user not auth! uid={}", user.getUid());
            return R.fail("ELECTRICITY.0041", "未实名认证");
        }

        if (Objects.equals(userInfo.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_YES)) {
            log.error("payDeposit  ERROR! user is rent deposit! ,uid={} ", user.getUid());
            return R.fail("ELECTRICITY.0049", "已缴纳押金");
        }

        Franchisee franchisee = franchiseeService.queryByIdFromDB(unionTradeOrderAdd.getFranchiseeId());
        if (Objects.isNull(franchisee)) {
            log.error("payDeposit  ERROR! not found Franchisee ！franchiseeId={},uid={}", unionTradeOrderAdd.getFranchiseeId(), user.getUid());
            return R.fail("ELECTRICITY.0038", "未找到加盟商");
        }

        BigDecimal depositPayAmount = null;

        if (Objects.equals(franchisee.getModelType(), Franchisee.OLD_MODEL_TYPE)) {
            depositPayAmount = franchisee.getBatteryDeposit();
        }

        //型号押金计算
        if (Objects.equals(franchisee.getModelType(), Franchisee.NEW_MODEL_TYPE)) {
            if (Objects.isNull(unionTradeOrderAdd.getModel())) {
                return R.fail("ELECTRICITY.0007", "不合法的参数");
            }

            //型号押金
            List<Map> modelBatteryDepositList = JsonUtil.fromJson(franchisee.getModelBatteryDeposit(), List.class);
            if (ObjectUtil.isEmpty(modelBatteryDepositList)) {
                log.error("payDeposit  ERROR! not found modelBatteryDepositList ！franchiseeId={},uid={}", unionTradeOrderAdd.getFranchiseeId(), user.getUid());
                return R.fail("ELECTRICITY.00110", "未找到押金");
            }


            for (Map map : modelBatteryDepositList) {
                if ((double) (map.get("model")) - unionTradeOrderAdd.getModel() < 1 && (double) (map.get("model")) - unionTradeOrderAdd.getModel() >= 0) {
                    depositPayAmount = BigDecimal.valueOf((double) map.get("batteryDeposit"));
                    break;
                }
            }
        }

        if (Objects.isNull(depositPayAmount)) {
            log.error("payDeposit  ERROR! payAmount is null ！franchiseeId{},uid={}", unionTradeOrderAdd.getFranchiseeId(), user.getUid());
            return R.fail("ELECTRICITY.00110", "未找到押金");
        }

        //查询保险
        FranchiseeInsurance franchiseeInsurance = franchiseeInsuranceService.queryByCache(unionTradeOrderAdd.getInsuranceId());

        if (Objects.isNull(franchiseeInsurance)) {
            log.error("CREATE INSURANCE_ORDER ERROR,NOT FOUND MEMBER_CARD BY ID={},uid={}", unionTradeOrderAdd.getInsuranceId(), user.getUid());
            return R.fail("100305", "未找到保险!");
        }
        if (ObjectUtil.equal(FranchiseeInsurance.STATUS_UN_USABLE, franchiseeInsurance.getStatus())) {
            log.error("CREATE INSURANCE_ORDER ERROR ,MEMBER_CARD IS UN_USABLE ID={},uid={}", unionTradeOrderAdd.getInsuranceId(), user.getUid());
            return R.fail("100306", "保险已禁用!");
        }

        if (Objects.isNull(franchiseeInsurance.getPremium())) {
            log.error("CREATE INSURANCE_ORDER ERROR! payAmount is null ！franchiseeId={},uid={}", unionTradeOrderAdd.getFranchiseeId(), user.getUid());
            return R.fail("100305", "未找到保险");
        }

        //生成押金独立订单
        String orderId = generateDepositOrderId(user.getUid());
        EleDepositOrder eleDepositOrder = EleDepositOrder.builder()
                .orderId(orderId)
                .uid(user.getUid())
                .phone(userInfo.getPhone())
                .name(userInfo.getName())
                .payAmount(depositPayAmount)
                .status(EleDepositOrder.STATUS_INIT)
                .createTime(System.currentTimeMillis())
                .updateTime(System.currentTimeMillis())
                .tenantId(tenantId)
                .franchiseeId(franchisee.getId())
                .payType(EleDepositOrder.ONLINE_PAYMENT)
                .storeId(null)
                .modelType(franchisee.getModelType()).build();

        if (Objects.equals(franchisee.getModelType(), Franchisee.NEW_MODEL_TYPE)) {
            eleDepositOrder.setBatteryType(BatteryConstant.acquireBatteryShort(unionTradeOrderAdd.getModel()));
        }
        eleDepositOrderService.insert(eleDepositOrder);

        //生成保险独立订单
        String insuranceOrderId = generateInsuranceOrderId(user.getUid());
        InsuranceOrder insuranceOrder = InsuranceOrder.builder()
                .insuranceId(franchiseeInsurance.getId())
                .insuranceName(franchiseeInsurance.getName())
                .insuranceType(InsuranceOrder.BATTERY_INSURANCE_TYPE)
                .orderId(insuranceOrderId)
                .cid(franchiseeInsurance.getCid())
                .franchiseeId(franchisee.getId())
                .isUse(InsuranceOrder.NOT_USE)
                .payAmount(franchiseeInsurance.getPremium())
                .forehead(franchiseeInsurance.getForehead())
                .payType(InsuranceOrder.ONLINE_PAY_TYPE)
                .phone(userInfo.getPhone())
                .status(InsuranceOrder.STATUS_INIT)
                .tenantId(tenantId)
                .uid(user.getUid())
                .userName(userInfo.getName())
                .validDays(franchiseeInsurance.getValidDays())
                .createTime(System.currentTimeMillis())
                .updateTime(System.currentTimeMillis()).build();
        insuranceOrderService.insert(insuranceOrder);

        List<String> orderList = new ArrayList<>();
        orderList.add(orderId);
        orderList.add(insuranceOrderId);

        List<Integer> orderTypeList = new ArrayList<>();
        orderTypeList.add(UnionPayOrder.ORDER_TYPE_DEPOSIT);
        orderTypeList.add(UnionPayOrder.ORDER_TYPE_INSURANCE);

        List<BigDecimal> allPayAmount = new ArrayList<>();
        allPayAmount.add(depositPayAmount);
        allPayAmount.add(franchiseeInsurance.getPremium());

        //调起支付
        try {
            UnionPayOrder unionPayOrder = UnionPayOrder.builder()
                    .jsonOrderId(JsonUtil.toJson(orderList))
                    .jsonOrderType(JsonUtil.toJson(orderTypeList))
                    .jsonSingleFee(JsonUtil.toJson(allPayAmount))
                    .payAmount(depositPayAmount.add(franchiseeInsurance.getPremium()))
                    .tenantId(tenantId)
                    .attach(UnionTradeOrder.ATTACH_UNION_INSURANCE_AND_DEPOSIT)
                    .description("保险押金联合收费")
                    .uid(user.getUid()).build();
            WechatJsapiOrderResultDTO resultDTO =
                    unionTradeOrderService.unionCreateTradeOrderAndGetPayParams(unionPayOrder, electricityPayParams, userOauthBind.getThirdId(), request);
            return R.ok(resultDTO);
        } catch (WechatPayException e) {
            log.error("CREATE UNION_INSURANCE_DEPOSIT_ORDER ERROR! wechat v3 order  error! uid={}", user.getUid(), e);
        }

        return R.fail("ELECTRICITY.0099", "下单失败");
    }

    @Override
    public Triple<Boolean, String, Object> integratedPayment(IntegratedPaymentAdd integratedPaymentAdd, HttpServletRequest request) {

        //用户
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("rentBattery  ERROR! not found user ");
            return Triple.of(false, "ELECTRICITY.0001", "未找到用户");
        }

        //租户
        Integer tenantId = TenantContextHolder.getTenantId();

        //支付相关
        ElectricityPayParams electricityPayParams = electricityPayParamsService.queryFromCache(tenantId);
        if (Objects.isNull(electricityPayParams)) {
            log.error("CREATE INSURANCE_ORDER ERROR ,NOT FOUND PAY_PARAMS");
            return Triple.of(false, "100307", "未配置支付参数!");
        }

        UserOauthBind userOauthBind = userOauthBindService.queryUserOauthBySysId(user.getUid(), tenantId);
        if (Objects.isNull(userOauthBind) || Objects.isNull(userOauthBind.getThirdId())) {
            log.error("CREATE INSURANCE_ORDER ERROR ,NOT FOUND USEROAUTHBIND OR THIRDID IS NULL UID={}", user.getUid());
            return Triple.of(false, "100308", "未找到用户的第三方授权信息!");
        }

        //用户
        UserInfo userInfo = userInfoService.queryByUidFromCache(user.getUid());
        if (Objects.isNull(userInfo)) {
            log.error("CREATE INSURANCE_ORDER ERROR! not found user,uid={} ", user.getUid());
            return Triple.of(false, "ELECTRICITY.0019", "未找到用户");
        }

        //用户是否可用
        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            log.error("CREATE INSURANCE_ORDER ERROR! user is unUsable! uid={} ", user.getUid());
            return Triple.of(false, "ELECTRICITY.0024", "用户已被禁用");
        }

        //未实名认证
        if (!Objects.equals(userInfo.getAuthStatus(), UserInfo.AUTH_STATUS_REVIEW_PASSED)) {
            log.error("CREATE INSURANCE_ORDER ERROR! user not auth! uid={}", user.getUid());
            return Triple.of(false, "ELECTRICITY.0041", "未实名认证");
        }

        if (Objects.equals(userInfo.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_YES)) {
            log.error("payDeposit  ERROR! user is rent deposit! ,uid={} ", user.getUid());
            return Triple.of(false, "ELECTRICITY.0049", "已缴纳押金");
        }


        List<String> orderList = new ArrayList<>();
        List<Integer> orderTypeList = new ArrayList<>();
        List<BigDecimal> allPayAmount = new ArrayList<>();

        //生成押金订单
        if (Objects.nonNull(integratedPaymentAdd.getFranchiseeId())) {
            Triple<Boolean, String, Object> generateDepositOrderResult = generateDepositOrder(userInfo, integratedPaymentAdd.getFranchiseeId(), integratedPaymentAdd.getModel());
            if (!generateDepositOrderResult.getLeft()) {
                return generateDepositOrderResult;
            }
            EleDepositOrder eleDepositOrder = (EleDepositOrder) generateDepositOrderResult.getRight();
            orderList.add(eleDepositOrder.getOrderId());
            orderTypeList.add(UnionPayOrder.ORDER_TYPE_DEPOSIT);
            allPayAmount.add(eleDepositOrder.getPayAmount());
        }

        //生成套餐订单
        if (Objects.nonNull(integratedPaymentAdd.getMemberCardId())) {

        }


        //调起支付
        try {


            UnionPayOrder unionPayOrder = UnionPayOrder.builder()
                    .jsonOrderId(JsonUtil.toJson(orderList))
                    .jsonOrderType(JsonUtil.toJson(orderTypeList))
                    .jsonSingleFee(JsonUtil.toJson(allPayAmount))
                    .payAmount(depositPayAmount.add(franchiseeInsurance.getPremium()))
                    .tenantId(tenantId)
                    .attach(UnionTradeOrder.ATTACH_UNION_INSURANCE_AND_DEPOSIT)
                    .description("保险押金联合收费")
                    .uid(user.getUid()).build();
            WechatJsapiOrderResultDTO resultDTO =
                    unionTradeOrderService.unionCreateTradeOrderAndGetPayParams(unionPayOrder, electricityPayParams, userOauthBind.getThirdId(), request);
            return R.ok(resultDTO);
        } catch (WechatPayException e) {
            log.error("CREATE UNION_INSURANCE_DEPOSIT_ORDER ERROR! wechat v3 order  error! uid={}", user.getUid(), e);
        }

        return null;
    }


    private Triple<Boolean, String, Object> generateDepositOrder(UserInfo userInfo, Long franchiseeId, Integer model) {

        Franchisee franchisee = franchiseeService.queryByIdFromCache(franchiseeId);
        if (Objects.isNull(franchisee)) {
            log.error("payDeposit  ERROR! not found Franchisee ！franchiseeId={},uid={}", franchiseeId, userInfo.getUid());
            return Triple.of(false, "ELECTRICITY.0038", "未找到加盟商");
        }

        BigDecimal depositPayAmount = null;

        if (Objects.equals(franchisee.getModelType(), Franchisee.OLD_MODEL_TYPE)) {
            depositPayAmount = franchisee.getBatteryDeposit();
        }

        //型号押金计算
        if (Objects.equals(franchisee.getModelType(), Franchisee.NEW_MODEL_TYPE)) {
            if (Objects.isNull(model)) {
                return Triple.of(false, "ELECTRICITY.0007", "不合法的参数");
            }

            //型号押金
            List<Map> modelBatteryDepositList = JsonUtil.fromJson(franchisee.getModelBatteryDeposit(), List.class);
            if (ObjectUtil.isEmpty(modelBatteryDepositList)) {
                log.error("payDeposit  ERROR! not found modelBatteryDepositList ！franchiseeId={},uid={}", franchiseeId, userInfo.getUid());
                return Triple.of(false, "ELECTRICITY.00110", "未找到押金");
            }


            for (Map map : modelBatteryDepositList) {
                if ((double) (map.get("model")) - model < 1 && (double) (map.get("model")) - model >= 0) {
                    depositPayAmount = BigDecimal.valueOf((double) map.get("batteryDeposit"));
                    break;
                }
            }
        }

        if (Objects.isNull(depositPayAmount)) {
            log.error("payDeposit  ERROR! payAmount is null ！franchiseeId{},uid={}", franchiseeId, userInfo.getUid());
            return Triple.of(false, "ELECTRICITY.00110", "未找到押金");
        }

        //生成押金独立订单
        String depositOrderId = OrderIdUtil.generateBusinessOrderId(BusinessType.BATTERY_DEPOSIT, userInfo.getUid());
        EleDepositOrder eleDepositOrder = EleDepositOrder.builder()
                .orderId(depositOrderId)
                .uid(userInfo.getUid())
                .phone(userInfo.getPhone())
                .name(userInfo.getName())
                .payAmount(depositPayAmount)
                .status(EleDepositOrder.STATUS_INIT)
                .createTime(System.currentTimeMillis())
                .updateTime(System.currentTimeMillis())
                .tenantId(userInfo.getTenantId())
                .franchiseeId(franchisee.getId())
                .payType(EleDepositOrder.ONLINE_PAYMENT)
                .storeId(null)
                .modelType(franchisee.getModelType()).build();

        if (Objects.equals(franchisee.getModelType(), Franchisee.NEW_MODEL_TYPE)) {
            eleDepositOrder.setBatteryType(BatteryConstant.acquireBatteryShort(model));
        }
        eleDepositOrderService.insert(eleDepositOrder);


        return Triple.of(true, null, eleDepositOrder);
    }


    private Triple<Boolean, String, Object> generateMemberCardOrder(UserInfo userInfo, Long franchiseeId, Integer model) {

        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(userInfo.getUid());

        //判断是否缴纳押金
        if (Objects.nonNull(userBatteryMemberCard) && Objects.equals(userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_DISABLE)) {
            log.error("CREATE MEMBER_ORDER ERROR! not pay deposit! uid={} ", userInfo.getUid());
            return Triple.of(false, "100241", "当前套餐暂停中，请先启用套餐");
        }

        Long now = System.currentTimeMillis();

        //判断用户电池服务费
        ServiceFeeUserInfo serviceFeeUserInfo = serviceFeeUserInfoService.queryByUidFromCache(userInfo.getUid());
        if (Objects.nonNull(serviceFeeUserInfo) && Objects.nonNull(serviceFeeUserInfo.getServiceFeeGenerateTime())) {
            long cardDays = (now - serviceFeeUserInfo.getServiceFeeGenerateTime()) / 1000L / 60 / 60 / 24;
            BigDecimal userMemberCardExpireServiceFee = electricityMemberCardOrderService.checkUserMemberCardExpireBatteryService(userInfo, null, cardDays);
            if (BigDecimal.valueOf(0).compareTo(userMemberCardExpireServiceFee) != 0) {
                return R.fail("ELECTRICITY.100000", "用户存在电池服务费", userMemberCardExpireServiceFee);
            }
        }

        ElectricityMemberCard electricityMemberCard = electricityMemberCardService.queryByCache(electricityMemberCardOrderQuery.getMemberId());
        if (Objects.isNull(electricityMemberCard)) {
            log.error("CREATE MEMBER_ORDER ERROR ,NOT FOUND MEMBER_CARD BY ID:{}", electricityMemberCardOrderQuery.getMemberId());
            return R.fail("ELECTRICITY.0087", "未找到月卡套餐!");
        }
        if (ObjectUtil.equal(ElectricityMemberCard.STATUS_UN_USEABLE, electricityMemberCard.getStatus())) {
            log.error("CREATE MEMBER_ORDER ERROR ,MEMBER_CARD IS UN_USABLE ID:{}", electricityMemberCardOrderQuery.getMemberId());
            return R.fail("ELECTRICITY.0088", "月卡已禁用!");
        }

        //判断是否已绑定限次数套餐并且换电次数为负
        ElectricityMemberCard bindElectricityMemberCard = electricityMemberCardService.queryByCache(userBatteryMemberCard.getMemberCardId().intValue());

        if (Objects.nonNull(bindElectricityMemberCard) && !Objects.equals(bindElectricityMemberCard.getLimitCount(), ElectricityMemberCard.UN_LIMITED_COUNT_TYPE) && Objects.nonNull(userBatteryMemberCard.getRemainingNumber()) && userBatteryMemberCard.getRemainingNumber() < 0) {
            if (!Objects.equals(electricityMemberCard.getLimitCount(), ElectricityMemberCard.UN_LIMITED_COUNT_TYPE)) {
                log.error("payDeposit  ERROR! not buy same memberCard uid:{}", user.getUid());
                return R.fail("ELECTRICITY.00119", "套餐剩余次数为负,应购买相同类型套餐抵扣");
            }
        }


        //查找计算优惠券
        //满减折扣劵
        UserCoupon userCoupon = null;
        BigDecimal payAmount = electricityMemberCard.getHolidayPrice();
        if (Objects.nonNull(electricityMemberCardOrderQuery.getUserCouponId())) {
            userCoupon = userCouponService.queryByIdFromDB(electricityMemberCardOrderQuery.getUserCouponId());
            if (Objects.isNull(userCoupon)) {
                log.error("ELECTRICITY  ERROR! not found userCoupon! userCouponId:{} ", electricityMemberCardOrderQuery.getUserCouponId());
                return R.fail("ELECTRICITY.0085", "未找到优惠券");
            }

            //优惠券是否使用
            if (Objects.equals(UserCoupon.STATUS_USED, userCoupon.getStatus())) {
                log.error("ELECTRICITY  ERROR!  userCoupon is used! userCouponId:{} ", electricityMemberCardOrderQuery.getUserCouponId());
                return R.fail("ELECTRICITY.0090", "您的优惠券已被使用");
            }

            //优惠券是否过期
            if (userCoupon.getDeadline() < System.currentTimeMillis()) {
                log.error("ELECTRICITY  ERROR!  userCoupon is deadline!userCouponId:{} ", electricityMemberCardOrderQuery.getUserCouponId());
                return R.fail("ELECTRICITY.0091", "您的优惠券已过期");
            }

            Coupon coupon = couponService.queryByIdFromCache(userCoupon.getCouponId());
            if (Objects.isNull(coupon)) {
                log.error("ELECTRICITY  ERROR! not found coupon! userCouponId:{} ", electricityMemberCardOrderQuery.getUserCouponId());
                return R.fail("ELECTRICITY.0085", "未找到优惠券");
            }

            //使用满减劵
            if (Objects.equals(userCoupon.getDiscountType(), UserCoupon.FULL_REDUCTION)) {

                //计算满减
                payAmount = payAmount.subtract(coupon.getAmount());
            }

            //使用折扣劵
            if (Objects.equals(userCoupon.getDiscountType(), UserCoupon.DISCOUNT)) {

                //计算折扣
                payAmount = payAmount.multiply(coupon.getDiscount().divide(BigDecimal.valueOf(100)));
            }

        }

        //支付金额不能为负数
        if (payAmount.compareTo(BigDecimal.valueOf(0.01)) < 0) {
            payAmount = BigDecimal.valueOf(0);
        }

        Long remainingNumber = electricityMemberCard.getMaxUseCount();

        //同一个套餐可以续费
        if (Objects.nonNull(bindElectricityMemberCard) && Objects.equals(bindElectricityMemberCard.getLimitCount(), electricityMemberCard.getLimitCount())) {
            if (Objects.nonNull(userBatteryMemberCard.getMemberCardExpireTime()) && now < userBatteryMemberCard.getMemberCardExpireTime()) {
                now = userBatteryMemberCard.getMemberCardExpireTime();
            }
            //TODO 使用次数暂时叠加
            if (!Objects.equals(bindElectricityMemberCard.getLimitCount(), ElectricityMemberCard.UN_LIMITED_COUNT_TYPE)) {
                remainingNumber = remainingNumber + userBatteryMemberCard.getRemainingNumber();
            }

        } else {
            if (Objects.nonNull(userBatteryMemberCard.getMemberCardExpireTime())
                    && Objects.nonNull(userBatteryMemberCard.getRemainingNumber()) &&
                    userBatteryMemberCard.getMemberCardExpireTime() > now &&
                    (ObjectUtil.equal(ElectricityMemberCard.UN_LIMITED_COUNT, userBatteryMemberCard.getRemainingNumber()) || userBatteryMemberCard.getRemainingNumber() > 0)) {
                log.error("CREATE MEMBER_ORDER ERROR ,MEMBER_CARD IS NOT EXPIRED USERINFO:{}", userInfo);
                return R.fail("ELECTRICITY.0089", "您的套餐未过期，只能购买相同类型的套餐!");
            }
        }

        ElectricityMemberCardOrder electricityMemberCardOrder = new ElectricityMemberCardOrder();
        electricityMemberCardOrder.setOrderId(String.valueOf(System.currentTimeMillis()));
        electricityMemberCardOrder.setCreateTime(System.currentTimeMillis());
        electricityMemberCardOrder.setUpdateTime(System.currentTimeMillis());
        electricityMemberCardOrder.setStatus(ElectricityMemberCardOrder.STATUS_INIT);
        electricityMemberCardOrder.setMemberCardId(electricityMemberCardOrderQuery.getMemberId());
        electricityMemberCardOrder.setUid(user.getUid());
        electricityMemberCardOrder.setMaxUseCount(electricityMemberCard.getMaxUseCount());
        electricityMemberCardOrder.setMemberCardType(electricityMemberCard.getType());
        electricityMemberCardOrder.setCardName(electricityMemberCard.getName());
        electricityMemberCardOrder.setPayAmount(payAmount);
        electricityMemberCardOrder.setUserName(userInfo.getName());
        electricityMemberCardOrder.setValidDays(electricityMemberCard.getValidDays());
        electricityMemberCardOrder.setTenantId(electricityMemberCard.getTenantId());
        electricityMemberCardOrder.setFranchiseeId(franchiseeId);
        electricityMemberCardOrder.setIsBindActivity(electricityMemberCard.getIsBindActivity());
        electricityMemberCardOrder.setActivityId(electricityMemberCard.getActivityId());
        baseMapper.insert(electricityMemberCardOrder);
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
