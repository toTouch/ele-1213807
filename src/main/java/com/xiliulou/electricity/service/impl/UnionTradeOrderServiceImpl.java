package com.xiliulou.electricity.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.bo.base.BasePayConfig;
import com.xiliulou.electricity.bo.wechat.WechatPayParamsDetails;
import com.xiliulou.electricity.config.WechatConfig;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.constant.TimeConstant;
import com.xiliulou.electricity.constant.merchant.MerchantConstant;
import com.xiliulou.electricity.converter.ElectricityPayParamsConverter;
import com.xiliulou.electricity.converter.PayConfigConverter;
import com.xiliulou.electricity.converter.model.OrderCreateParamConverterModel;
import com.xiliulou.electricity.dto.ActivityProcessDTO;
import com.xiliulou.electricity.dto.DivisionAccountOrderDTO;
import com.xiliulou.electricity.entity.BatteryMemberCard;
import com.xiliulou.electricity.entity.CarLockCtrlHistory;
import com.xiliulou.electricity.entity.EleBatteryServiceFeeOrder;
import com.xiliulou.electricity.entity.EleDepositOrder;
import com.xiliulou.electricity.entity.EleDisableMemberCardRecord;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.ElectricityCar;
import com.xiliulou.electricity.entity.ElectricityConfig;
import com.xiliulou.electricity.entity.ElectricityMemberCardOrder;
import com.xiliulou.electricity.entity.ElectricityTradeOrder;
import com.xiliulou.electricity.entity.EnableMemberCardRecord;
import com.xiliulou.electricity.entity.FranchiseeInsurance;
import com.xiliulou.electricity.entity.InsuranceOrder;
import com.xiliulou.electricity.entity.InsuranceUserInfo;
import com.xiliulou.electricity.entity.ServiceFeeUserInfo;
import com.xiliulou.electricity.entity.UnionPayOrder;
import com.xiliulou.electricity.entity.UnionTradeOrder;
import com.xiliulou.electricity.entity.UserBatteryDeposit;
import com.xiliulou.electricity.entity.UserBatteryMemberCard;
import com.xiliulou.electricity.entity.UserBatteryMemberCardPackage;
import com.xiliulou.electricity.entity.UserCoupon;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.entity.UserInfoExtra;
import com.xiliulou.electricity.entity.car.CarRentalPackageMemberTermPo;
import com.xiliulou.electricity.entity.car.CarRentalPackageOrderFreezePo;
import com.xiliulou.electricity.entity.car.CarRentalPackageOrderSlippagePo;
import com.xiliulou.electricity.entity.enterprise.EnterpriseChannelUser;
import com.xiliulou.electricity.enums.ActivityEnum;
import com.xiliulou.electricity.enums.DivisionAccountEnum;
import com.xiliulou.electricity.enums.MemberTermStatusEnum;
import com.xiliulou.electricity.enums.OverdueType;
import com.xiliulou.electricity.enums.PackageTypeEnum;
import com.xiliulou.electricity.enums.PayStateEnum;
import com.xiliulou.electricity.enums.ServiceFeeEnum;
import com.xiliulou.electricity.enums.SlippageTypeEnum;
import com.xiliulou.electricity.enums.YesNoEnum;
import com.xiliulou.electricity.enums.enterprise.CloudBeanStatusEnum;
import com.xiliulou.electricity.enums.enterprise.EnterprisePaymentStatusEnum;
import com.xiliulou.electricity.event.publish.OverdueUserRemarkPublish;
import com.xiliulou.electricity.mapper.UnionTradeOrderMapper;
import com.xiliulou.electricity.mq.constant.MqProducerConstant;
import com.xiliulou.electricity.mq.model.BatteryMemberCardMerchantRebate;
import com.xiliulou.electricity.mq.producer.ActivityProducer;
import com.xiliulou.electricity.mq.producer.DivisionAccountProducer;
import com.xiliulou.electricity.query.enterprise.EnterpriseChannelUserQuery;
import com.xiliulou.electricity.service.ActivityService;
import com.xiliulou.electricity.service.BatteryMemberCardOrderCouponService;
import com.xiliulou.electricity.service.BatteryMemberCardService;
import com.xiliulou.electricity.service.CarLockCtrlHistoryService;
import com.xiliulou.electricity.service.ChannelActivityHistoryService;
import com.xiliulou.electricity.service.DivisionAccountRecordService;
import com.xiliulou.electricity.service.EleBatteryServiceFeeOrderService;
import com.xiliulou.electricity.service.EleDepositOrderService;
import com.xiliulou.electricity.service.EleDisableMemberCardRecordService;
import com.xiliulou.electricity.service.ElectricityCabinetService;
import com.xiliulou.electricity.service.ElectricityCarService;
import com.xiliulou.electricity.service.ElectricityConfigService;
import com.xiliulou.electricity.service.ElectricityMemberCardOrderService;
import com.xiliulou.electricity.service.ElectricityMemberCardService;
import com.xiliulou.electricity.service.ElectricityTradeOrderService;
import com.xiliulou.electricity.service.EnableMemberCardRecordService;
import com.xiliulou.electricity.service.FranchiseeInsuranceService;
import com.xiliulou.electricity.service.InsuranceOrderService;
import com.xiliulou.electricity.service.InsuranceUserInfoService;
import com.xiliulou.electricity.service.InvitationActivityRecordService;
import com.xiliulou.electricity.service.JoinShareActivityHistoryService;
import com.xiliulou.electricity.service.JoinShareActivityRecordService;
import com.xiliulou.electricity.service.JoinShareMoneyActivityHistoryService;
import com.xiliulou.electricity.service.JoinShareMoneyActivityRecordService;
import com.xiliulou.electricity.service.MemberCardBatteryTypeService;
import com.xiliulou.electricity.service.OldUserActivityService;
import com.xiliulou.electricity.service.ServiceFeeUserInfoService;
import com.xiliulou.electricity.service.ShareActivityMemberCardService;
import com.xiliulou.electricity.service.ShareActivityRecordService;
import com.xiliulou.electricity.service.ShareMoneyActivityRecordService;
import com.xiliulou.electricity.service.ShareMoneyActivityService;
import com.xiliulou.electricity.service.ShippingManagerService;
import com.xiliulou.electricity.service.UnionTradeOrderService;
import com.xiliulou.electricity.service.UserAmountService;
import com.xiliulou.electricity.service.UserBatteryDepositService;
import com.xiliulou.electricity.service.UserBatteryMemberCardPackageService;
import com.xiliulou.electricity.service.UserBatteryMemberCardService;
import com.xiliulou.electricity.service.UserBatteryService;
import com.xiliulou.electricity.service.UserBatteryTypeService;
import com.xiliulou.electricity.service.UserCarDepositService;
import com.xiliulou.electricity.service.UserCarMemberCardService;
import com.xiliulou.electricity.service.UserCarService;
import com.xiliulou.electricity.service.UserCouponService;
import com.xiliulou.electricity.service.UserInfoExtraService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.service.car.CarRentalPackageMemberTermService;
import com.xiliulou.electricity.service.car.CarRentalPackageOrderFreezeService;
import com.xiliulou.electricity.service.car.CarRentalPackageOrderSlippageService;
import com.xiliulou.electricity.service.car.biz.CarRentalOrderBizService;
import com.xiliulou.electricity.service.enterprise.AnotherPayMembercardRecordService;
import com.xiliulou.electricity.service.enterprise.EnterpriseChannelUserService;
import com.xiliulou.electricity.service.retrofit.Jt808RetrofitService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.mq.service.RocketMqService;
import com.xiliulou.core.base.enums.ChannelEnum;
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
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;
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
 * @author: Mr.HRP
 * @create: 2022-11-07 11:34
 **/
@Service
@Slf4j
public class UnionTradeOrderServiceImpl extends ServiceImpl<UnionTradeOrderMapper, UnionTradeOrder> implements UnionTradeOrderService {
    
    @Resource
    private RocketMqService rocketMqService;
    
    @Resource
    private CarRentalOrderBizService carRentalOrderBizService;
    
    @Resource
    private CarRentalPackageMemberTermService carRentalPackageMemberTermService;
    
    @Resource
    private CarRentalPackageOrderFreezeService carRentalPackageOrderFreezeService;
    
    @Resource
    private CarRentalPackageOrderSlippageService carRentalPackageOrderSlippageService;
    
    @Autowired
    WechatConfig wechatConfig;
    
    @Autowired
    EleDepositOrderService eleDepositOrderService;
    
    @Autowired
    UserInfoService userInfoService;
    
    @Autowired
    FranchiseeInsuranceService franchiseeInsuranceService;
    
    @Autowired
    InsuranceOrderService insuranceOrderService;
    
    @Autowired
    InsuranceUserInfoService insuranceUserInfoService;
    
    @Autowired
    ElectricityTradeOrderService electricityTradeOrderService;
    
    @Autowired
    UserBatteryDepositService userBatteryDepositService;
    
    @Autowired
    UserBatteryService userBatteryService;
    
    @Autowired
    ElectricityMemberCardOrderService electricityMemberCardOrderService;
    
    @Autowired
    ElectricityMemberCardService electricityMemberCardService;
    
    @Autowired
    OldUserActivityService oldUserActivityService;
    
    @Autowired
    UserCouponService userCouponService;
    
    @Autowired
    UserBatteryMemberCardService userBatteryMemberCardService;
    
    @Autowired
    ServiceFeeUserInfoService serviceFeeUserInfoService;
    
    @Autowired
    JoinShareActivityRecordService joinShareActivityRecordService;
    
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
    UserCarDepositService userCarDepositService;
    
    @Autowired
    UserCarMemberCardService userCarMemberCardService;
    
    @Autowired
    UserCarService userCarService;
    
    @Autowired
    RedisService redisService;
    
    @Autowired
    ElectricityCarService electricityCarService;
    
    @Autowired
    Jt808RetrofitService jt808RetrofitService;
    
    @Autowired
    ChannelActivityHistoryService channelActivityHistoryService;
    
    @Autowired
    ElectricityConfigService electricityConfigService;
    
    @Autowired
    CarLockCtrlHistoryService carLockCtrlHistoryService;
    
    @Autowired
    ShippingManagerService shippingManagerService;
    
    @Autowired
    DivisionAccountRecordService divisionAccountRecordService;
    
    @Autowired
    ShareActivityMemberCardService shareActivityMemberCardService;
    
    @Autowired
    InvitationActivityRecordService invitationActivityRecordService;
    
    @Autowired
    BatteryMemberCardOrderCouponService memberCardOrderCouponService;
    
    @Autowired
    MemberCardBatteryTypeService memberCardBatteryTypeService;
    
    @Autowired
    UserBatteryTypeService userBatteryTypeService;
    
    @Autowired
    BatteryMemberCardService batteryMemberCardService;
    
    @Autowired
    UserBatteryMemberCardPackageService userBatteryMemberCardPackageService;
    
    @Autowired
    DivisionAccountProducer divisionAccountProducer;
    
    @Autowired
    ActivityProducer activityProducer;
    
    @Autowired
    private ActivityService activityService;
    
    @Autowired
    EleBatteryServiceFeeOrderService eleBatteryServiceFeeOrderService;
    
    @Autowired
    EnableMemberCardRecordService enableMemberCardRecordService;
    
    @Autowired
    EleDisableMemberCardRecordService eleDisableMemberCardRecordService;
    
    @Autowired
    ElectricityCabinetService electricityCabinetService;
    
    @Resource
    AnotherPayMembercardRecordService anotherPayMembercardRecordService;
    
    @Resource
    EnterpriseChannelUserService enterpriseChannelUserService;
    
    @Autowired
    UserInfoExtraService userInfoExtraService;
    
    @Autowired
    private OverdueUserRemarkPublish overdueUserRemarkPublish;
    
    @Resource
    private WechatV3JsapiInvokeService wechatV3JsapiInvokeService;
    
    @Autowired
    private PayConfigConverter payConfigConverter;
    
    @Resource
    private PayServiceDispatcher payServiceDispatcher;
    
