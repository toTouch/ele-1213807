/**
 * Copyright(c) 2018 Sunyur.com, All Rights Reserved. Author: sunyur Create date: 2024/7/16
 */

package com.xiliulou.electricity.converter;

import com.xiliulou.electricity.bo.base.BasePayConfig;
import com.xiliulou.electricity.bo.pay.AlipayAppConfigBizDetails;
import com.xiliulou.electricity.bo.pay.PayParamsBizDetails;
import com.xiliulou.electricity.bo.wechat.WechatPayParamsDetails;
import com.xiliulou.electricity.config.AliPayConfig;
import com.xiliulou.electricity.config.BasePayCallBackConfig;
import com.xiliulou.electricity.config.WechatConfig;
import com.xiliulou.electricity.converter.model.OrderCreateParamConverterModel;
import com.xiliulou.electricity.converter.model.OrderRefundParamConverterModel;
import com.xiliulou.electricity.enums.PaymentMethodEnum;
import com.xiliulou.pay.alipay.request.AliPayCreateOrderRequest;
import com.xiliulou.pay.alipay.request.AliPayOrderRefundRequest;
import com.xiliulou.pay.base.enums.PayTypeEnum;
import com.xiliulou.pay.base.request.BasePayCreateOrderRequest;
import com.xiliulou.pay.base.request.BasePayOrderRefundRequest;
import com.xiliulou.pay.base.request.BasePayRequest;
import com.xiliulou.pay.weixinv3.v2.query.WechatV3OrderRequest;
import com.xiliulou.pay.weixinv3.v2.query.WechatV3RefundRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * description:
 *
 * @author caobotao.cbt
 * @date 2024/7/16 16:28
 */
@Slf4j
@Component
public class PayConfigConverter {
    
    @Resource
    private WechatConfig wechatConfig;
    
    @Resource
    private AliPayConfig aliPayConfig;
    
    
    /**
     * 支付参数转换
     *
     * @param wrap
     * @param callBackUrlGet 回调url连接获取
     * @author caobotao.cbt
     * @date 2024/7/16 17:04
     */
    public <T extends BasePayConfig> BasePayRequest orderCreateParamConverter(OrderCreateParamConverterModel<T> wrap, CallBackUrlGet callBackUrlGet) {
        
        PayParamsBizDetails<T> payParamsBizDetails = wrap.getPayParamsBizDetails();
        
        BasePayCreateOrderRequest basePayCreateOrderRequest = null;
        Integer payType = null;
        if (PaymentMethodEnum.WECHAT.getCode().equals(payParamsBizDetails.getPaymentMethod())) {
            // 微信支付
            basePayCreateOrderRequest = this.wechatOrderCreateParamConverter(wrap, callBackUrlGet);
            payType = PayTypeEnum.WX_V3_JSP_ORDER.getPayType();
        } else if (PaymentMethodEnum.ALI_PAY.getCode().equals(payParamsBizDetails.getPaymentMethod())) {
            // 支付宝支付
            basePayCreateOrderRequest = this.alipayOrderCreateParamConverter(wrap, callBackUrlGet);
            payType = PayTypeEnum.ALI_MINI_ORDER.getPayType();
        }
        
        if (Objects.isNull(basePayCreateOrderRequest)) {
            log.warn("PayParamsConverter.orderCreateParamConverter WARN!  paymentMethod:{} is not found !", payParamsBizDetails.getPaymentMethod());
            return null;
        }
        
        BasePayRequest basePayRequest = new BasePayRequest();
        basePayRequest.setPayType(payType);
        basePayRequest.setBizParam(basePayCreateOrderRequest);
        
        return basePayRequest;
    }
    
    
    /**
     * 退款参数转换
     *
     * @param wrap
     * @param callBackUrlGet 回调url连接获取
     * @author caobotao.cbt
     * @date 2024/7/16 17:04
     */
    public <T extends BasePayConfig> BasePayRequest orderRefundParamConverter(OrderRefundParamConverterModel<T> wrap, CallBackUrlGet callBackUrlGet) {
        
        PayParamsBizDetails<T> payParamsBizDetails = wrap.getPayParamsBizDetails();
        
        BasePayOrderRefundRequest refundRequest = null;
        Integer payType = null;
        if (PaymentMethodEnum.WECHAT.getCode().equals(payParamsBizDetails.getPaymentMethod())) {
            // 微信退款
            refundRequest = this.wechatOrderRefundParamConverter(wrap, callBackUrlGet);
            payType = PayTypeEnum.WX_V3_JSP_ORDER_REFUND.getPayType();
        } else if (PaymentMethodEnum.ALI_PAY.getCode().equals(payParamsBizDetails.getPaymentMethod())) {
            // 支付宝退款
            refundRequest = this.alipayOrderRefundParamConverter(wrap, callBackUrlGet);
            payType = PayTypeEnum.ALI_MINI_REFUND.getPayType();
        }
        
        if (Objects.isNull(refundRequest)) {
            log.warn("PayParamsConverter.orderCreateParamConverter WARN!  paymentMethod:{} is not found !", payParamsBizDetails.getPaymentMethod());
            return null;
        }
        
        BasePayRequest basePayRequest = new BasePayRequest();
        basePayRequest.setPayType(payType);
        basePayRequest.setBizParam(refundRequest);
        
        return basePayRequest;
    }
    
