package com.xiliulou.electricity.service.impl.exrefund;

import cn.hutool.core.util.IdUtil;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.constant.MerchantConstant;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.constant.WechatPayConstant;
import com.xiliulou.electricity.dto.DivisionAccountOrderDTO;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.enums.DivisionAccountEnum;
import com.xiliulou.electricity.enums.PackageTypeEnum;
import com.xiliulou.electricity.mq.constant.MqProducerConstant;
import com.xiliulou.electricity.mq.model.BatteryMemberCardMerchantRebate;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.service.wxrefund.WxRefundPayService;
import com.xiliulou.mq.service.RocketMqService;
import com.xiliulou.pay.weixinv3.dto.WechatJsapiRefundOrderCallBackResource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-07-29-9:59
 */
@Slf4j
@Service("wxRefundPayBatteryRentServiceImpl")
public class WxRefundPayBatteryRentServiceImpl implements WxRefundPayService {

    @Autowired
    private RedisService redisService;

    @Autowired
    private BatteryMembercardRefundOrderService batteryMembercardRefundOrderService;

    @Autowired
    private ElectricityMemberCardOrderService electricityMemberCardOrderService;

    @Autowired
    private UserInfoService userInfoService;

    @Autowired
    private UserBatteryMemberCardService userBatteryMemberCardService;

    @Autowired
    private UserBatteryMemberCardPackageService userBatteryMemberCardPackageService;

    @Autowired
    private ServiceFeeUserInfoService serviceFeeUserInfoService;

    @Autowired
    private DivisionAccountRecordService divisionAccountRecordService;

    @Autowired
    private BatteryMemberCardService batteryMemberCardService;
    
    @Autowired
    private UserInfoExtraService userInfoExtraService;
    
    @Autowired
    private RocketMqService rocketMqService;


