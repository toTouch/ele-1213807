package com.xiliulou.electricity.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.config.WechatConfig;
import com.xiliulou.electricity.constant.WechatPayConstant;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.mapper.UnionTradeOrderMapper;
import com.xiliulou.electricity.service.*;
import com.xiliulou.pay.weixinv3.dto.WechatJsapiOrderCallBackResource;
import com.xiliulou.pay.weixinv3.dto.WechatJsapiOrderResultDTO;
import com.xiliulou.pay.weixinv3.exception.WechatPayException;
import com.xiliulou.pay.weixinv3.query.WechatV3OrderQuery;
import com.xiliulou.pay.weixinv3.service.WechatV3JsapiService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
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

/**
 * @program: XILIULOU
 * @description:
 * @author: Mr.HRP
 * @create: 2022-11-07 11:34
 **/
@Service
@Slf4j
public class UnionTradeOrderServiceImpl extends
        ServiceImpl<UnionTradeOrderMapper, UnionTradeOrder> implements UnionTradeOrderService {

    @Resource
    UnionTradeOrderMapper unionTradeOrderMapper;

    @Autowired
    WechatConfig wechatConfig;

    @Autowired
    WechatV3JsapiService wechatV3JsapiService;

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
    CarDepositOrderService carDepositOrderService;
    @Autowired
    UserCarDepositService userCarDepositService;
    @Autowired
    CarMemberCardOrderService carMemberCardOrderService;
    @Autowired
    UserCarMemberCardService userCarMemberCardService;
    @Autowired
    UserCarService userCarService;
    @Autowired
    RedisService redisService;

    @Override
    public WechatJsapiOrderResultDTO unionCreateTradeOrderAndGetPayParams(UnionPayOrder unionPayOrder, ElectricityPayParams electricityPayParams, String openId, HttpServletRequest request) throws WechatPayException {

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
            electricityTradeOrderService.insert(electricityTradeOrder);
        }

        //支付参数
        WechatV3OrderQuery wechatV3OrderQuery = new WechatV3OrderQuery();
        wechatV3OrderQuery.setOrderId(unionTradeOrder.getTradeOrderNo());
        wechatV3OrderQuery.setTenantId(unionTradeOrder.getTenantId());
        wechatV3OrderQuery.setNotifyUrl(wechatConfig.getPayCallBackUrl() + unionTradeOrder.getTenantId());
        wechatV3OrderQuery.setExpireTime(System.currentTimeMillis() + 3600000);
        wechatV3OrderQuery.setOpenId(openId);
        wechatV3OrderQuery.setDescription(unionPayOrder.getDescription());
        wechatV3OrderQuery.setCurrency("CNY");
        wechatV3OrderQuery.setAttach(unionPayOrder.getAttach());
        wechatV3OrderQuery.setAmount(unionPayOrder.getPayAmount().multiply(new BigDecimal(100)).intValue());
        wechatV3OrderQuery.setAppid(electricityPayParams.getMerchantMinProAppId());
        log.info("wechatV3OrderQuery is -->{}", wechatV3OrderQuery);
        return wechatV3JsapiService.order(wechatV3OrderQuery);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Pair<Boolean, Object> notifyUnionDepositAndInsurance(WechatJsapiOrderCallBackResource callBackResource) {

        //回调参数
        String tradeOrderNo = callBackResource.getOutTradeNo();
        String tradeState = callBackResource.getTradeState();
        String transactionId = callBackResource.getTransactionId();

        UnionTradeOrder unionTradeOrder = baseMapper.selectTradeOrderByTradeOrderNo(tradeOrderNo);
        if (Objects.isNull(unionTradeOrder)) {
            log.error("NOTIFY_INSURANCE_UNION_DEPOSIT_ORDER ERROR ,NOT FOUND ELECTRICITY_TRADE_ORDER TRADE_ORDER_NO:{}", tradeOrderNo);
            return Pair.of(false, "未找到交易订单!");
        }
        if (ObjectUtil.notEqual(UnionTradeOrder.STATUS_INIT, unionTradeOrder.getStatus())) {
            log.error("NOTIFY_INSURANCE_UNION_DEPOSIT_ORDER ERROR , ELECTRICITY_TRADE_ORDER  STATUS IS NOT INIT, TRADE_ORDER_NO:{}", tradeOrderNo);
            return Pair.of(false, "交易订单已处理");
        }

        List<ElectricityTradeOrder> electricityTradeOrderList = electricityTradeOrderService.selectTradeOrderByParentOrderId(unionTradeOrder.getId());
        if (Objects.isNull(electricityTradeOrderList)) {
            log.error("NOTIFY_INSURANCE_UNION_DEPOSIT_ORDER ERROR ,NOT FOUND ELECTRICITY_TRADE_ORDER TRADE_ORDER_NO:{}", tradeOrderNo);
            return Pair.of(false, "未找到交易订单!");
        }

        //处理保险订单
        String jsonOrderId = unionTradeOrder.getJsonOrderId();
        List<String> orderIdLIst = JsonUtil.fromJsonArray(jsonOrderId, String.class);
        if (CollectionUtils.isEmpty(orderIdLIst)) {
            log.error("NOTIFY_INSURANCE_UNION_DEPOSIT_ORDER ERROR ,NOT FOUND ELECTRICITY_TRADE_ORDER TRADE_ORDER_NO:{}", tradeOrderNo);
            return Pair.of(false, "未找到交易订单");
        }

        //押金订单
        EleDepositOrder eleDepositOrder = eleDepositOrderService.queryByOrderId(orderIdLIst.get(0));
        if (ObjectUtil.isEmpty(eleDepositOrder)) {
            log.error("NOTIFY_DEPOSIT_ORDER ERROR ,NOT FOUND ELECTRICITY_DEPOSIT_ORDER ORDER_NO:{}", orderIdLIst.get(0));
            return Pair.of(false, "未找到订单!");
        }

        if (!ObjectUtil.equal(EleDepositOrder.STATUS_INIT, eleDepositOrder.getStatus())) {
            log.error("NOTIFY_DEPOSIT_ORDER ERROR , ELECTRICITY_DEPOSIT_ORDER  STATUS IS NOT INIT, ORDER_NO:{}", orderIdLIst.get(0));
            return Pair.of(false, "押金订单已处理!");
        }

        //保险订单
        InsuranceOrder insuranceOrder = insuranceOrderService.queryByOrderId(orderIdLIst.get(1));
        if (ObjectUtil.isEmpty(insuranceOrder)) {
            log.error("NOTIFY_INSURANCE_ORDER ERROR ,NOT FOUND ELECTRICITY_DEPOSIT_ORDER ORDER_NO:{}", orderIdLIst.get(1));
            return Pair.of(false, "未找到订单!");
        }

        if (!ObjectUtil.equal(EleBatteryServiceFeeOrder.STATUS_INIT, insuranceOrder.getStatus())) {
            log.error("NOTIFY_INSURANCE_ORDER ERROR , ELECTRICITY_DEPOSIT_ORDER  STATUS IS NOT INIT, ORDER_NO:{}", orderIdLIst.get(1));
            return Pair.of(false, "押金订单已处理!");
        }

        FranchiseeInsurance franchiseeInsurance = franchiseeInsuranceService.queryByCache(insuranceOrder.getInsuranceId());
        if (ObjectUtil.isEmpty(insuranceOrder)) {
            log.error("NOTIFY_INSURANCE_ORDER ERROR ,NOT FOUND ELECTRICITY_DEPOSIT_ORDER ORDER_NO:{}", orderIdLIst.get(1));
            return Pair.of(false, "未找到订单!");
        }


        Integer tradeOrderStatus = ElectricityTradeOrder.STATUS_FAIL;
        Integer depositOrderStatus = EleDepositOrder.STATUS_FAIL;
        boolean result = false;
        if (StringUtils.isNotEmpty(tradeState) && ObjectUtil.equal("SUCCESS", tradeState)) {
            tradeOrderStatus = ElectricityTradeOrder.STATUS_SUCCESS;
            depositOrderStatus = EleDepositOrder.STATUS_SUCCESS;
            result = true;
        } else {
            log.error("NOTIFY REDULT PAY FAIL,ORDER_NO:{}" + tradeOrderNo);
        }

        //用户
        UserInfo userInfo = userInfoService.queryByUidFromCache(eleDepositOrder.getUid());
        if (Objects.isNull(userInfo)) {
            log.error("NOTIFY  ERROR,NOT FOUND USERINFO,USERID:{},ORDER_NO:{}", eleDepositOrder.getUid(), tradeOrderNo);
            return Pair.of(false, "未找到用户信息!");
        }

        //用户押金和保险
        if (Objects.equals(depositOrderStatus, EleDepositOrder.STATUS_SUCCESS)) {

            UserInfo updateUserInfo = new UserInfo();
            updateUserInfo.setUid(userInfo.getUid());
            updateUserInfo.setBatteryDepositStatus(UserInfo.BATTERY_DEPOSIT_STATUS_YES);
            updateUserInfo.setUpdateTime(System.currentTimeMillis());
            updateUserInfo.setFranchiseeId(eleDepositOrder.getFranchiseeId());
            userInfoService.updateByUid(updateUserInfo);

            UserBatteryDeposit userBatteryDeposit = new UserBatteryDeposit();
            userBatteryDeposit.setUid(userInfo.getUid());
            userBatteryDeposit.setOrderId(eleDepositOrder.getOrderId());
            userBatteryDeposit.setUpdateTime(System.currentTimeMillis());
            userBatteryDeposit.setDid(eleDepositOrder.getId());
            userBatteryDeposit.setDelFlag(UserBatteryDeposit.DEL_NORMAL);
            userBatteryDeposit.setCreateTime(System.currentTimeMillis());
            userBatteryDeposit.setBatteryDeposit(eleDepositOrder.getPayAmount());
            userBatteryDeposit.setTenantId(userInfo.getTenantId());
            userBatteryDepositService.insertOrUpdate(userBatteryDeposit);

            UserBattery userBattery = new UserBattery();
            userBattery.setUid(userInfo.getUid());
            userBattery.setUpdateTime(System.currentTimeMillis());
            userBattery.setDelFlag(UserBattery.DEL_NORMAL);
            if (Objects.equals(eleDepositOrder.getModelType(), Franchisee.NEW_MODEL_TYPE)) {
                userBattery.setBatteryType(eleDepositOrder.getBatteryType());
            }
            userBatteryService.insertOrUpdate(userBattery);


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

        //系统订单
        UnionTradeOrder unionTradeOrderUpdate = new UnionTradeOrder();
        unionTradeOrderUpdate.setId(unionTradeOrder.getId());
        unionTradeOrderUpdate.setStatus(tradeOrderStatus);
        unionTradeOrderUpdate.setUpdateTime(System.currentTimeMillis());
        unionTradeOrderUpdate.setChannelOrderNo(transactionId);
        baseMapper.updateById(unionTradeOrderUpdate);

        //混合支付的子订单
        electricityTradeOrderList.parallelStream().forEach(item -> {
            ElectricityTradeOrder electricityTradeOrder = new ElectricityTradeOrder();
            electricityTradeOrder.setId(item.getId());
            electricityTradeOrder.setStatus(item.getStatus());
            electricityTradeOrder.setUpdateTime(System.currentTimeMillis());
            electricityTradeOrder.setChannelOrderNo(transactionId);
            electricityTradeOrderService.updateElectricityTradeOrderById(electricityTradeOrder);
        });


        //押金订单
        EleDepositOrder eleDepositOrderUpdate = new EleDepositOrder();
        eleDepositOrderUpdate.setId(eleDepositOrder.getId());
        eleDepositOrderUpdate.setStatus(depositOrderStatus);
        eleDepositOrderUpdate.setUpdateTime(System.currentTimeMillis());
        eleDepositOrderService.update(eleDepositOrderUpdate);

        //保险订单
        InsuranceOrder updateInsuranceOrder = new InsuranceOrder();
        updateInsuranceOrder.setId(insuranceOrder.getId());
        updateInsuranceOrder.setUpdateTime(System.currentTimeMillis());
        updateInsuranceOrder.setStatus(depositOrderStatus);
        insuranceOrderService.updateOrderStatusById(updateInsuranceOrder);
        return Pair.of(result, null);
    }

    /**
     * 混合支付回调
     *
     * @param callBackResource
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Pair<Boolean, Object> notifyIntegratedPayment(WechatJsapiOrderCallBackResource callBackResource) {

        //回调参数
        String tradeOrderNo = callBackResource.getOutTradeNo();
        String tradeState = callBackResource.getTradeState();
        String transactionId = callBackResource.getTransactionId();

        UnionTradeOrder unionTradeOrder = baseMapper.selectTradeOrderByTradeOrderNo(tradeOrderNo);
        if (Objects.isNull(unionTradeOrder)) {
            log.error("NOTIFY_INSURANCE_UNION_DEPOSIT_ORDER ERROR ,NOT FOUND ELECTRICITY_TRADE_ORDER TRADE_ORDER_NO={}", tradeOrderNo);
            return Pair.of(false, "未找到交易订单!");
        }
        if (ObjectUtil.notEqual(UnionTradeOrder.STATUS_INIT, unionTradeOrder.getStatus())) {
            log.error("NOTIFY_INSURANCE_UNION_DEPOSIT_ORDER ERROR , ELECTRICITY_TRADE_ORDER  STATUS IS NOT INIT, TRADE_ORDER_NO={}", tradeOrderNo);
            return Pair.of(false, "交易订单已处理");
        }

        List<ElectricityTradeOrder> electricityTradeOrderList = electricityTradeOrderService.selectTradeOrderByParentOrderId(unionTradeOrder.getId());
        if (Objects.isNull(electricityTradeOrderList)) {
            log.error("NOTIFY_INSURANCE_UNION_DEPOSIT_ORDER ERROR ,NOT FOUND ELECTRICITY_TRADE_ORDER TRADE_ORDER_NO={}", tradeOrderNo);
            return Pair.of(false, "未找到交易订单!");
        }

        String jsonOrderType = unionTradeOrder.getJsonOrderType();
        List<Integer> orderTypeList = JsonUtil.fromJsonArray(jsonOrderType, Integer.class);

        String jsonOrderId = unionTradeOrder.getJsonOrderId();
        List<String> orderIdLIst = JsonUtil.fromJsonArray(jsonOrderId, String.class);

        if (CollectionUtils.isEmpty(orderIdLIst)) {
            log.error("NOTIFY_INSURANCE_UNION_DEPOSIT_ORDER ERROR ,NOT FOUND ELECTRICITY_TRADE_ORDER TRADE_ORDER_NO={}", tradeOrderNo);
            return Pair.of(false, "未找到交易订单");
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
                //租车押金
                Pair<Boolean, Object> rentCarDepositOrderResult = handleRentCarDepositOrder(orderIdLIst.get(i), depositOrderStatus);
                if (!rentCarDepositOrderResult.getLeft()) {
                    return rentCarDepositOrderResult;
                }
            } else if (Objects.equals(orderTypeList.get(i), UnionPayOrder.ORDER_TYPE_RENT_CAR_MEMBER_CARD)) {
                //租车套餐
                Pair<Boolean, Object> rentCarMemberCardOrderResult = handleRentCarMemberCardOrder(orderIdLIst.get(i), depositOrderStatus);
                if (!rentCarMemberCardOrderResult.getLeft()) {
                    return rentCarMemberCardOrderResult;
                }
            }
        }

        //系统订单
        UnionTradeOrder unionTradeOrderUpdate = new UnionTradeOrder();
        unionTradeOrderUpdate.setId(unionTradeOrder.getId());
        unionTradeOrderUpdate.setStatus(tradeOrderStatus);
        unionTradeOrderUpdate.setUpdateTime(System.currentTimeMillis());
        unionTradeOrderUpdate.setChannelOrderNo(transactionId);
        baseMapper.updateById(unionTradeOrderUpdate);

        //混合支付的子订单
        electricityTradeOrderList.parallelStream().forEach(item -> {
            ElectricityTradeOrder electricityTradeOrder = new ElectricityTradeOrder();
            electricityTradeOrder.setId(item.getId());
            electricityTradeOrder.setStatus(item.getStatus());
            electricityTradeOrder.setUpdateTime(System.currentTimeMillis());
            electricityTradeOrder.setChannelOrderNo(transactionId);
            electricityTradeOrderService.updateElectricityTradeOrderById(electricityTradeOrder);
        });
        return Pair.of(result, null);
    }

    //处理押金订单
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Pair<Boolean, Object> manageDepositOrder(String orderNo, Integer orderStatus) {

        //押金订单
        EleDepositOrder eleDepositOrder = eleDepositOrderService.queryByOrderId(orderNo);
        if (ObjectUtil.isEmpty(eleDepositOrder)) {
            log.error("NOTIFY_DEPOSIT_ORDER ERROR ,NOT FOUND ELECTRICITY_DEPOSIT_ORDER ORDER_NO={}", orderNo);
            return Pair.of(false, "未找到订单!");
        }

        if (!ObjectUtil.equal(EleDepositOrder.STATUS_INIT, eleDepositOrder.getStatus())) {
            log.error("NOTIFY_DEPOSIT_ORDER ERROR , ELECTRICITY_DEPOSIT_ORDER  STATUS IS NOT INIT, ORDER_NO={}", orderNo);
            return Pair.of(false, "押金订单已处理!");
        }

        //用户
        UserInfo userInfo = userInfoService.queryByUidFromCache(eleDepositOrder.getUid());
        if (Objects.isNull(userInfo)) {
            log.error("NOTIFY  ERROR,NOT FOUND USERINFO,USERID={},ORDER_NO={}", eleDepositOrder.getUid(), orderNo);
            return Pair.of(false, "未找到用户信息!");
        }

        //用户押金
        if (Objects.equals(orderStatus, EleDepositOrder.STATUS_SUCCESS)) {
            UserInfo updateUserInfo = new UserInfo();
            updateUserInfo.setUid(userInfo.getUid());
            updateUserInfo.setBatteryDepositStatus(UserInfo.BATTERY_DEPOSIT_STATUS_YES);
            updateUserInfo.setFranchiseeId(eleDepositOrder.getFranchiseeId());
            updateUserInfo.setUpdateTime(System.currentTimeMillis());
            userInfoService.updateByUid(updateUserInfo);

            UserBatteryDeposit userBatteryDeposit = new UserBatteryDeposit();
            userBatteryDeposit.setUid(userInfo.getUid());
            userBatteryDeposit.setOrderId(eleDepositOrder.getOrderId());
            userBatteryDeposit.setDid(eleDepositOrder.getId());
            userBatteryDeposit.setUpdateTime(System.currentTimeMillis());
            userBatteryDeposit.setCreateTime(System.currentTimeMillis());
            userBatteryDeposit.setDelFlag(UserBatteryDeposit.DEL_NORMAL);
            userBatteryDeposit.setBatteryDeposit(eleDepositOrder.getPayAmount());
            userBatteryDeposit.setTenantId(userInfo.getTenantId());
            userBatteryDepositService.insertOrUpdate(userBatteryDeposit);

            UserBattery userBattery = new UserBattery();
            userBattery.setUid(userInfo.getUid());
            userBattery.setUpdateTime(System.currentTimeMillis());
            if (Objects.equals(eleDepositOrder.getModelType(), Franchisee.NEW_MODEL_TYPE)) {
                userBattery.setBatteryType(eleDepositOrder.getBatteryType());
            }
            userBatteryService.insertOrUpdate(userBattery);
        }

        //押金订单
        EleDepositOrder eleDepositOrderUpdate = new EleDepositOrder();
        eleDepositOrderUpdate.setId(eleDepositOrder.getId());
        eleDepositOrderUpdate.setStatus(orderStatus);
        eleDepositOrderUpdate.setUpdateTime(System.currentTimeMillis());
        eleDepositOrderService.update(eleDepositOrderUpdate);
        return Pair.of(true, null);
    }


    //处理购卡订单
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Pair<Boolean, Object> manageMemberCardOrder(String orderNo, Integer orderStatus) {

        //购卡订单
        ElectricityMemberCardOrder electricityMemberCardOrder = electricityMemberCardOrderService.selectByOrderNo(orderNo);
        if (ObjectUtil.isEmpty(electricityMemberCardOrder)) {
            log.error("NOTIFY_MEMBER_ORDER ERROR ,NOT FOUND ELECTRICITY_MEMBER_CARD_ORDER ORDER_NO={}", orderNo);
            return Pair.of(false, "未找到订单!");
        }
        if (!ObjectUtil.equal(ElectricityMemberCardOrder.STATUS_INIT, electricityMemberCardOrder.getStatus())) {
            log.error("NOTIFY_MEMBER_ORDER ERROR , ELECTRICITY_MEMBER_CARD_ORDER  STATUS IS NOT INIT, ORDER_NO={}", orderNo);
            return Pair.of(false, "套餐订单已处理!");
        }

        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(electricityMemberCardOrder.getUid());
//        if (Objects.isNull(userBatteryMemberCard) || Objects.isNull(userBatteryMemberCard.getMemberCardExpireTime()) || Objects.isNull(userBatteryMemberCard.getRemainingNumber())) {
//            log.error("HOME WARN! user haven't memberCard uid={}", electricityMemberCardOrder.getUid());
//            return Pair.of(false, "未找到用户信息!");
//        }


        Long now = System.currentTimeMillis();
        Long memberCardExpireTime;
        Long remainingNumber = electricityMemberCardOrder.getMaxUseCount();

        //月卡订单
        ElectricityMemberCardOrder electricityMemberCardOrderUpdate = new ElectricityMemberCardOrder();


        if (Objects.equals(orderStatus, EleDepositOrder.STATUS_SUCCESS)) {


            //查看月卡是否绑定活动
            ElectricityMemberCard electricityMemberCard = electricityMemberCardService.queryByCache(electricityMemberCardOrder.getMemberCardId());

            if (Objects.nonNull(electricityMemberCard)) {

                //月卡是否绑定活动
                if (Objects.equals(electricityMemberCard.getIsBindActivity(), ElectricityMemberCard.BIND_ACTIVITY) && Objects.nonNull(electricityMemberCard.getActivityId())) {
                    OldUserActivity oldUserActivity = oldUserActivityService.queryByIdFromCache(electricityMemberCard.getActivityId());

                    if (Objects.nonNull(oldUserActivity)) {

                        //次数
                        if (Objects.equals(oldUserActivity.getDiscountType(), OldUserActivity.TYPE_COUNT) && Objects.nonNull(oldUserActivity.getCount())) {
                            remainingNumber = remainingNumber + oldUserActivity.getCount();
                        }

                        //优惠券
                        if (Objects.equals(oldUserActivity.getDiscountType(), OldUserActivity.TYPE_COUPON) && Objects.nonNull(oldUserActivity.getCouponId())) {
                            //发放优惠券
                            Long[] uids = new Long[1];
                            uids[0] = electricityMemberCardOrder.getUid();
                            userCouponService.batchRelease(oldUserActivity.getCouponId(), uids);
                        }
                    }
                }
            }


            UserBatteryMemberCard userBatteryMemberCardUpdate = new UserBatteryMemberCard();
            userBatteryMemberCardUpdate.setUid(electricityMemberCardOrder.getUid());

            if (Objects.equals(electricityMemberCard.getLimitCount(), ElectricityMemberCard.UN_LIMITED_COUNT_TYPE)) {
                if (Objects.isNull(userBatteryMemberCard) || Objects.isNull(userBatteryMemberCard.getMemberCardExpireTime()) || userBatteryMemberCard.getMemberCardExpireTime() < now) {
                    memberCardExpireTime = System.currentTimeMillis() +
                            electricityMemberCardOrder.getValidDays() * (24 * 60 * 60 * 1000L);
                } else {
                    memberCardExpireTime = userBatteryMemberCard.getMemberCardExpireTime() +
                            electricityMemberCardOrder.getValidDays() * (24 * 60 * 60 * 1000L);
                }
            } else {
                if (Objects.isNull(userBatteryMemberCard) || Objects.isNull(userBatteryMemberCard.getMemberCardExpireTime()) || userBatteryMemberCard.getMemberCardExpireTime() < now || Objects.isNull(userBatteryMemberCard.getRemainingNumber()) || userBatteryMemberCard.getRemainingNumber() == 0) {
                    memberCardExpireTime = System.currentTimeMillis() +
                            electricityMemberCardOrder.getValidDays() * (24 * 60 * 60 * 1000L);
                } else {
                    memberCardExpireTime = userBatteryMemberCard.getMemberCardExpireTime() +
                            electricityMemberCardOrder.getValidDays() * (24 * 60 * 60 * 1000L);
                    remainingNumber = remainingNumber + userBatteryMemberCard.getRemainingNumber();
                }
            }


            userBatteryMemberCardUpdate.setMemberCardExpireTime(memberCardExpireTime);
            userBatteryMemberCardUpdate.setRemainingNumber(remainingNumber.intValue());
            userBatteryMemberCardUpdate.setMemberCardStatus(UserBatteryMemberCard.MEMBER_CARD_NOT_DISABLE);
            userBatteryMemberCardUpdate.setMemberCardId(electricityMemberCardOrder.getMemberCardId().longValue());
            userBatteryMemberCardUpdate.setUpdateTime(System.currentTimeMillis());
            userBatteryMemberCardUpdate.setDelFlag(UserBatteryMemberCard.DEL_NORMAL);
            userBatteryMemberCardUpdate.setCreateTime(System.currentTimeMillis());
            userBatteryMemberCardUpdate.setTenantId(electricityMemberCardOrder.getTenantId());
            userBatteryMemberCardUpdate.setCardPayCount(Objects.isNull(userBatteryMemberCard) ? 1 : userBatteryMemberCard.getCardPayCount() + 1);
            userBatteryMemberCardService.insertOrUpdate(userBatteryMemberCardUpdate);


            ServiceFeeUserInfo serviceFeeUserInfo = serviceFeeUserInfoService.queryByUidFromCache(userBatteryMemberCardUpdate.getUid());
            ServiceFeeUserInfo serviceFeeUserInfoInsertOrUpdate = new ServiceFeeUserInfo();
            serviceFeeUserInfoInsertOrUpdate.setServiceFeeGenerateTime(memberCardExpireTime);
            serviceFeeUserInfoInsertOrUpdate.setUid(userBatteryMemberCardUpdate.getUid());
            serviceFeeUserInfoInsertOrUpdate.setFranchiseeId(electricityMemberCardOrder.getFranchiseeId());
            serviceFeeUserInfoInsertOrUpdate.setUpdateTime(System.currentTimeMillis());
            serviceFeeUserInfoInsertOrUpdate.setTenantId(electricityMemberCardOrder.getTenantId());
            if (Objects.isNull(serviceFeeUserInfo)) {
                serviceFeeUserInfoInsertOrUpdate.setCreateTime(System.currentTimeMillis());
                serviceFeeUserInfoInsertOrUpdate.setDelFlag(ServiceFeeUserInfo.DEL_NORMAL);
                serviceFeeUserInfoInsertOrUpdate.setDisableMemberCardNo("");
                serviceFeeUserInfoInsertOrUpdate.setExistBatteryServiceFee(ServiceFeeUserInfo.NOT_EXIST_SERVICE_FEE);
                serviceFeeUserInfoService.insert(serviceFeeUserInfoInsertOrUpdate);
            } else {
                serviceFeeUserInfoService.updateByUid(serviceFeeUserInfoInsertOrUpdate);
            }

            electricityMemberCardOrderUpdate.setPayCount(userBatteryMemberCardUpdate.getCardPayCount());


            if (Objects.nonNull(electricityMemberCardOrder.getCouponId())) {
                UserCoupon userCoupon = userCouponService.queryByIdFromDB(electricityMemberCardOrder.getCouponId().intValue());
                if (Objects.nonNull(userCoupon)) {
                    //修改劵可用状态
                    userCoupon.setStatus(UserCoupon.STATUS_USED);
                    userCoupon.setUpdateTime(System.currentTimeMillis());
                    userCoupon.setOrderId(electricityMemberCardOrder.getOrderId());
                    userCouponService.update(userCoupon);
                }
            }

            //被邀请新买月卡用户
            //是否是新用户
            if (Objects.isNull(userBatteryMemberCard) || Objects.isNull(userBatteryMemberCard.getMemberCardId())) {
                //是否有人邀请
                JoinShareActivityRecord joinShareActivityRecord = joinShareActivityRecordService.queryByJoinUid(electricityMemberCardOrder.getUid());
                if (Objects.nonNull(joinShareActivityRecord)) {
                    //修改邀请状态
                    joinShareActivityRecord.setStatus(JoinShareActivityRecord.STATUS_SUCCESS);
                    joinShareActivityRecord.setUpdateTime(System.currentTimeMillis());
                    joinShareActivityRecordService.update(joinShareActivityRecord);

                    //修改历史记录状态
                    JoinShareActivityHistory oldJoinShareActivityHistory = joinShareActivityHistoryService.queryByRecordIdAndStatus(joinShareActivityRecord.getId());
                    if (Objects.nonNull(oldJoinShareActivityHistory)) {
                        oldJoinShareActivityHistory.setStatus(JoinShareActivityHistory.STATUS_SUCCESS);
                        oldJoinShareActivityHistory.setUpdateTime(System.currentTimeMillis());
                        joinShareActivityHistoryService.update(oldJoinShareActivityHistory);
                    }

                    //给邀请人增加邀请成功人数
                    shareActivityRecordService.addCountByUid(joinShareActivityRecord.getUid());
                }

                //是否有人返现邀请
                JoinShareMoneyActivityRecord joinShareMoneyActivityRecord = joinShareMoneyActivityRecordService.queryByJoinUid(electricityMemberCardOrder.getUid());
                if (Objects.nonNull(joinShareMoneyActivityRecord)) {
                    //修改邀请状态
                    joinShareMoneyActivityRecord.setStatus(JoinShareMoneyActivityRecord.STATUS_SUCCESS);
                    joinShareMoneyActivityRecord.setUpdateTime(System.currentTimeMillis());
                    joinShareMoneyActivityRecordService.update(joinShareMoneyActivityRecord);

                    //修改历史记录状态
                    JoinShareMoneyActivityHistory oldJoinShareMoneyActivityHistory = joinShareMoneyActivityHistoryService.queryByRecordIdAndStatus(joinShareMoneyActivityRecord.getId());
                    if (Objects.nonNull(oldJoinShareMoneyActivityHistory)) {
                        oldJoinShareMoneyActivityHistory.setStatus(JoinShareMoneyActivityHistory.STATUS_SUCCESS);
                        oldJoinShareMoneyActivityHistory.setUpdateTime(System.currentTimeMillis());
                        joinShareMoneyActivityHistoryService.update(oldJoinShareMoneyActivityHistory);
                    }

                    ShareMoneyActivity shareMoneyActivity = shareMoneyActivityService.queryByIdFromCache(joinShareMoneyActivityRecord.getActivityId());

                    if (Objects.nonNull(shareMoneyActivity)) {
                        //给邀请人增加邀请成功人数
                        shareMoneyActivityRecordService.addCountByUid(joinShareMoneyActivityRecord.getUid(), shareMoneyActivity.getMoney());

                        //返现
                        userAmountService.handleAmount(joinShareMoneyActivityRecord.getUid(), joinShareMoneyActivityRecord.getJoinUid(), shareMoneyActivity.getMoney(), electricityMemberCardOrder.getTenantId());

                    }

                }
            }
        }


        electricityMemberCardOrderUpdate.setId(electricityMemberCardOrder.getId());
        electricityMemberCardOrderUpdate.setStatus(orderStatus);
        electricityMemberCardOrderUpdate.setUpdateTime(System.currentTimeMillis());
        electricityMemberCardOrderService.updateByID(electricityMemberCardOrderUpdate);
        return Pair.of(true, null);
    }


    //处理保险订单
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Pair<Boolean, Object> manageInsuranceOrder(String orderNo, Integer orderStatus) {

        //保险订单
        InsuranceOrder insuranceOrder = insuranceOrderService.queryByOrderId(orderNo);
        if (ObjectUtil.isEmpty(insuranceOrder)) {
            log.error("NOTIFY_INSURANCE_ORDER ERROR ,NOT FOUND ELECTRICITY_DEPOSIT_ORDER ORDER_NO={}", orderNo);
            return Pair.of(false, "未找到订单!");
        }

        if (!ObjectUtil.equal(EleBatteryServiceFeeOrder.STATUS_INIT, insuranceOrder.getStatus())) {
            log.error("NOTIFY_INSURANCE_ORDER ERROR , ELECTRICITY_DEPOSIT_ORDER  STATUS IS NOT INIT, ORDER_NO={}", orderNo);
            return Pair.of(false, "押金订单已处理!");
        }

        FranchiseeInsurance franchiseeInsurance = franchiseeInsuranceService.queryByCache(insuranceOrder.getInsuranceId());
        if (ObjectUtil.isEmpty(insuranceOrder)) {
            log.error("NOTIFY_INSURANCE_ORDER ERROR ,NOT FOUND ELECTRICITY_DEPOSIT_ORDER ORDER_NO={}", orderNo);
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

            InsuranceUserInfo insuranceUserInfo = insuranceUserInfoService.queryByUidFromCache(insuranceOrder.getUid());
            if (Objects.isNull(insuranceUserInfo)) {
                insuranceUserInfoService.insert(updateOrAddInsuranceUserInfo);
            } else {
                insuranceUserInfoService.update(updateOrAddInsuranceUserInfo);
            }
        }

        //保险订单
        InsuranceOrder updateInsuranceOrder = new InsuranceOrder();
        updateInsuranceOrder.setId(insuranceOrder.getId());
        updateInsuranceOrder.setUpdateTime(System.currentTimeMillis());
        updateInsuranceOrder.setStatus(orderStatus);
        insuranceOrderService.updateOrderStatusById(updateInsuranceOrder);

        return Pair.of(true, null);
    }

    /**
     * 处理租车押金
     *
     * @return
     */
    public Pair<Boolean, Object> handleRentCarDepositOrder(String orderNo, Integer depositOrderStatus) {

        CarDepositOrder carDepositOrder = carDepositOrderService.selectByOrderId(orderNo);
        if (Objects.isNull(carDepositOrder)) {
            log.error("WECHATV3 NOTIFY ERROR!not found carDepositOrder,orderNo={}", orderNo);
            return Pair.of(false, "未找到订单!");
        }
        if (!Objects.equals(carDepositOrder.getStatus(), CarDepositOrder.STATUS_INIT)) {
            log.error("WECHATV3 NOTIFY ERROR!carDepositOrder status is not init,orderNo={}", orderNo);
            return Pair.of(false, "订单已处理!");
        }

        UserInfo userInfo = userInfoService.queryByUidFromCache(carDepositOrder.getUid());
        if (Objects.isNull(userInfo)) {
            log.error("WECHATV3 NOTIFY ERROR!userInfo is null,orderNo={},uid={}", orderNo, carDepositOrder.getUid());
            return Pair.of(false, "用户不存在!");
        }

        //支付成功
        if (Objects.equals(depositOrderStatus, EleDepositOrder.STATUS_SUCCESS)) {
            UserInfo updateUserInfo = new UserInfo();
            updateUserInfo.setUid(userInfo.getUid());
            updateUserInfo.setCarDepositStatus(UserInfo.CAR_DEPOSIT_STATUS_YES);
            updateUserInfo.setFranchiseeId(carDepositOrder.getFranchiseeId());
            updateUserInfo.setUpdateTime(System.currentTimeMillis());
            userInfoService.updateByUid(updateUserInfo);

            UserCarDeposit userCarDeposit = new UserCarDeposit();
            userCarDeposit.setUid(userInfo.getUid());
            userCarDeposit.setDid(carDepositOrder.getId());
            userCarDeposit.setOrderId(carDepositOrder.getOrderId());
            userCarDeposit.setCarDeposit(carDepositOrder.getPayAmount());
            userCarDeposit.setTenantId(userInfo.getTenantId());
            userCarDeposit.setDelFlag(UserCarDeposit.DEL_NORMAL);
            userCarDeposit.setCreateTime(System.currentTimeMillis());
            userCarDeposit.setUpdateTime(System.currentTimeMillis());
            userCarDepositService.insertOrUpdate(userCarDeposit);

            UserCar userCar = new UserCar();
            userCar.setUid(userInfo.getUid());
            userCar.setCarModel(carDepositOrder.getCarModelId());
            userCar.setDelFlag(UserCar.DEL_NORMAL);
            userCar.setTenantId(userInfo.getTenantId());
            userCar.setCreateTime(System.currentTimeMillis());
            userCar.setUpdateTime(System.currentTimeMillis());
            userCarService.insertOrUpdate(userCar);
        }

        //更新订单状态
        CarDepositOrder updateCarDepositOrder = new CarDepositOrder();
        updateCarDepositOrder.setId(carDepositOrder.getId());
        updateCarDepositOrder.setStatus(depositOrderStatus);
        updateCarDepositOrder.setUpdateTime(System.currentTimeMillis());
        carDepositOrderService.update(updateCarDepositOrder);
        return Pair.of(true, null);
    }

    /**
     * 处理租车套餐
     *
     * @return
     */
    public Pair<Boolean, Object> handleRentCarMemberCardOrder(String orderNo, Integer orderStatus) {

        CarMemberCardOrder carMemberCardOrder = carMemberCardOrderService.selectByOrderId(orderNo);
        if (Objects.isNull(carMemberCardOrder)) {
            log.error("WECHATV3 NOTIFY ERROR!not found carMemberCardOrder,orderNo={}", orderNo);
            return Pair.of(false, "未找到订单!");
        }
        if (!Objects.equals(carMemberCardOrder.getStatus(), CarMemberCardOrder.STATUS_INIT)) {
            log.error("WECHATV3 NOTIFY ERROR!carMemberCardOrder status is not init,orderNo={}", orderNo);
            return Pair.of(false, "订单已处理!");
        }

        UserInfo userInfo = userInfoService.queryByUidFromCache(carMemberCardOrder.getUid());
        if (Objects.isNull(userInfo)) {
            log.error("WECHATV3 NOTIFY ERROR!userInfo is null,orderNo={},uid={}", orderNo, carMemberCardOrder.getUid());
            return Pair.of(false, "用户不存在!");
        }

        if (Objects.equals(orderStatus, CarMemberCardOrder.STATUS_SUCCESS)) {
            UserCarMemberCard userCarMemberCard = userCarMemberCardService.selectByUidFromCache(carMemberCardOrder.getUid());

            UserCarMemberCard updateUserCarMemberCard = new UserCarMemberCard();
            updateUserCarMemberCard.setUid(userInfo.getUid());
            updateUserCarMemberCard.setCardId(carMemberCardOrder.getId());
            updateUserCarMemberCard.setOrderId(carMemberCardOrder.getOrderId());
            updateUserCarMemberCard.setMemberCardExpireTime(electricityMemberCardOrderService.calcRentCarMemberCardExpireTime(carMemberCardOrder.getMemberCardType(), carMemberCardOrder.getValidDays(), userCarMemberCard));
            updateUserCarMemberCard.setDelFlag(UserCarMemberCard.DEL_NORMAL);
            updateUserCarMemberCard.setCreateTime(System.currentTimeMillis());
            updateUserCarMemberCard.setUpdateTime(System.currentTimeMillis());

            userCarMemberCardService.insertOrUpdate(updateUserCarMemberCard);
        }

        CarMemberCardOrder updateCarMemberCardOrder = new CarMemberCardOrder();
        updateCarMemberCardOrder.setId(carMemberCardOrder.getId());
        updateCarMemberCardOrder.setStatus(orderStatus);
        updateCarMemberCardOrder.setUpdateTime(System.currentTimeMillis());
        carMemberCardOrderService.update(updateCarMemberCardOrder);

        return Pair.of(true, null);
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