    /**
     * 支付宝退款
     *
     * @param wrap
     * @param callBackUrlGet
     * @author caobotao.cbt
     * @date 2024/7/18 10:04
     */
    private <T extends BasePayConfig> AliPayOrderRefundRequest alipayOrderRefundParamConverter(OrderRefundParamConverterModel<T> wrap, CallBackUrlGet callBackUrlGet) {
        PayParamsBizDetails<T> payParamsBizDetails = wrap.getPayParamsBizDetails();
        if (Objects.isNull(payParamsBizDetails)) {
            return null;
        }
        AlipayAppConfigBizDetails config = (AlipayAppConfigBizDetails) payParamsBizDetails.getPayParamConfig();
        
        AliPayOrderRefundRequest aliPayOrderRefundRequest = new AliPayOrderRefundRequest();
        aliPayOrderRefundRequest.setOutRequestNo(wrap.getRefundId());
        aliPayOrderRefundRequest.setOutTradeNo(wrap.getOrderId());
        aliPayOrderRefundRequest.setRefundReason(wrap.getReason());
        aliPayOrderRefundRequest.setRefundAmount(wrap.getRefund().toPlainString());
        aliPayOrderRefundRequest.setRefundCurrency(wrap.getCurrency());
        aliPayOrderRefundRequest.setOpenId(wrap.getOrderId());
        aliPayOrderRefundRequest.setAppId(config.getAppId());
        aliPayOrderRefundRequest.setAppPrivateKey(config.getAppPrivateKey());
        aliPayOrderRefundRequest.setAlipayPublicKey(config.getPublicKey());
        aliPayOrderRefundRequest.setNotifyUrl(callBackUrlGet.getCallBackUrl(this.aliPayConfig));
        return aliPayOrderRefundRequest;
    }
    
    
    /**
     * 微信退款
     *
     * @param wrap
     * @param callBackUrlGet
     * @author caobotao.cbt
     * @date 2024/7/18 09:59
     */
    private <T extends BasePayConfig> WechatV3RefundRequest wechatOrderRefundParamConverter(OrderRefundParamConverterModel<T> wrap, CallBackUrlGet callBackUrlGet) {
        
        PayParamsBizDetails<T> payParamsBizDetails = wrap.getPayParamsBizDetails();
        if (Objects.isNull(payParamsBizDetails)) {
            return null;
        }
        WechatPayParamsDetails wechatPayParamsDetails = (WechatPayParamsDetails) payParamsBizDetails.getPayParamConfig();
        WechatV3RefundRequest wechatV3RefundRequest = new WechatV3RefundRequest();
        wechatV3RefundRequest.setRefundId(wrap.getRefundId());
        wechatV3RefundRequest.setOrderId(wrap.getOrderId());
        wechatV3RefundRequest.setReason(wrap.getReason());
        wechatV3RefundRequest.setNotifyUrl(callBackUrlGet.getCallBackUrl(this.wechatConfig));
        wechatV3RefundRequest.setRefund(wrap.getRefund().multiply(new BigDecimal(100)).intValue());
        wechatV3RefundRequest.setTotal(wrap.getTotal().multiply(new BigDecimal(100)).intValue());
        wechatV3RefundRequest.setCurrency(wrap.getCurrency());
        wechatV3RefundRequest.setCommonRequest(ElectricityPayParamsConverter.qryDetailsToCommonRequest(wechatPayParamsDetails));
        return wechatV3RefundRequest;
    }
    
    
    /**
     * 支付宝支付
     *
     * @author caobotao.cbt
     * @date 2024/7/17 10:42
     */
    private <T extends BasePayConfig> AliPayCreateOrderRequest alipayOrderCreateParamConverter(OrderCreateParamConverterModel<T> wrap, CallBackUrlGet callBackUrlGet) {
        
        PayParamsBizDetails<T> payParamsBizDetails = wrap.getPayParamsBizDetails();
        if (Objects.isNull(payParamsBizDetails)) {
            return null;
        }
        T payParamConfig = payParamsBizDetails.getPayParamConfig();
        
        AliPayCreateOrderRequest aliPayCreateOrderRequest = new AliPayCreateOrderRequest();
        // 支付配置转换
        AlipayAppConfigBizDetails alipayAppConfig = (AlipayAppConfigBizDetails) payParamConfig;
        aliPayCreateOrderRequest.setAppId(alipayAppConfig.getAppId());
        aliPayCreateOrderRequest.setSellerId(alipayAppConfig.getSellerId());
        aliPayCreateOrderRequest.setAppPrivateKey(alipayAppConfig.getAppPrivateKey());
        aliPayCreateOrderRequest.setAlipayPublicKey(alipayAppConfig.getPublicKey());
        
        // 其他参数
        aliPayCreateOrderRequest.setNotifyUrl(callBackUrlGet.getCallBackUrl(this.aliPayConfig));
        aliPayCreateOrderRequest.setBuyerOpenId(wrap.getOpenId());
        aliPayCreateOrderRequest.setOutTradeNo(wrap.getOrderId());
        aliPayCreateOrderRequest.setTotalAmount(wrap.getAmount().toPlainString());
        aliPayCreateOrderRequest.setTimeExpire(wrap.getExpireTime() + "");
        // TODO: 2024/7/16 CBT Subject？
        aliPayCreateOrderRequest.setSubject(wrap.getDescription());
        aliPayCreateOrderRequest.setBody(wrap.getDescription());
        aliPayCreateOrderRequest.setPassbackParams(wrap.getAttach());
        //        aliPayCreateOrderRequest.setExtendParams(new AliPayCreateOrderRequest.ExtendParams());
        return aliPayCreateOrderRequest;
    }
    
