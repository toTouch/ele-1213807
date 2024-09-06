package com.xiliulou.electricity.service.impl;


import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.base.enums.ChannelEnum;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.bo.base.BasePayConfig;
import com.xiliulou.electricity.bo.wechat.WechatPayParamsDetails;
import com.xiliulou.electricity.config.WechatConfig;
import com.xiliulou.electricity.converter.ElectricityPayParamsConverter;
import com.xiliulou.electricity.converter.PayConfigConverter;
import com.xiliulou.electricity.converter.model.OrderCreateParamConverterModel;
import com.xiliulou.electricity.entity.CommonPayOrder;
import com.xiliulou.electricity.entity.EleBatteryServiceFeeOrder;
import com.xiliulou.electricity.entity.ElectricityTradeOrder;
import com.xiliulou.electricity.entity.FranchiseeInsurance;
import com.xiliulou.electricity.entity.InsuranceOrder;
import com.xiliulou.electricity.entity.InsuranceUserInfo;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.entity.enterprise.CloudBeanUseRecord;
import com.xiliulou.electricity.entity.enterprise.EnterpriseCloudBeanOrder;
import com.xiliulou.electricity.entity.enterprise.EnterpriseInfo;
import com.xiliulou.electricity.mapper.ElectricityMemberCardOrderMapper;
import com.xiliulou.electricity.mapper.ElectricityTradeOrderMapper;
import com.xiliulou.electricity.mq.producer.ActivityProducer;
import com.xiliulou.electricity.mq.producer.DivisionAccountProducer;
import com.xiliulou.electricity.service.ActivityService;
import com.xiliulou.electricity.service.BatteryMemberCardOrderCouponService;
import com.xiliulou.electricity.service.BatteryMemberCardService;
import com.xiliulou.electricity.service.CarLockCtrlHistoryService;
import com.xiliulou.electricity.service.ChannelActivityHistoryService;
import com.xiliulou.electricity.service.DivisionAccountRecordService;
import com.xiliulou.electricity.service.EleBatteryServiceFeeOrderService;
import com.xiliulou.electricity.service.EleDepositOrderService;
import com.xiliulou.electricity.service.EleDisableMemberCardRecordService;
import com.xiliulou.electricity.service.ElectricityCarService;
import com.xiliulou.electricity.service.ElectricityConfigService;
import com.xiliulou.electricity.service.ElectricityMemberCardOrderService;
import com.xiliulou.electricity.service.ElectricityMemberCardService;
import com.xiliulou.electricity.service.ElectricityTradeOrderService;
import com.xiliulou.electricity.service.EnableMemberCardRecordService;
import com.xiliulou.electricity.service.FranchiseeAmountService;
import com.xiliulou.electricity.service.FranchiseeInsuranceService;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.InsuranceOrderService;
import com.xiliulou.electricity.service.InsuranceUserInfoService;
import com.xiliulou.electricity.service.InvitationActivityRecordService;
import com.xiliulou.electricity.service.JoinShareActivityHistoryService;
import com.xiliulou.electricity.service.JoinShareActivityRecordService;
import com.xiliulou.electricity.service.JoinShareMoneyActivityHistoryService;
import com.xiliulou.electricity.service.JoinShareMoneyActivityRecordService;
import com.xiliulou.electricity.service.OldUserActivityService;
import com.xiliulou.electricity.service.ServiceFeeUserInfoService;
import com.xiliulou.electricity.service.ShareActivityMemberCardService;
import com.xiliulou.electricity.service.ShareActivityRecordService;
import com.xiliulou.electricity.service.ShareMoneyActivityRecordService;
import com.xiliulou.electricity.service.ShareMoneyActivityService;
import com.xiliulou.electricity.service.ShippingManagerService;
import com.xiliulou.electricity.service.StoreAmountService;
import com.xiliulou.electricity.service.StoreService;
import com.xiliulou.electricity.service.UserAmountService;
import com.xiliulou.electricity.service.UserBatteryDepositService;
import com.xiliulou.electricity.service.UserBatteryMemberCardService;
import com.xiliulou.electricity.service.UserBatteryService;
import com.xiliulou.electricity.service.UserCarDepositService;
import com.xiliulou.electricity.service.UserCarMemberCardService;
import com.xiliulou.electricity.service.UserCarService;
import com.xiliulou.electricity.service.UserCouponService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.service.car.CarRentalPackageDepositPayService;
import com.xiliulou.electricity.service.car.CarRentalPackageMemberTermService;
import com.xiliulou.electricity.service.car.CarRentalPackageOrderService;
import com.xiliulou.electricity.service.car.biz.CarRentalPackageOrderBizService;
import com.xiliulou.electricity.service.enterprise.AnotherPayMembercardRecordService;
import com.xiliulou.electricity.service.enterprise.CloudBeanUseRecordService;
import com.xiliulou.electricity.service.enterprise.EnterpriseChannelUserService;
import com.xiliulou.electricity.service.enterprise.EnterpriseCloudBeanOrderService;
import com.xiliulou.electricity.service.enterprise.EnterpriseInfoService;
import com.xiliulou.mq.service.RocketMqService;
import com.xiliulou.pay.base.PayServiceDispatcher;
import com.xiliulou.pay.base.dto.BasePayOrderCreateDTO;
import com.xiliulou.pay.base.exception.PayException;
import com.xiliulou.pay.base.request.BaseOrderCallBackResource;
import com.xiliulou.pay.base.request.BasePayRequest;
import com.xiliulou.pay.weixinv3.dto.WechatJsapiOrderResultDTO;
import com.xiliulou.pay.weixinv3.exception.WechatPayException;
import com.xiliulou.pay.weixinv3.v2.query.WechatV3OrderRequest;
import com.xiliulou.pay.weixinv3.v2.service.WechatV3JsapiInvokeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * @program: XILIULOU
 * @description:
 * @author: Mr.YG
 * @create: 2020-12-03 11:34
 **/
