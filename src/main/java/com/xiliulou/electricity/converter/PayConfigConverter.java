/**
 * Copyright(c) 2018 Sunyur.com, All Rights Reserved. Author: sunyur Create date: 2024/7/19
 */

package com.xiliulou.electricity.converter;

import com.xiliulou.electricity.bo.base.BasePayConfig;
import com.xiliulou.electricity.bo.pay.AlipayAppConfigBizDetails;
import com.xiliulou.electricity.bo.wechat.WechatPayParamsDetails;
import com.xiliulou.electricity.config.AliPayConfig;
import com.xiliulou.electricity.config.BasePayCallBackConfig;
import com.xiliulou.electricity.config.WechatConfig;
import com.xiliulou.electricity.converter.model.OrderCreateParamConverterModel;
import com.xiliulou.electricity.converter.model.OrderRefundParamConverterModel;
import com.xiliulou.pay.alipay.request.AliPayCreateOrderRequest;
import com.xiliulou.pay.alipay.request.AliPayOrderRefundRequest;
import com.xiliulou.core.base.enums.ChannelEnum;
import com.xiliulou.pay.base.request.BasePayRequest;
import com.xiliulou.pay.weixinv3.v2.query.WechatV3OrderRequest;
import com.xiliulou.pay.weixinv3.v2.query.WechatV3RefundRequest;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * description:
 *
 * @author caobotao.cbt
 * @date 2024/7/19 11:33
 */
@Component
public class PayConfigConverter {
    
    
    private Map<String, PayConfigConverterHandler<OrderCreateParamConverterModel>> orderCreateMap = new HashMap<>();
    
    private Map<String, PayConfigConverterHandler<OrderRefundParamConverterModel>> orderRefundMap = new HashMap<>();
    
    @Resource
    private WechatConfig wechatConfig;
    
    @Resource
    private AliPayConfig aliPayConfig;
    
    
    @PostConstruct
    public void init() {
        initWx();
        initAlipay();
    }
    
    
    private void initAlipay() {
        // 支付宝下单转换
        orderCreateMap.put(ChannelEnum.ALIPAY.getCode(), (model, call) -> alipayOrderCreateParamConverter(model, call));
        //微信退款转换
        orderRefundMap.put(ChannelEnum.ALIPAY.getCode(), (model, call) -> alipayOrderRefundParamConverter(model, call));
    }
    
    
    private void initWx() {
        //微信下单转换
        orderCreateMap.put(ChannelEnum.WECHAT.getCode(), (model, call) -> wechatOrderCreateParamConverter(model, call));
        //微信退款转换
        orderRefundMap.put(ChannelEnum.WECHAT.getCode(), (model, call) -> wechatOrderRefundParamConverter(model, call));
    }
    
    /**
     * 下单转换
     *
     * @param model
     * @param callBackUrlGet
     * @author caobotao.cbt
     * @date 2024/7/19 13:54
     */
    public BasePayRequest converterOrderCreate(OrderCreateParamConverterModel model, CallBackUrlGet callBackUrlGet) {
        BasePayConfig payConfig = model.getPayConfig();
        PayConfigConverterHandler<OrderCreateParamConverterModel> handler = orderCreateMap.get(payConfig.getPaymentChannel());
        if (Objects.isNull(handler)) {
            return null;
        }
        return handler.execute(model, callBackUrlGet);
    }
    
    /***
     * 退款转换
     * @author caobotao.cbt
     * @date 2024/7/19 13:54
     * @param model
     * @param callBackUrlGet
     */
    public BasePayRequest converterOrderRefund(OrderRefundParamConverterModel model, CallBackUrlGet callBackUrlGet) {
        BasePayConfig payConfig = model.getPayConfig();
        PayConfigConverterHandler<OrderRefundParamConverterModel> handler = orderRefundMap.get(payConfig.getPaymentChannel());
        if (Objects.isNull(handler)) {
            return null;
        }
        return handler.execute(model, callBackUrlGet);
    }
    
