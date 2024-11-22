/**
 * Create date: 2024/6/13
 */

package com.xiliulou.electricity.handler;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.constant.WechatPayConstant;
import com.xiliulou.electricity.entity.ElectricityPayParams;
import com.xiliulou.electricity.entity.ElectricityTradeOrder;
import com.xiliulou.electricity.entity.UnionTradeOrder;
import com.xiliulou.electricity.enums.CallBackEnums;
import com.xiliulou.electricity.service.EleRefundOrderService;
import com.xiliulou.electricity.service.ElectricityPayParamsService;
import com.xiliulou.electricity.service.ElectricityTradeOrderService;
import com.xiliulou.electricity.service.UnionTradeOrderService;
import com.xiliulou.electricity.service.installment.InstallmentRecordService;
import com.xiliulou.pay.weixinv3.dto.WechatJsapiOrderCallBackResource;
import com.xiliulou.pay.weixinv3.dto.WechatJsapiRefundOrderCallBackResource;
import com.xiliulou.pay.weixinv3.query.WechatCallBackResouceData;
import com.xiliulou.pay.weixinv3.service.WechatV3PostProcessHandler;
import com.xiliulou.pay.weixinv3.util.AesUtil;
import com.xiliulou.pay.weixinv3.v2.handler.WechatV3PostProcessExecuteHandler;
import com.xiliulou.pay.weixinv3.v2.query.WechatV3OrderCallBackRequest;
import com.xiliulou.pay.weixinv3.v2.query.WechatV3OrderRequest;
import com.xiliulou.pay.weixinv3.v2.query.WechatV3RefundOrderCallBackRequest;
import com.xiliulou.pay.weixinv3.v2.query.WechatV3RefundRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * description:
 *
 * @author caobotao.cbt
 * @date 2024/6/13 11:16
 */
@Slf4j
@Service
public class WechatV3FranchiseePostProcessHandlerImpl implements WechatV3PostProcessExecuteHandler {
    
    @Autowired
    ElectricityPayParamsService electricityPayParamsService;
    
    @Autowired
    RedisService redisService;
    
    @Autowired
    ElectricityTradeOrderService electricityTradeOrderService;
    
    @Autowired
    EleRefundOrderService eleRefundOrderService;
    
    @Autowired
    UnionTradeOrderService unionTradeOrderService;
    
    @Override
    public void postProcessBeforeWechatPay(WechatV3OrderRequest request) {
        //暂时什么都不处理 TODO
    }
    