@Service
@Slf4j
public class ElectricityTradeOrderServiceImpl extends ServiceImpl<ElectricityTradeOrderMapper, ElectricityTradeOrder> implements ElectricityTradeOrderService {
    
    @Resource
    private CarRentalPackageOrderBizService carRentalPackageOrderBizService;
    
    @Autowired
    UserInfoService userInfoService;
    
    @Autowired
    EleDepositOrderService eleDepositOrderService;
    
    @Autowired
    WechatConfig wechatConfig;
    
    @Resource
    WechatV3JsapiInvokeService wechatV3JsapiInvokeService;
    
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
    
    @Autowired
    EnterpriseCloudBeanOrderService enterpriseCloudBeanOrderService;
    
    @Autowired
    EnterpriseInfoService enterpriseInfoService;
    
    @Autowired
    CloudBeanUseRecordService cloudBeanUseRecordService;
    
    @Resource
    private PayConfigConverter payConfigConverter;
    
    @Resource
    private PayServiceDispatcher payServiceDispatcher;
    
    /**
     * 租车套餐购买回调
     *
     * @param callBackResource
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Pair<Boolean, Object> notifyCarRenalPackageOrder(BaseOrderCallBackResource callBackResource) {
        log.info("notifyCarRenalPackageOrder params callBackResource is {}", JSON.toJSONString(callBackResource));
        if (ObjectUtils.isEmpty(callBackResource)) {
            log.warn("NotifyCarRenalPackageOrder failed, callBackResource is empty");
            return Pair.of(false, "参数为空");
        }
        
        //回调参数
        String tradeOrderNo = callBackResource.getOutTradeNo();
        String transactionId = callBackResource.getTransactionId();
        
        // 支付状态
        Integer tradeOrderStatus = callBackResource.converterTradeState(ElectricityTradeOrder.STATUS_SUCCESS, ElectricityTradeOrder.STATUS_FAIL);
        
        // 1. 处理交易流水订单
        ElectricityTradeOrder electricityTradeOrder = baseMapper.selectTradeOrderByTradeOrderNo(tradeOrderNo);
        if (Objects.isNull(electricityTradeOrder)) {
            log.warn("NotifyCarRenalPackageOrder failed, not found electricity_trade_order, trade_order_no is {}", tradeOrderNo);
            return Pair.of(false, "未找到交易流水订单");
        }
        
        if (ObjectUtil.notEqual(ElectricityTradeOrder.STATUS_INIT, electricityTradeOrder.getStatus())) {
            log.warn("NotifyCarRenalPackageOrder failed, electricity_trade_order processed, trade_order_no is {}", tradeOrderNo);
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
        
        if (callBackResource.tradeStateIsSuccess()) {
            return handSuccess(orderNo, tenantId, uid, transactionId, callBackResource.getChannel());
        } else {
            return handFailed(orderNo, tenantId, uid);
        }
    }
    
    /**
     * 租车套餐支付回调-支付失败
     *
     * @param orderNo  租车套餐购买订单编号
     * @param tenantId 租户ID
     * @param uid      用户ID
     * @return
     */
    private Pair<Boolean, Object> handFailed(String orderNo, Integer tenantId, Long uid) {
        return carRentalPackageOrderBizService.handBuyRentalPackageOrderFailed(orderNo, tenantId, uid);
    }
    
