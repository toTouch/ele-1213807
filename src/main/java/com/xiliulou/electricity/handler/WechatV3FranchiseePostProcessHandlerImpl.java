/**
 * Create date: 2024/6/13
 */

package com.xiliulou.electricity.handler;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.WechatPayConstant;
import com.xiliulou.electricity.entity.ElectricityPayParams;
import com.xiliulou.electricity.entity.ElectricityTradeOrder;
import com.xiliulou.electricity.entity.UnionTradeOrder;
import com.xiliulou.electricity.enums.CallBackEnums;
import com.xiliulou.electricity.service.EleRefundOrderService;
import com.xiliulou.electricity.service.ElectricityPayParamsService;
import com.xiliulou.electricity.service.ElectricityTradeOrderService;
import com.xiliulou.electricity.service.UnionTradeOrderService;
import com.xiliulou.electricity.service.merchant.MerchantWithdrawApplicationBizService;
import com.xiliulou.pay.weixinv3.dto.WechatJsapiOrderCallBackResource;
import com.xiliulou.pay.weixinv3.dto.WechatJsapiRefundOrderCallBackResource;
import com.xiliulou.pay.weixinv3.dto.WechatTransferOrderCallBackResource;
import com.xiliulou.pay.weixinv3.query.WechatCallBackResouceData;
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

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
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

    @Resource
    private MerchantWithdrawApplicationBizService merchantWithdrawApplicationBizService;
    
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
        } else if (Objects.equals(callBackResource.getAttach(), UnionTradeOrder.ATTACH_PLACE_ORDER)) {
            unionTradeOrderService.notifyPlaceOrder(callBackResource);
        }else {
            log.error("WX PAY CALL BACK FAIL!not found attach typeo,rderId={}", orderNo);
        }
    }
    
    
    @Override
    public void postProcessBeforeWechatRefund(WechatV3RefundRequest request) {
    
    }
    
    public static void main(String[] args) throws GeneralSecurityException {
        String str="{\"id\":\"19559b5a-a63b-5ff2-a8d1-8a3ad4b0552b\",\"create_time\":\"2024-11-14T10:00:08+08:00\",\"resource_type\":\"encrypt-resource\",\"event_type\":\"REFUND.SUCCESS\",\"summary\":\"退款成功\",\"resource\":{\"original_type\":\"refund\",\"algorithm\":\"AEAD_AES_256_GCM\",\"ciphertext\":\"xFoTKS6jKzpYonlRCuYQRTezITwC1bRHxwgD9C3+11P2NhfR0x+mDAI6RY8adklbFtqH9EjmeeG4cbKqAVfnM1rg5XYTP4c2z/abV57laJAChat+9JeKxirOScAx1FpxZwIpC+R+X7ziVczkwnwTaXQIKAffQHneYrWCJ6Xw/4Y493ebeHEYQayZ/3t2K7p00nNYS155BRM94Pyg7IT4d0lHlMNaD8QvJPCPp3OUUHcc9uHfjuVl/sA0+ur0F7QY0mMxfCZV9nJOFd5p8+8unfk6zNkEoBGmEi0x6+Q6pSzGG747tMckXhwaFC5N5KCKhdjyN5PdpZ47eP+Est9xVfbvr9+ujG0C0bPt6XEeceWgFPpIwQE+0gSp73lGf+F4ewJ3kM8CjGbTTlPd9so9Z9nILNRRj7JzgFPIRDFMHJ2xuBbwtONIXBugVfx6QgxgCog4AjQNZh3YTLxn/ds29k5fr8onmyonVUDCuc83nfq9frog8bHAYT1OIjFVr2VOPSFFCrDNyg7+M8IiebBeR9/bknXceBKr+w==\",\"associated_data\":\"refund\",\"nonce\":\"9sdCQRqxoLY5\"}} ";
        WechatV3RefundOrderCallBackRequest request = JsonUtil.fromJson(str, WechatV3RefundOrderCallBackRequest.class);
        WechatCallBackResouceData resource = request.getResource();
        String s = AesUtil
                .decryptToString(resource.getAssociated_data().getBytes(StandardCharsets.UTF_8), resource.getNonce().getBytes(StandardCharsets.UTF_8), resource.getCiphertext(),
                        "Jziyou37130219820910431119821620".getBytes(StandardCharsets.UTF_8));
        System.out.println();
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

    @Override
    public void postProcessAfterMerchantWithdraw(WechatV3OrderCallBackRequest wechatV3OrderCallBackQuery) {
        WechatCallBackResouceData resource = wechatV3OrderCallBackQuery.getResource();
        if (Objects.isNull(resource)) {
            log.error("MERCHANT WITHDRAW NOTIFY ERROR! no wechat's info ! msg={}", wechatV3OrderCallBackQuery);
            return;
        }

        String apiV3Key = this.getApiV3Key(wechatV3OrderCallBackQuery.getTenantId(), wechatV3OrderCallBackQuery.getFranchiseeId());

        String decryptJson = null;
        try {
            decryptJson = AesUtil
                    .decryptToString(resource.getAssociated_data().getBytes(StandardCharsets.UTF_8), resource.getNonce().getBytes(StandardCharsets.UTF_8), resource.getCiphertext(),
                            apiV3Key.getBytes(StandardCharsets.UTF_8));

        } catch (Exception e) {
            log.error("MERCHANT WITHDRAW NOTIFY ERROR! wechat decrypt error! msg={}", wechatV3OrderCallBackQuery, e);
            return;
        }

        WechatTransferOrderCallBackResource callBackResource = JsonUtil.fromJson(decryptJson, WechatTransferOrderCallBackResource.class);

        //幂等加锁
        String outBillNo = callBackResource.getOutBillNo();
        if (!redisService.setNx(CacheConstant.CACHE_MERCHANT_WITHDRAW_NOTIFY_LOCK + outBillNo, String.valueOf(System.currentTimeMillis()), 10 * 1000L, false)) {
            log.info("MERCHANT WITHDRAW NOTIFY INFO! order in process outBillNo={}", outBillNo);
            return;
        }

        merchantWithdrawApplicationBizService.handleNotify(callBackResource);

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