    @Deprecated
    @Override
    public WechatJsapiOrderResultDTO unionCreateTradeOrderAndGetPayParams(UnionPayOrder unionPayOrder, WechatPayParamsDetails wechatPayParamsDetails, String openId,
            HttpServletRequest request) throws WechatPayException {
        
        String ip = request.getRemoteAddr();
        UnionTradeOrder unionTradeOrder = new UnionTradeOrder();
        unionTradeOrder.setJsonOrderId(unionPayOrder.getJsonOrderId());
        unionTradeOrder.setJsonSingleFee(unionPayOrder.getJsonSingleFee());
        unionTradeOrder.setTradeOrderNo(String.valueOf(System.currentTimeMillis()).substring(2) + unionPayOrder.getUid() + RandomUtil.randomNumbers(6));
        unionTradeOrder.setClientId(ip);
        unionTradeOrder.setCreateTime(System.currentTimeMillis());
        unionTradeOrder.setUpdateTime(System.currentTimeMillis());
        unionTradeOrder.setJsonOrderType(unionPayOrder.getJsonOrderType());
        unionTradeOrder.setStatus(UnionTradeOrder.STATUS_INIT);
        unionTradeOrder.setTotalFee(unionPayOrder.getPayAmount());
        unionTradeOrder.setUid(unionPayOrder.getUid());
        unionTradeOrder.setTenantId(unionPayOrder.getTenantId());
        unionTradeOrder.setParamFranchiseeId(wechatPayParamsDetails.getFranchiseeId());
        unionTradeOrder.setWechatMerchantId(wechatPayParamsDetails.getWechatMerchantId());
        baseMapper.insert(unionTradeOrder);
        
        List<String> jsonOrderList = JsonUtil.fromJsonArray(unionPayOrder.getJsonOrderId(), String.class);
        for (int i = 0; i < jsonOrderList.size(); i++) {
            ElectricityTradeOrder electricityTradeOrder = new ElectricityTradeOrder();
            electricityTradeOrder.setOrderNo(JsonUtil.fromJsonArray(unionPayOrder.getJsonOrderId(), String.class).get(i));
            electricityTradeOrder.setTradeOrderNo(String.valueOf(System.currentTimeMillis()));
            electricityTradeOrder.setClientId(ip);
            electricityTradeOrder.setCreateTime(System.currentTimeMillis());
            electricityTradeOrder.setUpdateTime(System.currentTimeMillis());
            electricityTradeOrder.setOrderType(JsonUtil.fromJsonArray(unionPayOrder.getJsonOrderType(), Integer.class).get(i));
            electricityTradeOrder.setStatus(ElectricityTradeOrder.STATUS_INIT);
            electricityTradeOrder.setTotalFee(JsonUtil.fromJsonArray(unionPayOrder.getJsonSingleFee(), BigDecimal.class).get(i));
            electricityTradeOrder.setUid(unionPayOrder.getUid());
            electricityTradeOrder.setTenantId(unionPayOrder.getTenantId());
            electricityTradeOrder.setParentOrderId(unionTradeOrder.getId());
            electricityTradeOrder.setPayFranchiseeId(wechatPayParamsDetails.getFranchiseeId());
            electricityTradeOrder.setWechatMerchantId(wechatPayParamsDetails.getWechatMerchantId());
            electricityTradeOrderService.insert(electricityTradeOrder);
        }
        
        // 支付参数
        WechatV3OrderRequest wechatV3OrderQuery = new WechatV3OrderRequest();
        wechatV3OrderQuery.setAppid(wechatPayParamsDetails.getMerchantMinProAppId());
        wechatV3OrderQuery.setDescription(unionPayOrder.getDescription());
        wechatV3OrderQuery.setOrderId(unionTradeOrder.getTradeOrderNo());
        wechatV3OrderQuery.setExpireTime(System.currentTimeMillis() + 3600000);
        wechatV3OrderQuery.setAttach(unionPayOrder.getAttach());
        wechatV3OrderQuery.setNotifyUrl(wechatConfig.getPayCallBackUrl() + unionTradeOrder.getTenantId() + "/" + wechatPayParamsDetails.getFranchiseeId());
        wechatV3OrderQuery.setAmount(unionPayOrder.getPayAmount().multiply(new BigDecimal(100)).intValue());
        wechatV3OrderQuery.setCurrency("CNY");
        wechatV3OrderQuery.setOpenId(openId);
        wechatV3OrderQuery.setCommonRequest(ElectricityPayParamsConverter.qryDetailsToCommonRequest(wechatPayParamsDetails));
        log.info("wechatV3OrderQuery is -->{}", wechatV3OrderQuery);
        return wechatV3JsapiInvokeService.order(wechatV3OrderQuery);
    }
    
    @Override
    public BasePayOrderCreateDTO unionCreateTradeOrderAndGetPayParams(UnionPayOrder unionPayOrder, BasePayConfig payParamConfig, String openId, HttpServletRequest request)
            throws PayException {
        String ip = request.getRemoteAddr();
        UnionTradeOrder unionTradeOrder = new UnionTradeOrder();
        unionTradeOrder.setJsonOrderId(unionPayOrder.getJsonOrderId());
        unionTradeOrder.setJsonSingleFee(unionPayOrder.getJsonSingleFee());
        unionTradeOrder.setTradeOrderNo(String.valueOf(System.currentTimeMillis()).substring(2) + unionPayOrder.getUid() + RandomUtil.randomNumbers(6));
        unionTradeOrder.setClientId(ip);
        unionTradeOrder.setCreateTime(System.currentTimeMillis());
        unionTradeOrder.setUpdateTime(System.currentTimeMillis());
        unionTradeOrder.setJsonOrderType(unionPayOrder.getJsonOrderType());
        unionTradeOrder.setStatus(UnionTradeOrder.STATUS_INIT);
        unionTradeOrder.setTotalFee(unionPayOrder.getPayAmount());
        unionTradeOrder.setUid(unionPayOrder.getUid());
        unionTradeOrder.setTenantId(unionPayOrder.getTenantId());
        unionTradeOrder.setParamFranchiseeId(payParamConfig.getFranchiseeId());
        unionTradeOrder.setWechatMerchantId(payParamConfig.getThirdPartyMerchantId());
        baseMapper.insert(unionTradeOrder);
        
        List<String> jsonOrderList = JsonUtil.fromJsonArray(unionPayOrder.getJsonOrderId(), String.class);
        for (int i = 0; i < jsonOrderList.size(); i++) {
            ElectricityTradeOrder electricityTradeOrder = new ElectricityTradeOrder();
            electricityTradeOrder.setOrderNo(JsonUtil.fromJsonArray(unionPayOrder.getJsonOrderId(), String.class).get(i));
            electricityTradeOrder.setTradeOrderNo(String.valueOf(System.currentTimeMillis()));
            electricityTradeOrder.setClientId(ip);
            electricityTradeOrder.setCreateTime(System.currentTimeMillis());
            electricityTradeOrder.setUpdateTime(System.currentTimeMillis());
            electricityTradeOrder.setOrderType(JsonUtil.fromJsonArray(unionPayOrder.getJsonOrderType(), Integer.class).get(i));
            electricityTradeOrder.setStatus(ElectricityTradeOrder.STATUS_INIT);
            electricityTradeOrder.setTotalFee(JsonUtil.fromJsonArray(unionPayOrder.getJsonSingleFee(), BigDecimal.class).get(i));
            electricityTradeOrder.setUid(unionPayOrder.getUid());
            electricityTradeOrder.setTenantId(unionPayOrder.getTenantId());
            electricityTradeOrder.setParentOrderId(unionTradeOrder.getId());
            electricityTradeOrder.setPayFranchiseeId(payParamConfig.getFranchiseeId());
            electricityTradeOrder.setWechatMerchantId(payParamConfig.getThirdPartyMerchantId());
            electricityTradeOrderService.insert(electricityTradeOrder);
        }
        
        OrderCreateParamConverterModel model = new OrderCreateParamConverterModel();
        model.setOrderId(unionTradeOrder.getTradeOrderNo());
        model.setExpireTime(System.currentTimeMillis() + 3600000);
        model.setDescription(unionPayOrder.getDescription());
        model.setAttach(unionPayOrder.getAttach());
        model.setAmount(unionPayOrder.getPayAmount());
        model.setCurrency("CNY");
        model.setOpenId(openId);
        model.setPayConfig(payParamConfig);
        
        BasePayRequest basePayRequest = payConfigConverter
                .converterOrderCreate(model, config -> config.getPayCallBackUrl() + unionTradeOrder.getTenantId() + "/" + payParamConfig.getFranchiseeId());
        
        return payServiceDispatcher.order(basePayRequest);
    }
    