    /**
     * 租车套餐支付回调-支付成功
     *
     * @param orderNo  租车套餐购买订单编号
     * @param tenantId 租户ID
     * @param uid      用户ID
     * @param channel
     * @return
     */
    private Pair<Boolean, Object> handSuccess(String orderNo, Integer tenantId, Long uid, String transactionId, String channel) {
        Pair<Boolean, Object> pair = carRentalPackageOrderBizService.handBuyRentalPackageOrderSuccess(orderNo, tenantId, uid, null);
        if (!pair.getLeft()) {
            return pair;
        }
        
        // 最后一步，小程序虚拟发货
        String phone = pair.getRight().toString();
        if (ChannelEnum.WECHAT.getCode().equals(channel)) {
            shippingManagerService.uploadShippingInfo(uid, phone, transactionId, tenantId);
        }
        
        return Pair.of(true, null);
    }
    
    @Override
    public WechatJsapiOrderResultDTO commonCreateTradeOrderAndGetPayParams(CommonPayOrder commonOrder, WechatPayParamsDetails wechatPayParamsDetails, String openId,
            HttpServletRequest request) throws WechatPayException {
        //生成支付订单
        String ip = request.getRemoteAddr();
        ElectricityTradeOrder electricityTradeOrder = new ElectricityTradeOrder();
        electricityTradeOrder.setOrderNo(commonOrder.getOrderId());
        electricityTradeOrder.setTradeOrderNo(String.valueOf(System.currentTimeMillis()) + commonOrder.getUid());
        electricityTradeOrder.setClientId(ip);
        electricityTradeOrder.setCreateTime(System.currentTimeMillis());
        electricityTradeOrder.setUpdateTime(System.currentTimeMillis());
        electricityTradeOrder.setOrderType(commonOrder.getOrderType());
        electricityTradeOrder.setStatus(ElectricityTradeOrder.STATUS_INIT);
        electricityTradeOrder.setTotalFee(commonOrder.getPayAmount().multiply(new BigDecimal(100)));
        electricityTradeOrder.setUid(commonOrder.getUid());
        electricityTradeOrder.setTenantId(commonOrder.getTenantId());
        electricityTradeOrder.setPayFranchiseeId(wechatPayParamsDetails.getFranchiseeId());
        electricityTradeOrder.setWechatMerchantId(wechatPayParamsDetails.getWechatMerchantId());
        electricityTradeOrder.setPaymentChannel(wechatPayParamsDetails.getPaymentChannel());
        baseMapper.insert(electricityTradeOrder);
        
        //支付参数
        WechatV3OrderRequest wechatV3OrderRequest = new WechatV3OrderRequest();
        wechatV3OrderRequest.setAppid(wechatPayParamsDetails.getMerchantMinProAppId());
        wechatV3OrderRequest.setDescription(commonOrder.getDescription());
        wechatV3OrderRequest.setOrderId(electricityTradeOrder.getTradeOrderNo());
        wechatV3OrderRequest.setExpireTime(System.currentTimeMillis() + 3600000);
        wechatV3OrderRequest.setAttach(commonOrder.getAttach());
        wechatV3OrderRequest.setNotifyUrl(wechatConfig.getPayCallBackUrl() + electricityTradeOrder.getTenantId() + "/" + electricityTradeOrder.getPayFranchiseeId());
        wechatV3OrderRequest.setAmount(commonOrder.getPayAmount().multiply(new BigDecimal(100)).intValue());
        wechatV3OrderRequest.setCurrency("CNY");
        wechatV3OrderRequest.setOpenId(openId);
        wechatV3OrderRequest.setCommonRequest(ElectricityPayParamsConverter.qryDetailsToCommonRequest(wechatPayParamsDetails));
        log.info("wechatV3FranchiseeOrderRequest is {}", JsonUtil.toJson(wechatV3OrderRequest));
        
        return wechatV3JsapiInvokeService.order(wechatV3OrderRequest);
        
    }
    
