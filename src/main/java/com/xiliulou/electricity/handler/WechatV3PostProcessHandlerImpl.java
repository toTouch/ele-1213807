package com.xiliulou.electricity.handler;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.constant.WechatPayConstant;
import com.xiliulou.electricity.entity.ElectricityTradeOrder;
import com.xiliulou.electricity.entity.UnionTradeOrder;
import com.xiliulou.electricity.enums.CallBackEnums;
import com.xiliulou.electricity.service.EleRefundOrderService;
import com.xiliulou.electricity.service.ElectricityTradeOrderService;
import com.xiliulou.electricity.service.UnionTradeOrderService;
import com.xiliulou.pay.weixinv3.dto.WechatJsapiOrderCallBackResource;
import com.xiliulou.pay.weixinv3.dto.WechatJsapiRefundOrderCallBackResource;
import com.xiliulou.pay.weixinv3.query.*;
import com.xiliulou.pay.weixinv3.service.WechatV3MerchantLoadAndUpdateCertificateService;
import com.xiliulou.pay.weixinv3.service.WechatV3PostProcessHandler;
import com.xiliulou.pay.weixinv3.util.AesUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * @author: eclair
 * @Date: 2021/3/18 19:10
 * @Description:
 */
@Service
@Slf4j
public class WechatV3PostProcessHandlerImpl implements WechatV3PostProcessHandler {

    @Autowired
    WechatV3MerchantLoadAndUpdateCertificateService certificateService;


    @Autowired
    RedisService redisService;

    @Autowired
    ElectricityTradeOrderService electricityTradeOrderService;

    @Autowired
    EleRefundOrderService eleRefundOrderService;

