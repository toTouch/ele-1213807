/**
 * Copyright(c) 2018 Sunyur.com, All Rights Reserved. Author: sunyur Create date: 2024/7/16
 */

package com.xiliulou.electricity.converter;

import com.xiliulou.electricity.bo.pay.PayParamsBizDetails;
import com.xiliulou.electricity.bo.wechat.WechatPayParamsDetails;
import com.xiliulou.electricity.config.AliPayConfig;
import com.xiliulou.electricity.config.WechatConfig;
import com.xiliulou.electricity.entity.AlipayAppConfig;
import com.xiliulou.electricity.entity.CommonPayOrder;
import com.xiliulou.electricity.entity.ElectricityTradeOrder;
import com.xiliulou.pay.alipay.request.AliPayCreateOrderRequest;
import com.xiliulou.pay.base.enums.PayTypeEnum;
import com.xiliulou.pay.base.request.BasePayCreateOrderRequest;
import com.xiliulou.pay.base.request.BasePayRequest;
import com.xiliulou.pay.weixinv3.v2.query.WechatV3OrderRequest;
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
@Component
public class PayParamsConverter {
    
    @Resource
    private WechatConfig wechatConfig;
    
    @Resource
    private AliPayConfig aliPayConfig;
    
    
    /**
     * 支付参数转换
     *
     * @param payParamsBizDetails
     * @param commonOrder
     * @param electricityTradeOrder
     * @param openId
     * @author caobotao.cbt
     * @date 2024/7/16 17:04
     */
    public BasePayRequest<BasePayCreateOrderRequest> orderCreateParamConverter(PayParamsBizDetails payParamsBizDetails, CommonPayOrder commonOrder,
            ElectricityTradeOrder electricityTradeOrder, String openId) {
        
        BasePayRequest<BasePayCreateOrderRequest> request = new BasePayRequest<>();
        
        int amount = commonOrder.getPayAmount().multiply(new BigDecimal(100)).intValue();
        
        long expireTime = System.currentTimeMillis() + 3600000;
        
        // TODO: 2024/7/16 CBT 魔法值后续更改
        if (1 == payParamsBizDetails.getPayType()) {
            if (Objects.isNull(payParamsBizDetails.getWechatPayParamsDetails())) {
                return null;
            }
            WechatPayParamsDetails wechatPayParamsDetails = payParamsBizDetails.getWechatPayParamsDetails();
            //支付参数
            WechatV3OrderRequest wechatV3OrderRequest = new WechatV3OrderRequest();
            wechatV3OrderRequest.setAppid(wechatPayParamsDetails.getMerchantMinProAppId());
            wechatV3OrderRequest.setDescription(commonOrder.getDescription());
            wechatV3OrderRequest.setOrderId(electricityTradeOrder.getTradeOrderNo());
            wechatV3OrderRequest.setExpireTime(expireTime);
            wechatV3OrderRequest.setAttach(commonOrder.getAttach());
            wechatV3OrderRequest.setNotifyUrl(wechatConfig.getPayCallBackUrl() + electricityTradeOrder.getTenantId() + "/" + electricityTradeOrder.getPayFranchiseeId());
            wechatV3OrderRequest.setAmount(amount);
            wechatV3OrderRequest.setCurrency("CNY");
            wechatV3OrderRequest.setOpenId(openId);
            wechatV3OrderRequest.setCommonRequest(ElectricityPayParamsConverter.qryDetailsToCommonRequest(wechatPayParamsDetails));
            request.setBizParam(wechatV3OrderRequest);
            request.setPayType(PayTypeEnum.WX_V3_JSP_ORDER.getPayType());
            return request;
        }
        
        AlipayAppConfig alipayAppConfig = payParamsBizDetails.getAlipayAppConfig();
        if (Objects.isNull(alipayAppConfig)) {
            return null;
        }
        
        AliPayCreateOrderRequest aliPayCreateOrderRequest = new AliPayCreateOrderRequest();
        aliPayCreateOrderRequest.setAppId(alipayAppConfig.getAppId());
        aliPayCreateOrderRequest.setSellerId(alipayAppConfig.getSellerId());
        aliPayCreateOrderRequest.setAppPrivateKey(alipayAppConfig.getAppPrivateKey());
        aliPayCreateOrderRequest.setAlipayPublicKey(alipayAppConfig.getPublicKey());
        aliPayCreateOrderRequest.setNotifyUrl(aliPayConfig.getPayCallBackUrl());
        aliPayCreateOrderRequest.setBuyerOpenId(openId);
        aliPayCreateOrderRequest.setOutTradeNo(electricityTradeOrder.getTradeOrderNo());
        aliPayCreateOrderRequest.setTotalAmount(amount + "");
        // TODO: 2024/7/16 CBT 不缺认怎么传入
        aliPayCreateOrderRequest.setSubject(commonOrder.getDescription());
        aliPayCreateOrderRequest.setBody(commonOrder.getDescription());
        aliPayCreateOrderRequest.setExtendParams(new AliPayCreateOrderRequest.ExtendParams());
        aliPayCreateOrderRequest.setPassbackParams(commonOrder.getAttach());
        aliPayCreateOrderRequest.setTimeExpire(expireTime + "");
        request.setBizParam(aliPayCreateOrderRequest);
        request.setPayType(PayTypeEnum.ALI_MINI_ORDER.getPayType());
        return request;
    }
}