    /**
     * 微信支付请求参数构建
     *
     * @author caobotao.cbt
     * @date 2024/7/17 10:37
     */
    private BasePayRequest<WechatV3OrderRequest> wechatOrderCreateParamConverter(OrderCreateParamConverterModel model, CallBackUrlGet callBackUrlGet) {
        
        BasePayConfig payConfig = model.getPayConfig();
        if (Objects.isNull(payConfig)) {
            return null;
        }
        
        WechatPayParamsDetails wechatPayParamsDetails = (WechatPayParamsDetails) payConfig;
        //支付参数
        WechatV3OrderRequest wechatV3OrderRequest = new WechatV3OrderRequest();
        
        wechatV3OrderRequest.setDescription(model.getDescription());
        wechatV3OrderRequest.setOrderId(model.getOrderId());
        wechatV3OrderRequest.setExpireTime(model.getExpireTime());
        wechatV3OrderRequest.setAttach(model.getAttach());
        wechatV3OrderRequest.setAmount(model.getAmount().multiply(new BigDecimal(100)).intValue());
        wechatV3OrderRequest.setCurrency(model.getCurrency());
        wechatV3OrderRequest.setOpenId(model.getOpenId());
        wechatV3OrderRequest.setNotifyUrl(callBackUrlGet.getCallBackUrl(this.wechatConfig));
        wechatV3OrderRequest.setAppid(wechatPayParamsDetails.getMerchantMinProAppId());
        wechatV3OrderRequest.setCommonRequest(ElectricityPayParamsConverter.qryDetailsToCommonRequest(wechatPayParamsDetails));
        
        BasePayRequest<WechatV3OrderRequest> basePayRequest = new BasePayRequest();
        basePayRequest.setChannel(ChannelEnum.WECHAT.getCode());
        basePayRequest.setBizParam(wechatV3OrderRequest);
        return basePayRequest;
    }
    
    
    /**
     * 微信退款
     *
     * @param wrap
     * @param callBackUrlGet
     * @author caobotao.cbt
     * @date 2024/7/18 09:59
     */
    private BasePayRequest<WechatV3RefundRequest> wechatOrderRefundParamConverter(OrderRefundParamConverterModel wrap, CallBackUrlGet callBackUrlGet) {
        
        BasePayConfig payConfig = wrap.getPayConfig();
        if (Objects.isNull(payConfig)) {
            return null;
        }
        WechatPayParamsDetails wechatPayParamsDetails = (WechatPayParamsDetails) payConfig;
        
        WechatV3RefundRequest wechatV3RefundRequest = new WechatV3RefundRequest();
        wechatV3RefundRequest.setRefundId(wrap.getRefundId());
        wechatV3RefundRequest.setOrderId(wrap.getOrderId());
        wechatV3RefundRequest.setReason(wrap.getReason());
        wechatV3RefundRequest.setNotifyUrl(callBackUrlGet.getCallBackUrl(this.wechatConfig));
        wechatV3RefundRequest.setRefund(wrap.getRefund().multiply(new BigDecimal(100)).intValue());
        wechatV3RefundRequest.setTotal(wrap.getTotal());
        wechatV3RefundRequest.setCurrency(wrap.getCurrency());
        wechatV3RefundRequest.setCommonRequest(ElectricityPayParamsConverter.qryDetailsToCommonRequest(wechatPayParamsDetails));
        
        BasePayRequest<WechatV3RefundRequest> basePayRequest = new BasePayRequest();
        basePayRequest.setChannel(ChannelEnum.WECHAT.getCode());
        basePayRequest.setBizParam(wechatV3RefundRequest);
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
    private BasePayRequest<AliPayOrderRefundRequest> alipayOrderRefundParamConverter(OrderRefundParamConverterModel wrap, CallBackUrlGet callBackUrlGet) {
        BasePayConfig payConfig = wrap.getPayConfig();
        if (Objects.isNull(payConfig)) {
            return null;
        }
        AlipayAppConfigBizDetails config = (AlipayAppConfigBizDetails) payConfig;
        
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
        
        BasePayRequest<AliPayOrderRefundRequest> basePayRequest = new BasePayRequest();
        basePayRequest.setChannel(ChannelEnum.ALIPAY.getCode());
        basePayRequest.setBizParam(aliPayOrderRefundRequest);
        return basePayRequest;
    }
    
    /**
     * 支付宝支付
     *
     * @author caobotao.cbt
     * @date 2024/7/17 10:42
     */
    private BasePayRequest<AliPayCreateOrderRequest> alipayOrderCreateParamConverter(OrderCreateParamConverterModel wrap, CallBackUrlGet callBackUrlGet) {
        
        BasePayConfig payConfig = wrap.getPayConfig();
        if (Objects.isNull(payConfig)) {
            return null;
        }
        AlipayAppConfigBizDetails alipayAppConfig = (AlipayAppConfigBizDetails) payConfig;
        
        // 支付配置转换
        AliPayCreateOrderRequest aliPayCreateOrderRequest = new AliPayCreateOrderRequest();
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
        BasePayRequest<AliPayCreateOrderRequest> basePayRequest = new BasePayRequest();
        basePayRequest.setChannel(ChannelEnum.ALIPAY.getCode());
        basePayRequest.setBizParam(aliPayCreateOrderRequest);
        return basePayRequest;
    }
    
    
    @FunctionalInterface
    interface PayConfigConverterHandler<T> {
        
        
        BasePayRequest execute(T p, CallBackUrlGet callBackUrlGet);
        
    }
    
    @FunctionalInterface
    public interface CallBackUrlGet {
        
        String getCallBackUrl(BasePayCallBackConfig config);
    }
    
}
