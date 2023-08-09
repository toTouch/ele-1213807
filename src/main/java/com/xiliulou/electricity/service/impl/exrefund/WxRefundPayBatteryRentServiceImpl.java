package com.xiliulou.electricity.service.impl.exrefund;

import cn.hutool.core.util.IdUtil;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.constant.WechatPayConstant;
import com.xiliulou.electricity.dto.DivisionAccountOrderDTO;
import com.xiliulou.electricity.entity.BatteryMembercardRefundOrder;
import com.xiliulou.electricity.entity.ElectricityMemberCardOrder;
import com.xiliulou.electricity.entity.UserBatteryMemberCard;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.enums.DivisionAccountEnum;
import com.xiliulou.electricity.enums.PackageTypeEnum;
import com.xiliulou.electricity.mq.producer.DivisionAccountProducer;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.service.wxrefund.WxRefundPayService;
import com.xiliulou.pay.weixinv3.dto.WechatJsapiRefundOrderCallBackResource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    private DivisionAccountProducer divisionAccountProducer;

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
                userBatteryMemberCardService.unbindMembercardInfoByUid(userInfo.getUid());
                serviceFeeUserInfoService.deleteByUid(userInfo.getUid());
            } else {
                //退未使用的
                userBatteryMemberCardService.deductionExpireTime(userInfo.getUid(), electricityMemberCardOrder.getValidDays().longValue(), System.currentTimeMillis());
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
            divisionAccountOrderDTO.setOrderNo(electricityMemberCardOrder.getOrderId());
            divisionAccountOrderDTO.setType(PackageTypeEnum.PACKAGE_TYPE_BATTERY.getCode());
            divisionAccountOrderDTO.setDivisionAccountType(DivisionAccountEnum.DA_TYPE_REFUND.getCode());
            divisionAccountOrderDTO.setTraceId(IdUtil.simpleUUID());
            divisionAccountProducer.sendSyncMessage(JsonUtil.toJson(divisionAccountOrderDTO));
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

    @Override
    public String getOptType() {
        return null;
    }
}
