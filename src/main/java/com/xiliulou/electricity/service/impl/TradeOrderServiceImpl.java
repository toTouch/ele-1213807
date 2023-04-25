package com.xiliulou.electricity.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.BatteryConstant;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.enums.BusinessType;
import com.xiliulou.electricity.query.IntegratedPaymentAdd;
import com.xiliulou.electricity.query.ModelBatteryDeposit;
import com.xiliulou.electricity.query.UnionTradeOrderAdd;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.OrderIdUtil;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.pay.weixinv3.dto.WechatJsapiOrderResultDTO;
import com.xiliulou.pay.weixinv3.exception.WechatPayException;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.*;

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
            log.error("CREATE INSURANCE_ORDER ERROR ,NOT FOUND PAY_PARAMS,uid={}", user.getUid());
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
            eleDepositOrder.setBatteryType(batteryModelService.acquireBatteryShort(unionTradeOrderAdd.getModel(),tenantId));
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
    
        BigDecimal totalPayAmount = new BigDecimal(0);
        totalPayAmount.add(depositPayAmount);
        totalPayAmount.add(franchiseeInsurance.getPremium());
    
        //处理0元问题
        if (BigDecimal.valueOf(0.01).compareTo(totalPayAmount) == NumberConstant.ONE) {
        
            Triple<Boolean, String, Object> result = handleTotalAmountZero(userInfo, orderList, orderTypeList);
            if (Boolean.FALSE.equals(result.getLeft())) {
                R r = R.fail(null);
                r.setErrCode(result.getMiddle());
                r.setErrMsg(String.valueOf(result.getRight()));
                return r;
            }
        
            return R.ok();
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

        return R.fail("ELECTRICITY.0099", "下单失败");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Triple<Boolean, String, Object> integratedPayment(IntegratedPaymentAdd integratedPaymentAdd, HttpServletRequest request) {

        //用户
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("rentBattery  ERROR! not found user ");
            return Triple.of(false, "ELECTRICITY.0001", "未找到用户");
        }

        //租户
        Integer tenantId = TenantContextHolder.getTenantId();

        boolean getLockSuccess = redisService.setNx(CacheConstant.ELE_CACHE_USER_DEPOSIT_LOCK_KEY + user.getUid(), "1", 3 * 1000L, false);
        if (!getLockSuccess) {
            return Triple.of(false, "ELECTRICITY.0034", "操作频繁");
        }

        //支付相关
        ElectricityPayParams electricityPayParams = electricityPayParamsService.queryFromCache(tenantId);
        if (Objects.isNull(electricityPayParams)) {
            log.error("CREATE INSURANCE_ORDER ERROR ,NOT FOUND PAY_PARAMS,uid={}", user.getUid());
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

        //处理押金订单
        Triple<Boolean, String, Object> generateDepositOrderResult = generateDepositOrder(userInfo, integratedPaymentAdd.getFranchiseeId(), integratedPaymentAdd.getModel());
        if (!generateDepositOrderResult.getLeft()) {
            return generateDepositOrderResult;
        }

        //处理套餐订单
        Triple<Boolean, String, Object> generateMemberCardOrderResult = generateMemberCardOrder(userInfo, integratedPaymentAdd);
        if (!generateMemberCardOrderResult.getLeft()) {
            return generateMemberCardOrderResult;
        }

        //处理保险订单
        Triple<Boolean, String, Object> generateInsuranceOrderResult = generateInsuranceOrder(userInfo, integratedPaymentAdd.getInsuranceId());
        if (!generateInsuranceOrderResult.getLeft()) {
            return generateInsuranceOrderResult;
        }

        List<String> orderList = new ArrayList<>();
        List<Integer> orderTypeList = new ArrayList<>();
        List<BigDecimal> allPayAmount = new ArrayList<>();

        BigDecimal integratedPaAmount = BigDecimal.valueOf(0);

        //保存押金订单
        if (generateDepositOrderResult.getLeft() && Objects.nonNull(generateDepositOrderResult.getRight())) {
            EleDepositOrder eleDepositOrder = (EleDepositOrder) generateDepositOrderResult.getRight();
            if (Objects.equals(eleDepositOrder.getModelType(), Franchisee.NEW_MODEL_TYPE)) {
                eleDepositOrder.setBatteryType(batteryModelService.acquireBatteryShort(integratedPaymentAdd.getModel(),tenantId));
            }
            eleDepositOrderService.insert(eleDepositOrder);

            orderList.add(eleDepositOrder.getOrderId());
            orderTypeList.add(UnionPayOrder.ORDER_TYPE_DEPOSIT);
            allPayAmount.add(eleDepositOrder.getPayAmount());
            integratedPaAmount = integratedPaAmount.add(eleDepositOrder.getPayAmount());
        }

        //保存套餐订单
        if (generateMemberCardOrderResult.getLeft() && !CollectionUtils.isEmpty((List) generateMemberCardOrderResult.getRight())) {
            ElectricityMemberCardOrder electricityMemberCardOrder = (ElectricityMemberCardOrder) ((List) generateMemberCardOrderResult.getRight()).get(0);
            electricityMemberCardOrderService.insert(electricityMemberCardOrder);
            orderList.add(electricityMemberCardOrder.getOrderId());
            orderTypeList.add(UnionPayOrder.ORDER_TYPE_MEMBER_CARD);
            allPayAmount.add(electricityMemberCardOrder.getPayAmount());
            integratedPaAmount = integratedPaAmount.add(electricityMemberCardOrder.getPayAmount());

            //优惠券处理
            if (Objects.nonNull(integratedPaymentAdd.getUserCouponId()) && ((List) generateMemberCardOrderResult.getRight()).size() > 1) {

                UserCoupon userCoupon = (UserCoupon) ((List) generateMemberCardOrderResult.getRight()).get(1);
                //修改劵可用状态
                if (Objects.nonNull(userCoupon)) {
                    userCoupon.setStatus(UserCoupon.STATUS_IS_BEING_VERIFICATION);
                    userCoupon.setUpdateTime(System.currentTimeMillis());
                    userCoupon.setOrderId(electricityMemberCardOrder.getOrderId());
                    userCouponService.update(userCoupon);
                }
            }
        }

        //保存保险订单
        if (generateInsuranceOrderResult.getLeft() && Objects.nonNull(generateInsuranceOrderResult.getRight())) {
            InsuranceOrder insuranceOrder = (InsuranceOrder) generateInsuranceOrderResult.getRight();
            insuranceOrderService.insert(insuranceOrder);
            orderList.add(insuranceOrder.getOrderId());
            orderTypeList.add(UnionPayOrder.ORDER_TYPE_INSURANCE);
            allPayAmount.add(insuranceOrder.getPayAmount());
            integratedPaAmount = integratedPaAmount.add(insuranceOrder.getPayAmount());
        }


        //处理0元问题
        if (BigDecimal.valueOf(0.01).compareTo(integratedPaAmount) == NumberConstant.ONE) {

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

        return Triple.of(false, "ELECTRICITY.0099", "下单失败");
    }

    /**
     * 处理混合支付总金额为0的场景
     *
     * @param userInfo
     * @param orderList
     * @param orderTypeList
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Triple<Boolean, String, Object> handleTotalAmountZero(UserInfo userInfo, List<String> orderList, List<Integer> orderTypeList) {
        if (CollectionUtils.isEmpty(orderList) || CollectionUtils.isEmpty(orderTypeList)) {
            log.error("ELE UNION DEPOSIT ORDER ERROR! orderList is empty,uid={}", userInfo.getUid());
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
                unionTradeOrderService.manageMemberCardOrder(memberCardOrderId, ElectricityMemberCardOrder.STATUS_SUCCESS);
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


    private Triple<Boolean, String, Object> generateDepositOrder(UserInfo userInfo, Long franchiseeId, Integer model) {

        if (Objects.isNull(franchiseeId)) {
            return Triple.of(true, "", null);
        }

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
            List<ModelBatteryDeposit> modelBatteryDepositList = JsonUtil.fromJsonArray(franchisee.getModelBatteryDeposit(), ModelBatteryDeposit.class);
            if (ObjectUtil.isEmpty(modelBatteryDepositList)) {
                log.error("payDeposit  ERROR! not found modelBatteryDepositList ！franchiseeId={},uid={}", franchiseeId, userInfo.getUid());
                return Triple.of(false, "ELECTRICITY.00110", "未找到押金");
            }

            for (ModelBatteryDeposit modelBatteryDeposit : modelBatteryDepositList) {
                if ((double) (modelBatteryDeposit.getModel()) - model < 1 && (double) (modelBatteryDeposit.getModel()) - model >= 0) {
                    depositPayAmount = modelBatteryDeposit.getBatteryDeposit();
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


        return Triple.of(true, null, eleDepositOrder);
    }

    private Triple<Boolean, String, Object> generateMemberCardOrder(UserInfo userInfo, IntegratedPaymentAdd integratedPaymentAdd) {

        if (Objects.isNull(integratedPaymentAdd.getMemberCardId())) {
            return Triple.of(true, "", null);
        }

        ElectricityMemberCard electricityMemberCard = electricityMemberCardService.queryByCache(integratedPaymentAdd.getMemberCardId());
        if (Objects.isNull(electricityMemberCard)) {
            log.error("BATTERY MEMBER_ORDER ERROR!not found battery membercard,membercardId={},uid={}", integratedPaymentAdd.getMemberCardId(), userInfo.getUid());
            return Triple.of(false, "ELECTRICITY.0087", "未找到月卡套餐!");
        }
        if (ObjectUtil.equal(ElectricityMemberCard.STATUS_UN_USEABLE, electricityMemberCard.getStatus())) {
            log.error("BATTERY MEMBER_ORDER ERROR!battery membercard is un_usable membercardId={},uid={}", integratedPaymentAdd.getMemberCardId(), userInfo.getUid());
            return Triple.of(false, "ELECTRICITY.0088", "月卡已禁用!");
        }

        //购买套餐扫码的柜机
        Long refId = null;
        //购买套餐来源
        Integer source = ElectricityMemberCardOrder.SOURCE_NOT_SCAN;
        if (StringUtils.isNotBlank(integratedPaymentAdd.getProductKey()) && StringUtils.isNotBlank(integratedPaymentAdd.getDeviceName())) {
            ElectricityCabinet electricityCabinet = electricityCabinetService.queryFromCacheByProductAndDeviceName(integratedPaymentAdd.getProductKey(), integratedPaymentAdd.getDeviceName());
            if (Objects.isNull(electricityCabinet)) {
                log.error("BATTERY MEMBER ORDER ERROR!not found electricityCabinet,p={},d={}", integratedPaymentAdd.getProductKey(), integratedPaymentAdd.getDeviceName());
                return Triple.of(false, "ELECTRICITY.0005", "未找到换电柜");
            }

            //查找换电柜门店
            if (Objects.isNull(electricityCabinet.getStoreId())) {
                log.error("BATTERY MEMBER ORDER ERROR!not found store,eid={},uid={}", electricityCabinet.getId(), userInfo.getUid());
                return Triple.of(false, "ELECTRICITY.0097", "换电柜未绑定门店，不可用");
            }
            Store store = storeService.queryByIdFromCache(electricityCabinet.getStoreId());
            if (Objects.isNull(store)) {
                log.error("BATTERY MEMBER ORDER ERROR!not found store,storeId={},uid={}", electricityCabinet.getStoreId(), userInfo.getUid());
                return Triple.of(false, "ELECTRICITY.0018", "未找到门店");
            }

            //查找门店加盟商
            if (Objects.isNull(store.getFranchiseeId())) {
                log.error("BATTERY MEMBER ORDER ERROR!not found Franchisee,storeId={},uid={}", store.getId(), userInfo.getUid());
                return Triple.of(false, "ELECTRICITY.0098", "换电柜门店未绑定加盟商，不可用");
            }

            //换电柜加盟商和用户加盟商一致  则保存套餐来源
            if (Objects.equals(store.getFranchiseeId(), electricityMemberCard.getFranchiseeId())) {
                source = ElectricityMemberCardOrder.SOURCE_SCAN;
                refId = electricityCabinet.getId().longValue();
            }
        }


        //查找计算优惠券
        //满减折扣劵
        UserCoupon userCoupon = null;
        BigDecimal payAmount = electricityMemberCard.getHolidayPrice();
        if (Objects.nonNull(integratedPaymentAdd.getUserCouponId())) {
            userCoupon = userCouponService.queryByIdFromDB(integratedPaymentAdd.getUserCouponId());
            if (Objects.isNull(userCoupon)) {
                log.error("ELECTRICITY  ERROR! not found userCoupon! userCouponId={},uid={}", integratedPaymentAdd.getUserCouponId(), userInfo.getUid());
                return Triple.of(false, "ELECTRICITY.0085", "未找到优惠券");
            }

            //优惠券是否使用
            if (Objects.equals(UserCoupon.STATUS_USED, userCoupon.getStatus())) {
                log.error("ELECTRICITY  ERROR!  userCoupon is used! userCouponId={},uid={}", integratedPaymentAdd.getUserCouponId(), userInfo.getUid());
                return Triple.of(false, "ELECTRICITY.0090", "您的优惠券已被使用");
            }

            //优惠券是否过期
            if (userCoupon.getDeadline() < System.currentTimeMillis()) {
                log.error("ELECTRICITY  ERROR!  userCoupon is deadline!userCouponId={},uid={}", integratedPaymentAdd.getUserCouponId(), userInfo.getUid());
                return Triple.of(false, "ELECTRICITY.0091", "您的优惠券已过期");
            }

            Coupon coupon = couponService.queryByIdFromCache(userCoupon.getCouponId());
            if (Objects.isNull(coupon)) {
                log.error("ELECTRICITY  ERROR! not found coupon! userCouponId={},uid={}", integratedPaymentAdd.getUserCouponId(), userInfo.getUid());
                return Triple.of(false, "ELECTRICITY.0085", "未找到优惠券");
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
    
        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService
                .selectByUidFromCache(userInfo.getUid());
        Integer payCount = electricityMemberCardOrderService.queryMaxPayCount(userBatteryMemberCard);

        ElectricityMemberCardOrder electricityMemberCardOrder = new ElectricityMemberCardOrder();
        electricityMemberCardOrder.setOrderId(OrderIdUtil.generateBusinessOrderId(BusinessType.BATTERY_PACKAGE, userInfo.getUid()));
        electricityMemberCardOrder.setCreateTime(System.currentTimeMillis());
        electricityMemberCardOrder.setUpdateTime(System.currentTimeMillis());
        electricityMemberCardOrder.setStatus(ElectricityMemberCardOrder.STATUS_INIT);
        electricityMemberCardOrder.setMemberCardId(integratedPaymentAdd.getMemberCardId());
        electricityMemberCardOrder.setUid(userInfo.getUid());
        electricityMemberCardOrder.setMaxUseCount(electricityMemberCard.getMaxUseCount());
        electricityMemberCardOrder.setMemberCardType(electricityMemberCard.getType());
        electricityMemberCardOrder.setCardName(electricityMemberCard.getName());
        electricityMemberCardOrder.setPayAmount(payAmount);
        electricityMemberCardOrder.setUserName(userInfo.getName());
        electricityMemberCardOrder.setValidDays(electricityMemberCard.getValidDays());
        electricityMemberCardOrder.setTenantId(electricityMemberCard.getTenantId());
        electricityMemberCardOrder.setFranchiseeId(integratedPaymentAdd.getFranchiseeId());
        electricityMemberCardOrder.setIsBindActivity(electricityMemberCard.getIsBindActivity());
        electricityMemberCardOrder.setActivityId(electricityMemberCard.getActivityId());
        electricityMemberCardOrder.setPayCount(payCount);
        electricityMemberCardOrder.setSource(source);
        electricityMemberCardOrder.setRefId(refId);
        if (Objects.nonNull(integratedPaymentAdd.getUserCouponId())) {
            electricityMemberCardOrder.setCouponId(integratedPaymentAdd.getUserCouponId().longValue());
        }

        List<Object> list = new ArrayList<>();
        list.add(electricityMemberCardOrder);
        list.add(userCoupon);

        return Triple.of(true, null, list);
    }

    private Triple<Boolean, String, Object> generateInsuranceOrder(UserInfo userInfo, Integer insuranceId) {

        if (Objects.isNull(insuranceId)) {
            return Triple.of(true, "", null);
        }

        //查询保险
        FranchiseeInsurance franchiseeInsurance = franchiseeInsuranceService.queryByCache(insuranceId);

        if (Objects.isNull(franchiseeInsurance)) {
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
                .insuranceType(InsuranceOrder.BATTERY_INSURANCE_TYPE)
                .orderId(insuranceOrderId)
                .cid(franchiseeInsurance.getCid())
                .franchiseeId(franchiseeInsurance.getFranchiseeId())
                .isUse(InsuranceOrder.NOT_USE)
                .payAmount(franchiseeInsurance.getPremium())
                .forehead(franchiseeInsurance.getForehead())
                .payType(InsuranceOrder.ONLINE_PAY_TYPE)
                .phone(userInfo.getPhone())
                .status(InsuranceOrder.STATUS_INIT)
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
