/**
 * Copyright(c) 2018 Sunyur.com, All Rights Reserved. Author: sunyur Create date: 2024/7/16
 */

package com.xiliulou.electricity.controller.outer;

import com.xiliulou.electricity.bo.pay.AlipayAppConfigBizDetails;
import com.xiliulou.electricity.enums.RefundPayOptTypeEnum;
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
     * 支付宝支付通知 支付宝小程序支付所以的都是一个通知接口
     *
     * @return
     */
    @SneakyThrows
    @PostMapping("/outer/alipay/pay/notified/{tenantId}/{franchiseeId}")
    public String payNotified(@PathVariable("tenantId") Integer tenantId, @PathVariable(value = "franchiseeId") Long franchiseeId, HttpServletRequest request) {
        
        AlipayAppConfigBizDetails alipayAppConfig = alipayAppConfigService.queryPreciseByTenantIdAndFranchiseeId(tenantId, franchiseeId);
        if (Objects.isNull(alipayAppConfig)) {
            throw new AliPayException("alipayAppConfig is null");
        }
        
        Map<String, String[]> requestParams = request.getParameterMap();
        return aliPayPostProcessHandler.postProcessCallback(new AliPayOrderCallBackRequest(requestParams, alipayAppConfig.getPublicKey(), null));
    }
    
    
}
