package com.xiliulou.electricity.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.config.WechatConfig;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.dto.ActivityProcessDTO;
import com.xiliulou.electricity.dto.DivisionAccountOrderDTO;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.enums.ActivityEnum;
import com.xiliulou.electricity.enums.DivisionAccountEnum;
import com.xiliulou.electricity.enums.PackageTypeEnum;
import com.xiliulou.electricity.mapper.ElectricityMemberCardOrderMapper;
import com.xiliulou.electricity.mapper.ElectricityTradeOrderMapper;
import com.xiliulou.electricity.mq.producer.ActivityProducer;
import com.xiliulou.electricity.mq.producer.DivisionAccountProducer;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.service.car.CarRentalPackageDepositPayService;
import com.xiliulou.electricity.service.car.CarRentalPackageMemberTermService;
import com.xiliulou.electricity.service.car.CarRentalPackageOrderService;
import com.xiliulou.electricity.service.car.biz.CarRentalPackageOrderBizService;
import com.xiliulou.mq.service.RocketMqService;
import com.xiliulou.pay.weixinv3.dto.WechatJsapiOrderCallBackResource;
import com.xiliulou.pay.weixinv3.dto.WechatJsapiOrderResultDTO;
import com.xiliulou.pay.weixinv3.exception.WechatPayException;
import com.xiliulou.pay.weixinv3.query.WechatV3OrderQuery;
import com.xiliulou.pay.weixinv3.service.WechatV3JsapiService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shaded.org.apache.commons.lang3.StringUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @program: XILIULOU
 * @description:
 * @author: Mr.YG
 * @create: 2020-12-03 11:34
 **/
