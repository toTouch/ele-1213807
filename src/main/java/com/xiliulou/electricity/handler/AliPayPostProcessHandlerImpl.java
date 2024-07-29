/**
 * Copyright(c) 2018 Sunyur.com, All Rights Reserved. Author: sunyur Create date: 2024/7/16
 */

package com.xiliulou.electricity.handler;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.electricity.constant.WechatPayConstant;
import com.xiliulou.electricity.factory.paycallback.RefundPayServiceFactory;
import com.xiliulou.electricity.service.wxrefund.RefundPayService;
import com.xiliulou.pay.alipay.constants.AliPayConstant;
import com.xiliulou.pay.alipay.dto.AliPayRefundOrderDTO;
import com.xiliulou.pay.alipay.exception.AliPayException;
import com.xiliulou.pay.alipay.handler.AbstractAliPayPostProcessCallBackHandler;
import com.xiliulou.pay.alipay.request.AliPayCallBackResource;
import com.xiliulou.pay.alipay.request.ApiPayRefundOrderCallBackResource;
import lombok.extern.slf4j.Slf4j;
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
    
    /**
     * 支付宝退款回调只有部分退款才会回调，此处不做处理，统一在退款后同步处理
     *
     * @param backResource
     * @param customParam
     * @author caobotao.cbt
     * @date 2024/7/29 19:29
     */
    @Override
    protected void refundCallback(ApiPayRefundOrderCallBackResource backResource, String customParam) {
        log.info("AliPayPostProcessHandlerImpl.refundCallback Refunds are not processed");
    }
    
    @Override
    protected void payOrderCallback(AliPayCallBackResource backResource, String customParam) {
        //幂等加锁
        String orderNo = backResource.getOutTradeNo();
        if (!redisService.setNx(WechatPayConstant.PAY_ORDER_ID_CALL_BACK + orderNo, String.valueOf(System.currentTimeMillis()), 10 * 1000L, false)) {
            log.info("ELE INFO! alipay order in process orderNo={}", orderNo);
            return;
        }
        orderCallbackDispatcher.dispatch(backResource);
    }
    
    @Override
    public void postProcessAfterAliRefund(AliPayRefundOrderDTO orderDTO) throws AliPayException {
        // 成功
        ApiPayRefundOrderCallBackResource backResource = new ApiPayRefundOrderCallBackResource();
        backResource.setTradeStatus(orderDTO.getRefundStatus());
        backResource.setOutTradeNo(orderDTO.getOutTradeNo());
        backResource.setOutBizNo(orderDTO.getRefundNo());
        RefundPayService service = RefundPayServiceFactory.getService(orderDTO.getRefundType());
        if (Objects.isNull(service)) {
            log.warn("RefundPayService is null RefundType={}", orderDTO.getRefundType());
            return;
        }
        service.process(backResource);
    }
}