    @Override
    public BasePayOrderCreateDTO commonCreateTradeOrderAndGetPayParamsV2(CommonPayOrder commonPayOrder, BasePayConfig payConfig, String openId, HttpServletRequest request)
            throws PayException {
        // 构建交易订单
        ElectricityTradeOrder electricityTradeOrder = this.buildElectricityTradeOrder(request, commonPayOrder, payConfig);
        baseMapper.insert(electricityTradeOrder);
        
        // 构建参数转换模型
        OrderCreateParamConverterModel model = new OrderCreateParamConverterModel();
        model.setOrderId(electricityTradeOrder.getTradeOrderNo());
        model.setExpireTime(System.currentTimeMillis() + 3600000);
        model.setDescription(commonPayOrder.getDescription());
        model.setAttach(commonPayOrder.getAttach());
        model.setAmount(commonPayOrder.getPayAmount());
        model.setCurrency("CNY");
        model.setOpenId(openId);
        model.setPayConfig(payConfig);
        model.setTenantId(electricityTradeOrder.getTenantId());
        model.setFranchiseeId(electricityTradeOrder.getPayFranchiseeId());
        BasePayRequest basePayRequest = payConfigConverter.converterOrderCreate(model);
        
        return payServiceDispatcher.order(basePayRequest);
    }
    