@Service
@Slf4j
public class ElectricityTradeOrderServiceImpl extends
        ServiceImpl<ElectricityTradeOrderMapper, ElectricityTradeOrder> implements ElectricityTradeOrderService {

    @Resource
    private CarRentalPackageOrderBizService carRentalPackageOrderBizService;

    @Resource
    private RocketMqService rocketMqService;

    @Resource
    private CarRentalPackageMemberTermService carRentalPackageMemberTermService;

    @Resource
    private CarRentalPackageDepositPayService carRentalPackageDepositPayService;

    @Resource
    private CarRentalPackageOrderService carRentalPackageOrderService;

    @Resource
    ElectricityMemberCardOrderMapper electricityMemberCardOrderMapper;
    @Autowired
    UserInfoService userInfoService;
    @Autowired
    EleDepositOrderService eleDepositOrderService;
    @Autowired
    WechatConfig wechatConfig;
    @Autowired
    WechatV3JsapiService wechatV3JsapiService;
    @Autowired
    JoinShareActivityRecordService joinShareActivityRecordService;
    @Autowired
    ShareActivityRecordService shareActivityRecordService;
    @Autowired
    JoinShareActivityHistoryService joinShareActivityHistoryService;
    @Autowired
    UserCouponService userCouponService;
    @Autowired
    StoreService storeService;
    @Autowired
    FranchiseeService franchiseeService;
    @Autowired
    FranchiseeAmountService franchiseeAmountService;
    @Autowired
    StoreAmountService storeAmountService;
    @Autowired
    JoinShareMoneyActivityRecordService joinShareMoneyActivityRecordService;
    @Autowired
    ShareMoneyActivityRecordService shareMoneyActivityRecordService;
    @Autowired
    JoinShareMoneyActivityHistoryService joinShareMoneyActivityHistoryService;
    @Autowired
    ShareMoneyActivityService shareMoneyActivityService;
    @Autowired
    ElectricityMemberCardService electricityMemberCardService;
    @Autowired
    OldUserActivityService oldUserActivityService;
    @Autowired
    UserAmountService userAmountService;
    @Autowired
    EleBatteryServiceFeeOrderService eleBatteryServiceFeeOrderService;
    @Autowired
    InsuranceOrderService insuranceOrderService;
    @Autowired
    InsuranceUserInfoService insuranceUserInfoService;
    @Autowired
    FranchiseeInsuranceService franchiseeInsuranceService;
    @Autowired
    ServiceFeeUserInfoService serviceFeeUserInfoService;
    @Autowired
    EleDisableMemberCardRecordService eleDisableMemberCardRecordService;
    @Autowired
    EnableMemberCardRecordService enableMemberCardRecordService;
    @Autowired
    RedisService redisService;
    @Autowired
    UserBatteryDepositService userBatteryDepositService;
    @Autowired
    UserCarDepositService userCarDepositService;
    @Autowired
    UserCarMemberCardService userCarMemberCardService;
    @Autowired
    UserBatteryService userBatteryService;
    @Autowired
    UserBatteryMemberCardService userBatteryMemberCardService;
    @Autowired
    UserCarService userCarService;
    @Autowired
    ElectricityMemberCardOrderService electricityMemberCardOrderService;
    @Autowired
    CarDepositOrderService carDepositOrderService;
    @Autowired
    CarMemberCardOrderService carMemberCardOrderService;
    @Autowired
    ElectricityCarService electricityCarService;
    @Autowired
    ShippingManagerService shippingManagerService;
    @Autowired
    DivisionAccountRecordService divisionAccountRecordService;

    @Autowired
    ChannelActivityHistoryService channelActivityHistoryService;

    @Autowired
    ElectricityConfigService electricityConfigService;

    @Autowired
    CarLockCtrlHistoryService carLockCtrlHistoryService;

    @Autowired
    ShareActivityMemberCardService shareActivityMemberCardService;

    @Autowired
    InvitationActivityRecordService invitationActivityRecordService;

    @Autowired
    BatteryMemberCardOrderCouponService memberCardOrderCouponService;

    @Autowired
    BatteryMemberCardService batteryMemberCardService;

    @Autowired
    DivisionAccountProducer divisionAccountProducer;

    @Autowired
    ActivityProducer activityProducer;

    @Autowired
    ActivityService activityService;

    /**
     * 租车套餐购买回调
     *
     * @param callBackResource
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Pair<Boolean, Object> notifyCarRenalPackageOrder(WechatJsapiOrderCallBackResource callBackResource) {
        log.info("notifyCarRenalPackageOrder params callBackResource is {}", JSON.toJSONString(callBackResource));
        if (ObjectUtils.isEmpty(callBackResource)) {
            log.error("NotifyCarRenalPackageOrder failed, callBackResource is empty");
            return Pair.of(false, "参数为空");
        }

        //回调参数
        String tradeOrderNo = callBackResource.getOutTradeNo();
        String tradeState = callBackResource.getTradeState();
        String transactionId = callBackResource.getTransactionId();

        // 支付状态
        Integer tradeOrderStatus = WechatJsapiOrderCallBackResource.TRADE_STATUS_SUCCESS.equals(tradeState) ? ElectricityTradeOrder.STATUS_SUCCESS : ElectricityTradeOrder.STATUS_FAIL;

        // 1. 处理交易流水订单
        ElectricityTradeOrder electricityTradeOrder = baseMapper.selectTradeOrderByTradeOrderNo(tradeOrderNo);
        if (Objects.isNull(electricityTradeOrder)) {
            log.error("NotifyCarRenalPackageOrder failed, not found electricity_trade_order, trade_order_no is {}", tradeOrderNo);
            return Pair.of(false, "未找到交易流水订单");
        }

        if (ObjectUtil.notEqual(ElectricityTradeOrder.STATUS_INIT, electricityTradeOrder.getStatus())) {
            log.error("NotifyCarRenalPackageOrder failed, electricity_trade_order processed, trade_order_no is {}", tradeOrderNo);
            return Pair.of(false, "交易流水订单已处理");
        }

        ElectricityTradeOrder electricityTradeOrderUpdate = new ElectricityTradeOrder();
        electricityTradeOrderUpdate.setId(electricityTradeOrder.getId());
        electricityTradeOrderUpdate.setStatus(tradeOrderStatus);
        electricityTradeOrderUpdate.setUpdateTime(System.currentTimeMillis());
        electricityTradeOrderUpdate.setChannelOrderNo(transactionId);
        baseMapper.updateById(electricityTradeOrderUpdate);


        // 租车套餐购买订单编码
        String orderNo = electricityTradeOrder.getOrderNo();
        // 租户ID
        Integer tenantId = electricityTradeOrder.getTenantId();
        // 用户ID
        Long uid = electricityTradeOrder.getUid();

        if (WechatJsapiOrderCallBackResource.TRADE_STATUS_SUCCESS.equals(tradeState)) {
            return handSuccess(orderNo, tenantId, uid, transactionId);
        } else {
            return handFailed(orderNo, tenantId, uid);
        }
    }

    /**
     * 租车套餐支付回调-支付失败
     * @param orderNo 租车套餐购买订单编号
     * @param tenantId 租户ID
     * @param uid 用户ID
     * @return
     */
    private Pair<Boolean, Object> handFailed(String orderNo, Integer tenantId, Long uid) {
        return carRentalPackageOrderBizService.handBuyRentalPackageOrderFailed(orderNo, tenantId, uid);
    }

    /**
     * 租车套餐支付回调-支付成功
     * @param orderNo 租车套餐购买订单编号
     * @param tenantId 租户ID
     * @param uid 用户ID
     * @return
     */
    private Pair<Boolean, Object> handSuccess(String orderNo, Integer tenantId, Long uid, String transactionId) {
        Pair<Boolean, Object> pair = carRentalPackageOrderBizService.handBuyRentalPackageOrderSuccess(orderNo, tenantId, uid, null);
        if (!pair.getLeft()) {
            return pair;
        }

        // 最后一步，小程序虚拟发货
        String phone = pair.getRight().toString();
        shippingManagerService.uploadShippingInfo(uid, phone, transactionId, tenantId);

        return Pair.of(true, null);
    }

    @Override
    public WechatJsapiOrderResultDTO commonCreateTradeOrderAndGetPayParams(CommonPayOrder commonOrder, ElectricityPayParams electricityPayParams, String openId, HttpServletRequest request) throws WechatPayException {
        log.info("commonCreateTradeOrderAndGetPayParams paymentAmount is {}", commonOrder.getPayAmount());

        //生成支付订单
        String ip = request.getRemoteAddr();
        ElectricityTradeOrder electricityTradeOrder = new ElectricityTradeOrder();
        electricityTradeOrder.setOrderNo(commonOrder.getOrderId());
        electricityTradeOrder.setTradeOrderNo(String.valueOf(System.currentTimeMillis()));
        electricityTradeOrder.setClientId(ip);
        electricityTradeOrder.setCreateTime(System.currentTimeMillis());
        electricityTradeOrder.setUpdateTime(System.currentTimeMillis());
        electricityTradeOrder.setOrderType(commonOrder.getOrderType());
        electricityTradeOrder.setStatus(ElectricityTradeOrder.STATUS_INIT);
        electricityTradeOrder.setTotalFee(commonOrder.getPayAmount().multiply(new BigDecimal(100)));
        electricityTradeOrder.setUid(commonOrder.getUid());
        electricityTradeOrder.setTenantId(commonOrder.getTenantId());
        baseMapper.insert(electricityTradeOrder);

        //支付参数
        WechatV3OrderQuery wechatV3OrderQuery = new WechatV3OrderQuery();
        wechatV3OrderQuery.setOrderId(electricityTradeOrder.getTradeOrderNo());
        wechatV3OrderQuery.setTenantId(electricityTradeOrder.getTenantId());
        wechatV3OrderQuery.setNotifyUrl(wechatConfig.getPayCallBackUrl() + electricityTradeOrder.getTenantId());
        wechatV3OrderQuery.setExpireTime(System.currentTimeMillis() + 3600000);
        wechatV3OrderQuery.setOpenId(openId);
        wechatV3OrderQuery.setDescription(commonOrder.getDescription());
        wechatV3OrderQuery.setCurrency("CNY");
        wechatV3OrderQuery.setAttach(commonOrder.getAttach());
        wechatV3OrderQuery.setAmount(commonOrder.getPayAmount().multiply(new BigDecimal(100)).intValue());
        wechatV3OrderQuery.setAppid(electricityPayParams.getMerchantMinProAppId());
        log.info("wechatV3OrderQuery is -->{}", wechatV3OrderQuery);
        return wechatV3JsapiService.order(wechatV3OrderQuery);

    }

    /**
     * 处理月卡回调
     *
     * @param callBackResource
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Pair<Boolean, Object> notifyMemberOrder(WechatJsapiOrderCallBackResource callBackResource) {
        //回调参数
        String tradeOrderNo = callBackResource.getOutTradeNo();
        String tradeState = callBackResource.getTradeState();
        String transactionId = callBackResource.getTransactionId();

        //交易订单
        ElectricityTradeOrder electricityTradeOrder = baseMapper.selectTradeOrderByTradeOrderNo(tradeOrderNo);
        if (Objects.isNull(electricityTradeOrder)) {
            log.error("NOTIFY_MEMBER_ORDER ERROR ,NOT FOUND ELECTRICITY_TRADE_ORDER ORDER_NO={}", tradeOrderNo);
            return Pair.of(false, "未找到交易订单!");
        }

        if (ObjectUtil.notEqual(ElectricityTradeOrder.STATUS_INIT, electricityTradeOrder.getStatus())) {
            log.error("NOTIFY_MEMBER_ORDER ERROR , ELECTRICITY_TRADE_ORDER  STATUS IS NOT INIT, ORDER_NO={}", tradeOrderNo);
            return Pair.of(false, "交易订单已处理");
        }

        //购卡订单
        ElectricityMemberCardOrder electricityMemberCardOrder = electricityMemberCardOrderMapper.selectByOrderNo(electricityTradeOrder.getOrderNo());
        if (ObjectUtil.isEmpty(electricityMemberCardOrder)) {
            log.error("NOTIFY_MEMBER_ORDER ERROR ,NOT FOUND ELECTRICITY_MEMBER_CARD_ORDER ORDER_NO={}", electricityTradeOrder.getOrderNo());
            return Pair.of(false, "未找到订单!");
        }

        //处理用户端取消支付的问题
        if(Objects.equals(ElectricityMemberCardOrder.STATUS_CANCELL, electricityMemberCardOrder.getStatus())){
            electricityMemberCardOrder.setStatus(ElectricityMemberCardOrder.STATUS_INIT);
        }

        if (!ObjectUtil.equal(ElectricityMemberCardOrder.STATUS_INIT, electricityMemberCardOrder.getStatus())) {
            log.error("NOTIFY_MEMBER_ORDER ERROR , ELECTRICITY_MEMBER_CARD_ORDER  STATUS IS NOT INIT, ORDER_NO={}", electricityTradeOrder.getOrderNo());
            return Pair.of(false, "套餐订单已处理!");
        }

        //获取套餐订单优惠券
        List<Long> userCouponIds = memberCardOrderCouponService.selectCouponIdsByOrderId(electricityMemberCardOrder.getOrderId());

        //成功或失败
        Integer tradeOrderStatus = ElectricityTradeOrder.STATUS_FAIL;
        Integer memberOrderStatus = ElectricityMemberCardOrder.STATUS_FAIL;
        boolean result = false;
        if (StringUtils.isNotEmpty(tradeState) && ObjectUtil.equal("SUCCESS", tradeState)) {
            tradeOrderStatus = ElectricityTradeOrder.STATUS_SUCCESS;
            memberOrderStatus = ElectricityMemberCardOrder.STATUS_SUCCESS;
            result = true;
        } else {
            log.error("NOTIFY REDULT PAY FAIL,ORDER_NO={}" + tradeOrderNo);
        }

        //用户
        UserInfo userInfo = userInfoService.queryByUidFromCache(electricityMemberCardOrder.getUid());
        if (Objects.isNull(userInfo)) {
            log.error("NOTIFY  ERROR,NOT FOUND USERINFO,USERID={},ORDER_NO={}", electricityMemberCardOrder.getUid(), tradeOrderNo);
            return Pair.of(false, "未找到用户信息!");
        }

        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(electricityMemberCardOrder.getUid());

        Long now = System.currentTimeMillis();
        Long memberCardExpireTime;
        Long remainingNumber = electricityMemberCardOrder.getMaxUseCount();
        Integer payCount = electricityMemberCardOrderService.queryMaxPayCount(userBatteryMemberCard);

        //月卡订单
        ElectricityMemberCardOrder electricityMemberCardOrderUpdate = new ElectricityMemberCardOrder();

        if (Objects.equals(memberOrderStatus, EleDepositOrder.STATUS_SUCCESS)) {

            BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(electricityMemberCardOrder.getMemberCardId());
            if (Objects.isNull(batteryMemberCard)) {
                log.error("NOTIFY ERROR!not found batteryMemberCard,uid={},mid={}", electricityMemberCardOrder.getUid(), electricityMemberCardOrder.getMemberCardId());
                return Pair.of(false, "换电套餐不存在!");
            }

            electricityMemberCardOrderService.handlerBatteryMembercardPaymentNotify(batteryMemberCard, electricityMemberCardOrder,  userBatteryMemberCardService.selectByUidFromCache(electricityMemberCardOrder.getUid()),  userInfo);

            if(CollectionUtils.isNotEmpty(userCouponIds)){
                Set<Integer> couponIds=userCouponIds.parallelStream().map(Long::intValue).collect(Collectors.toSet());
                userCouponService.batchUpdateUserCoupon(electricityMemberCardOrderService.buildUserCouponList(couponIds, UserCoupon.STATUS_USED, electricityMemberCardOrder.getOrderId()));
            }
            // 8. 处理分账
            DivisionAccountOrderDTO divisionAccountOrderDTO = new DivisionAccountOrderDTO();
            divisionAccountOrderDTO.setOrderNo(electricityMemberCardOrder.getOrderId());
            divisionAccountOrderDTO.setType(PackageTypeEnum.PACKAGE_TYPE_BATTERY.getCode());
            divisionAccountOrderDTO.setDivisionAccountType(DivisionAccountEnum.DA_TYPE_PURCHASE.getCode());
            divisionAccountOrderDTO.setTraceId(IdUtil.simpleUUID());
            divisionAccountRecordService.asyncHandleDivisionAccount(divisionAccountOrderDTO);

            // 9. 处理活动
            ActivityProcessDTO activityProcessDTO = new ActivityProcessDTO();
            activityProcessDTO.setOrderNo(electricityMemberCardOrder.getOrderId());
            activityProcessDTO.setType(PackageTypeEnum.PACKAGE_TYPE_BATTERY.getCode());
            activityProcessDTO.setActivityType(ActivityEnum.INVITATION_CRITERIA_BUY_PACKAGE.getCode());
            activityProcessDTO.setTraceId(IdUtil.simpleUUID());
            activityService.asyncProcessActivity(activityProcessDTO);
            //TODO 发送MQ 更新优惠券状态 处理活动 分帐 相关

            electricityMemberCardOrderService.sendUserCoupon(batteryMemberCard, electricityMemberCardOrder);

            shippingManagerService.uploadShippingInfo(userInfo.getUid(), userInfo.getPhone(), transactionId, userInfo.getTenantId());
        } else {

            if(CollectionUtils.isNotEmpty(userCouponIds)){
                Set<Integer> couponIds=userCouponIds.parallelStream().map(Long::intValue).collect(Collectors.toSet());
                userCouponService.batchUpdateUserCoupon(electricityMemberCardOrderService.buildUserCouponList(couponIds, UserCoupon.STATUS_UNUSED, electricityMemberCardOrder.getOrderId()));
            }

            electricityMemberCardOrderUpdate.setRefId(NumberConstant.ZERO_L);
            electricityMemberCardOrderUpdate.setSource(NumberConstant.ZERO);
        }

        //交易订单
        ElectricityTradeOrder electricityTradeOrderUpdate = new ElectricityTradeOrder();
        electricityTradeOrderUpdate.setId(electricityTradeOrder.getId());
        electricityTradeOrderUpdate.setStatus(tradeOrderStatus);
        electricityTradeOrderUpdate.setUpdateTime(System.currentTimeMillis());
        electricityTradeOrderUpdate.setChannelOrderNo(transactionId);
        baseMapper.updateById(electricityTradeOrderUpdate);

        electricityMemberCardOrderUpdate.setId(electricityMemberCardOrder.getId());
        electricityMemberCardOrderUpdate.setStatus(memberOrderStatus);
        electricityMemberCardOrderUpdate.setUpdateTime(System.currentTimeMillis());
        electricityMemberCardOrderUpdate.setPayCount(payCount + 1);
        electricityMemberCardOrderMapper.updateById(electricityMemberCardOrderUpdate);

        return Pair.of(result, null);
    }

    //押金支付回调
    @Override
    public Pair<Boolean, Object> notifyDepositOrder(WechatJsapiOrderCallBackResource callBackResource) {
        //回调参数
        String tradeOrderNo = callBackResource.getOutTradeNo();
        String tradeState = callBackResource.getTradeState();
        String transactionId = callBackResource.getTransactionId();

        //系统订单
        ElectricityTradeOrder electricityTradeOrder = baseMapper.selectTradeOrderByTradeOrderNo(tradeOrderNo);
        if (Objects.isNull(electricityTradeOrder)) {
            log.error("DEPOSIT NOTIFY ERROR!not found electricity trade order,orderNo={}", tradeOrderNo);
            return Pair.of(false, "未找到交易订单!");
        }
        if (ObjectUtil.notEqual(ElectricityTradeOrder.STATUS_INIT, electricityTradeOrder.getStatus())) {
            log.error("DEPOSIT NOTIFY ERROR! electricity trade order  status is not init, orderNo={}", tradeOrderNo);
            return Pair.of(false, "交易订单已处理");
        }

        //押金订单
        EleDepositOrder eleDepositOrder = eleDepositOrderService.queryByOrderId(electricityTradeOrder.getOrderNo());
        if (ObjectUtil.isEmpty(eleDepositOrder)) {
            log.error("DEPOSIT NOTIFY ERROR!not found electricity deposit order orderNo={}", electricityTradeOrder.getOrderNo());
            return Pair.of(false, "未找到订单!");
        }

        if (!ObjectUtil.equal(EleDepositOrder.STATUS_INIT, eleDepositOrder.getStatus())) {
            log.error("DEPOSIT NOTIFY ERROR!electricity_deposit_order  status is not init,orderNo={}", electricityTradeOrder.getOrderNo());
            return Pair.of(false, "押金订单已处理!");
        }

        Integer tradeOrderStatus = ElectricityTradeOrder.STATUS_FAIL;
        Integer depositOrderStatus = EleDepositOrder.STATUS_FAIL;
        boolean result = false;
        if (StringUtils.isNotEmpty(tradeState) && ObjectUtil.equal("SUCCESS", tradeState)) {
            tradeOrderStatus = ElectricityTradeOrder.STATUS_SUCCESS;
            depositOrderStatus = EleDepositOrder.STATUS_SUCCESS;
            result = true;
        } else {
            log.error("DEPOSIT NOTIFY ERROR!notify redult pay fail,orderNo={}" + tradeOrderNo);
        }

        UserInfo userInfo = userInfoService.queryByUidFromCache(eleDepositOrder.getUid());
        if (Objects.isNull(userInfo)) {
            log.error("DEPOSIT NOTIFY ERROR!not found userinfo,userId={},orderNo={}", eleDepositOrder.getUid(), tradeOrderNo);
            return Pair.of(false, "未找到用户信息!");
        }

        //用户押金
        if (Objects.equals(depositOrderStatus, EleDepositOrder.STATUS_SUCCESS)) {

            UserInfo updateUserInfo = new UserInfo();
            updateUserInfo.setUid(userInfo.getUid());
            updateUserInfo.setBatteryDepositStatus(UserInfo.BATTERY_DEPOSIT_STATUS_YES);
            updateUserInfo.setUpdateTime(System.currentTimeMillis());
            updateUserInfo.setFranchiseeId(eleDepositOrder.getFranchiseeId());
            userInfoService.updateByUid(updateUserInfo);

            UserBatteryDeposit userBatteryDeposit = new UserBatteryDeposit();
            userBatteryDeposit.setUid(userInfo.getUid());
            userBatteryDeposit.setBatteryDeposit(eleDepositOrder.getPayAmount());
            userBatteryDeposit.setDid(eleDepositOrder.getId());
            userBatteryDeposit.setOrderId(eleDepositOrder.getOrderId());
            userBatteryDeposit.setDelFlag(UserBatteryDeposit.DEL_NORMAL);
            userBatteryDeposit.setApplyDepositTime(System.currentTimeMillis());
            userBatteryDeposit.setDepositType(UserBatteryDeposit.DEPOSIT_TYPE_DEFAULT);
            userBatteryDeposit.setTenantId(eleDepositOrder.getTenantId());
            userBatteryDeposit.setCreateTime(System.currentTimeMillis());
            userBatteryDeposit.setUpdateTime(System.currentTimeMillis());
            userBatteryDepositService.insertOrUpdate(userBatteryDeposit);

        }

        //交易订单
        ElectricityTradeOrder electricityTradeOrderUpdate = new ElectricityTradeOrder();
        electricityTradeOrderUpdate.setId(electricityTradeOrder.getId());
        electricityTradeOrderUpdate.setStatus(tradeOrderStatus);
        electricityTradeOrderUpdate.setUpdateTime(System.currentTimeMillis());
        electricityTradeOrderUpdate.setChannelOrderNo(transactionId);
        baseMapper.updateById(electricityTradeOrderUpdate);

        //押金订单
        EleDepositOrder eleDepositOrderUpdate = new EleDepositOrder();
        eleDepositOrderUpdate.setId(eleDepositOrder.getId());
        eleDepositOrderUpdate.setStatus(depositOrderStatus);
        eleDepositOrderUpdate.setUpdateTime(System.currentTimeMillis());
        eleDepositOrderService.update(eleDepositOrderUpdate);

        //小程序虚拟发货
        shippingManagerService
                .uploadShippingInfo(userInfo.getUid(), userInfo.getPhone(), transactionId, userInfo.getTenantId());

        return Pair.of(result, null);
    }

    @Override
    public Pair<Boolean, Object> notifyBatteryServiceFeeOrder(WechatJsapiOrderCallBackResource callBackResource) {
        //回调参数
        String tradeOrderNo = callBackResource.getOutTradeNo();
        String tradeState = callBackResource.getTradeState();
        String transactionId = callBackResource.getTransactionId();

        //系统订单
        ElectricityTradeOrder electricityTradeOrder = baseMapper.selectTradeOrderByTradeOrderNo(tradeOrderNo);
        if (Objects.isNull(electricityTradeOrder)) {
            log.error("NOTIFY_BATTERY_SERVICE_FEE_ORDER ERROR ,NOT FOUND ELECTRICITY_TRADE_ORDER orderNo={}", tradeOrderNo);
            return Pair.of(false, "未找到交易订单!");
        }
        if (ObjectUtil.notEqual(ElectricityTradeOrder.STATUS_INIT, electricityTradeOrder.getStatus())) {
            log.error("NOTIFY_BATTERY_SERVICE_FEE_ORDER ERROR , ELECTRICITY_TRADE_ORDER  STATUS IS NOT INIT, orderNo={}", tradeOrderNo);
            return Pair.of(false, "交易订单已处理");
        }
        //电池服务费订单
        EleBatteryServiceFeeOrder eleBatteryServiceFeeOrder = eleBatteryServiceFeeOrderService.queryEleBatteryServiceFeeOrderByOrderId(electricityTradeOrder.getOrderNo());
        if (ObjectUtil.isEmpty(eleBatteryServiceFeeOrder)) {
            log.error("NOTIFY_BATTERY_SERVICE_FEE_ORDER ERROR ,NOT FOUND ELECTRICITY_DEPOSIT_ORDER orderNo={}", electricityTradeOrder.getOrderNo());
            return Pair.of(false, "未找到订单!");
        }

        if (!ObjectUtil.equal(EleBatteryServiceFeeOrder.STATUS_INIT, eleBatteryServiceFeeOrder.getStatus())) {
            log.error("NOTIFY_BATTERY_SERVICE_FEE_ORDER ERROR , ELECTRICITY_DEPOSIT_ORDER  STATUS IS NOT INIT, orderNo={}", electricityTradeOrder.getOrderNo());
            return Pair.of(false, "订单已处理!");
        }

        Integer tradeOrderStatus = ElectricityTradeOrder.STATUS_FAIL;
        Integer eleBatteryServiceFeeOrderStatus = EleBatteryServiceFeeOrder.STATUS_FAIL;
        boolean result = false;
        if (StringUtils.isNotEmpty(tradeState) && ObjectUtil.equal("SUCCESS", tradeState)) {
            tradeOrderStatus = ElectricityTradeOrder.STATUS_SUCCESS;
            eleBatteryServiceFeeOrderStatus = EleBatteryServiceFeeOrder.STATUS_SUCCESS;
            result = true;
        } else {
            log.error("NOTIFY REDULT PAY FAIL,ORDER_NO={}" + tradeOrderNo);
        }

        UserInfo userInfo = userInfoService.queryByUidFromCache(eleBatteryServiceFeeOrder.getUid());
        if (Objects.isNull(userInfo)) {
            log.error("NOTIFY  ERROR,NOT FOUND USERINFO,USERID={},orderNo={}", eleBatteryServiceFeeOrder.getUid(), tradeOrderNo);
            return Pair.of(false, "未找到用户信息!");
        }

        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(userInfo.getUid());
        if(Objects.isNull(userBatteryMemberCard)){
            log.error("BATTERY SERVICE FEE NOTIFY ERROR!not found userBatteryMemberCard,uid={},orderNo={}", eleBatteryServiceFeeOrder.getUid(), tradeOrderNo);
            return Pair.of(false, "未找到用户信息!");
        }

        BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(userBatteryMemberCard.getMemberCardId());
        if(Objects.isNull(batteryMemberCard)){
            log.error("BATTERY SERVICE FEE NOTIFY ERROR!not found batteryMemberCard,uid={},mid={}", eleBatteryServiceFeeOrder.getUid(), userBatteryMemberCard.getMemberCardId());
            return Pair.of(false, "未找到套餐信息!");
        }

        ServiceFeeUserInfo serviceFeeUserInfo = serviceFeeUserInfoService.queryByUidFromCache(userInfo.getUid());
        if(Objects.isNull(serviceFeeUserInfo)){
            log.error("BATTERY SERVICE FEE NOTIFY ERROR!not found serviceFeeUserInfo,uid={},orderNo={}", eleBatteryServiceFeeOrder.getUid(), tradeOrderNo);
            return Pair.of(false, "未找到用户信息!");
        }

        //电池服务费订单
        EleBatteryServiceFeeOrder eleBatteryServiceFeeOrderUpdate = new EleBatteryServiceFeeOrder();
        eleBatteryServiceFeeOrderUpdate.setBatteryServiceFeeGenerateTime(userBatteryMemberCard.getMemberCardExpireTime() - (24 * 60 * 60 * 1000L));
        eleBatteryServiceFeeOrderUpdate.setBatteryServiceFeeEndTime(System.currentTimeMillis());

        if (Objects.equals(eleBatteryServiceFeeOrderStatus, EleDepositOrder.STATUS_SUCCESS)) {

            UserBatteryMemberCard userBatteryMemberCardUpdate = new UserBatteryMemberCard();
            EleDisableMemberCardRecord eleDisableMemberCardRecord = eleDisableMemberCardRecordService.queryByDisableMemberCardNo(serviceFeeUserInfo.getDisableMemberCardNo(),userInfo.getTenantId());

            //如果是限时间停卡，服务费的开始产生时间应拿当时停卡记录的停卡时间
            if (Objects.nonNull(eleDisableMemberCardRecord) && Objects.nonNull(serviceFeeUserInfo) && Objects.equals(eleDisableMemberCardRecord.getDisableMemberCardNo(), serviceFeeUserInfo.getDisableMemberCardNo())) {
                eleBatteryServiceFeeOrderUpdate.setBatteryServiceFeeGenerateTime(eleDisableMemberCardRecord.getCreateTime());
            }

            if (Objects.equals(userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_DISABLE)) {
                Long memberCardExpireTime = System.currentTimeMillis() + (userBatteryMemberCard.getMemberCardExpireTime() - userBatteryMemberCard.getDisableMemberCardTime());
                userBatteryMemberCardUpdate.setMemberCardExpireTime(memberCardExpireTime);

                eleBatteryServiceFeeOrderUpdate.setBatteryServiceFeeGenerateTime(userBatteryMemberCard.getDisableMemberCardTime());
                if (Objects.equals(eleDisableMemberCardRecord.getDisableCardTimeType(), EleDisableMemberCardRecord.DISABLE_CARD_LIMIT_TIME)) {

                    Integer disableDays = eleDisableMemberCardRecord.getChooseDays();
                    if (Objects.nonNull(eleDisableMemberCardRecord.getRealDays())) {
                        disableDays = eleDisableMemberCardRecord.getRealDays();
                    }
                    eleBatteryServiceFeeOrderUpdate.setBatteryServiceFeeEndTime(userBatteryMemberCard.getDisableMemberCardTime() + (disableDays * (24 * 60 * 60 * 1000L)));
                }


                EnableMemberCardRecord enableMemberCardRecord = enableMemberCardRecordService.queryByDisableCardNO(eleDisableMemberCardRecord.getDisableMemberCardNo(), userInfo.getTenantId());
                Long cardDays = (System.currentTimeMillis() - userBatteryMemberCard.getDisableMemberCardTime()) / 1000L / 60 / 60 / 24;
                if (Objects.isNull(enableMemberCardRecord)) {
                    EnableMemberCardRecord enableMemberCardRecordInsert = EnableMemberCardRecord.builder()
                            .disableMemberCardNo(eleDisableMemberCardRecord.getDisableMemberCardNo())
                            .memberCardName(batteryMemberCard.getName())
                            .enableTime(System.currentTimeMillis())
                            .enableType(EnableMemberCardRecord.ARTIFICIAL_ENABLE)
                            .batteryServiceFeeStatus(EnableMemberCardRecord.STATUS_SUCCESS)
                            .disableDays(cardDays.intValue())
                            .disableTime(eleDisableMemberCardRecord.getCreateTime())
                            .franchiseeId(userInfo.getFranchiseeId())
                            .phone(userInfo.getPhone())
                            .serviceFee(eleBatteryServiceFeeOrder.getBatteryServiceFee())
                            .createTime(System.currentTimeMillis())
                            .tenantId(userInfo.getTenantId())
                            .uid(userInfo.getUid())
                            .userName(userInfo.getName())
                            .updateTime(System.currentTimeMillis()).build();
                    enableMemberCardRecordService.insert(enableMemberCardRecordInsert);
                } else {
                    EnableMemberCardRecord enableMemberCardRecordUpdate = new EnableMemberCardRecord();
                    enableMemberCardRecordUpdate.setId(enableMemberCardRecord.getId());
                    enableMemberCardRecordUpdate.setDisableDays(cardDays.intValue());
                    enableMemberCardRecordUpdate.setServiceFee(eleBatteryServiceFeeOrder.getBatteryServiceFee());
                    enableMemberCardRecordUpdate.setBatteryServiceFeeStatus(EnableMemberCardRecord.STATUS_SUCCESS);
                    enableMemberCardRecordUpdate.setUpdateTime(System.currentTimeMillis());
                    enableMemberCardRecordService.update(enableMemberCardRecordUpdate);
                }


            }
            userBatteryMemberCardUpdate.setUid(userInfo.getUid());
            userBatteryMemberCardUpdate.setUpdateTime(System.currentTimeMillis());
            userBatteryMemberCardUpdate.setDisableMemberCardTime(null);
            userBatteryMemberCardUpdate.setMemberCardStatus(UserBatteryMemberCard.MEMBER_CARD_NOT_DISABLE);
            userBatteryMemberCardService.updateByUidForDisableCard(userBatteryMemberCardUpdate);

            ServiceFeeUserInfo serviceFeeUserInfoUpdate = new ServiceFeeUserInfo();
            serviceFeeUserInfoUpdate.setUid(userInfo.getUid());
            serviceFeeUserInfoUpdate.setDisableMemberCardNo("");
            serviceFeeUserInfoUpdate.setOrderNo("");
            serviceFeeUserInfoUpdate.setUpdateTime(System.currentTimeMillis());
            serviceFeeUserInfoUpdate.setTenantId(serviceFeeUserInfo.getTenantId());
            if (Objects.equals(userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_DISABLE)) {
                Long memberCardExpireTime = System.currentTimeMillis() + (userBatteryMemberCard.getMemberCardExpireTime() - userBatteryMemberCard.getDisableMemberCardTime());
                serviceFeeUserInfoUpdate.setServiceFeeGenerateTime(memberCardExpireTime);
            } else {
                if (userBatteryMemberCard.getMemberCardExpireTime() < System.currentTimeMillis()) {
                    serviceFeeUserInfoUpdate.setServiceFeeGenerateTime(System.currentTimeMillis());
                } else {
                    serviceFeeUserInfoUpdate.setServiceFeeGenerateTime(userBatteryMemberCard.getMemberCardExpireTime());
                }
            }

            serviceFeeUserInfoService.updateByUid(serviceFeeUserInfoUpdate);

            if (Objects.nonNull(eleDisableMemberCardRecord) && Objects.nonNull(userBatteryMemberCard.getDisableMemberCardTime()) && Objects.equals(userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_DISABLE)) {
                Long now = System.currentTimeMillis();
                //判断用户是否产生电池服务费
                Long cardDays = (now - userBatteryMemberCard.getDisableMemberCardTime()) / 1000L / 60 / 60 / 24;

                //不足一天按一天计算
                double time = Math.ceil((now - userBatteryMemberCard.getDisableMemberCardTime()) / 1000L / 60 / 60.0);
                if (time < 24) {
                    cardDays = 1L;
                }
                //启用套餐时需要更新停卡记录中的实际停卡天数
                EleDisableMemberCardRecord updateDisableMemberCardRecord = new EleDisableMemberCardRecord();
                updateDisableMemberCardRecord.setId(eleDisableMemberCardRecord.getId());
                updateDisableMemberCardRecord.setRealDays(cardDays.intValue());
                updateDisableMemberCardRecord.setUpdateTime(System.currentTimeMillis());
                eleDisableMemberCardRecordService.updateBYId(updateDisableMemberCardRecord);
            }
        }

        //交易订单
        ElectricityTradeOrder electricityTradeOrderUpdate = new ElectricityTradeOrder();
        electricityTradeOrderUpdate.setId(electricityTradeOrder.getId());
        electricityTradeOrderUpdate.setStatus(tradeOrderStatus);
        electricityTradeOrderUpdate.setUpdateTime(System.currentTimeMillis());
        electricityTradeOrderUpdate.setChannelOrderNo(transactionId);
        baseMapper.updateById(electricityTradeOrderUpdate);

        eleBatteryServiceFeeOrderUpdate.setId(eleBatteryServiceFeeOrder.getId());
        eleBatteryServiceFeeOrderUpdate.setStatus(eleBatteryServiceFeeOrderStatus);
        eleBatteryServiceFeeOrderUpdate.setUpdateTime(System.currentTimeMillis());
        eleBatteryServiceFeeOrderService.update(eleBatteryServiceFeeOrderUpdate);

        //小程序虚拟发货
        shippingManagerService.uploadShippingInfo(userInfo.getUid(), userInfo.getPhone(), transactionId, userInfo.getTenantId());

        return Pair.of(result, null);
    }

    @Override
    public Pair<Boolean, Object> notifyRentCarDepositOrder(WechatJsapiOrderCallBackResource callBackResource) {
        //回调参数
        String tradeOrderNo = callBackResource.getOutTradeNo();
        String tradeState = callBackResource.getTradeState();
        String transactionId = callBackResource.getTransactionId();

        //系统订单
        ElectricityTradeOrder electricityTradeOrder = baseMapper.selectTradeOrderByTradeOrderNo(tradeOrderNo);
        if (Objects.isNull(electricityTradeOrder)) {
            log.error("NOTIFY_RENT_CAR_DEPOSIT_ORDER ERROR ,NOT FOUND ELECTRICITY_TRADE_ORDER TRADE_ORDER_NO={}", tradeOrderNo);
            return Pair.of(false, "未找到交易订单!");
        }
        if (ObjectUtil.notEqual(ElectricityTradeOrder.STATUS_INIT, electricityTradeOrder.getStatus())) {
            log.error("NOTIFY_RENT_CAR_DEPOSIT_ORDER ERROR , ELECTRICITY_TRADE_ORDER  STATUS IS NOT INIT, TRADE_ORDER_NO={}", tradeOrderNo);
            return Pair.of(false, "交易订单已处理");
        }

        //押金订单
        CarDepositOrder carDepositOrder = carDepositOrderService.selectByOrderId(electricityTradeOrder.getOrderNo());
        if (ObjectUtil.isEmpty(carDepositOrder)) {
            log.error("NOTIFY_RENT_CAR_DEPOSIT_ORDER ERROR ,NOT FOUND ELECTRICITY_DEPOSIT_ORDER ORDER_NO={}", electricityTradeOrder.getOrderNo());
            return Pair.of(false, "未找到订单!");
        }

        if (!ObjectUtil.equal(EleDepositOrder.STATUS_INIT, carDepositOrder.getStatus())) {
            log.error("NOTIFY_RENT_CAR_DEPOSIT_ORDER ERROR , ELECTRICITY_DEPOSIT_ORDER  STATUS IS NOT INIT, ORDER_NO={}", electricityTradeOrder.getOrderNo());
            return Pair.of(false, "押金订单已处理!");
        }

        Integer tradeOrderStatus = ElectricityTradeOrder.STATUS_FAIL;
        Integer depositOrderStatus = EleDepositOrder.STATUS_FAIL;
        boolean result = false;
        if (StringUtils.isNotEmpty(tradeState) && ObjectUtil.equal("SUCCESS", tradeState)) {
            tradeOrderStatus = ElectricityTradeOrder.STATUS_SUCCESS;
            depositOrderStatus = EleDepositOrder.STATUS_SUCCESS;
            result = true;
        } else {
            log.error("NOTIFY REDULT PAY FAIL,ORDER_NO={}" + tradeOrderNo);
        }

        UserInfo userInfo = userInfoService.queryByUidFromCache(carDepositOrder.getUid());
        if (Objects.isNull(userInfo)) {
            log.error("NOTIFY ERROR,NOT FOUND USERINFO,USERID={},ORDER_NO={}", carDepositOrder.getUid(), tradeOrderNo);
            return Pair.of(false, "未找到用户信息!");
        }


        //用户押金
        if (Objects.equals(depositOrderStatus, EleDepositOrder.STATUS_SUCCESS)) {

            UserInfo updateUserInfo = new UserInfo();
            updateUserInfo.setUid(userInfo.getUid());
            updateUserInfo.setCarDepositStatus(UserInfo.CAR_DEPOSIT_STATUS_YES);
            updateUserInfo.setUpdateTime(System.currentTimeMillis());
            userInfoService.updateByUid(updateUserInfo);

            UserCarDeposit userCarDeposit = new UserCarDeposit();
            userCarDeposit.setUid(userInfo.getUid());
            userCarDeposit.setOrderId(carDepositOrder.getOrderId());
            userCarDeposit.setDelFlag(UserCarDeposit.DEL_NORMAL);
            userCarDeposit.setApplyDepositTime(System.currentTimeMillis());
            userCarDeposit.setDepositType(UserBatteryDeposit.DEPOSIT_TYPE_DEFAULT);
            userCarDeposit.setTenantId(carDepositOrder.getTenantId());
            userCarDeposit.setCreateTime(System.currentTimeMillis());
            userCarDeposit.setUpdateTime(System.currentTimeMillis());
            userCarDepositService.insertOrUpdate(userCarDeposit);

            UserCar userCar = new UserCar();
            userCar.setUid(userInfo.getUid());
            userCar.setCarModel(carDepositOrder.getCarModelId());
            userCar.setTenantId(userInfo.getTenantId());
            userCar.setCreateTime(System.currentTimeMillis());
            userCar.setUpdateTime(System.currentTimeMillis());
            userCarService.insertOrUpdate(userCar);
        }

        //交易订单
        ElectricityTradeOrder electricityTradeOrderUpdate = new ElectricityTradeOrder();
        electricityTradeOrderUpdate.setId(electricityTradeOrder.getId());
        electricityTradeOrderUpdate.setStatus(tradeOrderStatus);
        electricityTradeOrderUpdate.setUpdateTime(System.currentTimeMillis());
        electricityTradeOrderUpdate.setChannelOrderNo(transactionId);
        baseMapper.updateById(electricityTradeOrderUpdate);

        //押金订单
        EleDepositOrder eleDepositOrderUpdate = new EleDepositOrder();
        eleDepositOrderUpdate.setId(carDepositOrder.getId());
        eleDepositOrderUpdate.setStatus(depositOrderStatus);
        eleDepositOrderUpdate.setUpdateTime(System.currentTimeMillis());
        eleDepositOrderService.update(eleDepositOrderUpdate);

        //小程序虚拟发货
        shippingManagerService
                .uploadShippingInfo(userInfo.getUid(), userInfo.getPhone(), transactionId, userInfo.getTenantId());

        return Pair.of(result, null);
    }

    @Override
    public Pair<Boolean, Object> notifyRentCarMemberOrder(WechatJsapiOrderCallBackResource callBackResource) {

        //回调参数
        String tradeOrderNo = callBackResource.getOutTradeNo();
        String tradeState = callBackResource.getTradeState();
        String transactionId = callBackResource.getTransactionId();

        //交易订单
        ElectricityTradeOrder electricityTradeOrder = baseMapper.selectTradeOrderByTradeOrderNo(tradeOrderNo);
        if (Objects.isNull(electricityTradeOrder)) {
            log.error("NOTIFY_MEMBER_ORDER ERROR ,NOT FOUND ELECTRICITY_TRADE_ORDER ORDER_NO={}", tradeOrderNo);
            return Pair.of(false, "未找到交易订单!");
        }
        if (ObjectUtil.notEqual(ElectricityTradeOrder.STATUS_INIT, electricityTradeOrder.getStatus())) {
            log.error("NOTIFY_MEMBER_ORDER ERROR , ELECTRICITY_TRADE_ORDER  STATUS IS NOT INIT, ORDER_NO={}", tradeOrderNo);
            return Pair.of(false, "交易订单已处理");
        }

        //购卡订单
        CarMemberCardOrder carMemberCardOrder = carMemberCardOrderService.selectByOrderId(electricityTradeOrder.getOrderNo());
        if (ObjectUtil.isEmpty(carMemberCardOrder)) {
            log.error("NOTIFY_MEMBER_ORDER ERROR ,NOT FOUND ELECTRICITY_MEMBER_CARD_ORDER ORDER_NO={}", electricityTradeOrder.getOrderNo());
            return Pair.of(false, "未找到订单!");
        }
        if (!ObjectUtil.equal(ElectricityMemberCardOrder.STATUS_INIT, carMemberCardOrder.getStatus())) {
            log.error("NOTIFY_MEMBER_ORDER ERROR , ELECTRICITY_MEMBER_CARD_ORDER  STATUS IS NOT INIT, ORDER_NO={}", electricityTradeOrder.getOrderNo());
            return Pair.of(false, "套餐订单已处理!");
        }

        //成功或失败
        Integer tradeOrderStatus = ElectricityTradeOrder.STATUS_FAIL;
        Integer memberOrderStatus = ElectricityMemberCardOrder.STATUS_FAIL;
        boolean result = false;
        if (StringUtils.isNotEmpty(tradeState) && ObjectUtil.equal("SUCCESS", tradeState)) {
            tradeOrderStatus = ElectricityTradeOrder.STATUS_SUCCESS;
            memberOrderStatus = ElectricityMemberCardOrder.STATUS_SUCCESS;
            result = true;
        } else {
            log.error("NOTIFY REDULT PAY FAIL,ORDER_NO={}" + tradeOrderNo);
        }

        //用户
        UserInfo userInfo = userInfoService.queryByUidFromCache(carMemberCardOrder.getUid());
        if (Objects.isNull(userInfo)) {
            log.error("NOTIFY  ERROR,NOT FOUND USERINFO,USERID={},ORDER_NO={}", carMemberCardOrder.getUid(), tradeOrderNo);
            return Pair.of(false, "未找到用户信息!");
        }


        if (Objects.equals(memberOrderStatus, EleDepositOrder.STATUS_SUCCESS)) {
            UserCarMemberCard userCarMemberCard = userCarMemberCardService.selectByUidFromCache(userInfo.getUid());

            UserCarMemberCard updateUserCarMemberCard = new UserCarMemberCard();
            updateUserCarMemberCard.setUid(userInfo.getUid());
            updateUserCarMemberCard.setOrderId(carMemberCardOrder.getOrderId());
            updateUserCarMemberCard.setCardId(carMemberCardOrder.getCarModelId());
            updateUserCarMemberCard.setMemberCardExpireTime(electricityMemberCardOrderService.calcRentCarMemberCardExpireTime(carMemberCardOrder.getMemberCardType(), carMemberCardOrder.getValidDays(), userCarMemberCard));
            updateUserCarMemberCard.setDelFlag(UserCarMemberCard.DEL_NORMAL);
            updateUserCarMemberCard.setCreateTime(System.currentTimeMillis());
            updateUserCarMemberCard.setUpdateTime(System.currentTimeMillis());

            userCarMemberCardService.insertOrUpdate(updateUserCarMemberCard);

            //用户是否有绑定了车
            ElectricityCar electricityCar = electricityCarService.queryInfoByUid(userInfo.getUid());
            ElectricityConfig electricityConfig = electricityConfigService
                    .queryFromCacheByTenantId(userInfo.getTenantId());
            if (Objects.nonNull(electricityCar) && Objects.nonNull(electricityConfig) && Objects
                    .equals(electricityConfig.getIsOpenCarControl(), ElectricityConfig.ENABLE_CAR_CONTROL)
                    && System.currentTimeMillis() < updateUserCarMemberCard.getMemberCardExpireTime()) {
                boolean boo = electricityCarService
                        .retryCarLockCtrl(electricityCar.getSn(), ElectricityCar.TYPE_UN_LOCK, 3);

                CarLockCtrlHistory carLockCtrlHistory = new CarLockCtrlHistory();
                carLockCtrlHistory.setUid(userInfo.getUid());
                carLockCtrlHistory.setName(userInfo.getName());
                carLockCtrlHistory.setPhone(userInfo.getPhone());
                carLockCtrlHistory.setStatus(
                        boo ? CarLockCtrlHistory.STATUS_UN_LOCK_SUCCESS : CarLockCtrlHistory.STATUS_UN_LOCK_FAIL);
                carLockCtrlHistory.setType(CarLockCtrlHistory.TYPE_MEMBER_CARD_UN_LOCK);
                carLockCtrlHistory.setCarModelId(electricityCar.getModelId().longValue());
                carLockCtrlHistory.setCarModel(electricityCar.getModel());
                carLockCtrlHistory.setCarId(electricityCar.getId().longValue());
                carLockCtrlHistory.setCarSn(electricityCar.getSn());
                carLockCtrlHistory.setCreateTime(System.currentTimeMillis());
                carLockCtrlHistory.setUpdateTime(System.currentTimeMillis());
                carLockCtrlHistory.setTenantId(userInfo.getTenantId());
                carLockCtrlHistoryService.insert(carLockCtrlHistory);
            }
    
            ChannelActivityHistory channelActivityHistory = channelActivityHistoryService.queryByUid(userInfo.getUid());
            if (Objects.nonNull(channelActivityHistory) && Objects
                    .equals(channelActivityHistory.getStatus(), ChannelActivityHistory.STATUS_INIT)) {
                ChannelActivityHistory updateChannelActivityHistory = new ChannelActivityHistory();
                updateChannelActivityHistory.setId(channelActivityHistory.getId());
                updateChannelActivityHistory.setStatus(ChannelActivityHistory.STATUS_SUCCESS);
                updateChannelActivityHistory.setUpdateTime(System.currentTimeMillis());
                channelActivityHistoryService.update(updateChannelActivityHistory);
            }

            divisionAccountRecordService.handleCarMembercardDivisionAccount(carMemberCardOrder);
        }

        //交易订单
        ElectricityTradeOrder electricityTradeOrderUpdate = new ElectricityTradeOrder();
        electricityTradeOrderUpdate.setId(electricityTradeOrder.getId());
        electricityTradeOrderUpdate.setStatus(tradeOrderStatus);
        electricityTradeOrderUpdate.setUpdateTime(System.currentTimeMillis());
        electricityTradeOrderUpdate.setChannelOrderNo(transactionId);
        baseMapper.updateById(electricityTradeOrderUpdate);

        //租车套餐订单
        CarMemberCardOrder updateCarMemberCardOrder = new CarMemberCardOrder();
        updateCarMemberCardOrder.setId(carMemberCardOrder.getId());
        updateCarMemberCardOrder.setStatus(memberOrderStatus);
        updateCarMemberCardOrder.setUpdateTime(System.currentTimeMillis());
        carMemberCardOrderService.update(updateCarMemberCardOrder);

        //小程序虚拟发货
        shippingManagerService
                .uploadShippingInfo(userInfo.getUid(), userInfo.getPhone(), transactionId, userInfo.getTenantId());

        return Pair.of(result, null);

    }

    @Override
    public Pair<Boolean, Object> notifyInsuranceOrder(WechatJsapiOrderCallBackResource callBackResource) {

        //回调参数
        String tradeOrderNo = callBackResource.getOutTradeNo();
        String tradeState = callBackResource.getTradeState();
        String transactionId = callBackResource.getTransactionId();

        //系统订单
        ElectricityTradeOrder electricityTradeOrder = baseMapper.selectTradeOrderByTradeOrderNo(tradeOrderNo);
        if (Objects.isNull(electricityTradeOrder)) {
            log.error("NOTIFY_INSURANCE_ORDER ERROR ,NOT FOUND ELECTRICITY_TRADE_ORDER TRADE_ORDER_NO={}", tradeOrderNo);
            return Pair.of(false, "未找到交易订单!");
        }
        if (ObjectUtil.notEqual(ElectricityTradeOrder.STATUS_INIT, electricityTradeOrder.getStatus())) {
            log.error("NOTIFY_INSURANCE_ORDER ERROR , ELECTRICITY_TRADE_ORDER  STATUS IS NOT INIT, TRADE_ORDER_NO={}", tradeOrderNo);
            return Pair.of(false, "交易订单已处理");
        }
        //保险订单
        InsuranceOrder insuranceOrder = insuranceOrderService.queryByOrderId(electricityTradeOrder.getOrderNo());
        if (ObjectUtil.isEmpty(insuranceOrder)) {
            log.error("NOTIFY_INSURANCE_ORDER ERROR ,NOT FOUND ELECTRICITY_DEPOSIT_ORDER ORDER_NO={}", electricityTradeOrder.getOrderNo());
            return Pair.of(false, "未找到订单!");
        }

        if (!ObjectUtil.equal(EleBatteryServiceFeeOrder.STATUS_INIT, insuranceOrder.getStatus())) {
            log.error("NOTIFY_INSURANCE_ORDER ERROR , ELECTRICITY_DEPOSIT_ORDER  STATUS IS NOT INIT, ORDER_NO={}", electricityTradeOrder.getOrderNo());
            return Pair.of(false, "押金订单已处理!");
        }

        FranchiseeInsurance franchiseeInsurance = franchiseeInsuranceService.queryByIdFromCache(insuranceOrder.getInsuranceId());
        if (ObjectUtil.isEmpty(insuranceOrder)) {
            log.error("NOTIFY_INSURANCE_ORDER ERROR ,NOT FOUND ELECTRICITY_DEPOSIT_ORDER ORDER_NO={}", electricityTradeOrder.getOrderNo());
            return Pair.of(false, "未找到订单!");
        }

        Integer tradeOrderStatus = ElectricityTradeOrder.STATUS_FAIL;
        Integer insuranceOrderStatus = EleBatteryServiceFeeOrder.STATUS_FAIL;
        boolean result = false;
        if (StringUtils.isNotEmpty(tradeState) && ObjectUtil.equal("SUCCESS", tradeState)) {
            tradeOrderStatus = ElectricityTradeOrder.STATUS_SUCCESS;
            insuranceOrderStatus = EleBatteryServiceFeeOrder.STATUS_SUCCESS;
            result = true;
        } else {
            log.error("NOTIFY REDULT PAY FAIL,ORDER_NO:{}" + tradeOrderNo);
        }

        //用户
        UserInfo userInfo = userInfoService.queryByUidFromCache(insuranceOrder.getUid());
        if (Objects.isNull(userInfo)) {
            log.error("NOTIFY  ERROR,NOT FOUND USERINFO,USERID:{},ORDER_NO={}", insuranceOrder.getUid(), tradeOrderNo);
            return Pair.of(false, "未找到用户信息!");
        }

        if (Objects.equals(insuranceOrderStatus, InsuranceOrder.STATUS_SUCCESS)) {
            InsuranceUserInfo updateOrAddInsuranceUserInfo = new InsuranceUserInfo();
            updateOrAddInsuranceUserInfo.setUid(userInfo.getUid());
            updateOrAddInsuranceUserInfo.setUpdateTime(System.currentTimeMillis());
            updateOrAddInsuranceUserInfo.setIsUse(InsuranceUserInfo.NOT_USE);
            updateOrAddInsuranceUserInfo.setInsuranceOrderId(insuranceOrder.getOrderId());
            updateOrAddInsuranceUserInfo.setInsuranceId(franchiseeInsurance.getId());
            updateOrAddInsuranceUserInfo.setInsuranceExpireTime(System.currentTimeMillis() + franchiseeInsurance.getValidDays() * ((24 * 60 * 60 * 1000L)));
            updateOrAddInsuranceUserInfo.setTenantId(insuranceOrder.getTenantId());
            updateOrAddInsuranceUserInfo.setForehead(franchiseeInsurance.getForehead());
            updateOrAddInsuranceUserInfo.setPremium(franchiseeInsurance.getPremium());
            updateOrAddInsuranceUserInfo.setFranchiseeId(franchiseeInsurance.getFranchiseeId());
            updateOrAddInsuranceUserInfo.setCreateTime(System.currentTimeMillis());

            InsuranceUserInfo insuranceUserInfo = insuranceUserInfoService.queryByUidFromCache(userInfo.getUid());
            if (Objects.isNull(insuranceUserInfo)) {
                insuranceUserInfoService.insert(updateOrAddInsuranceUserInfo);
            } else {
                insuranceUserInfoService.update(updateOrAddInsuranceUserInfo);
            }
        }

        //交易订单
        ElectricityTradeOrder electricityTradeOrderUpdate = new ElectricityTradeOrder();
        electricityTradeOrderUpdate.setId(electricityTradeOrder.getId());
        electricityTradeOrderUpdate.setStatus(tradeOrderStatus);
        electricityTradeOrderUpdate.setUpdateTime(System.currentTimeMillis());
        electricityTradeOrderUpdate.setChannelOrderNo(transactionId);
        baseMapper.updateById(electricityTradeOrderUpdate);

        //保险订单
        InsuranceOrder updateInsuranceOrder = new InsuranceOrder();
        updateInsuranceOrder.setId(insuranceOrder.getId());
        updateInsuranceOrder.setUpdateTime(System.currentTimeMillis());
        updateInsuranceOrder.setStatus(insuranceOrderStatus);
        insuranceOrderService.updateOrderStatusById(updateInsuranceOrder);

        //小程序虚拟发货
        shippingManagerService
                .uploadShippingInfo(userInfo.getUid(), userInfo.getPhone(), transactionId, userInfo.getTenantId());

        return Pair.of(result, null);
    }

    @Override
    public ElectricityTradeOrder selectTradeOrderByTradeOrderNo(String outTradeNo) {
        return baseMapper.selectTradeOrderByTradeOrderNo(outTradeNo);
    }

    @Override
    public ElectricityTradeOrder selectTradeOrderByOrderId(String orderId) {
        return baseMapper.selectTradeOrderByOrderId(orderId);
    }

    @Override
    public void insert(ElectricityTradeOrder electricityTradeOrder) {
        baseMapper.insert(electricityTradeOrder);
    }

    @Override
    public List<ElectricityTradeOrder> selectTradeOrderByParentOrderId(Long parentOrderId) {
        return baseMapper.selectList(Wrappers.<ElectricityTradeOrder>lambdaQuery()
                .eq(ElectricityTradeOrder::getParentOrderId, parentOrderId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer updateElectricityTradeOrderById(ElectricityTradeOrder electricityTradeOrder) {
        return baseMapper.updateById(electricityTradeOrder);
    }

    private void handleSplitAccount(ElectricityMemberCardOrder electricityMemberCardOrder) {
        //加盟商分账
        Franchisee franchisee = franchiseeService.queryByIdFromDB(electricityMemberCardOrder.getFranchiseeId());
        if (Objects.isNull(franchisee)) {
            log.error("ELE ORDER ERROR! franchisee not exists! franchiseeId={}", electricityMemberCardOrder.getFranchiseeId());
            return;
        }

        int percent1 = franchisee.getPercent();
        if (percent1 < 0 || percent1 > 100) {
            log.error("ELE ORDER ERROR! franchisee split percent is illegal! franchiseeId={},percent={}", franchisee.getId(), percent1);
        } else {
            franchiseeAmountService.handleSplitAccount(franchisee, electricityMemberCardOrder, percent1);
        }

        //门店分账
        List<Store> storeList = storeService.queryByFranchiseeId(electricityMemberCardOrder.getFranchiseeId());
        if (ObjectUtil.isEmpty(storeList)) {
            log.error("ELE ORDER ERROR! franchisee not bind store! franchiseeId={}", franchisee.getId());
            return;
        }

        for (Store store : storeList) {
            int percent2 = store.getPercent();
            if (percent2 < 0 || percent2 > 100) {
                log.error("ELE ORDER ERROR! store split percent is illegal! storeId={},percent={}", store.getId(), percent2);
            } else {
                storeAmountService.handleSplitAccount(store, electricityMemberCardOrder, percent2);
            }
        }
    }

}