    /**
     * 押金套餐混合支付回调 （新）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Pair<Boolean, Object> notifyIntegratedPayment(BaseOrderCallBackResource callBackResource) {
        
        // 回调参数
        String tradeOrderNo = callBackResource.getOutTradeNo();
        String transactionId = callBackResource.getTransactionId();
        
        UnionTradeOrder unionTradeOrder = baseMapper.selectTradeOrderByTradeOrderNo(tradeOrderNo);
        if (Objects.isNull(unionTradeOrder)) {
            log.warn("NOTIFY_INSURANCE_UNION_DEPOSIT_ORDER WARN ,NOT FOUND ELECTRICITY_TRADE_ORDER TRADE_ORDER_NO={}", tradeOrderNo);
            return Pair.of(false, "未找到交易订单!");
        }
        if (ObjectUtil.notEqual(UnionTradeOrder.STATUS_INIT, unionTradeOrder.getStatus())) {
            log.warn("NOTIFY_INSURANCE_UNION_DEPOSIT_ORDER WARN , ELECTRICITY_TRADE_ORDER  STATUS IS NOT INIT, TRADE_ORDER_NO={}", tradeOrderNo);
            return Pair.of(false, "交易订单已处理");
        }
        
        List<ElectricityTradeOrder> electricityTradeOrderList = electricityTradeOrderService.selectTradeOrderByParentOrderId(unionTradeOrder.getId());
        if (Objects.isNull(electricityTradeOrderList)) {
            log.warn("NOTIFY_INSURANCE_UNION_DEPOSIT_ORDER WARN ,NOT FOUND ELECTRICITY_TRADE_ORDER TRADE_ORDER_NO={}", tradeOrderNo);
            return Pair.of(false, "未找到交易订单!");
        }
        
        String jsonOrderType = unionTradeOrder.getJsonOrderType();
        List<Integer> orderTypeList = JsonUtil.fromJsonArray(jsonOrderType, Integer.class);
        
        String jsonOrderId = unionTradeOrder.getJsonOrderId();
        List<String> orderIdLIst = JsonUtil.fromJsonArray(jsonOrderId, String.class);
        
        if (CollectionUtils.isEmpty(orderIdLIst)) {
            log.warn("NOTIFY_INSURANCE_UNION_DEPOSIT_ORDER WARN ,NOT FOUND ELECTRICITY_TRADE_ORDER TRADE_ORDER_NO={}", tradeOrderNo);
            return Pair.of(false, "未找到交易订单");
        }
        
        Integer tradeOrderStatus = ElectricityTradeOrder.STATUS_FAIL;
        Integer depositOrderStatus = EleDepositOrder.STATUS_FAIL;
        boolean result = false;
        if (callBackResource.tradeStateIsSuccess()) {
            tradeOrderStatus = ElectricityTradeOrder.STATUS_SUCCESS;
            depositOrderStatus = EleDepositOrder.STATUS_SUCCESS;
            result = true;
        } else {
            log.warn("NOTIFY REDULT PAY FAIL,ORDER_NO={}" + tradeOrderNo);
        }
        
        for (int i = 0; i < orderTypeList.size(); i++) {
            if (Objects.equals(orderTypeList.get(i), UnionPayOrder.ORDER_TYPE_DEPOSIT)) {
                Pair<Boolean, Object> manageDepositOrderResult = manageDepositOrder(orderIdLIst.get(i), depositOrderStatus);
                if (!manageDepositOrderResult.getLeft()) {
                    return manageDepositOrderResult;
                }
            } else if (Objects.equals(orderTypeList.get(i), UnionPayOrder.ORDER_TYPE_INSURANCE)) {
                Pair<Boolean, Object> manageInsuranceOrderResult = manageInsuranceOrder(orderIdLIst.get(i), depositOrderStatus);
                if (!manageInsuranceOrderResult.getLeft()) {
                    return manageInsuranceOrderResult;
                }
            } else if (Objects.equals(orderTypeList.get(i), UnionPayOrder.ORDER_TYPE_MEMBER_CARD)) {
                Pair<Boolean, Object> manageMemberCardOrderResult = manageMemberCardOrder(orderIdLIst.get(i), depositOrderStatus);
                if (!manageMemberCardOrderResult.getLeft()) {
                    return manageMemberCardOrderResult;
                }
            } else if (Objects.equals(orderTypeList.get(i), UnionPayOrder.ORDER_TYPE_RENT_CAR_DEPOSIT)) {
            
            } else if (Objects.equals(orderTypeList.get(i), UnionPayOrder.ORDER_TYPE_RENT_CAR_MEMBER_CARD)) {
            
            }
        }
        
        // 系统订单
        UnionTradeOrder unionTradeOrderUpdate = new UnionTradeOrder();
        unionTradeOrderUpdate.setId(unionTradeOrder.getId());
        unionTradeOrderUpdate.setStatus(tradeOrderStatus);
        unionTradeOrderUpdate.setUpdateTime(System.currentTimeMillis());
        unionTradeOrderUpdate.setChannelOrderNo(transactionId);
        baseMapper.updateById(unionTradeOrderUpdate);
        
        // 混合支付的子订单
        Integer finalTradeOrderStatus = tradeOrderStatus;
        electricityTradeOrderList.parallelStream().forEach(item -> {
            ElectricityTradeOrder electricityTradeOrder = new ElectricityTradeOrder();
            electricityTradeOrder.setId(item.getId());
            electricityTradeOrder.setStatus(finalTradeOrderStatus);
            electricityTradeOrder.setUpdateTime(System.currentTimeMillis());
            electricityTradeOrder.setChannelOrderNo(transactionId);
            electricityTradeOrderService.updateElectricityTradeOrderById(electricityTradeOrder);
        });
        
        UserInfo userInfo = userInfoService.queryByUidFromCache(unionTradeOrder.getUid());
        if (Objects.isNull(userInfo)) {
            
            return Pair.of(result, null);
        }
        
        // 小程序虚拟发货
        if (ChannelEnum.WECHAT.getCode().equals(callBackResource.getChannel())) {
            shippingManagerService.uploadShippingInfo(unionTradeOrder.getUid(), userInfo.getPhone(), transactionId, userInfo.getTenantId());
        }
        
        return Pair.of(result, null);
    }
    
    /**
     * 套餐&保险支付回调  抄的上面的支付回调 @See notifyIntegratedPayment
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Pair<Boolean, Object> notifyMembercardInsurance(BaseOrderCallBackResource callBackResource) {
        
        String tradeOrderNo = callBackResource.getOutTradeNo();
        String transactionId = callBackResource.getTransactionId();
        
        UnionTradeOrder unionTradeOrder = baseMapper.selectTradeOrderByTradeOrderNo(tradeOrderNo);
        if (Objects.isNull(unionTradeOrder)) {
            log.warn("NOTIFY MEMBERCARD INSURANCE ORDER WARN!not found unionTradeOrder,tradeOrderNo={}", tradeOrderNo);
            return Pair.of(false, "未找到交易订单!");
        }
        if (ObjectUtil.notEqual(UnionTradeOrder.STATUS_INIT, unionTradeOrder.getStatus())) {
            log.warn("NOTIFY MEMBERCARD INSURANCE ORDER WARN! unionTradeOrder status is not init,tradeOrderNo={}", tradeOrderNo);
            return Pair.of(false, "交易订单已处理");
        }
        
        List<ElectricityTradeOrder> electricityTradeOrderList = electricityTradeOrderService.selectTradeOrderByParentOrderId(unionTradeOrder.getId());
        if (CollectionUtils.isEmpty(electricityTradeOrderList)) {
            log.warn("NOTIFY MEMBERCARD INSURANCE ORDER WARN! electricityTradeOrderList is empty,tradeOrderNo={}", tradeOrderNo);
            return Pair.of(false, "未找到交易订单!");
        }
        
        List<Integer> orderTypeList = JsonUtil.fromJsonArray(unionTradeOrder.getJsonOrderType(), Integer.class);
        if (CollectionUtils.isEmpty(orderTypeList)) {
            log.warn("NOTIFY MEMBERCARD INSURANCE ORDER WARN! orderTypeList is empty,tradeOrderNo={}", tradeOrderNo);
            return Pair.of(false, "未找到交易订单");
        }
        
        List<String> orderIdList = JsonUtil.fromJsonArray(unionTradeOrder.getJsonOrderId(), String.class);
        if (CollectionUtils.isEmpty(orderIdList)) {
            log.warn("NOTIFY MEMBERCARD INSURANCE ORDER WARN! orderIdList is empty,tradeOrderNo={}", tradeOrderNo);
            return Pair.of(false, "未找到交易订单");
        }
        
        Integer tradeOrderStatus = ElectricityTradeOrder.STATUS_FAIL;
        if (callBackResource.tradeStateIsSuccess()) {
            tradeOrderStatus = ElectricityTradeOrder.STATUS_SUCCESS;
        } else {
            log.warn("NOTIFY REDULT PAY FAIL,ORDER_NO={}" + tradeOrderNo);
        }
        
        UserInfo userInfo = userInfoService.queryByUidFromCache(unionTradeOrder.getUid());
        if (Objects.isNull(userInfo)) {
            log.warn("NOTIFY MEMBERCARD INSURANCE ORDER WARN! not found userInfo,uid={}", unionTradeOrder.getUid());
            return Pair.of(true, null);
        }
        
        for (int i = 0; i < orderTypeList.size(); i++) {
            if (Objects.equals(orderTypeList.get(i), UnionPayOrder.ORDER_TYPE_INSURANCE)) {
                manageInsuranceOrder(orderIdList.get(i), tradeOrderStatus);
            }
            
            if (Objects.equals(orderTypeList.get(i), UnionPayOrder.ORDER_TYPE_MEMBER_CARD)) {
                manageMemberCardOrderV2(orderIdList.get(i), tradeOrderStatus);
            }
        }
        
        // 系统订单
        UnionTradeOrder unionTradeOrderUpdate = new UnionTradeOrder();
        unionTradeOrderUpdate.setId(unionTradeOrder.getId());
        unionTradeOrderUpdate.setStatus(tradeOrderStatus);
        unionTradeOrderUpdate.setUpdateTime(System.currentTimeMillis());
        unionTradeOrderUpdate.setChannelOrderNo(transactionId);
        baseMapper.updateById(unionTradeOrderUpdate);
        
        // 混合支付的子订单
        Integer finalTradeOrderStatus = tradeOrderStatus;
        electricityTradeOrderList.parallelStream().forEach(item -> {
            ElectricityTradeOrder electricityTradeOrder = new ElectricityTradeOrder();
            electricityTradeOrder.setId(item.getId());
            electricityTradeOrder.setStatus(finalTradeOrderStatus);
            electricityTradeOrder.setUpdateTime(System.currentTimeMillis());
            electricityTradeOrder.setChannelOrderNo(transactionId);
            electricityTradeOrderService.updateElectricityTradeOrderById(electricityTradeOrder);
        });
        
        // 小程序虚拟发货
        if (ChannelEnum.WECHAT.getCode().equals(callBackResource.getChannel())) {
            shippingManagerService.uploadShippingInfo(unionTradeOrder.getUid(), userInfo.getPhone(), transactionId, userInfo.getTenantId());
        }
        return Pair.of(true, null);
    }
    
    // 处理押金订单
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Pair<Boolean, Object> manageDepositOrder(String orderNo, Integer orderStatus) {
        
        // 押金订单
        EleDepositOrder eleDepositOrder = eleDepositOrderService.queryByOrderId(orderNo);
        if (ObjectUtil.isEmpty(eleDepositOrder)) {
            log.warn("NOTIFY_DEPOSIT_ORDER WARN ,NOT FOUND ELECTRICITY_DEPOSIT_ORDER ORDER_NO={}", orderNo);
            return Pair.of(false, "未找到订单!");
        }
        
        if (!ObjectUtil.equal(EleDepositOrder.STATUS_INIT, eleDepositOrder.getStatus())) {
            log.warn("NOTIFY_DEPOSIT_ORDER WARN , ELECTRICITY_DEPOSIT_ORDER  STATUS IS NOT INIT, ORDER_NO={}", orderNo);
            return Pair.of(false, "押金订单已处理!");
        }
        
        // 用户
        UserInfo userInfo = userInfoService.queryByUidFromCache(eleDepositOrder.getUid());
        if (Objects.isNull(userInfo)) {
            log.warn("NOTIFY_DEPOSIT_ORDER WARN,NOT FOUND USERINFO,USERID={},ORDER_NO={}", eleDepositOrder.getUid(), orderNo);
            return Pair.of(false, "未找到用户信息!");
        }
        
        // 用户押金
        if (Objects.equals(orderStatus, EleDepositOrder.STATUS_SUCCESS)) {
            UserInfo updateUserInfo = new UserInfo();
            updateUserInfo.setUid(userInfo.getUid());
            updateUserInfo.setBatteryDepositStatus(UserInfo.BATTERY_DEPOSIT_STATUS_YES);
            if (Objects.equals(userInfo.getFranchiseeId(), NumberConstant.ZERO_L)) {
                updateUserInfo.setFranchiseeId(eleDepositOrder.getFranchiseeId());
            }
            if (Objects.equals(userInfo.getStoreId(), NumberConstant.ZERO_L)) {
                updateUserInfo.setStoreId(eleDepositOrder.getStoreId());
            }
            updateUserInfo.setUpdateTime(System.currentTimeMillis());
            userInfoService.updateByUid(updateUserInfo);
            
            UserBatteryDeposit userBatteryDeposit = new UserBatteryDeposit();
            userBatteryDeposit.setUid(userInfo.getUid());
            userBatteryDeposit.setOrderId(eleDepositOrder.getOrderId());
            userBatteryDeposit.setDid(eleDepositOrder.getMid());
            userBatteryDeposit.setBatteryDeposit(eleDepositOrder.getPayAmount());
            userBatteryDeposit.setCreateTime(System.currentTimeMillis());
            userBatteryDeposit.setDelFlag(UserBatteryDeposit.DEL_NORMAL);
            userBatteryDeposit.setApplyDepositTime(System.currentTimeMillis());
            userBatteryDeposit.setDepositType(UserBatteryDeposit.DEPOSIT_TYPE_DEFAULT);
            userBatteryDeposit.setUpdateTime(System.currentTimeMillis());
            userBatteryDepositService.insertOrUpdate(userBatteryDeposit);
            
            // 保存用户押金对应的电池型号
            List<String> batteryTypeList = memberCardBatteryTypeService.selectBatteryTypeByMid(eleDepositOrder.getMid());
            if (CollectionUtils.isNotEmpty(batteryTypeList)) {
                userBatteryTypeService.batchInsert(userBatteryTypeService.buildUserBatteryType(batteryTypeList, userInfo));
            }
            
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
                @Override
                public void afterCommit() {
                    // 清理缓存，避免缓存操作和数据库提交在同一个事务中失效的问题
                    redisService.delete(CacheConstant.CACHE_USER_INFO + userInfo.getUid());
                    redisService.delete(CacheConstant.CACHE_USER_DEPOSIT + userInfo.getUid());
                }
                
            });
        }
        
        // 押金订单
        EleDepositOrder eleDepositOrderUpdate = new EleDepositOrder();
        eleDepositOrderUpdate.setId(eleDepositOrder.getId());
        eleDepositOrderUpdate.setStatus(orderStatus);
        eleDepositOrderUpdate.setUpdateTime(System.currentTimeMillis());
        eleDepositOrderService.update(eleDepositOrderUpdate);
        return Pair.of(true, null);
    }
    
    // 处理购卡订单
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Pair<Boolean, Object> manageMemberCardOrder(String orderNo, Integer orderStatus) {
        
        // 购卡订单
        ElectricityMemberCardOrder electricityMemberCardOrder = electricityMemberCardOrderService.selectByOrderNo(orderNo);
        if (ObjectUtil.isEmpty(electricityMemberCardOrder)) {
            log.warn("NOTIFY_MEMBER_ORDER WARN ,NOT FOUND ELECTRICITY_MEMBER_CARD_ORDER ORDER_NO={}", orderNo);
            return Pair.of(false, "未找到订单!");
        }
        
        // 处理用户端取消支付的问题
        if (Objects.equals(ElectricityMemberCardOrder.STATUS_CANCELL, electricityMemberCardOrder.getStatus())) {
            electricityMemberCardOrder.setStatus(ElectricityMemberCardOrder.STATUS_INIT);
        }
        
        if (!ObjectUtil.equal(ElectricityMemberCardOrder.STATUS_INIT, electricityMemberCardOrder.getStatus())) {
            log.warn("NOTIFY_MEMBER_ORDER WARN , ELECTRICITY_MEMBER_CARD_ORDER  STATUS IS NOT INIT, ORDER_NO={}", orderNo);
            return Pair.of(false, "套餐订单已处理!");
        }
        
        UserInfo userInfo = userInfoService.queryByUidFromCache(electricityMemberCardOrder.getUid());
        if (Objects.isNull(userInfo)) {
            log.warn("NOTIFY_MEMBER_ORDER WARN!userInfo is null,uid={}", electricityMemberCardOrder.getUid());
            return Pair.of(false, "用户不存在");
        }
        
        BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(electricityMemberCardOrder.getMemberCardId());
        if (Objects.isNull(batteryMemberCard)) {
            log.warn("NOTIFY_MEMBER_ORDER WARN!batteryMemberCard is null,uid={},mid={}", electricityMemberCardOrder.getUid(), electricityMemberCardOrder.getMemberCardId());
            return Pair.of(false, "套餐不存在");
        }
        
        // 获取套餐订单优惠券
        List<Long> userCouponIds = memberCardOrderCouponService.selectCouponIdsByOrderId(electricityMemberCardOrder.getOrderId());
        
        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(electricityMemberCardOrder.getUid());
        Long remainingNumber = electricityMemberCardOrder.getMaxUseCount();
        Integer payCount = electricityMemberCardOrderService.queryMaxPayCount(userBatteryMemberCard);
        
        // 月卡订单
        ElectricityMemberCardOrder electricityMemberCardOrderUpdate = new ElectricityMemberCardOrder();
        
        if (Objects.equals(orderStatus, EleDepositOrder.STATUS_SUCCESS)) {
            
            UserBatteryMemberCard userBatteryMemberCardUpdate = new UserBatteryMemberCard();
            userBatteryMemberCardUpdate.setUid(electricityMemberCardOrder.getUid());
            userBatteryMemberCardUpdate.setOrderId(electricityMemberCardOrder.getOrderId());
            userBatteryMemberCardUpdate.setOrderExpireTime(
                    System.currentTimeMillis() + batteryMemberCardService.transformBatteryMembercardEffectiveTime(batteryMemberCard, electricityMemberCardOrder));
            userBatteryMemberCardUpdate.setOrderEffectiveTime(System.currentTimeMillis());
            userBatteryMemberCardUpdate.setMemberCardExpireTime(
                    System.currentTimeMillis() + batteryMemberCardService.transformBatteryMembercardEffectiveTime(batteryMemberCard, electricityMemberCardOrder));
            userBatteryMemberCardUpdate.setOrderRemainingNumber(remainingNumber);
            userBatteryMemberCardUpdate.setRemainingNumber(remainingNumber);
            userBatteryMemberCardUpdate.setMemberCardStatus(UserBatteryMemberCard.MEMBER_CARD_NOT_DISABLE);
            userBatteryMemberCardUpdate.setMemberCardId(electricityMemberCardOrder.getMemberCardId());
            userBatteryMemberCardUpdate.setUpdateTime(System.currentTimeMillis());
            userBatteryMemberCardUpdate.setDelFlag(UserBatteryMemberCard.DEL_NORMAL);
            userBatteryMemberCardUpdate.setCreateTime(System.currentTimeMillis());
            userBatteryMemberCardUpdate.setTenantId(electricityMemberCardOrder.getTenantId());
            userBatteryMemberCardUpdate.setCardPayCount(payCount + 1);
            if (Objects.isNull(userBatteryMemberCard)) {
                userBatteryMemberCardService.insert(userBatteryMemberCardUpdate);
            } else {
                userBatteryMemberCardService.updateByUid(userBatteryMemberCardUpdate);
            }
            
            ServiceFeeUserInfo serviceFeeUserInfo = serviceFeeUserInfoService.queryByUidFromCache(userBatteryMemberCardUpdate.getUid());
            ServiceFeeUserInfo serviceFeeUserInfoInsertOrUpdate = new ServiceFeeUserInfo();
            serviceFeeUserInfoInsertOrUpdate.setServiceFeeGenerateTime(
                    System.currentTimeMillis() + batteryMemberCardService.transformBatteryMembercardEffectiveTime(batteryMemberCard, electricityMemberCardOrder));
            serviceFeeUserInfoInsertOrUpdate.setUid(userBatteryMemberCardUpdate.getUid());
            serviceFeeUserInfoInsertOrUpdate.setFranchiseeId(electricityMemberCardOrder.getFranchiseeId());
            serviceFeeUserInfoInsertOrUpdate.setUpdateTime(System.currentTimeMillis());
            serviceFeeUserInfoInsertOrUpdate.setTenantId(electricityMemberCardOrder.getTenantId());
            if (Objects.isNull(serviceFeeUserInfo)) {
                serviceFeeUserInfoInsertOrUpdate.setCreateTime(System.currentTimeMillis());
                serviceFeeUserInfoInsertOrUpdate.setDelFlag(ServiceFeeUserInfo.DEL_NORMAL);
                serviceFeeUserInfoInsertOrUpdate.setDisableMemberCardNo("");
                serviceFeeUserInfoService.insert(serviceFeeUserInfoInsertOrUpdate);
            } else {
                serviceFeeUserInfoService.updateByUid(serviceFeeUserInfoInsertOrUpdate);
            }
            
            // 更新优惠券状态
            if (CollectionUtils.isNotEmpty(userCouponIds)) {
                Set<Integer> couponIds = userCouponIds.parallelStream().map(Long::intValue).collect(Collectors.toSet());
                userCouponService
                        .batchUpdateUserCoupon(electricityMemberCardOrderService.buildUserCouponList(couponIds, UserCoupon.STATUS_USED, electricityMemberCardOrder.getOrderId()));
            }
            
            // 修改套餐订单购买次数
            electricityMemberCardOrderUpdate.setPayCount(userBatteryMemberCardUpdate.getCardPayCount());
            
            electricityMemberCardOrderUpdate.setUseStatus(ElectricityMemberCardOrder.USE_STATUS_USING);
            
            UserInfo userInfoUpdate = new UserInfo();
            userInfoUpdate.setUid(userInfo.getUid());
            userInfoUpdate.setPayCount(userInfo.getPayCount() + 1);
            userInfoUpdate.setUpdateTime(System.currentTimeMillis());
            if (Objects.nonNull(electricityMemberCardOrder.getRefId()) && Objects.equals(userInfo.getStoreId(), NumberConstant.ZERO_L)) {
                ElectricityCabinet electricityCabinet = electricityCabinetService.queryByIdFromCache(electricityMemberCardOrder.getRefId().intValue());
                if (Objects.nonNull(electricityCabinet)) {
                    userInfoUpdate.setStoreId(electricityCabinet.getStoreId());
                }
            }
            userInfoService.updateByUid(userInfoUpdate);
            
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
                @Override
                public void afterCommit() {
                    // 清理缓存，避免缓存操作和数据库提交在同一个事务中失效的问题。如有其他业务，请加在清理缓存之后处理
                    redisService.delete(CacheConstant.CACHE_USER_BATTERY_MEMBERCARD + userInfo.getUid());
                    redisService.delete(CacheConstant.SERVICE_FEE_USER_INFO + userInfo.getUid());
                    redisService.delete(CacheConstant.CACHE_USER_INFO + userInfo.getUid());
                    
                    // 8. 处理分账
                    DivisionAccountOrderDTO divisionAccountOrderDTO = new DivisionAccountOrderDTO();
                    divisionAccountOrderDTO.setOrderNo(orderNo);
                    divisionAccountOrderDTO.setType(PackageTypeEnum.PACKAGE_TYPE_BATTERY.getCode());
                    divisionAccountOrderDTO.setDivisionAccountType(DivisionAccountEnum.DA_TYPE_PURCHASE.getCode());
                    divisionAccountOrderDTO.setTraceId(IdUtil.simpleUUID());
                    divisionAccountRecordService.asyncHandleDivisionAccount(divisionAccountOrderDTO);
                    
                    // 9. 处理活动
                    ActivityProcessDTO activityProcessDTO = new ActivityProcessDTO();
                    activityProcessDTO.setOrderNo(orderNo);
                    activityProcessDTO.setType(PackageTypeEnum.PACKAGE_TYPE_BATTERY.getCode());
                    activityProcessDTO.setActivityType(ActivityEnum.INVITATION_CRITERIA_BUY_PACKAGE.getCode());
                    activityProcessDTO.setTraceId(IdUtil.simpleUUID());
                    activityService.asyncProcessActivity(activityProcessDTO);
                    
                    electricityMemberCardOrderService.sendUserCoupon(batteryMemberCard, electricityMemberCardOrder);
                }
            });
        } else {
            // 支付失败 更新优惠券状态
            if (CollectionUtils.isNotEmpty(userCouponIds)) {
                Set<Integer> couponIds = userCouponIds.parallelStream().map(Long::intValue).collect(Collectors.toSet());
                userCouponService
                        .batchUpdateUserCoupon(electricityMemberCardOrderService.buildUserCouponList(couponIds, UserCoupon.STATUS_UNUSED, electricityMemberCardOrder.getOrderId()));
            }
        }
        
        electricityMemberCardOrderUpdate.setId(electricityMemberCardOrder.getId());
        electricityMemberCardOrderUpdate.setStatus(orderStatus);
        electricityMemberCardOrderUpdate.setUpdateTime(System.currentTimeMillis());
        electricityMemberCardOrderService.updateByID(electricityMemberCardOrderUpdate);
        redisService.delete(CacheConstant.CACHE_USER_BATTERY_MEMBERCARD + userInfo.getUid());
        return Pair.of(true, null);
    }
    
    
    private void sendMerchantRebateMQ(Long uid, String orderId) {
        UserInfoExtra userInfoExtra = userInfoExtraService.queryByUidFromCache(uid);
        if (Objects.isNull(userInfoExtra)) {
            log.warn("BATTERY MERCHANT REBATE WARN!userInfoExtra is null,uid={}", uid);
            return;
        }
        
        if (Objects.isNull(userInfoExtra.getMerchantId())) {
            log.warn("BATTERY MERCHANT REBATE WARN!merchantId is null,uid={}", uid);
            return;
        }
        
        BatteryMemberCardMerchantRebate merchantRebate = new BatteryMemberCardMerchantRebate();
        merchantRebate.setUid(uid);
        merchantRebate.setOrderId(orderId);
        merchantRebate.setType(MerchantConstant.TYPE_PURCHASE);
        merchantRebate.setMerchantId(userInfoExtra.getMerchantId());
        // 续费成功  发送返利MQ
        rocketMqService.sendAsyncMsg(MqProducerConstant.BATTERY_MEMBER_CARD_MERCHANT_REBATE_TOPIC, JsonUtil.toJson(merchantRebate));
    }
    
    /**
     * 3.0电池套餐续费回调处理
     *
     * @param orderNo
     * @param orderStatus
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Pair<Boolean, Object> manageMemberCardOrderV2(String orderNo, Integer orderStatus) {
        ElectricityMemberCardOrder electricityMemberCardOrder = electricityMemberCardOrderService.selectByOrderNo(orderNo);
        if (ObjectUtil.isEmpty(electricityMemberCardOrder)) {
            log.warn("NOTIFY MEMBERCARD INSURANCE ORDER WARN!not found electricityMemberCardOrder,orderNo={}", orderNo);
            return Pair.of(false, "未找到订单!");
        }
        
        // 处理用户端取消支付的问题
        if (Objects.equals(ElectricityMemberCardOrder.STATUS_CANCELL, electricityMemberCardOrder.getStatus())) {
            electricityMemberCardOrder.setStatus(ElectricityMemberCardOrder.STATUS_INIT);
        }
        
        if (!ObjectUtil.equal(ElectricityMemberCardOrder.STATUS_INIT, electricityMemberCardOrder.getStatus())) {
            log.warn("NOTIFY MEMBERCARD INSURANCE ORDER WARN!electricityMemberCardOrder status is not init, orderNo={}", orderNo);
            return Pair.of(false, "套餐订单已处理!");
        }
        
        UserInfo userInfo = userInfoService.queryByUidFromCache(electricityMemberCardOrder.getUid());
        if (Objects.isNull(userInfo)) {
            log.warn("NOTIFY MEMBERCARD INSURANCE ORDER WARN!userInfo is null,uid={}", electricityMemberCardOrder.getUid());
            return Pair.of(false, "用户不存在");
        }
        
        BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(electricityMemberCardOrder.getMemberCardId());
        if (Objects.isNull(batteryMemberCard)) {
            log.warn("NOTIFY MEMBERCARD INSURANCE ORDER WARN!batteryMemberCard is null,uid={},mid={}", electricityMemberCardOrder.getUid(),
                    electricityMemberCardOrder.getMemberCardId());
            return Pair.of(false, "套餐不存在");
        }
        
        // 获取套餐订单优惠券
        List<Long> userCouponIds = memberCardOrderCouponService.selectCouponIdsByOrderId(electricityMemberCardOrder.getOrderId());
        
        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(electricityMemberCardOrder.getUid());
        
        Long remainingNumber = electricityMemberCardOrder.getMaxUseCount();
        
        Integer payCount = electricityMemberCardOrderService.queryMaxPayCount(userBatteryMemberCard);
        
        // 月卡订单
        ElectricityMemberCardOrder electricityMemberCardOrderUpdate = new ElectricityMemberCardOrder();
        
        if (Objects.equals(orderStatus, ElectricityMemberCardOrder.STATUS_SUCCESS)) {
            UserBatteryMemberCard userBatteryMemberCardUpdate = new UserBatteryMemberCard();
            // 若用户未购买套餐  直接绑定
            if (Objects.isNull(userBatteryMemberCard) || Objects.isNull(userBatteryMemberCard.getMemberCardId()) || Objects
                    .equals(userBatteryMemberCard.getMemberCardId(), UserBatteryMemberCard.SEND_REMAINING_NUMBER)) {
                electricityMemberCardOrderUpdate.setUseStatus(ElectricityMemberCardOrder.USE_STATUS_USING);
                
                userBatteryMemberCardUpdate.setUid(electricityMemberCardOrder.getUid());
                userBatteryMemberCardUpdate.setOrderId(electricityMemberCardOrder.getOrderId());
                userBatteryMemberCardUpdate.setOrderExpireTime(
                        System.currentTimeMillis() + batteryMemberCardService.transformBatteryMembercardEffectiveTime(batteryMemberCard, electricityMemberCardOrder));
                userBatteryMemberCardUpdate.setOrderEffectiveTime(System.currentTimeMillis());
                userBatteryMemberCardUpdate.setMemberCardExpireTime(
                        System.currentTimeMillis() + batteryMemberCardService.transformBatteryMembercardEffectiveTime(batteryMemberCard, electricityMemberCardOrder));
                userBatteryMemberCardUpdate.setOrderRemainingNumber(remainingNumber);
                userBatteryMemberCardUpdate.setRemainingNumber(remainingNumber);
                userBatteryMemberCardUpdate.setMemberCardStatus(UserBatteryMemberCard.MEMBER_CARD_NOT_DISABLE);
                userBatteryMemberCardUpdate.setMemberCardId(electricityMemberCardOrder.getMemberCardId());
                userBatteryMemberCardUpdate.setUpdateTime(System.currentTimeMillis());
                userBatteryMemberCardUpdate.setDelFlag(UserBatteryMemberCard.DEL_NORMAL);
                userBatteryMemberCardUpdate.setCreateTime(System.currentTimeMillis());
                userBatteryMemberCardUpdate.setTenantId(electricityMemberCardOrder.getTenantId());
                userBatteryMemberCardUpdate.setCardPayCount(payCount + 1);
                
                // 保存用户押金对应的电池型号
                List<String> batteryTypeList = memberCardBatteryTypeService.selectBatteryTypeByMid(batteryMemberCard.getId());
                if (CollectionUtils.isNotEmpty(batteryTypeList)) {
                    userBatteryTypeService.batchInsert(userBatteryTypeService.buildUserBatteryType(batteryTypeList, userInfo));
                }
            } else {
                BatteryMemberCard userBindbatteryMemberCard = batteryMemberCardService.queryByIdFromCache(userBatteryMemberCard.getMemberCardId());
                // 若用户已购买套餐
                //     1.套餐过期，直接绑定
                //     2.套餐未过期，保存到资源包
                if (userBatteryMemberCard.getMemberCardExpireTime() < System.currentTimeMillis() || (
                        Objects.equals(userBindbatteryMemberCard.getLimitCount(), BatteryMemberCard.LIMIT) && userBatteryMemberCard.getRemainingNumber() <= 0)) {
                    
                    electricityMemberCardOrderUpdate.setUseStatus(ElectricityMemberCardOrder.USE_STATUS_USING);
                    
                    userBatteryMemberCardUpdate.setUid(userInfo.getUid());
                    userBatteryMemberCardUpdate.setMemberCardId(batteryMemberCard.getId());
                    userBatteryMemberCardUpdate.setOrderId(electricityMemberCardOrder.getOrderId());
                    userBatteryMemberCardUpdate.setMemberCardExpireTime(
                            System.currentTimeMillis() + batteryMemberCardService.transformBatteryMembercardEffectiveTime(batteryMemberCard, electricityMemberCardOrder));
                    userBatteryMemberCardUpdate.setOrderExpireTime(
                            System.currentTimeMillis() + batteryMemberCardService.transformBatteryMembercardEffectiveTime(batteryMemberCard, electricityMemberCardOrder));
                    userBatteryMemberCardUpdate.setOrderEffectiveTime(System.currentTimeMillis());
                    userBatteryMemberCardUpdate.setOrderRemainingNumber(electricityMemberCardOrder.getMaxUseCount());
                    userBatteryMemberCardUpdate.setRemainingNumber(electricityMemberCardOrder.getMaxUseCount());
                    userBatteryMemberCardUpdate.setMemberCardStatus(UserBatteryMemberCard.MEMBER_CARD_NOT_DISABLE);
                    userBatteryMemberCardUpdate.setDisableMemberCardTime(null);
                    userBatteryMemberCardUpdate.setDelFlag(UserBatteryMemberCard.DEL_NORMAL);
                    userBatteryMemberCardUpdate.setCreateTime(System.currentTimeMillis());
                    userBatteryMemberCardUpdate.setUpdateTime(System.currentTimeMillis());
                    userBatteryMemberCardUpdate.setTenantId(userInfo.getTenantId());
                    userBatteryMemberCardUpdate.setCardPayCount(electricityMemberCardOrderService.queryMaxPayCount(userBatteryMemberCard) + 1);
                    
                    // 如果用户原来绑定的有套餐 套餐过期了，需要把原来绑定的套餐订单状态更新为已过期
                    if (org.apache.commons.lang3.StringUtils.isNotBlank(userBatteryMemberCard.getOrderId())) {
                        ElectricityMemberCardOrder electricityMemberCardOrderUpdateUseStatus = new ElectricityMemberCardOrder();
                        electricityMemberCardOrderUpdateUseStatus.setOrderId(userBatteryMemberCard.getOrderId());
                        electricityMemberCardOrderUpdateUseStatus.setUseStatus(ElectricityMemberCardOrder.USE_STATUS_EXPIRE);
                        electricityMemberCardOrderUpdateUseStatus.setUpdateTime(System.currentTimeMillis());
                        electricityMemberCardOrderService.updateStatusByOrderNo(electricityMemberCardOrderUpdateUseStatus);
                    }
                    
                    // 保存用户押金对应的电池型号
                    //                    List<String> batteryTypeList = memberCardBatteryTypeService.selectBatteryTypeByMid(batteryMemberCard.getId());
                    //                    if (CollectionUtils.isNotEmpty(batteryTypeList)) {
                    //                        userBatteryTypeService.batchInsert(userBatteryTypeService.buildUserBatteryType(batteryTypeList, userInfo));
                    //                    }
                    
                    userBatteryTypeService.updateUserBatteryType(electricityMemberCardOrder, userInfo);
                } else {
                    
                    UserBatteryMemberCardPackage userBatteryMemberCardPackage = new UserBatteryMemberCardPackage();
                    userBatteryMemberCardPackage.setUid(userInfo.getUid());
                    userBatteryMemberCardPackage.setMemberCardId(electricityMemberCardOrder.getMemberCardId());
                    userBatteryMemberCardPackage.setOrderId(electricityMemberCardOrder.getOrderId());
                    userBatteryMemberCardPackage.setRemainingNumber(batteryMemberCard.getUseCount());
                    userBatteryMemberCardPackage
                            .setMemberCardExpireTime(batteryMemberCardService.transformBatteryMembercardEffectiveTime(batteryMemberCard, electricityMemberCardOrder));
                    userBatteryMemberCardPackage.setTenantId(userInfo.getTenantId());
                    userBatteryMemberCardPackage.setCreateTime(System.currentTimeMillis());
                    userBatteryMemberCardPackage.setUpdateTime(System.currentTimeMillis());
                    userBatteryMemberCardPackageService.insert(userBatteryMemberCardPackage);
                    
                    userBatteryMemberCardUpdate.setUid(userInfo.getUid());
                    userBatteryMemberCardUpdate.setMemberCardExpireTime(userBatteryMemberCard.getMemberCardExpireTime() + batteryMemberCardService
                            .transformBatteryMembercardEffectiveTime(batteryMemberCard, electricityMemberCardOrder));
                    userBatteryMemberCardUpdate.setRemainingNumber(userBatteryMemberCard.getRemainingNumber() + electricityMemberCardOrder.getMaxUseCount());
                    userBatteryMemberCardUpdate.setCardPayCount(electricityMemberCardOrderService.queryMaxPayCount(userBatteryMemberCard) + 1);
                    userBatteryMemberCardUpdate.setUpdateTime(System.currentTimeMillis());
                }
            }
            
            if (Objects.isNull(userBatteryMemberCard)) {
                userBatteryMemberCardService.insert(userBatteryMemberCardUpdate);
            } else {
                userBatteryMemberCardService.updateByUid(userBatteryMemberCardUpdate);
            }
            
            ServiceFeeUserInfo serviceFeeUserInfo = serviceFeeUserInfoService.queryByUidFromCache(userBatteryMemberCardUpdate.getUid());
            ServiceFeeUserInfo serviceFeeUserInfoInsertOrUpdate = new ServiceFeeUserInfo();
            serviceFeeUserInfoInsertOrUpdate.setServiceFeeGenerateTime(userBatteryMemberCardUpdate.getMemberCardExpireTime());
            serviceFeeUserInfoInsertOrUpdate.setUid(userBatteryMemberCardUpdate.getUid());
            serviceFeeUserInfoInsertOrUpdate.setFranchiseeId(electricityMemberCardOrder.getFranchiseeId());
            serviceFeeUserInfoInsertOrUpdate.setUpdateTime(System.currentTimeMillis());
            serviceFeeUserInfoInsertOrUpdate.setTenantId(electricityMemberCardOrder.getTenantId());
            if (Objects.isNull(serviceFeeUserInfo)) {
                serviceFeeUserInfoInsertOrUpdate.setCreateTime(System.currentTimeMillis());
                serviceFeeUserInfoInsertOrUpdate.setDelFlag(ServiceFeeUserInfo.DEL_NORMAL);
                serviceFeeUserInfoInsertOrUpdate.setDisableMemberCardNo("");
                serviceFeeUserInfoService.insert(serviceFeeUserInfoInsertOrUpdate);
            } else {
                serviceFeeUserInfoService.updateByUid(serviceFeeUserInfoInsertOrUpdate);
            }
            
            // 更新优惠券状态
            if (CollectionUtils.isNotEmpty(userCouponIds)) {
                Set<Integer> couponIds = userCouponIds.parallelStream().map(Long::intValue).collect(Collectors.toSet());
                userCouponService
                        .batchUpdateUserCoupon(electricityMemberCardOrderService.buildUserCouponList(couponIds, UserCoupon.STATUS_USED, electricityMemberCardOrder.getOrderId()));
            }
            
            // 修改套餐订单购买次数
            electricityMemberCardOrderUpdate.setPayCount(userBatteryMemberCardUpdate.getCardPayCount());
            
            UserInfo userInfoUpdate = new UserInfo();
            userInfoUpdate.setUid(userInfo.getUid());
            userInfoUpdate.setPayCount(userInfo.getPayCount() + 1);
            userInfoUpdate.setUpdateTime(System.currentTimeMillis());
            userInfoService.updateByUid(userInfoUpdate);
            
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
                @Override
                public void afterCommit() {
                    // 清理缓存，避免缓存操作和数据库提交在同一个事务中失效的问题。如有其他业务，请加在清理缓存之后处理
                    redisService.delete(CacheConstant.CACHE_USER_BATTERY_MEMBERCARD + userInfo.getUid());
                    redisService.delete(CacheConstant.SERVICE_FEE_USER_INFO + userInfo.getUid());
                    redisService.delete(CacheConstant.CACHE_USER_INFO + userInfo.getUid());
                    
                    // 8. 处理分账
                    DivisionAccountOrderDTO divisionAccountOrderDTO = new DivisionAccountOrderDTO();
                    divisionAccountOrderDTO.setOrderNo(orderNo);
                    divisionAccountOrderDTO.setType(PackageTypeEnum.PACKAGE_TYPE_BATTERY.getCode());
                    divisionAccountOrderDTO.setDivisionAccountType(DivisionAccountEnum.DA_TYPE_PURCHASE.getCode());
                    divisionAccountOrderDTO.setTraceId(IdUtil.simpleUUID());
                    divisionAccountRecordService.asyncHandleDivisionAccount(divisionAccountOrderDTO);
                    
                    // 9. 处理活动
                    ActivityProcessDTO activityProcessDTO = new ActivityProcessDTO();
                    activityProcessDTO.setOrderNo(orderNo);
                    activityProcessDTO.setType(PackageTypeEnum.PACKAGE_TYPE_BATTERY.getCode());
                    activityProcessDTO.setActivityType(ActivityEnum.INVITATION_CRITERIA_BUY_PACKAGE.getCode());
                    activityProcessDTO.setTraceId(IdUtil.simpleUUID());
                    activityService.asyncProcessActivity(activityProcessDTO);
                    
                    electricityMemberCardOrderService.sendUserCoupon(batteryMemberCard, electricityMemberCardOrder);
                    
                }
            });
        } else {
            // 支付失败 更新优惠券状态
            if (CollectionUtils.isNotEmpty(userCouponIds)) {
                Set<Integer> couponIds = userCouponIds.parallelStream().map(Long::intValue).collect(Collectors.toSet());
                userCouponService
                        .batchUpdateUserCoupon(electricityMemberCardOrderService.buildUserCouponList(couponIds, UserCoupon.STATUS_UNUSED, electricityMemberCardOrder.getOrderId()));
            }
        }
        
        electricityMemberCardOrderUpdate.setId(electricityMemberCardOrder.getId());
        electricityMemberCardOrderUpdate.setStatus(orderStatus);
        electricityMemberCardOrderUpdate.setUpdateTime(System.currentTimeMillis());
        electricityMemberCardOrderService.updateByID(electricityMemberCardOrderUpdate);
        
        return Pair.of(true, null);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Pair<Boolean, Object> manageEnterpriseMemberCardOrder(String orderNo, Integer orderStatus) {
        
        ElectricityMemberCardOrder electricityMemberCardOrder = electricityMemberCardOrderService.selectByOrderNo(orderNo);
        if (ObjectUtil.isEmpty(electricityMemberCardOrder)) {
            log.warn("notify member card order WARN, not found electricityMemberCardOrder,orderNo={}", orderNo);
            return Pair.of(false, "未找到订单!");
        }
        
        if (!ObjectUtil.equal(ElectricityMemberCardOrder.STATUS_INIT, electricityMemberCardOrder.getStatus())) {
            log.warn("notify member card order WARN, electricityMemberCardOrder status is not init, orderNo={}", orderNo);
            return Pair.of(false, "套餐订单已处理!");
        }
        
        UserInfo userInfo = userInfoService.queryByUidFromCache(electricityMemberCardOrder.getUid());
        if (Objects.isNull(userInfo)) {
            log.warn("notify member card order WARN, userInfo is null,uid={}", electricityMemberCardOrder.getUid());
            return Pair.of(false, "用户不存在");
        }
        
        BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(electricityMemberCardOrder.getMemberCardId());
        if (Objects.isNull(batteryMemberCard)) {
            log.warn("notify member card order WARN, batteryMemberCard is null,uid={},mid={}", electricityMemberCardOrder.getUid(), electricityMemberCardOrder.getMemberCardId());
            return Pair.of(false, "套餐不存在");
        }
        
        // 获取套餐订单优惠券
        // List<Long> userCouponIds = memberCardOrderCouponService.selectCouponIdsByOrderId(electricityMemberCardOrder.getOrderId());
        
        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(electricityMemberCardOrder.getUid());
        
        Long remainingNumber = electricityMemberCardOrder.getMaxUseCount();
        
        Integer payCount = electricityMemberCardOrderService.queryMaxPayCount(userBatteryMemberCard);
        
        // 套餐订单
        ElectricityMemberCardOrder electricityMemberCardOrderUpdate = new ElectricityMemberCardOrder();
        
        if (Objects.equals(orderStatus, ElectricityMemberCardOrder.STATUS_SUCCESS)) {
            UserBatteryMemberCard userBatteryMemberCardUpdate = new UserBatteryMemberCard();
            // 若用户未购买套餐  直接绑定
            if (Objects.isNull(userBatteryMemberCard) || Objects.isNull(userBatteryMemberCard.getMemberCardId())) {
                electricityMemberCardOrderUpdate.setUseStatus(ElectricityMemberCardOrder.USE_STATUS_USING);
                
                userBatteryMemberCardUpdate.setUid(electricityMemberCardOrder.getUid());
                userBatteryMemberCardUpdate.setOrderId(electricityMemberCardOrder.getOrderId());
                userBatteryMemberCardUpdate.setOrderExpireTime(
                        System.currentTimeMillis() + batteryMemberCardService.transformBatteryMembercardEffectiveTime(batteryMemberCard, electricityMemberCardOrder));
                userBatteryMemberCardUpdate.setOrderEffectiveTime(System.currentTimeMillis());
                userBatteryMemberCardUpdate.setMemberCardExpireTime(
                        System.currentTimeMillis() + batteryMemberCardService.transformBatteryMembercardEffectiveTime(batteryMemberCard, electricityMemberCardOrder));
                userBatteryMemberCardUpdate.setOrderRemainingNumber(remainingNumber);
                userBatteryMemberCardUpdate.setRemainingNumber(remainingNumber);
                userBatteryMemberCardUpdate.setMemberCardStatus(UserBatteryMemberCard.MEMBER_CARD_NOT_DISABLE);
                userBatteryMemberCardUpdate.setMemberCardId(electricityMemberCardOrder.getMemberCardId());
                userBatteryMemberCardUpdate.setUpdateTime(System.currentTimeMillis());
                userBatteryMemberCardUpdate.setDelFlag(UserBatteryMemberCard.DEL_NORMAL);
                userBatteryMemberCardUpdate.setCreateTime(System.currentTimeMillis());
                userBatteryMemberCardUpdate.setTenantId(electricityMemberCardOrder.getTenantId());
                userBatteryMemberCardUpdate.setCardPayCount(payCount + 1);
                
                // 新用户直接绑定电池
                // 更新用户电池型号
                userBatteryTypeService.updateUserBatteryType(electricityMemberCardOrder, userInfo);
                
            } else {
                BatteryMemberCard userBindbatteryMemberCard = batteryMemberCardService.queryByIdFromCache(userBatteryMemberCard.getMemberCardId());
                // 若用户已购买套餐
                //     1.套餐过期，直接绑定
                //     2.套餐未过期，保存到资源包
                if (userBatteryMemberCard.getMemberCardExpireTime() < System.currentTimeMillis() || (
                        Objects.equals(userBindbatteryMemberCard.getLimitCount(), BatteryMemberCard.LIMIT) && userBatteryMemberCard.getRemainingNumber() <= 0)) {
                    
                    electricityMemberCardOrderUpdate.setUseStatus(ElectricityMemberCardOrder.USE_STATUS_USING);
                    
                    userBatteryMemberCardUpdate.setUid(userInfo.getUid());
                    userBatteryMemberCardUpdate.setMemberCardId(batteryMemberCard.getId());
                    userBatteryMemberCardUpdate.setOrderId(electricityMemberCardOrder.getOrderId());
                    userBatteryMemberCardUpdate.setMemberCardExpireTime(
                            System.currentTimeMillis() + batteryMemberCardService.transformBatteryMembercardEffectiveTime(batteryMemberCard, electricityMemberCardOrder));
                    userBatteryMemberCardUpdate.setOrderExpireTime(
                            System.currentTimeMillis() + batteryMemberCardService.transformBatteryMembercardEffectiveTime(batteryMemberCard, electricityMemberCardOrder));
                    userBatteryMemberCardUpdate.setOrderEffectiveTime(System.currentTimeMillis());
                    userBatteryMemberCardUpdate.setOrderRemainingNumber(electricityMemberCardOrder.getMaxUseCount());
                    userBatteryMemberCardUpdate.setRemainingNumber(electricityMemberCardOrder.getMaxUseCount());
                    userBatteryMemberCardUpdate.setMemberCardStatus(UserBatteryMemberCard.MEMBER_CARD_NOT_DISABLE);
                    userBatteryMemberCardUpdate.setDisableMemberCardTime(null);
                    userBatteryMemberCardUpdate.setDelFlag(UserBatteryMemberCard.DEL_NORMAL);
                    userBatteryMemberCardUpdate.setCreateTime(System.currentTimeMillis());
                    userBatteryMemberCardUpdate.setUpdateTime(System.currentTimeMillis());
                    userBatteryMemberCardUpdate.setTenantId(userInfo.getTenantId());
                    userBatteryMemberCardUpdate.setCardPayCount(electricityMemberCardOrderService.queryMaxPayCount(userBatteryMemberCard) + 1);
                    
                    // 如果用户原来绑定的有套餐 套餐过期了，需要把原来绑定的套餐订单状态更新为已过期
                    if (org.apache.commons.lang3.StringUtils.isNotBlank(userBatteryMemberCard.getOrderId())) {
                        ElectricityMemberCardOrder electricityMemberCardOrderUpdateUseStatus = new ElectricityMemberCardOrder();
                        electricityMemberCardOrderUpdateUseStatus.setOrderId(userBatteryMemberCard.getOrderId());
                        electricityMemberCardOrderUpdateUseStatus.setUseStatus(ElectricityMemberCardOrder.USE_STATUS_EXPIRE);
                        electricityMemberCardOrderUpdateUseStatus.setUpdateTime(System.currentTimeMillis());
                        electricityMemberCardOrderService.updateStatusByOrderNo(electricityMemberCardOrderUpdateUseStatus);
                    }
                    
                    // 套餐过期后重新 绑定电池信息。
                    // 更新用户电池型号
                    userBatteryTypeService.updateUserBatteryType(electricityMemberCardOrder, userInfo);
                    
                } else {
                    
                    UserBatteryMemberCardPackage userBatteryMemberCardPackage = new UserBatteryMemberCardPackage();
                    userBatteryMemberCardPackage.setUid(userInfo.getUid());
                    userBatteryMemberCardPackage.setMemberCardId(electricityMemberCardOrder.getMemberCardId());
                    userBatteryMemberCardPackage.setOrderId(electricityMemberCardOrder.getOrderId());
                    userBatteryMemberCardPackage.setRemainingNumber(batteryMemberCard.getUseCount());
                    userBatteryMemberCardPackage
                            .setMemberCardExpireTime(batteryMemberCardService.transformBatteryMembercardEffectiveTime(batteryMemberCard, electricityMemberCardOrder));
                    userBatteryMemberCardPackage.setTenantId(userInfo.getTenantId());
                    userBatteryMemberCardPackage.setCreateTime(System.currentTimeMillis());
                    userBatteryMemberCardPackage.setUpdateTime(System.currentTimeMillis());
                    userBatteryMemberCardPackageService.insert(userBatteryMemberCardPackage);
                    
                    userBatteryMemberCardUpdate.setUid(userInfo.getUid());
                    userBatteryMemberCardUpdate.setMemberCardExpireTime(userBatteryMemberCard.getMemberCardExpireTime() + batteryMemberCardService
                            .transformBatteryMembercardEffectiveTime(batteryMemberCard, electricityMemberCardOrder));
                    userBatteryMemberCardUpdate.setRemainingNumber(userBatteryMemberCard.getRemainingNumber() + electricityMemberCardOrder.getMaxUseCount());
                    userBatteryMemberCardUpdate.setCardPayCount(electricityMemberCardOrderService.queryMaxPayCount(userBatteryMemberCard) + 1);
                    userBatteryMemberCardUpdate.setUpdateTime(System.currentTimeMillis());
                }
            }
            
            if (Objects.isNull(userBatteryMemberCard)) {
                userBatteryMemberCardService.insert(userBatteryMemberCardUpdate);
            } else {
                userBatteryMemberCardService.updateByUid(userBatteryMemberCardUpdate);
            }
            
            ServiceFeeUserInfo serviceFeeUserInfo = serviceFeeUserInfoService.queryByUidFromCache(userBatteryMemberCardUpdate.getUid());
            ServiceFeeUserInfo serviceFeeUserInfoInsertOrUpdate = new ServiceFeeUserInfo();
            serviceFeeUserInfoInsertOrUpdate.setServiceFeeGenerateTime(userBatteryMemberCardUpdate.getMemberCardExpireTime());
            serviceFeeUserInfoInsertOrUpdate.setUid(userBatteryMemberCardUpdate.getUid());
            serviceFeeUserInfoInsertOrUpdate.setFranchiseeId(electricityMemberCardOrder.getFranchiseeId());
            serviceFeeUserInfoInsertOrUpdate.setUpdateTime(System.currentTimeMillis());
            serviceFeeUserInfoInsertOrUpdate.setTenantId(electricityMemberCardOrder.getTenantId());
            if (Objects.isNull(serviceFeeUserInfo)) {
                serviceFeeUserInfoInsertOrUpdate.setCreateTime(System.currentTimeMillis());
                serviceFeeUserInfoInsertOrUpdate.setDelFlag(ServiceFeeUserInfo.DEL_NORMAL);
                serviceFeeUserInfoInsertOrUpdate.setDisableMemberCardNo("");
                serviceFeeUserInfoService.insert(serviceFeeUserInfoInsertOrUpdate);
            } else {
                serviceFeeUserInfoService.updateByUid(serviceFeeUserInfoInsertOrUpdate);
            }
            
            // 修改套餐订单购买次数
            electricityMemberCardOrderUpdate.setPayCount(userBatteryMemberCardUpdate.getCardPayCount());
            
            UserInfo userInfoUpdate = new UserInfo();
            userInfoUpdate.setUid(userInfo.getUid());
            userInfoUpdate.setPayCount(userInfo.getPayCount() + 1);
            userInfoUpdate.setUpdateTime(System.currentTimeMillis());
            userInfoService.updateByUid(userInfoUpdate);
            
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
                @Override
                public void afterCommit() {
                    // 清理缓存，避免缓存操作和数据库提交在同一个事务中失效的问题。如有其他业务，请加在清理缓存之后处理
                    redisService.delete(CacheConstant.CACHE_USER_BATTERY_MEMBERCARD + userInfo.getUid());
                    redisService.delete(CacheConstant.SERVICE_FEE_USER_INFO + userInfo.getUid());
                    redisService.delete(CacheConstant.CACHE_USER_INFO + userInfo.getUid());
                    
                }
            });
            
        }
        
        electricityMemberCardOrderUpdate.setId(electricityMemberCardOrder.getId());
        electricityMemberCardOrderUpdate.setStatus(orderStatus);
        electricityMemberCardOrderUpdate.setUpdateTime(System.currentTimeMillis());
        electricityMemberCardOrderService.updateByID(electricityMemberCardOrderUpdate);
        
        // 保存骑手购买套餐信息，用于云豆回收业务
        anotherPayMembercardRecordService
                .saveAnotherPayMembercardRecord(electricityMemberCardOrder.getUid(), electricityMemberCardOrder.getOrderId(), electricityMemberCardOrder.getTenantId());
        
        // 更新云豆状态为未回收状态,同时更新代付状态为已代付
        EnterpriseChannelUser enterpriseChannelUser = enterpriseChannelUserService.selectByUid(electricityMemberCardOrder.getUid());
        
        EnterpriseChannelUserQuery enterpriseChannelUserQuery = new EnterpriseChannelUserQuery();
        enterpriseChannelUserQuery.setId(enterpriseChannelUser.getId());
        enterpriseChannelUserQuery.setUid(electricityMemberCardOrder.getUid());
        enterpriseChannelUserQuery.setCloudBeanStatus(CloudBeanStatusEnum.NOT_RECYCLE.getCode());
        enterpriseChannelUserQuery.setPaymentStatus(EnterprisePaymentStatusEnum.PAYMENT_TYPE_SUCCESS.getCode());
        enterpriseChannelUserService.updateChannelUserStatus(enterpriseChannelUserQuery);
        
        return Pair.of(true, null);
        
    }
    
    // 处理保险订单
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Pair<Boolean, Object> manageInsuranceOrder(String orderNo, Integer orderStatus) {
        
        // 保险订单
        InsuranceOrder insuranceOrder = insuranceOrderService.queryByOrderId(orderNo);
        if (ObjectUtil.isEmpty(insuranceOrder)) {
            log.warn("NOTIFY_INSURANCE_ORDER WARN ,NOT FOUND ELECTRICITY_DEPOSIT_ORDER ORDER_NO={}", orderNo);
            return Pair.of(false, "未找到订单!");
        }
        
        if (!ObjectUtil.equal(EleBatteryServiceFeeOrder.STATUS_INIT, insuranceOrder.getStatus())) {
            log.warn("NOTIFY_INSURANCE_ORDER WARN , ELECTRICITY_DEPOSIT_ORDER  STATUS IS NOT INIT, ORDER_NO={}", orderNo);
            return Pair.of(false, "押金订单已处理!");
        }
        
        FranchiseeInsurance franchiseeInsurance = franchiseeInsuranceService.queryByIdFromCache(insuranceOrder.getInsuranceId());
        if (ObjectUtil.isEmpty(insuranceOrder)) {
            log.warn("NOTIFY_INSURANCE_ORDER WARN ,NOT FOUND ELECTRICITY_DEPOSIT_ORDER ORDER_NO={}", orderNo);
            return Pair.of(false, "未找到订单!");
        }
        
        if (Objects.equals(orderStatus, EleDepositOrder.STATUS_SUCCESS)) {
            InsuranceUserInfo updateOrAddInsuranceUserInfo = new InsuranceUserInfo();
            updateOrAddInsuranceUserInfo.setUid(insuranceOrder.getUid());
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
            updateOrAddInsuranceUserInfo.setType(insuranceOrder.getInsuranceType());
            
            InsuranceUserInfo insuranceUserInfo = insuranceUserInfoService.selectByUidAndTypeFromCache(insuranceOrder.getUid(), insuranceOrder.getInsuranceType());
            if (Objects.isNull(insuranceUserInfo)) {
                insuranceUserInfoService.insert(updateOrAddInsuranceUserInfo);
            } else {
                // 更新旧保险订单状态
                InsuranceOrder oldInsuranceUserOrder = insuranceOrderService.queryByOrderId(insuranceUserInfo.getInsuranceOrderId());
                if (Objects.nonNull(oldInsuranceUserOrder)) {
                    InsuranceOrder insuranceUserOrderUpdate = new InsuranceOrder();
                    insuranceUserOrderUpdate.setId(oldInsuranceUserOrder.getId());
                    insuranceUserOrderUpdate.setIsUse(Objects.equals(oldInsuranceUserOrder.getIsUse(), InsuranceOrder.IS_USE) ? InsuranceOrder.IS_USE : InsuranceOrder.INVALID);
                    insuranceUserOrderUpdate.setUpdateTime(System.currentTimeMillis());
                    insuranceOrderService.update(insuranceUserOrderUpdate);
                }
                
                insuranceUserInfoService.update(updateOrAddInsuranceUserInfo);
            }
            
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
                @Override
                public void afterCommit() {
                    // 清理缓存，避免缓存操作和数据库提交在同一个事务中失效的问题。如有其他业务，请加在清理缓存之后处理
                    redisService.delete(CacheConstant.CACHE_INSURANCE_USER_INFO + updateOrAddInsuranceUserInfo.getUid());
                    redisService.delete(CacheConstant.CACHE_INSURANCE_USER_INFO + updateOrAddInsuranceUserInfo.getUid() + ":" + updateOrAddInsuranceUserInfo.getType());
                    
                }
            });
            
        }
        
        // 保险订单
        InsuranceOrder updateInsuranceOrder = new InsuranceOrder();
        updateInsuranceOrder.setId(insuranceOrder.getId());
        updateInsuranceOrder.setUpdateTime(System.currentTimeMillis());
        updateInsuranceOrder.setStatus(orderStatus);
        insuranceOrderService.updateOrderStatusById(updateInsuranceOrder);
        
        return Pair.of(true, null);
    }
    
    /**
     * 滞纳金混合支付回调 抄的上面的支付回调  @See notifyIntegratedPayment
     *
     * @param callBackResource
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Pair<Boolean, Object> notifyServiceFee(BaseOrderCallBackResource callBackResource) {
        // 回调参数
        String tradeOrderNo = callBackResource.getOutTradeNo();
        String transactionId = callBackResource.getTransactionId();
        
        UnionTradeOrder unionTradeOrder = baseMapper.selectTradeOrderByTradeOrderNo(tradeOrderNo);
        if (Objects.isNull(unionTradeOrder)) {
            log.warn("NOTIFY SERVICE FEE UNION ORDER WARN!NOT FOUND ELECTRICITY_TRADE_ORDER TRADE_ORDER_NO={}", tradeOrderNo);
            return Pair.of(false, "未找到交易订单!");
        }
        if (ObjectUtil.notEqual(UnionTradeOrder.STATUS_INIT, unionTradeOrder.getStatus())) {
            log.warn("NOTIFY SERVICE FEE UNION ORDER WARN! ELECTRICITY_TRADE_ORDER  STATUS IS NOT INIT, TRADE_ORDER_NO={}", tradeOrderNo);
            return Pair.of(false, "交易订单已处理");
        }
        
        UserInfo userInfo = userInfoService.queryByUidFromCache(unionTradeOrder.getUid());
        if (Objects.isNull(userInfo)) {
            log.warn("NOTIFY SERVICE FEE UNION ORDER WARN! not found userInfo, TRADE_ORDER_NO={}", tradeOrderNo);
            return Pair.of(false, "未找到用户信息");
        }
        
        List<ElectricityTradeOrder> electricityTradeOrderList = electricityTradeOrderService.selectTradeOrderByParentOrderId(unionTradeOrder.getId());
        if (Objects.isNull(electricityTradeOrderList)) {
            log.warn("NOTIFY SERVICE FEE UNION ORDER WARN!NOT FOUND ELECTRICITY_TRADE_ORDER TRADE_ORDER_NO={}", tradeOrderNo);
            return Pair.of(false, "未找到交易订单!");
        }
        
        String jsonOrderType = unionTradeOrder.getJsonOrderType();
        List<Integer> orderTypeList = JsonUtil.fromJsonArray(jsonOrderType, Integer.class);
        
        String jsonOrderId = unionTradeOrder.getJsonOrderId();
        List<String> orderIdList = JsonUtil.fromJsonArray(jsonOrderId, String.class);
        
        List<String> jsonFreeList = JsonUtil.fromJsonArray(unionTradeOrder.getJsonSingleFee(), String.class);
        
        if (CollectionUtils.isEmpty(orderIdList)) {
            log.warn("NOTIFY SERVICE FEE UNION ORDER WARN!NOT FOUND ELECTRICITY_TRADE_ORDER TRADE_ORDER_NO={}", tradeOrderNo);
            return Pair.of(false, "未找到交易订单");
        }
        
        Integer tradeOrderStatus = ElectricityTradeOrder.STATUS_FAIL;
        if (callBackResource.tradeStateIsSuccess()) {
            tradeOrderStatus = ElectricityTradeOrder.STATUS_SUCCESS;
        } else {
            log.warn("NOTIFY SERVICE FEE UNION ORDER FAIL,ORDER_NO is {}", tradeOrderNo);
        }
        
        for (int i = 0; i < orderTypeList.size(); i++) {
            if (Objects.equals(orderTypeList.get(i), ServiceFeeEnum.BATTERY_PAUSE.getCode())) {
                handleBatteryMembercardPauseServiceFeeOrder(orderIdList.get(i), tradeOrderStatus, userInfo);
            } else if (Objects.equals(orderTypeList.get(i), ServiceFeeEnum.BATTERY_EXPIRE.getCode())) {
                handleBatteryMembercardExpireServiceFeeOrder(orderIdList.get(i), tradeOrderStatus, userInfo);
            } else if (Objects.equals(orderTypeList.get(i), ServiceFeeEnum.CAR_SLIPPAGE.getCode())) {
                // 车辆滞纳金
                handCarSupplierSuccess(orderIdList.get(i), jsonFreeList.get(i), tradeOrderStatus, userInfo, unionTradeOrder.getParamFranchiseeId(),
                        unionTradeOrder.getWechatMerchantId());
            }
        }
        
        // 系统订单
        UnionTradeOrder unionTradeOrderUpdate = new UnionTradeOrder();
        unionTradeOrderUpdate.setId(unionTradeOrder.getId());
        unionTradeOrderUpdate.setStatus(tradeOrderStatus);
        unionTradeOrderUpdate.setUpdateTime(System.currentTimeMillis());
        unionTradeOrderUpdate.setChannelOrderNo(transactionId);
        baseMapper.updateById(unionTradeOrderUpdate);
        
        // 混合支付的子订单
        Integer finalTradeOrderStatus = tradeOrderStatus;
        electricityTradeOrderList.parallelStream().forEach(item -> {
            ElectricityTradeOrder electricityTradeOrder = new ElectricityTradeOrder();
            electricityTradeOrder.setId(item.getId());
            electricityTradeOrder.setStatus(finalTradeOrderStatus);
            electricityTradeOrder.setUpdateTime(System.currentTimeMillis());
            electricityTradeOrder.setChannelOrderNo(transactionId);
            electricityTradeOrderService.updateElectricityTradeOrderById(electricityTradeOrder);
        });
        
        // 小程序虚拟发货
        if (ChannelEnum.WECHAT.getCode().equals(callBackResource.getChannel())) {
            shippingManagerService.uploadShippingInfo(unionTradeOrder.getUid(), userInfo.getPhone(), transactionId, userInfo.getTenantId());
        }
        
        return Pair.of(true, null);
    }
    
    /**
     * @param orderNo           逾期订单号
     * @param freeAmount        缴纳金额
     * @param tradeOrderStatus  支付状态
     * @param userInfo          用户信息
     * @param paramFranchiseeId 支付加盟商ID
     * @param wechatMerchantId  微信商户号
     */
    @Transactional(rollbackFor = Exception.class)
    public void handCarSupplierSuccess(String orderNo, String freeAmount, Integer tradeOrderStatus, UserInfo userInfo, Long paramFranchiseeId, String wechatMerchantId) {
        // 提前发布逾期用户备注清除事件
        overdueUserRemarkPublish.publish(userInfo.getUid(), OverdueType.CAR.getCode(), userInfo.getTenantId());
        Integer tenantId = userInfo.getTenantId();
        Long uid = userInfo.getUid();
        log.info("handCarSupplierSuccess, orderNo is {}, freeAmount is {}, tradeOrderStatus is {}, uid is {}", orderNo, freeAmount, tradeOrderStatus, uid);
        long now = System.currentTimeMillis();
        CarRentalPackageOrderSlippagePo slippageEntity = carRentalPackageOrderSlippageService.selectByOrderNo(orderNo);
        if (ObjectUtils.isEmpty(slippageEntity)) {
            log.warn("handCarSupplierSuccess, not found t_car_rental_package_order_slippage. orderNo is {}", orderNo);
            return;
        }
        
        if (PayStateEnum.SUCCESS.getCode().equals(slippageEntity.getPayState())) {
            log.warn("handCarSupplierSuccess, t_car_rental_package_order_slippage processed. orderNo is {}", orderNo);
            return;
        }
        
        // 支付成功
        if (ElectricityTradeOrder.STATUS_SUCCESS.equals(tradeOrderStatus)) {
            CarRentalPackageMemberTermPo memberTermEntity = carRentalPackageMemberTermService.selectByTenantIdAndUid(tenantId, uid);
            // 更新数据源
            CarRentalPackageOrderSlippagePo slippageUpdateEntity = new CarRentalPackageOrderSlippagePo();
            slippageUpdateEntity.setId(slippageEntity.getId());
            slippageUpdateEntity.setUpdateTime(now);
            slippageUpdateEntity.setPayState(PayStateEnum.SUCCESS.getCode());
            slippageUpdateEntity.setPayTime(now);
            slippageUpdateEntity.setPayFranchiseeId(paramFranchiseeId);
            slippageUpdateEntity.setWechatMerchantId(wechatMerchantId);
            
            Integer type = slippageEntity.getType();
            // 冻结
            if (SlippageTypeEnum.FREEZE.getCode().equals(type)) {
                if (ObjectUtils.isEmpty(slippageEntity.getLateFeeEndTime())) {
                    slippageUpdateEntity.setLateFeeEndTime(now);
                    slippageUpdateEntity.setLateFeePay(new BigDecimal(freeAmount));
                }
                
                CarRentalPackageOrderFreezePo freezeEntity = carRentalPackageOrderFreezeService.selectLastFreeByUid(uid);
                
                // 更改订单冻结表数据
                carRentalPackageOrderFreezeService.enableFreezeRentOrderByUidAndPackageOrderNo(slippageEntity.getRentalPackageOrderNo(), slippageEntity.getUid(), false, null);
                
                // 赋值会员更新
                CarRentalPackageMemberTermPo memberTermUpdateEntity = new CarRentalPackageMemberTermPo();
                memberTermUpdateEntity.setStatus(MemberTermStatusEnum.NORMAL.getCode());
                memberTermUpdateEntity.setId(memberTermEntity.getId());
                memberTermUpdateEntity.setUpdateTime(now);
                // 提前启用、计算差额
                long diffTime = (freezeEntity.getApplyTerm() * TimeConstant.DAY_MILLISECOND) - (now - freezeEntity.getApplyTime());
                memberTermUpdateEntity.setDueTime(memberTermEntity.getDueTime() - diffTime);
                memberTermUpdateEntity.setDueTimeTotal(memberTermEntity.getDueTimeTotal() - diffTime);
                
                carRentalPackageMemberTermService.updateById(memberTermUpdateEntity);
                
            }
            
            // 过期
            if (SlippageTypeEnum.EXPIRE.getCode().equals(type)) {
                slippageUpdateEntity.setLateFeeEndTime(now);
                slippageUpdateEntity.setLateFeePay(new BigDecimal(freeAmount));
                
                // 更改会员期限表数据
                CarRentalPackageMemberTermPo memberTermEntityUpdate = new CarRentalPackageMemberTermPo();
                memberTermEntityUpdate.setDueTime(now);
                memberTermEntityUpdate.setDueTimeTotal(now);
                memberTermEntityUpdate.setId(memberTermEntity.getId());
                carRentalPackageMemberTermService.updateById(memberTermEntityUpdate);
            }
            
            // 更新逾期订单
            carRentalPackageOrderSlippageService.updateById(slippageUpdateEntity);
            // 查询车辆
            ElectricityCar electricityCar = electricityCarService.selectByUid(tenantId, uid);
            if (ObjectUtils.isNotEmpty(electricityCar)) {
                // JT808解锁
                CarLockCtrlHistory carLockCtrlHistory = buildCarLockCtrlHistory(electricityCar, userInfo);
                // 生成日志
                if (ObjectUtils.isNotEmpty(carLockCtrlHistory)) {
                    carLockCtrlHistoryService.insert(carLockCtrlHistory);
                }
            }
        }
        
    }
    
    
    /**
     * 构建JT808
     *
     * @param electricityCar
     * @param userInfo
     * @return
     */
    private CarLockCtrlHistory buildCarLockCtrlHistory(ElectricityCar electricityCar, UserInfo userInfo) {
        ElectricityConfig electricityConfig = electricityConfigService.queryFromCacheByTenantId(TenantContextHolder.getTenantId());
        if (Objects.nonNull(electricityConfig) && Objects.equals(electricityConfig.getIsOpenCarControl(), ElectricityConfig.ENABLE_CAR_CONTROL)) {
            
            boolean result = carRentalOrderBizService.retryCarLockCtrl(electricityCar.getSn(), ElectricityCar.TYPE_UN_LOCK, 3);
            
            CarLockCtrlHistory carLockCtrlHistory = new CarLockCtrlHistory();
            carLockCtrlHistory.setUid(userInfo.getUid());
            carLockCtrlHistory.setName(userInfo.getName());
            carLockCtrlHistory.setPhone(userInfo.getPhone());
            carLockCtrlHistory.setStatus(result ? CarLockCtrlHistory.STATUS_LOCK_SUCCESS : CarLockCtrlHistory.STATUS_LOCK_FAIL);
            carLockCtrlHistory.setCarModelId(electricityCar.getModelId().longValue());
            carLockCtrlHistory.setCarModel(electricityCar.getModel());
            carLockCtrlHistory.setCarId(electricityCar.getId().longValue());
            carLockCtrlHistory.setCarSn(electricityCar.getSn());
            carLockCtrlHistory.setCreateTime(System.currentTimeMillis());
            carLockCtrlHistory.setUpdateTime(System.currentTimeMillis());
            carLockCtrlHistory.setTenantId(TenantContextHolder.getTenantId());
            carLockCtrlHistory.setType(CarLockCtrlHistory.TYPE_SLIPPAGE_UN_LOCK);
            
            return carLockCtrlHistory;
        }
        return null;
    }
    
