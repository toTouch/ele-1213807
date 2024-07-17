/**
 * Copyright(c) 2018 Sunyur.com, All Rights Reserved. Author: sunyur Create date: 2024/7/16
 */

package com.xiliulou.electricity.handler;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.electricity.constant.WechatPayConstant;
import com.xiliulou.electricity.entity.ElectricityTradeOrder;
import com.xiliulou.electricity.entity.UnionTradeOrder;
import com.xiliulou.electricity.enums.CallBackEnums;
import com.xiliulou.electricity.enums.WxRefundPayOptTypeEnum;
import com.xiliulou.electricity.factory.paycallback.WxRefundPayServiceFactory;
import com.xiliulou.electricity.service.EleRefundOrderService;
import com.xiliulou.electricity.service.wxrefund.WxRefundPayService;
import com.xiliulou.pay.alipay.handler.AbstractAliPayPostProcessCallBackHandler;
import com.xiliulou.pay.alipay.request.AliPayCallBackResource;
import com.xiliulou.pay.alipay.request.AliPayCreateOrderRequest;
import com.xiliulou.pay.alipay.request.AliPayOrderRefundRequest;
import com.xiliulou.pay.alipay.request.ApiPayRefundOrderCallBackResource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * description:
 *
 * @author caobotao.cbt
 * @date 2024/7/16 17:13
 */
@Slf4j
@Service
public class AliPayPostProcessHandlerImpl extends AbstractAliPayPostProcessCallBackHandler {
    
    @Resource
    private RedisService redisService;
    
    @Resource
    private OrderCallbackDispatcher orderCallbackDispatcher;
    
    
    @Override
    protected void aliPayOrderRefundSignatureCheckSuccess(ApiPayRefundOrderCallBackResource backResource, String customParam) {
        WxRefundPayService service = WxRefundPayServiceFactory.getService(customParam);
        if (Objects.isNull(service)) {
            log.warn("WxRefundPayService is null customParam={}", customParam);
            return;
        }
        service.process(backResource);
    }
    
    @Override
    protected void aliPaySignatureCheckSuccess(AliPayCallBackResource backResource, String customParam) {
        //幂等加锁
        String orderNo = backResource.getOutTradeNo();
        if (!redisService.setNx(WechatPayConstant.PAY_ORDER_ID_CALL_BACK + orderNo, String.valueOf(System.currentTimeMillis()), 10 * 1000L, false)) {
            log.info("ELE INFO! alipay order in process orderNo={}", orderNo);
            return;
        }
        orderCallbackDispatcher.dispatch(backResource);
    }
}