    /**
     * 微信支付请求参数构建
     *
     * @author caobotao.cbt
     * @date 2024/7/17 10:37
     */
    private <T extends BasePayConfig> WechatV3OrderRequest wechatOrderCreateParamConverter(OrderCreateParamConverterModel<T> wrap, CallBackUrlGet callBackUrlGet) {
        
        PayParamsBizDetails<T> payParamsBizDetails = wrap.getPayParamsBizDetails();
        if (Objects.isNull(payParamsBizDetails)) {
            return null;
        }
        T payParamConfig = payParamsBizDetails.getPayParamConfig();
        
        WechatPayParamsDetails wechatPayParamsDetails = (WechatPayParamsDetails) payParamConfig;
        //支付参数
        WechatV3OrderRequest wechatV3OrderRequest = new WechatV3OrderRequest();
        
        wechatV3OrderRequest.setDescription(wrap.getDescription());
        wechatV3OrderRequest.setOrderId(wrap.getOrderId());
        wechatV3OrderRequest.setExpireTime(wrap.getExpireTime());
        wechatV3OrderRequest.setAttach(wrap.getAttach());
        wechatV3OrderRequest.setAmount(wrap.getAmount().multiply(new BigDecimal(100)).intValue());
        wechatV3OrderRequest.setCurrency(wrap.getCurrency());
        wechatV3OrderRequest.setOpenId(wrap.getOpenId());
        wechatV3OrderRequest.setNotifyUrl(callBackUrlGet.getCallBackUrl(this.wechatConfig));
        wechatV3OrderRequest.setAppid(wechatPayParamsDetails.getMerchantMinProAppId());
        wechatV3OrderRequest.setCommonRequest(ElectricityPayParamsConverter.qryDetailsToCommonRequest(wechatPayParamsDetails));
        return wechatV3OrderRequest;
    }
    
    
    @FunctionalInterface
    public interface CallBackUrlGet {
        
        String getCallBackUrl(BasePayCallBackConfig config);
    }
    
}