    private void handleBatteryMembercardPauseServiceFeeOrder(String orderId, Integer status, UserInfo userInfo) {
        // 提前发布逾期用户备注清除事件
        if (Objects.equals(userInfo.getCarBatteryDepositStatus(), YesNoEnum.YES.getCode())) {
            overdueUserRemarkPublish.publish(userInfo.getUid(), OverdueType.CAR.getCode(), userInfo.getTenantId());
        } else {
            overdueUserRemarkPublish.publish(userInfo.getUid(), OverdueType.BATTERY.getCode(), userInfo.getTenantId());
        }
        EleBatteryServiceFeeOrder eleBatteryServiceFeeOrder = eleBatteryServiceFeeOrderService.selectByOrderNo(orderId);
        if (Objects.isNull(eleBatteryServiceFeeOrder)) {
            log.warn("NOTIFY SERVICE FEE UNION ORDER WARN!not found eleBatteryServiceFeeOrder,orderId={}", orderId);
            return;
        }
        
        if (Objects.equals(eleBatteryServiceFeeOrder.getStatus(), EleBatteryServiceFeeOrder.STATUS_SUCCESS)) {
            log.warn("NOTIFY SERVICE FEE UNION ORDER WARN!order status illegal,orderId={}", orderId);
            return;
        }
        
        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(eleBatteryServiceFeeOrder.getUid());
        if (Objects.isNull(userBatteryMemberCard)) {
            log.warn("NOTIFY SERVICE FEE UNION ORDER WARN!not found userBatteryMemberCard,uid={},orderId={}", eleBatteryServiceFeeOrder.getUid(), orderId);
            return;
        }
        
        ServiceFeeUserInfo serviceFeeUserInfo = serviceFeeUserInfoService.queryByUidFromCache(eleBatteryServiceFeeOrder.getUid());
        if (Objects.isNull(serviceFeeUserInfo)) {
            log.warn("NOTIFY SERVICE FEE UNION ORDER WARN!not found serviceFeeUserInfo,uid={},orderId={}", eleBatteryServiceFeeOrder.getUid(), orderId);
            return;
        }
        
        EleDisableMemberCardRecord eleDisableMemberCardRecord = eleDisableMemberCardRecordService.selectByDisableMemberCardNo(serviceFeeUserInfo.getDisableMemberCardNo());
        if (Objects.isNull(eleDisableMemberCardRecord)) {
            log.warn("NOTIFY SERVICE FEE UNION ORDER WARN!not found eleDisableMemberCardRecord,uid={},orderId={}", eleBatteryServiceFeeOrder.getUid(),
                    serviceFeeUserInfo.getDisableMemberCardNo());
            return;
        }
        
        if (Objects.equals(EleBatteryServiceFeeOrder.STATUS_SUCCESS, status)) {
            Long memberCardExpireTime;
            Long orderExpireTime;
            Long cardDays = 0L;
            
            // 用户套餐是否启用，若已启用  停卡时间取停卡记录中的停卡时间；未启用 取userBatteryMemberCard中的停卡时间。因为系统启用时会清除用户的停卡时间
            if (Objects.equals(userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_DISABLE)) {
                // 兼容2.0冻结不限制天数 冻结天数为空的场景
                if (Objects.isNull(eleDisableMemberCardRecord.getChooseDays())) {
                    memberCardExpireTime = System.currentTimeMillis() + (userBatteryMemberCard.getMemberCardExpireTime() - userBatteryMemberCard.getDisableMemberCardTime());
                    // orderExpireTime = System.currentTimeMillis() + (userBatteryMemberCard.getOrderExpireTime() - userBatteryMemberCard.getDisableMemberCardTime());
                    orderExpireTime = System.currentTimeMillis() + (userBatteryMemberCard.getOrderExpireTime() - userBatteryMemberCard.getDisableMemberCardTime());
                } else {
                    // 申请冻结的天数
                    Long chooseTime = eleDisableMemberCardRecord.getChooseDays() * TimeConstant.DAY_MILLISECOND;
                    // 实际的冻结时间
                    Long realTime = System.currentTimeMillis() - userBatteryMemberCard.getDisableMemberCardTime();
                    // memberCardExpireTime = System.currentTimeMillis() + (userBatteryMemberCard.getMemberCardExpireTime() - userBatteryMemberCard.getDisableMemberCardTime());
                    memberCardExpireTime = userBatteryMemberCard.getMemberCardExpireTime() - (chooseTime - realTime);
                    // orderExpireTime = System.currentTimeMillis() + (userBatteryMemberCard.getOrderExpireTime() - userBatteryMemberCard.getDisableMemberCardTime());
                    orderExpireTime = userBatteryMemberCard.getOrderExpireTime() - (chooseTime - realTime);
                }
                
                cardDays = (System.currentTimeMillis() - userBatteryMemberCard.getDisableMemberCardTime()) / 1000L / 60 / 60 / 24;
                
                // 处理企业用户对应的支付记录时间
                anotherPayMembercardRecordService.enableMemberCardHandler(userBatteryMemberCard.getUid());
                
                // 更新用户套餐到期时间，启用用户套餐
                UserBatteryMemberCard userBatteryMemberCardUpdate = new UserBatteryMemberCard();
                userBatteryMemberCardUpdate.setUid(userBatteryMemberCard.getUid());
                userBatteryMemberCardUpdate.setMemberCardStatus(UserBatteryMemberCard.MEMBER_CARD_NOT_DISABLE);
                userBatteryMemberCardUpdate.setDisableMemberCardTime(null);
                userBatteryMemberCardUpdate.setMemberCardExpireTime(memberCardExpireTime);
                userBatteryMemberCardUpdate.setOrderExpireTime(orderExpireTime);
                userBatteryMemberCardUpdate.setUpdateTime(System.currentTimeMillis());
                userBatteryMemberCardService.updateByUidForDisableCard(userBatteryMemberCardUpdate);
                
                // 解绑停卡单号，更新电池服务费产生时间,解绑停卡电池服务费订单号
                ServiceFeeUserInfo serviceFeeUserInfoUpdate = new ServiceFeeUserInfo();
                serviceFeeUserInfoUpdate.setUid(userBatteryMemberCard.getUid());
                serviceFeeUserInfoUpdate.setPauseOrderNo("");
                serviceFeeUserInfoUpdate.setDisableMemberCardNo("");
                serviceFeeUserInfoUpdate.setServiceFeeGenerateTime(userBatteryMemberCardUpdate.getMemberCardExpireTime());
                serviceFeeUserInfoUpdate.setUpdateTime(System.currentTimeMillis());
                serviceFeeUserInfoService.updateByUid(serviceFeeUserInfoUpdate);
            } else {
                //                memberCardExpireTime = System.currentTimeMillis() + (userBatteryMemberCard.getMemberCardExpireTime() - eleDisableMemberCardRecord.getDisableMemberCardTime());
                //                orderExpireTime = System.currentTimeMillis() + (userBatteryMemberCard.getOrderExpireTime() - eleDisableMemberCardRecord.getDisableMemberCardTime());
                //
                //                //更新用户套餐到期时间，启用用户套餐
                //                UserBatteryMemberCard userBatteryMemberCardUpdate = new UserBatteryMemberCard();
                //                userBatteryMemberCardUpdate.setUid(userBatteryMemberCard.getUid());
                //                userBatteryMemberCardUpdate.setMemberCardStatus(UserBatteryMemberCard.MEMBER_CARD_NOT_DISABLE);
                //                userBatteryMemberCardUpdate.setDisableMemberCardTime(null);
                //                userBatteryMemberCardUpdate.setMemberCardExpireTime(memberCardExpireTime);
                //                userBatteryMemberCardUpdate.setOrderExpireTime(orderExpireTime);
                //                userBatteryMemberCardUpdate.setUpdateTime(System.currentTimeMillis());
                //                userBatteryMemberCardService.updateByUidForDisableCard(userBatteryMemberCardUpdate);
                
                // 解绑停卡单号，更新电池服务费产生时间,解绑停卡电池服务费订单号
                ServiceFeeUserInfo serviceFeeUserInfoUpdate = new ServiceFeeUserInfo();
                serviceFeeUserInfoUpdate.setUid(userBatteryMemberCard.getUid());
                serviceFeeUserInfoUpdate.setPauseOrderNo("");
                serviceFeeUserInfoUpdate.setDisableMemberCardNo("");
                //                serviceFeeUserInfoUpdate.setServiceFeeGenerateTime(userBatteryMemberCardUpdate.getMemberCardExpireTime());
                serviceFeeUserInfoUpdate.setUpdateTime(System.currentTimeMillis());
                serviceFeeUserInfoService.updateByUid(serviceFeeUserInfoUpdate);
            }
            
            // 生成启用记录
            EnableMemberCardRecord enableMemberCardRecord = enableMemberCardRecordService
                    .queryByDisableCardNO(eleDisableMemberCardRecord.getDisableMemberCardNo(), userInfo.getTenantId());
            if (Objects.isNull(enableMemberCardRecord)) {
                EnableMemberCardRecord enableMemberCardRecordInsert = EnableMemberCardRecord.builder().disableMemberCardNo(eleDisableMemberCardRecord.getDisableMemberCardNo())
                        .memberCardName(eleDisableMemberCardRecord.getMemberCardName()).enableTime(System.currentTimeMillis()).enableType(EnableMemberCardRecord.ARTIFICIAL_ENABLE)
                        .batteryServiceFeeStatus(EnableMemberCardRecord.STATUS_SUCCESS).disableDays(cardDays.intValue()).disableTime(eleDisableMemberCardRecord.getCreateTime())
                        .franchiseeId(userInfo.getFranchiseeId()).storeId(userInfo.getStoreId()).phone(userInfo.getPhone())
                        .serviceFee(eleBatteryServiceFeeOrder.getBatteryServiceFee()).createTime(System.currentTimeMillis()).tenantId(userInfo.getTenantId()).uid(userInfo.getUid())
                        .userName(userInfo.getName()).orderId(userBatteryMemberCard.getOrderId()).updateTime(System.currentTimeMillis()).build();
                enableMemberCardRecordService.insert(enableMemberCardRecordInsert);
            } else {
                EnableMemberCardRecord enableMemberCardRecordUpdate = new EnableMemberCardRecord();
                enableMemberCardRecordUpdate.setId(enableMemberCardRecord.getId());
                enableMemberCardRecordUpdate.setBatteryServiceFeeStatus(EnableMemberCardRecord.STATUS_SUCCESS);
                enableMemberCardRecordUpdate.setUpdateTime(System.currentTimeMillis());
                enableMemberCardRecordService.update(enableMemberCardRecordUpdate);
            }
        }
        
        // 更新滞纳金订单
        EleBatteryServiceFeeOrder eleBatteryServiceFeeOrderUpdate = new EleBatteryServiceFeeOrder();
        eleBatteryServiceFeeOrderUpdate.setId(eleBatteryServiceFeeOrder.getId());
        eleBatteryServiceFeeOrderUpdate.setStatus(status);
        eleBatteryServiceFeeOrderUpdate.setBatteryServiceFeeEndTime(System.currentTimeMillis());
        eleBatteryServiceFeeOrderUpdate.setUpdateTime(System.currentTimeMillis());
        eleBatteryServiceFeeOrderUpdate.setPayTime(System.currentTimeMillis());
        eleBatteryServiceFeeOrderService.update(eleBatteryServiceFeeOrderUpdate);
    }
    