    @Override
    public void postProcessAfterWechatPay(WechatV3OrderCallBackRequest wechatV3OrderCallBackQuery) {
        WechatCallBackResouceData resource = wechatV3OrderCallBackQuery.getResource();
        if (Objects.isNull(resource)) {
            log.error("WECHAT ERROR! no wechat's info ! msg={}", wechatV3OrderCallBackQuery);
            return;
        }
        
        String apiV3Key = this.getApiV3Key(wechatV3OrderCallBackQuery.getTenantId(), wechatV3OrderCallBackQuery.getFranchiseeId());
        
        String decryptJson = null;
        try {
            decryptJson = AesUtil
                    .decryptToString(resource.getAssociated_data().getBytes(StandardCharsets.UTF_8), resource.getNonce().getBytes(StandardCharsets.UTF_8), resource.getCiphertext(),
                            apiV3Key.getBytes(StandardCharsets.UTF_8));
            
        } catch (Exception e) {
            log.error("WECHAT ERROR! wechat decrypt error! msg={}", wechatV3OrderCallBackQuery, e);
            return;
        }
        
        WechatJsapiOrderCallBackResource callBackResource = JsonUtil.fromJson(decryptJson, WechatJsapiOrderCallBackResource.class);
        
        //幂等加锁
        String orderNo = callBackResource.getOutTradeNo();
        if (!redisService.setNx(WechatPayConstant.PAY_ORDER_ID_CALL_BACK + orderNo, String.valueOf(System.currentTimeMillis()), 10 * 1000L, false)) {
            log.info("ELE INFO! order in process orderId={}", orderNo);
            return;
        }
        
        if (Objects.equals(callBackResource.getAttach(), CallBackEnums.CAR_RENAL_PACKAGE_ORDER.getDesc())) {
            // 租车套餐购买订单回调
            electricityTradeOrderService.notifyCarRenalPackageOrder(callBackResource);
        } else if (Objects.equals(callBackResource.getAttach(), ElectricityTradeOrder.ATTACH_INSURANCE)) {
            //保险支付回调
            electricityTradeOrderService.notifyInsuranceOrder(callBackResource);
        } else if (Objects.equals(callBackResource.getAttach(), UnionTradeOrder.ATTACH_INTEGRATED_PAYMENT)) {
            //换电套餐押金支付回调
            unionTradeOrderService.notifyIntegratedPayment(callBackResource);
        } else if (Objects.equals(callBackResource.getAttach(), UnionTradeOrder.ATTACH_MEMBERCARD_INSURANCE)) {
            //换电套餐续费回调
            unionTradeOrderService.notifyMembercardInsurance(callBackResource);
        } else if (Objects.equals(callBackResource.getAttach(), UnionTradeOrder.ATTACH_SERVUCE_FEE)) {
            //滞纳金支付回调
            unionTradeOrderService.notifyServiceFee(callBackResource);
        } else if (Objects.equals(callBackResource.getAttach(), ElectricityTradeOrder.ATTACH_CLOUD_BEAN_RECHARGE)) {
            //云豆充值支付回调
            electricityTradeOrderService.notifyCloudBeanRechargeOrder(callBackResource);
        } else if (Objects.equals(callBackResource.getAttach(), UnionTradeOrder.ATTACH_INSTALLMENT)) {
            unionTradeOrderService.notifyInstallmentPayment(callBackResource);
        }else {
            log.error("WX PAY CALL BACK FAIL!not found attach typeo,rderId={}", orderNo);
        }
    }
    
    
    @Override
    public void postProcessBeforeWechatRefund(WechatV3RefundRequest request) {
    
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public void postProcessAfterWechatRefund(WechatV3RefundOrderCallBackRequest wechatV3RefundOrderCallBackQuery) {
        WechatCallBackResouceData resource = wechatV3RefundOrderCallBackQuery.getResource();
        if (Objects.isNull(resource)) {
            log.error("WECHAT ERROR! no wechat's info ! msg={}", wechatV3RefundOrderCallBackQuery);
            return;
        }
        
        String apiV3Key = this.getApiV3Key(wechatV3RefundOrderCallBackQuery.getTenantId(), wechatV3RefundOrderCallBackQuery.getFranchiseeId());
        
        String decryptJson = null;
        try {
            decryptJson = AesUtil
                    .decryptToString(resource.getAssociated_data().getBytes(StandardCharsets.UTF_8), resource.getNonce().getBytes(StandardCharsets.UTF_8), resource.getCiphertext(),
                            apiV3Key.getBytes(StandardCharsets.UTF_8));
            
        } catch (Exception e) {
            log.error("WECHAT ERROR! wechat decrypt error! msg={}", wechatV3RefundOrderCallBackQuery, e);
            return;
        }
        
        WechatJsapiRefundOrderCallBackResource callBackResource = JsonUtil.fromJson(decryptJson, WechatJsapiRefundOrderCallBackResource.class);
        
        eleRefundOrderService.notifyDepositRefundOrder(callBackResource);
    }
    
    
    private String getApiV3Key(Integer tenantId, Long franchiseeId) {
        try {
            ElectricityPayParams payParams = electricityPayParamsService.queryPreciseCacheByTenantIdAndFranchiseeId(tenantId, franchiseeId);
            if (Objects.isNull(payParams)) {
                throw new AuthenticationServiceException("未能查找到appId和appSecret！");
            }
            return payParams.getWechatV3ApiKey();
        } catch (Exception e) {
            throw new RuntimeException("获取商户apiV3密钥失败！tenantId=" + tenantId + " franchiseeId=" + franchiseeId, e);
        }
    }
}