    @Override
    public void process(WechatJsapiRefundOrderCallBackResource callBackResource) {

        String refundOrderNo = callBackResource.getOutTradeNo();
        if (!redisService.setNx(WechatPayConstant.REFUND_ORDER_ID_CALL_BACK + refundOrderNo, String.valueOf(System.currentTimeMillis()), 10 * 1000L, false)) {
            return;
        }

        BatteryMembercardRefundOrder batteryMembercardRefundOrder = batteryMembercardRefundOrderService.selectByRefundOrderNo(callBackResource.getOutRefundNo());
        if (Objects.isNull(batteryMembercardRefundOrder)) {
            log.error("BATTERY MEMBERCARD REFUND ERROR!not found batteryMembercardRefundOrder,refundOrderNo={}", callBackResource.getOutRefundNo());
            return;
        }

        if (Objects.equals(batteryMembercardRefundOrder.getStatus(), BatteryMembercardRefundOrder.STATUS_SUCCESS)) {
            log.error("BATTERY MEMBERCARD REFUND ERROR!order status illegal,refundOrderNo={}", callBackResource.getOutRefundNo());
            return;
        }

        BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(batteryMembercardRefundOrder.getMid());
        if(Objects.isNull(batteryMemberCard)){
            log.error("BATTERY MEMBERCARD REFUND ERROR!not found batteryMemberCard,mid={},refundOrderNo={}",batteryMembercardRefundOrder.getMid(),batteryMembercardRefundOrder.getRefundOrderNo());
            return ;
        }

        String memberCardOrderNo = batteryMembercardRefundOrder.getMemberCardOrderNo();

        ElectricityMemberCardOrder electricityMemberCardOrder = electricityMemberCardOrderService.selectByOrderNo(memberCardOrderNo);
        if (Objects.isNull(electricityMemberCardOrder)) {
            log.error("BATTERY MEMBERCARD REFUND ERROR!not found electricityMemberCardOrder,memberCardOrderNo={}", memberCardOrderNo);
            return;
        }

        UserInfo userInfo = userInfoService.queryByUidFromCache(electricityMemberCardOrder.getUid());
        if (Objects.isNull(userInfo)) {
            log.error("BATTERY MEMBERCARD REFUND ERROR!not found userInfo,uid={},memberCardOrderNo={}", electricityMemberCardOrder.getUid(), memberCardOrderNo);
            return;
        }

        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(userInfo.getUid());
        if (Objects.isNull(userBatteryMemberCard)) {
            log.error("BATTERY MEMBERCARD REFUND ERROR!not found userBatteryMemberCard,uid={},memberCardOrderNo={}", electricityMemberCardOrder.getUid(), memberCardOrderNo);
            return;
        }

        Integer status = StringUtils.isNotBlank(callBackResource.getRefundStatus()) && Objects.equals(callBackResource.getRefundStatus(), "SUCCESS") ? BatteryMembercardRefundOrder.STATUS_SUCCESS : BatteryMembercardRefundOrder.STATUS_FAIL;
        if (Objects.equals(status, BatteryMembercardRefundOrder.STATUS_SUCCESS)) {
            if (Objects.equals(userBatteryMemberCard.getOrderId(), memberCardOrderNo)) {
                //退使用中的
                List<UserBatteryMemberCardPackage> userBatteryMemberCardPackages = userBatteryMemberCardPackageService.selectByUid(userBatteryMemberCard.getUid());
                if (CollectionUtils.isEmpty(userBatteryMemberCardPackages)) {
                    //退最后一个套餐
                    userBatteryMemberCardService.unbindMembercardInfoByUid(userInfo.getUid());
                    serviceFeeUserInfoService.unbindServiceFeeInfoByUid(userInfo.getUid());
                } else {
                    UserBatteryMemberCard userBatteryMemberCardUpdate = new UserBatteryMemberCard();
                    userBatteryMemberCardUpdate.setUid(userBatteryMemberCard.getUid());
                    userBatteryMemberCardUpdate.setOrderExpireTime(System.currentTimeMillis());
                    userBatteryMemberCardUpdate.setOrderRemainingNumber(NumberConstant.ZERO_L);
                    userBatteryMemberCardUpdate.setRemainingNumber(userBatteryMemberCard.getRemainingNumber() - userBatteryMemberCard.getOrderRemainingNumber());
                    userBatteryMemberCardUpdate.setMemberCardExpireTime(userBatteryMemberCard.getMemberCardExpireTime() - (userBatteryMemberCard.getOrderExpireTime() - System.currentTimeMillis()));
                    userBatteryMemberCardUpdate.setUpdateTime(System.currentTimeMillis());
                    userBatteryMemberCardService.updateByUid(userBatteryMemberCardUpdate);

                    ServiceFeeUserInfo serviceFeeUserInfo = serviceFeeUserInfoService.queryByUidFromCache(userBatteryMemberCard.getUid());
                    ServiceFeeUserInfo serviceFeeUserInfoUpdate = new ServiceFeeUserInfo();
                    serviceFeeUserInfoUpdate.setUid(userBatteryMemberCard.getUid());
                    serviceFeeUserInfoUpdate.setServiceFeeGenerateTime(serviceFeeUserInfo.getServiceFeeGenerateTime() - (userBatteryMemberCard.getOrderExpireTime() - System.currentTimeMillis()));
                    serviceFeeUserInfoUpdate.setUpdateTime(System.currentTimeMillis());
                    serviceFeeUserInfoService.updateByUid(serviceFeeUserInfo);
                }
            } else {

                long deductionExpireTime = 0L;
                if (Objects.equals(batteryMemberCard.getRentUnit(), BatteryMemberCard.RENT_UNIT_DAY)) {
                    deductionExpireTime = electricityMemberCardOrder.getValidDays() * 24 * 60 * 60 * 1000L;
                } else {
                    deductionExpireTime = electricityMemberCardOrder.getValidDays() * 60 * 1000L;
                }

                //退未使用的
                UserBatteryMemberCard userBatteryMemberCardUpdate = new UserBatteryMemberCard();
                userBatteryMemberCardUpdate.setUid(userBatteryMemberCard.getUid());
                userBatteryMemberCardUpdate.setRemainingNumber(userBatteryMemberCard.getRemainingNumber() - electricityMemberCardOrder.getMaxUseCount());
                userBatteryMemberCardUpdate.setMemberCardExpireTime(userBatteryMemberCard.getMemberCardExpireTime() - deductionExpireTime);
                userBatteryMemberCardUpdate.setUpdateTime(System.currentTimeMillis());
                userBatteryMemberCardService.updateByUid(userBatteryMemberCardUpdate);
                userBatteryMemberCardPackageService.deleteByOrderId(electricityMemberCardOrder.getOrderId());
            }

            BatteryMembercardRefundOrder batteryMembercardRefundOrderUpdate = new BatteryMembercardRefundOrder();
            batteryMembercardRefundOrderUpdate.setId(batteryMembercardRefundOrder.getId());
            batteryMembercardRefundOrderUpdate.setStatus(BatteryMembercardRefundOrder.STATUS_SUCCESS);
            batteryMembercardRefundOrderUpdate.setUpdateTime(System.currentTimeMillis());
            batteryMembercardRefundOrderService.update(batteryMembercardRefundOrderUpdate);

            ElectricityMemberCardOrder electricityMemberCardOrderUpdate = new ElectricityMemberCardOrder();
            electricityMemberCardOrderUpdate.setId(electricityMemberCardOrder.getId());
            electricityMemberCardOrderUpdate.setUseStatus(ElectricityMemberCardOrder.USE_STATUS_REFUND);
            electricityMemberCardOrderUpdate.setRefundStatus(ElectricityMemberCardOrder.REFUND_STATUS_SUCCESS);
            electricityMemberCardOrderUpdate.setUpdateTime(System.currentTimeMillis());
            electricityMemberCardOrderService.updateByID(electricityMemberCardOrderUpdate);

            //更新套餐绑定的优惠券为已失效
            batteryMembercardRefundOrderService.updateUserCouponStatus(electricityMemberCardOrder.getOrderId());

            // 8. 处理分账
            DivisionAccountOrderDTO divisionAccountOrderDTO = new DivisionAccountOrderDTO();
            divisionAccountOrderDTO.setOrderNo(batteryMembercardRefundOrder.getRefundOrderNo());
            divisionAccountOrderDTO.setType(PackageTypeEnum.PACKAGE_TYPE_BATTERY.getCode());
            divisionAccountOrderDTO.setDivisionAccountType(DivisionAccountEnum.DA_TYPE_REFUND.getCode());
            divisionAccountOrderDTO.setTraceId(IdUtil.simpleUUID());
            divisionAccountRecordService.asyncHandleDivisionAccount(divisionAccountOrderDTO);
        
            //退租 发送返利MQ
            sendMerchantRebateRefundMQ(batteryMembercardRefundOrder.getUid(),batteryMembercardRefundOrder.getRefundOrderNo());
        
        } else {
            BatteryMembercardRefundOrder batteryMembercardRefundOrderUpdate = new BatteryMembercardRefundOrder();
            batteryMembercardRefundOrderUpdate.setId(batteryMembercardRefundOrder.getId());
            batteryMembercardRefundOrderUpdate.setStatus(BatteryMembercardRefundOrder.STATUS_FAIL);
            batteryMembercardRefundOrderUpdate.setUpdateTime(System.currentTimeMillis());
            batteryMembercardRefundOrderService.update(batteryMembercardRefundOrderUpdate);

            ElectricityMemberCardOrder electricityMemberCardOrderUpdate = new ElectricityMemberCardOrder();
            electricityMemberCardOrderUpdate.setId(electricityMemberCardOrder.getId());
            electricityMemberCardOrderUpdate.setRefundStatus(ElectricityMemberCardOrder.REFUND_STATUS_FAIL);
            electricityMemberCardOrderUpdate.setUpdateTime(System.currentTimeMillis());
            electricityMemberCardOrderService.updateByID(electricityMemberCardOrderUpdate);
        }
    }
    
    private void sendMerchantRebateRefundMQ(Long uid, String orderId) {
        UserInfoExtra userInfoExtra = userInfoExtraService.queryByUidFromCache(uid);
        if(Objects.isNull(userInfoExtra)){
            log.warn("BATTERY MERCHANT REBATE REFUND WARN!userInfoExtra is null,uid={}",uid);
            return;
        }
        
        if(Objects.isNull(userInfoExtra.getMerchantId())){
            log.warn("BATTERY MERCHANT REBATE REFUND WARN!merchantId is null,uid={}",uid);
            return;
        }
        
        BatteryMemberCardMerchantRebate merchantRebate = new BatteryMemberCardMerchantRebate();
        merchantRebate.setUid(uid);
        merchantRebate.setOrderId(orderId);
        merchantRebate.setType(MerchantConstant.TYPE_REFUND);
        //续费成功  发送返利退费MQ
        rocketMqService.sendAsyncMsg(MqProducerConstant.BATTERY_MEMBER_CARD_MERCHANT_REBATE_TOPIC, JsonUtil.toJson(merchantRebate));
    }

    @Override
    public String getOptType() {
        return null;
    }
}
