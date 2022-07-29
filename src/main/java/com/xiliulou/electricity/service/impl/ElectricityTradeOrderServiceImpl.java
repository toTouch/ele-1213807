package com.xiliulou.electricity.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiliulou.electricity.config.WechatConfig;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.mapper.ElectricityMemberCardOrderMapper;
import com.xiliulou.electricity.mapper.ElectricityTradeOrderMapper;
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
    FranchiseeUserInfoService franchiseeUserInfoService;
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
            log.error("NOTIFY_MEMBER_ORDER ERROR ,NOT FOUND ELECTRICITY_TRADE_ORDER ORDER_NO:{}", tradeOrderNo);
            return Pair.of(false, "未找到交易订单!");
        }
        if (ObjectUtil.notEqual(ElectricityTradeOrder.STATUS_INIT, electricityTradeOrder.getStatus())) {
            log.error("NOTIFY_MEMBER_ORDER ERROR , ELECTRICITY_TRADE_ORDER  STATUS IS NOT INIT, ORDER_NO:{}", tradeOrderNo);
            return Pair.of(false, "交易订单已处理");
        }

        //购卡订单
        ElectricityMemberCardOrder electricityMemberCardOrder = electricityMemberCardOrderMapper.selectByOrderNo(electricityTradeOrder.getOrderNo());
        if (ObjectUtil.isEmpty(electricityMemberCardOrder)) {
            log.error("NOTIFY_MEMBER_ORDER ERROR ,NOT FOUND ELECTRICITY_MEMBER_CARD_ORDER ORDER_NO:{}", electricityTradeOrder.getOrderNo());
            return Pair.of(false, "未找到订单!");
        }
        if (!ObjectUtil.equal(ElectricityMemberCardOrder.STATUS_INIT, electricityMemberCardOrder.getStatus())) {
            log.error("NOTIFY_MEMBER_ORDER ERROR , ELECTRICITY_MEMBER_CARD_ORDER  STATUS IS NOT INIT, ORDER_NO:{}", electricityTradeOrder.getOrderNo());
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
            log.error("NOTIFY REDULT PAY FAIL,ORDER_NO:{}" + tradeOrderNo);
        }

        //用户
        UserInfo userInfo = userInfoService.selectUserByUid(electricityMemberCardOrder.getUid());
        if (Objects.isNull(userInfo)) {
            log.error("NOTIFY  ERROR,NOT FOUND USERINFO,USERID:{},ORDER_NO:{}", electricityMemberCardOrder.getUid(), tradeOrderNo);
            return Pair.of(false, "未找到用户信息!");
        }

        //是否缴纳押金，是否绑定电池
        FranchiseeUserInfo franchiseeUserInfo = franchiseeUserInfoService.queryByUserInfoId(userInfo.getId());

        //未找到用户
        if (Objects.isNull(franchiseeUserInfo)) {
            log.error("payDeposit  ERROR! not found user! userId:{}", userInfo.getUid());
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

            FranchiseeUserInfo franchiseeUserInfoUpdate = new FranchiseeUserInfo();
            franchiseeUserInfoUpdate.setId(franchiseeUserInfo.getId());

            if (Objects.equals(electricityMemberCard.getLimitCount(), ElectricityMemberCard.UN_LIMITED_COUNT_TYPE)) {
                if (Objects.isNull(franchiseeUserInfo.getMemberCardExpireTime()) || franchiseeUserInfo.getMemberCardExpireTime() < now) {
                    memberCardExpireTime = System.currentTimeMillis() +
                            electricityMemberCardOrder.getValidDays() * (24 * 60 * 60 * 1000L);
                } else {
                    memberCardExpireTime = franchiseeUserInfo.getMemberCardExpireTime() +
                            electricityMemberCardOrder.getValidDays() * (24 * 60 * 60 * 1000L);
                }
            } else {
                if (Objects.isNull(franchiseeUserInfo.getMemberCardExpireTime()) || franchiseeUserInfo.getMemberCardExpireTime() < now || Objects.isNull(franchiseeUserInfo.getRemainingNumber()) || franchiseeUserInfo.getRemainingNumber() == 0) {
                    memberCardExpireTime = System.currentTimeMillis() +
                            electricityMemberCardOrder.getValidDays() * (24 * 60 * 60 * 1000L);
                } else {
                    memberCardExpireTime = franchiseeUserInfo.getMemberCardExpireTime() +
                            electricityMemberCardOrder.getValidDays() * (24 * 60 * 60 * 1000L);
                    remainingNumber = remainingNumber + franchiseeUserInfo.getRemainingNumber();
                }
            }
            franchiseeUserInfoUpdate.setMemberCardExpireTime(memberCardExpireTime);
            franchiseeUserInfoUpdate.setBatteryServiceFeeGenerateTime(memberCardExpireTime);
            franchiseeUserInfoUpdate.setRemainingNumber(remainingNumber);
//            franchiseeUserInfoUpdate.setRemainingNumber(electricityMemberCardOrder.getMaxUseCount());
            franchiseeUserInfoUpdate.setMemberCardDisableStatus(FranchiseeUserInfo.MEMBER_CARD_NOT_DISABLE);
            franchiseeUserInfoUpdate.setCardId(electricityMemberCardOrder.getMemberCardId());
            franchiseeUserInfoUpdate.setCardType(electricityMemberCardOrder.getMemberCardType());
            franchiseeUserInfoUpdate.setCardName(electricityMemberCardOrder.getCardName());
            franchiseeUserInfoUpdate.setBatteryServiceFeeStatus(FranchiseeUserInfo.STATUS_NOT_IS_SERVICE_FEE);
            franchiseeUserInfoUpdate.setUpdateTime(System.currentTimeMillis());
            franchiseeUserInfoService.update(franchiseeUserInfoUpdate);

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
            if (Objects.isNull(franchiseeUserInfo.getCardId())) {
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
            log.error("NOTIFY_MEMBER_ORDER ERROR ,NOT FOUND ELECTRICITY_TRADE_ORDER TRADE_ORDER_NO:{}", tradeOrderNo);
            return Pair.of(false, "未找到交易订单!");
        }
        if (ObjectUtil.notEqual(ElectricityTradeOrder.STATUS_INIT, electricityTradeOrder.getStatus())) {
            log.error("NOTIFY_MEMBER_ORDER ERROR , ELECTRICITY_TRADE_ORDER  STATUS IS NOT INIT, TRADE_ORDER_NO:{}", tradeOrderNo);
            return Pair.of(false, "交易订单已处理");
        }

        //押金订单
        EleDepositOrder eleDepositOrder = eleDepositOrderService.queryByOrderId(electricityTradeOrder.getOrderNo());
        if (ObjectUtil.isEmpty(eleDepositOrder)) {
            log.error("NOTIFY_DEPOSIT_ORDER ERROR ,NOT FOUND ELECTRICITY_DEPOSIT_ORDER ORDER_NO:{}", electricityTradeOrder.getOrderNo());
            return Pair.of(false, "未找到订单!");
        }

        if (!ObjectUtil.equal(EleDepositOrder.STATUS_INIT, eleDepositOrder.getStatus())) {
            log.error("NOTIFY_DEPOSIT_ORDER ERROR , ELECTRICITY_DEPOSIT_ORDER  STATUS IS NOT INIT, ORDER_NO:{}", electricityTradeOrder.getOrderNo());
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
            log.error("NOTIFY REDULT PAY FAIL,ORDER_NO:{}" + tradeOrderNo);
        }

        //用户
        UserInfo userInfo = userInfoService.selectUserByUid(eleDepositOrder.getUid());
        if (Objects.isNull(userInfo)) {
            log.error("NOTIFY  ERROR,NOT FOUND USERINFO,USERID:{},ORDER_NO:{}", eleDepositOrder.getUid(), tradeOrderNo);
            return Pair.of(false, "未找到用户信息!");
        }

        //是否缴纳押金，是否绑定电池
        FranchiseeUserInfo franchiseeUserInfo = franchiseeUserInfoService.queryByUserInfoId(userInfo.getId());

        //未找到用户
        if (Objects.isNull(franchiseeUserInfo)) {
            log.error("payDeposit  ERROR! not found user! userId:{}", userInfo.getUid());
            return Pair.of(false, "未找到用户信息!");

        }

        //用户押金
        if (Objects.equals(depositOrderStatus, EleDepositOrder.STATUS_SUCCESS)) {

            FranchiseeUserInfo franchiseeUserInfoUpdate = new FranchiseeUserInfo();
            franchiseeUserInfoUpdate.setId(franchiseeUserInfo.getId());
            franchiseeUserInfoUpdate.setServiceStatus(FranchiseeUserInfo.STATUS_IS_DEPOSIT);
            franchiseeUserInfoUpdate.setUpdateTime(System.currentTimeMillis());
            franchiseeUserInfoUpdate.setBatteryDeposit(eleDepositOrder.getPayAmount());
            franchiseeUserInfoUpdate.setOrderId(eleDepositOrder.getOrderId());
            franchiseeUserInfoUpdate.setFranchiseeId(eleDepositOrder.getFranchiseeId());

            franchiseeUserInfoUpdate.setModelType(eleDepositOrder.getModelType());

            if (Objects.equals(eleDepositOrder.getModelType(), Franchisee.MEW_MODEL_TYPE)) {
                franchiseeUserInfoUpdate.setBatteryType(eleDepositOrder.getBatteryType());
            }
            franchiseeUserInfoService.update(franchiseeUserInfoUpdate);
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
            log.error("NOTIFY_BATTERY_SERVICE_FEE_ORDER ERROR ,NOT FOUND ELECTRICITY_TRADE_ORDER TRADE_ORDER_NO:{}", tradeOrderNo);
            return Pair.of(false, "未找到交易订单!");
        }
        if (ObjectUtil.notEqual(ElectricityTradeOrder.STATUS_INIT, electricityTradeOrder.getStatus())) {
            log.error("NOTIFY_BATTERY_SERVICE_FEE_ORDER ERROR , ELECTRICITY_TRADE_ORDER  STATUS IS NOT INIT, TRADE_ORDER_NO:{}", tradeOrderNo);
            return Pair.of(false, "交易订单已处理");
        }
        //电池服务费订单
        EleBatteryServiceFeeOrder eleBatteryServiceFeeOrder = eleBatteryServiceFeeOrderService.queryEleBatteryServiceFeeOrderByOrderId(electricityTradeOrder.getOrderNo());
        if (ObjectUtil.isEmpty(eleBatteryServiceFeeOrder)) {
            log.error("NOTIFY_BATTERY_SERVICE_FEE_ORDER ERROR ,NOT FOUND ELECTRICITY_DEPOSIT_ORDER ORDER_NO:{}", electricityTradeOrder.getOrderNo());
            return Pair.of(false, "未找到订单!");
        }

        if (!ObjectUtil.equal(EleBatteryServiceFeeOrder.STATUS_INIT, eleBatteryServiceFeeOrder.getStatus())) {
            log.error("NOTIFY_BATTERY_SERVICE_FEE_ORDER ERROR , ELECTRICITY_DEPOSIT_ORDER  STATUS IS NOT INIT, ORDER_NO:{}", electricityTradeOrder.getOrderNo());
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
        UserInfo userInfo = userInfoService.selectUserByUid(eleBatteryServiceFeeOrder.getUid());
        if (Objects.isNull(userInfo)) {
            log.error("NOTIFY  ERROR,NOT FOUND USERINFO,USERID:{},ORDER_NO:{}", eleBatteryServiceFeeOrder.getUid(), tradeOrderNo);
            return Pair.of(false, "未找到用户信息!");
        }

        FranchiseeUserInfo franchiseeUserInfo = franchiseeUserInfoService.queryByUserInfoId(userInfo.getId());

        if (Objects.equals(eleBatteryServiceFeeOrderStatus, EleDepositOrder.STATUS_SUCCESS)) {
            FranchiseeUserInfo franchiseeUserInfoUpdate = new FranchiseeUserInfo();
            franchiseeUserInfoUpdate.setId(franchiseeUserInfo.getId());
            franchiseeUserInfoUpdate.setBatteryServiceFeeStatus(FranchiseeUserInfo.STATUS_IS_SERVICE_FEE);
            if (Objects.equals(franchiseeUserInfo.getMemberCardDisableStatus(), FranchiseeUserInfo.MEMBER_CARD_DISABLE)) {
                franchiseeUserInfoUpdate.setDisableMemberCardTime(System.currentTimeMillis() + (1000L * 60 * 60 * 1));
            } else {
                franchiseeUserInfoUpdate.setBatteryServiceFeeGenerateTime(System.currentTimeMillis());
            }
            franchiseeUserInfoUpdate.setUpdateTime(System.currentTimeMillis());
            franchiseeUserInfoService.update(franchiseeUserInfoUpdate);
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
        eleBatteryServiceFeeOrderUpdate.setBatteryServiceFeeGenerateTime(franchiseeUserInfo.getBatteryServiceFeeGenerateTime());
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
            log.error("NOTIFY_RENT_CAR_DEPOSIT_ORDER ERROR ,NOT FOUND ELECTRICITY_TRADE_ORDER TRADE_ORDER_NO:{}", tradeOrderNo);
            return Pair.of(false, "未找到交易订单!");
        }
        if (ObjectUtil.notEqual(ElectricityTradeOrder.STATUS_INIT, electricityTradeOrder.getStatus())) {
            log.error("NOTIFY_RENT_CAR_DEPOSIT_ORDER ERROR , ELECTRICITY_TRADE_ORDER  STATUS IS NOT INIT, TRADE_ORDER_NO:{}", tradeOrderNo);
            return Pair.of(false, "交易订单已处理");
        }

        //押金订单
        EleDepositOrder eleDepositOrder = eleDepositOrderService.queryByOrderId(electricityTradeOrder.getOrderNo());
        if (ObjectUtil.isEmpty(eleDepositOrder)) {
            log.error("NOTIFY_RENT_CAR_DEPOSIT_ORDER ERROR ,NOT FOUND ELECTRICITY_DEPOSIT_ORDER ORDER_NO:{}", electricityTradeOrder.getOrderNo());
            return Pair.of(false, "未找到订单!");
        }

        if (!ObjectUtil.equal(EleDepositOrder.STATUS_INIT, eleDepositOrder.getStatus())) {
            log.error("NOTIFY_RENT_CAR_DEPOSIT_ORDER ERROR , ELECTRICITY_DEPOSIT_ORDER  STATUS IS NOT INIT, ORDER_NO:{}", electricityTradeOrder.getOrderNo());
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
            log.error("NOTIFY REDULT PAY FAIL,ORDER_NO:{}" + tradeOrderNo);
        }

        //用户
        UserInfo userInfo = userInfoService.selectUserByUid(eleDepositOrder.getUid());
        if (Objects.isNull(userInfo)) {
            log.error("NOTIFY  ERROR,NOT FOUND USERINFO,USERID:{},ORDER_NO:{}", eleDepositOrder.getUid(), tradeOrderNo);
            return Pair.of(false, "未找到用户信息!");
        }

        //是否缴纳押金，是否绑定电池
        FranchiseeUserInfo franchiseeUserInfo = franchiseeUserInfoService.queryByUserInfoId(userInfo.getId());

        //未找到用户
        if (Objects.isNull(franchiseeUserInfo)) {
            log.error("payDeposit  ERROR! not found user! userId:{}", userInfo.getUid());
            return Pair.of(false, "未找到用户信息!");
        }

        //用户押金
        if (Objects.equals(depositOrderStatus, EleDepositOrder.STATUS_SUCCESS)) {
            FranchiseeUserInfo franchiseeUserInfoUpdate = new FranchiseeUserInfo();
            franchiseeUserInfoUpdate.setId(franchiseeUserInfo.getId());
            franchiseeUserInfoUpdate.setRentCarStatus(FranchiseeUserInfo.RENT_CAR_STATUS_IS_DEPOSIT);
            franchiseeUserInfoUpdate.setRentCarDeposit(eleDepositOrder.getPayAmount());
            franchiseeUserInfoUpdate.setRentCarOrderId(eleDepositOrder.getOrderId());
            franchiseeUserInfoUpdate.setUpdateTime(System.currentTimeMillis());
            franchiseeUserInfoUpdate.setBindCarModelId(eleDepositOrder.getCarModelId());
            franchiseeUserInfoService.update(franchiseeUserInfoUpdate);
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
    public Pair<Boolean, Object> notifyRentCarMemberOrder(WechatJsapiOrderCallBackResource callBackResource) {

        //回调参数
        String tradeOrderNo = callBackResource.getOutTradeNo();
        String tradeState = callBackResource.getTradeState();
        String transactionId = callBackResource.getTransactionId();

        //交易订单
        ElectricityTradeOrder electricityTradeOrder = baseMapper.selectTradeOrderByTradeOrderNo(tradeOrderNo);
        if (Objects.isNull(electricityTradeOrder)) {
            log.error("NOTIFY_MEMBER_ORDER ERROR ,NOT FOUND ELECTRICITY_TRADE_ORDER ORDER_NO:{}", tradeOrderNo);
            return Pair.of(false, "未找到交易订单!");
        }
        if (ObjectUtil.notEqual(ElectricityTradeOrder.STATUS_INIT, electricityTradeOrder.getStatus())) {
            log.error("NOTIFY_MEMBER_ORDER ERROR , ELECTRICITY_TRADE_ORDER  STATUS IS NOT INIT, ORDER_NO:{}", tradeOrderNo);
            return Pair.of(false, "交易订单已处理");
        }

        //购卡订单
        ElectricityMemberCardOrder electricityMemberCardOrder = electricityMemberCardOrderMapper.selectByOrderNo(electricityTradeOrder.getOrderNo());
        if (ObjectUtil.isEmpty(electricityMemberCardOrder)) {
            log.error("NOTIFY_MEMBER_ORDER ERROR ,NOT FOUND ELECTRICITY_MEMBER_CARD_ORDER ORDER_NO:{}", electricityTradeOrder.getOrderNo());
            return Pair.of(false, "未找到订单!");
        }
        if (!ObjectUtil.equal(ElectricityMemberCardOrder.STATUS_INIT, electricityMemberCardOrder.getStatus())) {
            log.error("NOTIFY_MEMBER_ORDER ERROR , ELECTRICITY_MEMBER_CARD_ORDER  STATUS IS NOT INIT, ORDER_NO:{}", electricityTradeOrder.getOrderNo());
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
            log.error("NOTIFY REDULT PAY FAIL,ORDER_NO:{}" + tradeOrderNo);
        }

        //用户
        UserInfo userInfo = userInfoService.selectUserByUid(electricityMemberCardOrder.getUid());
        if (Objects.isNull(userInfo)) {
            log.error("NOTIFY  ERROR,NOT FOUND USERINFO,USERID:{},ORDER_NO:{}", electricityMemberCardOrder.getUid(), tradeOrderNo);
            return Pair.of(false, "未找到用户信息!");
        }

        //是否缴纳押金，是否绑定电池
        FranchiseeUserInfo franchiseeUserInfo = franchiseeUserInfoService.queryByUserInfoId(userInfo.getId());

        //未找到用户
        if (Objects.isNull(franchiseeUserInfo)) {
            log.error("payDeposit  ERROR! not found user! userId:{}", userInfo.getUid());
            return Pair.of(false, "未找到用户信息!");
        }
        Long now = System.currentTimeMillis();
        Long memberCardExpireTime;
        if (Objects.equals(memberOrderStatus, EleDepositOrder.STATUS_SUCCESS)) {

            if (Objects.isNull(franchiseeUserInfo.getRentCarMemberCardExpireTime()) || franchiseeUserInfo.getRentCarMemberCardExpireTime() < now) {
                memberCardExpireTime = System.currentTimeMillis() +
                        electricityMemberCardOrder.getValidDays() * (24 * 60 * 60 * 1000L);
            } else {
                memberCardExpireTime = franchiseeUserInfo.getRentCarMemberCardExpireTime() +
                        electricityMemberCardOrder.getValidDays() * (24 * 60 * 60 * 1000L);
            }
            FranchiseeUserInfo franchiseeUserInfoUpdate = new FranchiseeUserInfo();
            franchiseeUserInfoUpdate.setId(franchiseeUserInfo.getId());
            franchiseeUserInfoUpdate.setRentCarMemberCardExpireTime(memberCardExpireTime);
            franchiseeUserInfoUpdate.setRentCarCardId(electricityMemberCardOrder.getMemberCardId());
            franchiseeUserInfoUpdate.setUpdateTime(System.currentTimeMillis());
            franchiseeUserInfoService.update(franchiseeUserInfoUpdate);
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

    @Override
    public ElectricityTradeOrder selectTradeOrderByTradeOrderNo(String outTradeNo) {
        return baseMapper.selectTradeOrderByTradeOrderNo(outTradeNo);
    }

    @Override
    public ElectricityTradeOrder selectTradeOrderByOrderId(String orderId) {
        return baseMapper.selectTradeOrderByOrderId(orderId);
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