    @Override
    public Pair<Boolean, Object> notifyInsuranceOrder(BaseOrderCallBackResource callBackResource) {
        
        //回调参数
        String tradeOrderNo = callBackResource.getOutTradeNo();
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
        if (callBackResource.tradeStateIsSuccess()) {
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
        if (ChannelEnum.WECHAT.getCode().equals(callBackResource.getChannel())) {
            shippingManagerService.uploadShippingInfo(userInfo.getUid(), userInfo.getPhone(), transactionId, userInfo.getTenantId());
        }
        return Pair.of(result, null);
    }
    
    /**
     * 云豆充值回调
     */
    @Override
    public Pair<Boolean, Object> notifyCloudBeanRechargeOrder(BaseOrderCallBackResource callBackResource) {
        String transactionId = callBackResource.getTransactionId();
        
        Integer tradeOrderStatus = ElectricityTradeOrder.STATUS_FAIL;
        if (callBackResource.tradeStateIsSuccess()) {
            tradeOrderStatus = ElectricityTradeOrder.STATUS_SUCCESS;
        } else {
            log.error("NOTIFY CLOUD BEAN RECHARGE ERROR!pay fail,tradeOrderNo={}", callBackResource.getOutTradeNo());
        }
        
        //系统订单
        ElectricityTradeOrder electricityTradeOrder = baseMapper.selectTradeOrderByTradeOrderNo(callBackResource.getOutTradeNo());
        if (Objects.isNull(electricityTradeOrder)) {
            log.error("NOTIFY CLOUD BEAN RECHARGE ERROR!not found electricity_trade_order,tradeOrderNo={}", callBackResource.getOutTradeNo());
            return Pair.of(false, "未找到交易订单!");
        }
        if (ObjectUtil.notEqual(ElectricityTradeOrder.STATUS_INIT, electricityTradeOrder.getStatus())) {
            log.error("NOTIFY CLOUD BEAN RECHARGE ERROR!electricity_trade_order status is not init,tradeOrderNo={}", callBackResource.getOutTradeNo());
            return Pair.of(false, "交易订单已处理");
        }
        
        //云豆订单
        EnterpriseCloudBeanOrder enterpriseCloudBeanOrder = enterpriseCloudBeanOrderService.selectByOrderId(electricityTradeOrder.getOrderNo());
        if (Objects.isNull(enterpriseCloudBeanOrder)) {
            log.error("NOTIFY CLOUD BEAN RECHARGE ERROR!not found enterpriseCloudBeanOrder,orderNo={}", electricityTradeOrder.getOrderNo());
            return Pair.of(false, "云豆充值订单不存在");
        }
        
        if (!ObjectUtil.equal(EnterpriseCloudBeanOrder.STATUS_INIT, enterpriseCloudBeanOrder.getStatus())) {
            log.error("NOTIFY_INSURANCE_ORDER ERROR ! enterpriseCloudBeanOrder status is not init,orderNo={}", electricityTradeOrder.getOrderNo());
            return Pair.of(false, "云豆充值订单已处理!");
        }
        
        EnterpriseInfo enterpriseInfo = enterpriseInfoService.queryByIdFromDB(enterpriseCloudBeanOrder.getEnterpriseId());
        if (Objects.isNull(enterpriseInfo)) {
            log.error("NOTIFY_INSURANCE_ORDER ERROR ! not found enterpriseInfo,orderNo={},enterpriseId={}", electricityTradeOrder.getOrderNo(),
                    enterpriseCloudBeanOrder.getEnterpriseId());
            return Pair.of(false, "企业配置不存在!");
        }
        
        EnterpriseCloudBeanOrder enterpriseCloudBeanOrderUpdate = new EnterpriseCloudBeanOrder();
        enterpriseCloudBeanOrderUpdate.setId(enterpriseCloudBeanOrder.getId());
        enterpriseCloudBeanOrderUpdate.setStatus(EnterpriseCloudBeanOrder.STATUS_SUCCESS);
        enterpriseCloudBeanOrderUpdate.setUpdateTime(System.currentTimeMillis());
        
        if (Objects.equals(tradeOrderStatus, EnterpriseCloudBeanOrder.STATUS_SUCCESS)) {
            EnterpriseInfo enterpriseInfoUpdate = new EnterpriseInfo();
            enterpriseInfoUpdate.setId(enterpriseInfo.getId());
            enterpriseInfoUpdate.setTotalBeanAmount(enterpriseInfo.getTotalBeanAmount().add(enterpriseCloudBeanOrder.getBeanAmount()));
            enterpriseInfoUpdate.setUpdateTime(System.currentTimeMillis());
            enterpriseInfoService.update(enterpriseInfoUpdate);
            
            CloudBeanUseRecord cloudBeanUseRecord = new CloudBeanUseRecord();
            cloudBeanUseRecord.setEnterpriseId(enterpriseInfo.getId());
            cloudBeanUseRecord.setUid(enterpriseCloudBeanOrder.getUid());
            cloudBeanUseRecord.setType(CloudBeanUseRecord.TYPE_USER_RECHARGE);
            cloudBeanUseRecord.setBeanAmount(enterpriseCloudBeanOrder.getBeanAmount());
            cloudBeanUseRecord.setRemainingBeanAmount(enterpriseInfoUpdate.getTotalBeanAmount());
            cloudBeanUseRecord.setFranchiseeId(enterpriseInfo.getFranchiseeId());
            cloudBeanUseRecord.setRef(enterpriseCloudBeanOrder.getOrderId());
            cloudBeanUseRecord.setTenantId(enterpriseInfo.getTenantId());
            cloudBeanUseRecord.setCreateTime(System.currentTimeMillis());
            cloudBeanUseRecord.setUpdateTime(System.currentTimeMillis());
            cloudBeanUseRecordService.insert(cloudBeanUseRecord);
        } else {
            enterpriseCloudBeanOrderUpdate.setStatus(EnterpriseCloudBeanOrder.STATUS_FAIL);
        }
        
        enterpriseCloudBeanOrderService.update(enterpriseCloudBeanOrderUpdate);
        
        ElectricityTradeOrder electricityTradeOrderUpdate = new ElectricityTradeOrder();
        electricityTradeOrderUpdate.setId(electricityTradeOrder.getId());
        electricityTradeOrderUpdate.setStatus(tradeOrderStatus);
        electricityTradeOrderUpdate.setUpdateTime(System.currentTimeMillis());
        electricityTradeOrderUpdate.setChannelOrderNo(transactionId);
        baseMapper.updateById(electricityTradeOrderUpdate);
        
        return Pair.of(true, null);
    }
    
    @Override
    public ElectricityTradeOrder selectTradeOrderByTradeOrderNo(String outTradeNo) {
        return baseMapper.selectTradeOrderByTradeOrderNo(outTradeNo);
    }
    
    @Override
    public ElectricityTradeOrder selectTradeOrderByOrderId(String orderId) {
        return baseMapper.selectTradeOrderByOrderId(orderId);
    }
    
    /**
     * 测试使用
     */
    @Override
    public ElectricityTradeOrder selectTradeOrderByOrderIdV2(String orderId) {
        return baseMapper.selectOne(
                new LambdaQueryWrapper<ElectricityTradeOrder>().eq(ElectricityTradeOrder::getOrderNo, orderId).isNotNull(ElectricityTradeOrder::getChannelOrderNo)
                        .orderByDesc(ElectricityTradeOrder::getId).last("limit 1"));
    }
    
    @Override
    public void insert(ElectricityTradeOrder electricityTradeOrder) {
        baseMapper.insert(electricityTradeOrder);
    }
    
    @Override
    public List<ElectricityTradeOrder> selectTradeOrderByParentOrderId(Long parentOrderId) {
        return baseMapper.selectList(Wrappers.<ElectricityTradeOrder>lambdaQuery().eq(ElectricityTradeOrder::getParentOrderId, parentOrderId));
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer updateElectricityTradeOrderById(ElectricityTradeOrder electricityTradeOrder) {
        return baseMapper.updateById(electricityTradeOrder);
    }
    
    /**
     * 构建交易订单
     *
     * @param request
     * @param commonPayOrder
     * @param payConfig
     * @author caobotao.cbt
     * @date 2024/7/18 19:36
     */
    private ElectricityTradeOrder buildElectricityTradeOrder(HttpServletRequest request, CommonPayOrder commonPayOrder, BasePayConfig payConfig) {
        String ip = request.getRemoteAddr();
        ElectricityTradeOrder electricityTradeOrder = new ElectricityTradeOrder();
        electricityTradeOrder.setOrderNo(commonPayOrder.getOrderId());
        electricityTradeOrder.setTradeOrderNo(String.valueOf(System.currentTimeMillis()) + commonPayOrder.getUid());
        electricityTradeOrder.setClientId(ip);
        electricityTradeOrder.setCreateTime(System.currentTimeMillis());
        electricityTradeOrder.setUpdateTime(System.currentTimeMillis());
        electricityTradeOrder.setOrderType(commonPayOrder.getOrderType());
        electricityTradeOrder.setStatus(ElectricityTradeOrder.STATUS_INIT);
        electricityTradeOrder.setTotalFee(commonPayOrder.getPayAmount().multiply(new BigDecimal(100)));
        electricityTradeOrder.setUid(commonPayOrder.getUid());
        electricityTradeOrder.setTenantId(commonPayOrder.getTenantId());
        electricityTradeOrder.setPayFranchiseeId(payConfig.getFranchiseeId());
        electricityTradeOrder.setWechatMerchantId(payConfig.getThirdPartyMerchantId());
        electricityTradeOrder.setPaymentChannel(payConfig.getPaymentChannel());
        return electricityTradeOrder;
    }
    
}
