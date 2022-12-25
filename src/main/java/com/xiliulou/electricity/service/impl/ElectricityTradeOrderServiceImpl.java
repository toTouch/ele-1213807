package com.xiliulou.electricity.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.electricity.config.WechatConfig;
import com.xiliulou.electricity.constant.WechatPayConstant;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.mapper.ElectricityMemberCardOrderMapper;
import com.xiliulou.electricity.mapper.ElectricityTradeOrderMapper;
import com.xiliulou.electricity.query.CarMemberCardOrderQuery;
import com.xiliulou.electricity.service.*;
import com.xiliulou.pay.weixinv3.dto.WechatJsapiOrderCallBackResource;
import com.xiliulou.pay.weixinv3.dto.WechatJsapiOrderResultDTO;
import com.xiliulou.pay.weixinv3.exception.WechatPayException;
import com.xiliulou.pay.weixinv3.query.WechatV3OrderQuery;
import com.xiliulou.pay.weixinv3.service.WechatV3JsapiService;
import lombok.extern.slf4j.Slf4j;
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
 * @author: Mr.YG
 * @create: 2020-12-03 11:34
 **/
@Service
@Slf4j
public class ElectricityTradeOrderServiceImpl extends
        ServiceImpl<ElectricityTradeOrderMapper, ElectricityTradeOrder> implements ElectricityTradeOrderService {

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

    @Override
    public WechatJsapiOrderResultDTO commonCreateTradeOrderAndGetPayParams(CommonPayOrder commonOrder, ElectricityPayParams electricityPayParams, String openId, HttpServletRequest request) throws WechatPayException {

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
        electricityTradeOrder.setTotalFee(commonOrder.getPayAmount());
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
        if (!ObjectUtil.equal(ElectricityMemberCardOrder.STATUS_INIT, electricityMemberCardOrder.getStatus())) {
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
        UserInfo userInfo = userInfoService.queryByUidFromCache(electricityMemberCardOrder.getUid());
        if (Objects.isNull(userInfo)) {
            log.error("NOTIFY  ERROR,NOT FOUND USERINFO,USERID={},ORDER_NO={}", electricityMemberCardOrder.getUid(), tradeOrderNo);
            return Pair.of(false, "未找到用户信息!");
        }

//        //是否缴纳押金，是否绑定电池
//        FranchiseeUserInfo franchiseeUserInfo = franchiseeUserInfoService.queryByUserInfoId(userInfo.getId());
//
//        //未找到用户
//        if (Objects.isNull(franchiseeUserInfo)) {
//            log.error("payDeposit  ERROR! not found user! userId:{}", userInfo.getUid());
//            return Pair.of(false, "未找到用户信息!");
//
//        }

        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(electricityMemberCardOrder.getUid());
        if (Objects.isNull(userBatteryMemberCard) || Objects.isNull(userBatteryMemberCard.getMemberCardExpireTime()) || Objects.isNull(userBatteryMemberCard.getRemainingNumber())) {
            log.error("HOME WARN! user haven't memberCard uid={}", electricityMemberCardOrder.getUid());
            return Pair.of(false, "未找到用户信息!");
        }

        Long now = System.currentTimeMillis();
        Long memberCardExpireTime;
        Long remainingNumber = electricityMemberCardOrder.getMaxUseCount();

        //用户月卡
        if (Objects.equals(memberOrderStatus, EleDepositOrder.STATUS_SUCCESS)) {

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
            userBatteryMemberCardUpdate.setUid(userBatteryMemberCard.getUid());

            if (Objects.equals(electricityMemberCard.getLimitCount(), ElectricityMemberCard.UN_LIMITED_COUNT_TYPE)) {
                if (Objects.isNull(userBatteryMemberCard.getMemberCardExpireTime()) || userBatteryMemberCard.getMemberCardExpireTime() < now) {
                    memberCardExpireTime = System.currentTimeMillis() +
                            electricityMemberCardOrder.getValidDays() * (24 * 60 * 60 * 1000L);
                } else {
                    memberCardExpireTime = userBatteryMemberCard.getMemberCardExpireTime() +
                            electricityMemberCardOrder.getValidDays() * (24 * 60 * 60 * 1000L);
                }
            } else {
                if (Objects.isNull(userBatteryMemberCard.getMemberCardExpireTime()) || userBatteryMemberCard.getMemberCardExpireTime() < now || Objects.isNull(userBatteryMemberCard.getRemainingNumber()) || userBatteryMemberCard.getRemainingNumber() == 0) {
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
            userBatteryMemberCardService.updateByUid(userBatteryMemberCardUpdate);

            if (StringUtils.isNotEmpty(callBackResource.getAttach()) && !Objects.equals(callBackResource.getAttach(), "null")) {
                UserCoupon userCoupon = userCouponService.queryByIdFromDB(Integer.valueOf(callBackResource.getAttach()));
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
            if (Objects.isNull(userBatteryMemberCard.getMemberCardId())) {
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

            //月卡分账
            handleSplitAccount(electricityMemberCardOrder);

        }

        //交易订单
        ElectricityTradeOrder electricityTradeOrderUpdate = new ElectricityTradeOrder();
        electricityTradeOrderUpdate.setId(electricityTradeOrder.getId());
        electricityTradeOrderUpdate.setStatus(tradeOrderStatus);
        electricityTradeOrderUpdate.setUpdateTime(System.currentTimeMillis());
        electricityTradeOrderUpdate.setChannelOrderNo(transactionId);
        baseMapper.updateById(electricityTradeOrderUpdate);

        //月卡订单
        ElectricityMemberCardOrder electricityMemberCardOrderUpdate = new ElectricityMemberCardOrder();
        electricityMemberCardOrderUpdate.setId(electricityMemberCardOrder.getId());
        electricityMemberCardOrderUpdate.setStatus(memberOrderStatus);
        electricityMemberCardOrderUpdate.setUpdateTime(System.currentTimeMillis());
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
            userInfoService.updateByUid(updateUserInfo);

            UserBatteryDeposit userBatteryDeposit = new UserBatteryDeposit();
            userBatteryDeposit.setUid(userInfo.getUid());
            userBatteryDeposit.setBatteryDeposit(eleDepositOrder.getPayAmount());
            userBatteryDeposit.setOrderId(eleDepositOrder.getOrderId());
            userBatteryDeposit.setTenantId(eleDepositOrder.getTenantId());
            userBatteryDeposit.setCreateTime(System.currentTimeMillis());
            userBatteryDeposit.setUpdateTime(System.currentTimeMillis());
            userBatteryDepositService.insertOrUpdate(userBatteryDeposit);


            UserBattery userBattery = new UserBattery();
            userBattery.setUid(userInfo.getUid());
            userBattery.setBatteryType(eleDepositOrder.getBatteryType());
            userBattery.setTenantId(eleDepositOrder.getTenantId());
            userBattery.setCreateTime(System.currentTimeMillis());
            userBattery.setUpdateTime(System.currentTimeMillis());
            userBatteryService.insertOrUpdate(userBattery);

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
            return Pair.of(false, "押金订单已处理!");
        }

        Integer tradeOrderStatus = ElectricityTradeOrder.STATUS_FAIL;
        Integer eleBatteryServiceFeeOrderStatus = EleBatteryServiceFeeOrder.STATUS_FAIL;
        boolean result = false;
        if (StringUtils.isNotEmpty(tradeState) && ObjectUtil.equal("SUCCESS", tradeState)) {
            tradeOrderStatus = ElectricityTradeOrder.STATUS_SUCCESS;
            eleBatteryServiceFeeOrderStatus = EleBatteryServiceFeeOrder.STATUS_SUCCESS;
            result = true;
        } else {
            log.error("NOTIFY REDULT PAY FAIL,ORDER_NO:{}" + tradeOrderNo);
        }

        //用户
        UserInfo userInfo = userInfoService.queryByUidFromCache(eleBatteryServiceFeeOrder.getUid());
        if (Objects.isNull(userInfo)) {
            log.error("NOTIFY  ERROR,NOT FOUND USERINFO,USERID={},orderNo={}", eleBatteryServiceFeeOrder.getUid(), tradeOrderNo);
            return Pair.of(false, "未找到用户信息!");
        }

        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(userInfo.getUid());

        ElectricityMemberCard electricityMemberCard = electricityMemberCardService.queryByCache(userBatteryMemberCard.getMemberCardId().intValue());

        ServiceFeeUserInfo serviceFeeUserInfo = serviceFeeUserInfoService.queryByUidFromCache(userInfo.getUid());

        if (Objects.equals(eleBatteryServiceFeeOrderStatus, EleDepositOrder.STATUS_SUCCESS)) {


            UserBatteryMemberCard userBatteryMemberCardUpdate = new UserBatteryMemberCard();
            if (Objects.equals(userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_DISABLE)) {
                Long memberCardExpireTime = System.currentTimeMillis() + (userBatteryMemberCard.getMemberCardExpireTime() - userBatteryMemberCard.getDisableMemberCardTime());
                userBatteryMemberCardUpdate.setMemberCardExpireTime(memberCardExpireTime);
                EleDisableMemberCardRecord eleDisableMemberCardRecord = eleDisableMemberCardRecordService.queryCreateTimeMaxEleDisableMemberCardRecord(userInfo.getUid(), userInfo.getTenantId());


                EnableMemberCardRecord enableMemberCardRecord = enableMemberCardRecordService.queryByDisableCardNO(eleDisableMemberCardRecord.getDisableMemberCardNo(), userInfo.getTenantId());
                Long cardDays = (System.currentTimeMillis() - userBatteryMemberCard.getDisableMemberCardTime()) / 1000L / 60 / 60 / 24;
                if (Objects.isNull(enableMemberCardRecord)) {
                    EnableMemberCardRecord enableMemberCardRecordInsert = EnableMemberCardRecord.builder()
                            .disableMemberCardNo(eleDisableMemberCardRecord.getDisableMemberCardNo())
                            .memberCardName(electricityMemberCard.getName())
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
            userBatteryMemberCardUpdate.setUpdateTime(System.currentTimeMillis());
            userBatteryMemberCardUpdate.setDisableMemberCardTime(null);
            userBatteryMemberCardUpdate.setMemberCardStatus(UserBatteryMemberCard.MEMBER_CARD_NOT_DISABLE);
            userBatteryMemberCardService.updateByUid(userBatteryMemberCardUpdate);


            if (Objects.nonNull(serviceFeeUserInfo) && Objects.equals(serviceFeeUserInfo.getExistBatteryServiceFee(), ServiceFeeUserInfo.EXIST_SERVICE_FEE)) {
                ServiceFeeUserInfo serviceFeeUserInfoUpdate = new ServiceFeeUserInfo();
                serviceFeeUserInfoUpdate.setExistBatteryServiceFee(ServiceFeeUserInfo.NOT_EXIST_SERVICE_FEE);
                Long memberCardExpireTime = System.currentTimeMillis() + (userBatteryMemberCard.getMemberCardExpireTime() - userBatteryMemberCard.getDisableMemberCardTime());
                serviceFeeUserInfoUpdate.setServiceFeeGenerateTime(memberCardExpireTime);
                serviceFeeUserInfoUpdate.setUid(userInfo.getUid());
                serviceFeeUserInfoUpdate.setUpdateTime(System.currentTimeMillis());
                serviceFeeUserInfoService.updateByUid(serviceFeeUserInfoUpdate);
            }
        }

        //交易订单
        ElectricityTradeOrder electricityTradeOrderUpdate = new ElectricityTradeOrder();
        electricityTradeOrderUpdate.setId(electricityTradeOrder.getId());
        electricityTradeOrderUpdate.setStatus(tradeOrderStatus);
        electricityTradeOrderUpdate.setUpdateTime(System.currentTimeMillis());
        electricityTradeOrderUpdate.setChannelOrderNo(transactionId);
        baseMapper.updateById(electricityTradeOrderUpdate);

        //电池服务费订单
        EleBatteryServiceFeeOrder eleBatteryServiceFeeOrderUpdate = new EleBatteryServiceFeeOrder();
        eleBatteryServiceFeeOrderUpdate.setId(eleBatteryServiceFeeOrder.getId());
        eleBatteryServiceFeeOrderUpdate.setStatus(eleBatteryServiceFeeOrderStatus);
        eleBatteryServiceFeeOrderUpdate.setUpdateTime(System.currentTimeMillis());
        if (Objects.nonNull(serviceFeeUserInfo)) {
            eleBatteryServiceFeeOrderUpdate.setBatteryServiceFeeGenerateTime(serviceFeeUserInfo.getServiceFeeGenerateTime());
        }
        eleBatteryServiceFeeOrderService.update(eleBatteryServiceFeeOrderUpdate);
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

//        UserCarMemberCard userCarMemberCard = userCarMemberCardService.selectByUidFromCache(userInfo.getUid());
//        if (Objects.isNull(userCarMemberCard)) {
//            log.error("payDeposit ERROR! not found userCarMemberCard,uid={}", userInfo.getUid());
//            return Pair.of(false, "未找到用户信息!");
//        }

        if (Objects.equals(memberOrderStatus, EleDepositOrder.STATUS_SUCCESS)) {
            UserCarMemberCard userCarMemberCard = userCarMemberCardService.selectByUidFromCache(userInfo.getUid());

            UserCarMemberCard updateUserCarMemberCard = new UserCarMemberCard();
            updateUserCarMemberCard.setUid(userInfo.getUid());
            updateUserCarMemberCard.setCardId(carMemberCardOrder.getCarModelId());
            updateUserCarMemberCard.setMemberCardExpireTime(electricityMemberCardOrderService.calcRentCarMemberCardExpireTime(carMemberCardOrder.getMemberCardType(),carMemberCardOrder.getValidDays(), userCarMemberCard));
            updateUserCarMemberCard.setUpdateTime(System.currentTimeMillis());

            userCarMemberCardService.updateByUid(updateUserCarMemberCard);
        }


        //交易订单
        ElectricityTradeOrder electricityTradeOrderUpdate = new ElectricityTradeOrder();
        electricityTradeOrderUpdate.setId(electricityTradeOrder.getId());
        electricityTradeOrderUpdate.setStatus(tradeOrderStatus);
        electricityTradeOrderUpdate.setUpdateTime(System.currentTimeMillis());
        electricityTradeOrderUpdate.setChannelOrderNo(transactionId);
        baseMapper.updateById(electricityTradeOrderUpdate);

        //月卡订单
        ElectricityMemberCardOrder electricityMemberCardOrderUpdate = new ElectricityMemberCardOrder();
        electricityMemberCardOrderUpdate.setId(carMemberCardOrder.getId());
        electricityMemberCardOrderUpdate.setStatus(memberOrderStatus);
        electricityMemberCardOrderUpdate.setUpdateTime(System.currentTimeMillis());
        electricityMemberCardOrderMapper.updateById(electricityMemberCardOrderUpdate);

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
        //电池服务费订单
        InsuranceOrder insuranceOrder = insuranceOrderService.queryByOrderId(electricityTradeOrder.getOrderNo());
        if (ObjectUtil.isEmpty(insuranceOrder)) {
            log.error("NOTIFY_INSURANCE_ORDER ERROR ,NOT FOUND ELECTRICITY_DEPOSIT_ORDER ORDER_NO={}", electricityTradeOrder.getOrderNo());
            return Pair.of(false, "未找到订单!");
        }

        if (!ObjectUtil.equal(EleBatteryServiceFeeOrder.STATUS_INIT, insuranceOrder.getStatus())) {
            log.error("NOTIFY_INSURANCE_ORDER ERROR , ELECTRICITY_DEPOSIT_ORDER  STATUS IS NOT INIT, ORDER_NO={}", electricityTradeOrder.getOrderNo());
            return Pair.of(false, "押金订单已处理!");
        }

        FranchiseeInsurance franchiseeInsurance = franchiseeInsuranceService.queryByCache(insuranceOrder.getInsuranceId());
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