    @Autowired
    UnionTradeOrderService unionTradeOrderService;

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public void postProcessBeforeWechatPay(WechatV3OrderQuery wechatV3OrderQuery) {
        //暂时什么都不处理 TODO
    }

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public void postProcessAfterWechatPay(WechatV3OrderCallBackQuery wechatV3OrderCallBackQuery) {
        WechatCallBackResouceData resource = wechatV3OrderCallBackQuery.getResource();
        if (Objects.isNull(resource)) {
            log.error("WECHAT ERROR! no wechat's info ! msg={}", wechatV3OrderCallBackQuery);
            return;
        }

        String decryptJson = null;
        try {
            decryptJson = AesUtil.decryptToString(resource.getAssociated_data().getBytes(StandardCharsets.UTF_8),
                    resource.getNonce().getBytes(StandardCharsets.UTF_8),
                    resource.getCiphertext(),
                    certificateService.getMerchantApiV3Key(wechatV3OrderCallBackQuery.getTenantId()).getBytes(StandardCharsets.UTF_8));

        } catch (Exception e) {
            log.error("WECHAT ERROR! wechat decrypt error! msg={}", wechatV3OrderCallBackQuery, e);
            return;
        }

        WechatJsapiOrderCallBackResource callBackResource = JsonUtil.fromJson(decryptJson, WechatJsapiOrderCallBackResource.class);

        //幂等加锁
        String orderNo = callBackResource.getOutTradeNo();
        if (!redisService.setNx(WechatPayConstant.PAY_ORDER_ID_CALL_BACK + orderNo, String.valueOf(System.currentTimeMillis()), 10 * 1000L, false)) {
            return;
        }

        log.info("WECHAT INFO! orderNo={}", orderNo);

        if (Objects.equals(callBackResource.getAttach(), ElectricityTradeOrder.ATTACH_DEPOSIT)) {
            electricityTradeOrderService.notifyDepositOrder(callBackResource);
        } else if (Objects.equals(callBackResource.getAttach(), ElectricityTradeOrder.ATTACH_BATTERY_SERVICE_FEE)) {
            electricityTradeOrderService.notifyBatteryServiceFeeOrder(callBackResource);
        } else if (Objects.equals(callBackResource.getAttach(), ElectricityTradeOrder.ATTACH_RENT_CAR_DEPOSIT)) {
            electricityTradeOrderService.notifyRentCarDepositOrder(callBackResource);
        } else if (Objects.equals(callBackResource.getAttach(), ElectricityTradeOrder.ATTACH_RENT_CAR_MEMBER_CARD)) {
            electricityTradeOrderService.notifyRentCarMemberOrder(callBackResource);
        } else if (Objects.equals(callBackResource.getAttach(), CallBackEnums.CAR_RENAL_PACKAGE_ORDER.getDesc())) {
            // 租车套餐购买订单回调
            electricityTradeOrderService.notifyCarRenalPackageOrder(callBackResource);
        } else if (Objects.equals(callBackResource.getAttach(), ElectricityTradeOrder.ATTACH_INSURANCE)) {
            electricityTradeOrderService.notifyInsuranceOrder(callBackResource);
        } else if (Objects.equals(callBackResource.getAttach(), UnionTradeOrder.ATTACH_INTEGRATED_PAYMENT)) {
            unionTradeOrderService.notifyIntegratedPayment(callBackResource);
        } else if(Objects.equals(callBackResource.getAttach(), UnionTradeOrder.ATTACH_MEMBERCARD_INSURANCE)){
            unionTradeOrderService.notifyMembercardInsurance(callBackResource);
        }else if (Objects.equals(callBackResource.getAttach(), UnionTradeOrder.ATTACH_SERVUCE_FEE)){
            unionTradeOrderService.notifyServiceFee(callBackResource);
        }else if (Objects.equals(callBackResource.getAttach(), ElectricityTradeOrder.ATTACH_CLOUD_BEAN_RECHARGE)){
            electricityTradeOrderService.notifyCloudBeanRechargeOrder(callBackResource);
        }else if (Objects.equals(callBackResource.getAttach(), ElectricityTradeOrder.ATTACH_PURCHASE_ENTERPRISE_PACKAGE)){
            //企业渠道购买换电套餐回调
            electricityTradeOrderService.notifyPurchaseEnterprisePackageOrder(callBackResource);
        }else if (Objects.equals(callBackResource.getAttach(), UnionTradeOrder.ATTACH_ENTERPRISE_PACKAGE_DEPOSIT_PAYMENT)) {
            //企业渠道购买换电套餐+押金回调
            unionTradeOrderService.notifyEnterprisePackageAndDepositOrder(callBackResource);
        }else if(Objects.equals(callBackResource.getAttach(), UnionTradeOrder.ATTACH_ENTERPRISE_PACKAGE_WITHOUT_DEPOSIT_PAYMENT)){
            //企业渠道免押购买换电套餐回调
            //TODO
        } else {
            electricityTradeOrderService.notifyMemberOrder(callBackResource);
        }
    }


    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public void postProcessBeforeWechatRefund(WechatV3RefundQuery wechatV3RefundQuery) {
        return;
    }

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public void postProcessAfterWechatRefund(WechatV3RefundOrderCallBackQuery wechatV3RefundOrderCallBackQuery) {
        WechatCallBackResouceData resource = wechatV3RefundOrderCallBackQuery.getResource();
        if (Objects.isNull(resource)) {
            log.error("WECHAT ERROR! no wechat's info ! msg={}", wechatV3RefundOrderCallBackQuery);
            return;
        }

        String decryptJson = null;
        try {
            decryptJson = AesUtil.decryptToString(resource.getAssociated_data().getBytes(StandardCharsets.UTF_8),
                    resource.getNonce().getBytes(StandardCharsets.UTF_8),
                    resource.getCiphertext(),
                    certificateService.getMerchantApiV3Key(wechatV3RefundOrderCallBackQuery.getTenantId()).getBytes(StandardCharsets.UTF_8));

        } catch (Exception e) {
            log.error("WECHAT ERROR! wechat decrypt error! msg={}", wechatV3RefundOrderCallBackQuery, e);
            return;
        }

        WechatJsapiRefundOrderCallBackResource callBackResource = JsonUtil.fromJson(decryptJson, WechatJsapiRefundOrderCallBackResource.class);

        //幂等加锁
        String orderNo = callBackResource.getOutTradeNo();
        if (!redisService.setNx(WechatPayConstant.REFUND_ORDER_ID_CALL_BACK + orderNo, String.valueOf(System.currentTimeMillis()), 10 * 1000L, false)) {
            return;
        }


        eleRefundOrderService.notifyDepositRefundOrder(callBackResource);

    }

}
