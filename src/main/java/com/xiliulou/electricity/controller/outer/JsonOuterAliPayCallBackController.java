/**
 * Copyright(c) 2018 Sunyur.com, All Rights Reserved. Author: sunyur Create date: 2024/7/16
 */

package com.xiliulou.electricity.controller.outer;

import com.xiliulou.electricity.bo.pay.AlipayAppConfigBizDetails;
import com.xiliulou.electricity.entity.AlipayAppConfig;
import com.xiliulou.electricity.enums.WxRefundPayOptTypeEnum;
import com.xiliulou.electricity.service.AlipayAppConfigService;
import com.xiliulou.pay.alipay.exception.AliPayException;
import com.xiliulou.pay.alipay.handler.AliPayPostProcessHandler;
import com.xiliulou.pay.alipay.request.AliPayOrderCallBackRequest;
import com.xiliulou.pay.weixinv3.rsp.WechatV3CallBackResult;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Objects;

/**
 * description: 支付宝支付回调
 *
 * @author caobotao.cbt
 * @date 2024/7/16 17:12
 */
@RestController
@Slf4j
public class JsonOuterAliPayCallBackController {
    
    
    @Resource
    private AliPayPostProcessHandler aliPayPostProcessHandler;
    
    @Resource
    private AlipayAppConfigService alipayAppConfigService;
    
    /**
     * 支付宝支付通知
     *
     * @return
     */
    @SneakyThrows
    @PostMapping("/outer/alipay/pay/notified/{tenantId}/{franchiseeId}")
    public void payNotified(@PathVariable("tenantId") Integer tenantId, @PathVariable(value = "franchiseeId") Long franchiseeId, HttpServletRequest request) {
    
        AlipayAppConfigBizDetails alipayAppConfig = alipayAppConfigService.queryPreciseByTenantIdAndFranchiseeId(tenantId, franchiseeId);
        if (Objects.isNull(alipayAppConfig)) {
            throw new AliPayException("alipayAppConfig is null");
        }
        
        Map requestParams = request.getParameterMap();
        aliPayPostProcessHandler.postProcessAfterAliPay(new AliPayOrderCallBackRequest(requestParams, alipayAppConfig.getPublicKey(), null));
    }
    
    
    /**
     * 退款通知
     *
     * @return
     */
    @SneakyThrows
    @PostMapping("/outer/alipay/refund/notified/{tenantId}/{franchiseeId}")
    public void refundNotified(@PathVariable("tenantId") Integer tenantId, @PathVariable(value = "franchiseeId") Long franchiseeId, HttpServletRequest request) {
    
        AlipayAppConfigBizDetails alipayAppConfig = alipayAppConfigService.queryPreciseByTenantIdAndFranchiseeId(tenantId, franchiseeId);
        if (Objects.isNull(alipayAppConfig)) {
            throw new AliPayException("alipayAppConfig is null");
        }
        Map requestParams = request.getParameterMap();
        aliPayPostProcessHandler.postProcessAfterAliRefund(
                new AliPayOrderCallBackRequest(requestParams, alipayAppConfig.getPublicKey(), WxRefundPayOptTypeEnum.BATTERY_DEPOSIT_REFUND_CALL_BACK.getCode()));
    }
    
    /**
     * 退款通知(电池租金)
     *
     * @return
     */
    @SneakyThrows
    @PostMapping("/outer/alipay/battery/membercard/refund/notified/{tenantId}/{franchiseeId}")
    public WechatV3CallBackResult batteryMembercardRefundNotified(@PathVariable("tenantId") Integer tenantId, @PathVariable(value = "franchiseeId") Long franchiseeId,
            HttpServletRequest request) {
    
        AlipayAppConfigBizDetails alipayAppConfig = alipayAppConfigService.queryPreciseByTenantIdAndFranchiseeId(tenantId, franchiseeId);
        if (Objects.isNull(alipayAppConfig)) {
            throw new AliPayException("alipayAppConfig is null");
        }
        Map requestParams = request.getParameterMap();
        aliPayPostProcessHandler.postProcessAfterAliRefund(
                new AliPayOrderCallBackRequest(requestParams, alipayAppConfig.getPublicKey(), WxRefundPayOptTypeEnum.BATTERY_RENT_REFUND_CALL_BACK.getCode()));
        return WechatV3CallBackResult.success();
    }
    
    
    /**
     * 支付宝退款回调(租车押金)
     *
     * @param tenantId 租户ID
     * @param request  微信回调参数
     * @return
     */
    @SneakyThrows
    @PostMapping("/outer/alipay/refund/car/deposit/notified/{tenantId}/{franchiseeId}")
    public WechatV3CallBackResult carDepositRefundCallBackUrl(@PathVariable("tenantId") Integer tenantId, @PathVariable(value = "franchiseeId") Long franchiseeId,
            HttpServletRequest request) {
    
        AlipayAppConfigBizDetails alipayAppConfig = alipayAppConfigService.queryPreciseByTenantIdAndFranchiseeId(tenantId, franchiseeId);
        if (Objects.isNull(alipayAppConfig)) {
            throw new AliPayException("alipayAppConfig is null");
        }
        Map requestParams = request.getParameterMap();
        aliPayPostProcessHandler.postProcessAfterAliRefund(
                new AliPayOrderCallBackRequest(requestParams, alipayAppConfig.getPublicKey(), WxRefundPayOptTypeEnum.CAR_DEPOSIT_REFUND_CALL_BACK.getCode()));
        
        return WechatV3CallBackResult.success();
    }
    
    /**
     * 微信退款回调(租车租金)
     *
     * @param tenantId 租户ID
     * @param request  微信回调参数
     * @return
     */
    @SneakyThrows
    @PostMapping("/outer/alipay/refund/car/rent/notified/{tenantId}/{franchiseeId}")
    public WechatV3CallBackResult carRentRefundCallBackUrl(@PathVariable("tenantId") Integer tenantId, @PathVariable(value = "franchiseeId") Long franchiseeId,
            HttpServletRequest request) {
        AlipayAppConfigBizDetails alipayAppConfig = alipayAppConfigService.queryPreciseByTenantIdAndFranchiseeId(tenantId, franchiseeId);
        if (Objects.isNull(alipayAppConfig)) {
            throw new AliPayException("alipayAppConfig is null");
        }
        Map requestParams = request.getParameterMap();
        aliPayPostProcessHandler.postProcessAfterAliRefund(
                new AliPayOrderCallBackRequest(requestParams, alipayAppConfig.getPublicKey(), WxRefundPayOptTypeEnum.CAR_RENT_REFUND_CALL_BACK.getCode()));
        
        return WechatV3CallBackResult.success();
    }
    
    
}