    private void handleBatteryMembercardExpireServiceFeeOrder(String orderId, Integer status, UserInfo userInfo) {
        // 提前发布逾期用户备注清除事件
        if (Objects.equals(userInfo.getCarBatteryDepositStatus(), YesNoEnum.YES.getCode())) {
            overdueUserRemarkPublish.publish(userInfo.getUid(), OverdueType.CAR.getCode(), userInfo.getTenantId());
        } else {
            overdueUserRemarkPublish.publish(userInfo.getUid(), OverdueType.BATTERY.getCode(), userInfo.getTenantId());
        }
        EleBatteryServiceFeeOrder eleBatteryServiceFeeOrder = eleBatteryServiceFeeOrderService.selectByOrderNo(orderId);
        if (Objects.isNull(eleBatteryServiceFeeOrder)) {
            log.warn("NOTIFY SERVICE FEE UNION ORDER WARN!not found eleBatteryServiceFeeOrder,orderId={}", orderId);
            return;
        }
        
        if (Objects.equals(eleBatteryServiceFeeOrder.getStatus(), EleBatteryServiceFeeOrder.STATUS_SUCCESS)) {
            log.warn("NOTIFY SERVICE FEE UNION ORDER WARN!order status illegal,orderId={}", orderId);
            return;
        }
        
        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(eleBatteryServiceFeeOrder.getUid());
        if (Objects.isNull(userBatteryMemberCard)) {
            log.warn("NOTIFY SERVICE FEE UNION ORDER WARN!not found userBatteryMemberCard,uid={},orderId={}", eleBatteryServiceFeeOrder.getUid(), orderId);
            return;
        }
        
        ServiceFeeUserInfo serviceFeeUserInfo = serviceFeeUserInfoService.queryByUidFromCache(eleBatteryServiceFeeOrder.getUid());
        if (Objects.isNull(serviceFeeUserInfo)) {
            log.warn("NOTIFY SERVICE FEE UNION ORDER WARN!not found serviceFeeUserInfo,uid={},orderId={}", eleBatteryServiceFeeOrder.getUid(), orderId);
            return;
        }
        
        if (Objects.equals(EleBatteryServiceFeeOrder.STATUS_SUCCESS, status)) {
            
            // 更新用户套餐过期时间
            UserBatteryMemberCard userBatteryMemberCardUpdate = new UserBatteryMemberCard();
            userBatteryMemberCardUpdate.setUid(userBatteryMemberCard.getUid());
            userBatteryMemberCardUpdate.setMemberCardExpireTime(System.currentTimeMillis());
            userBatteryMemberCardUpdate.setOrderExpireTime(System.currentTimeMillis());
            userBatteryMemberCardUpdate.setUpdateTime(System.currentTimeMillis());
            userBatteryMemberCardService.updateByUidForDisableCard(userBatteryMemberCardUpdate);
            
            // 更新电池服务费产生时间,解绑套餐过期电池服务费订单号
            ServiceFeeUserInfo serviceFeeUserInfoUpdate = new ServiceFeeUserInfo();
            serviceFeeUserInfoUpdate.setUid(userBatteryMemberCard.getUid());
            serviceFeeUserInfoUpdate.setExpireOrderNo("");
            serviceFeeUserInfoUpdate.setServiceFeeGenerateTime(userBatteryMemberCardUpdate.getMemberCardExpireTime());
            serviceFeeUserInfoUpdate.setUpdateTime(System.currentTimeMillis());
            serviceFeeUserInfoService.updateByUid(serviceFeeUserInfoUpdate);
        }
        
        // 更新滞纳金订单
        EleBatteryServiceFeeOrder eleBatteryServiceFeeOrderUpdate = new EleBatteryServiceFeeOrder();
        eleBatteryServiceFeeOrderUpdate.setId(eleBatteryServiceFeeOrder.getId());
        eleBatteryServiceFeeOrderUpdate.setStatus(status);
        eleBatteryServiceFeeOrderUpdate.setBatteryServiceFeeEndTime(System.currentTimeMillis());
        eleBatteryServiceFeeOrderUpdate.setUpdateTime(System.currentTimeMillis());
        eleBatteryServiceFeeOrderUpdate.setPayTime(System.currentTimeMillis());
        eleBatteryServiceFeeOrderService.update(eleBatteryServiceFeeOrderUpdate);
    }
    
    @Override
    public UnionTradeOrder selectTradeOrderByOrderId(String orderId) {
        return baseMapper.selectTradeOrderByOrderId(orderId);
    }
    
    @Override
    public UnionTradeOrder selectTradeOrderById(Long id) {
        return baseMapper.selectById(id);
    }
    
}
