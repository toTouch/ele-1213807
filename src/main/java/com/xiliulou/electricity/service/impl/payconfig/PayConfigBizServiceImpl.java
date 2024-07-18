/**
 * Copyright(c) 2018 Sunyur.com, All Rights Reserved. Author: sunyur Create date: 2024/7/16
 */

package com.xiliulou.electricity.service.impl.payconfig;

import com.xiliulou.electricity.bo.base.BasePayConfig;
import com.xiliulou.electricity.bo.pay.PayParamsBizDetails;
import com.xiliulou.electricity.enums.PaymentMethodEnum;
import com.xiliulou.electricity.service.AlipayAppConfigService;
import com.xiliulou.electricity.service.WechatPayParamsBizService;
import com.xiliulou.electricity.service.pay.PayConfigBizService;
import com.xiliulou.pay.alipay.exception.AliPayException;
import com.xiliulou.pay.weixinv3.exception.WechatPayException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * description: 支付参数业务service
 *
 * @author caobotao.cbt
 * @date 2024/7/16 15:50
 */
@Slf4j
@Service
public class PayConfigBizServiceImpl implements PayConfigBizService {
    
    
    @Resource
    private PayConfigFactory payConfigFactory;
    
    
    @Override
    public <T extends BasePayConfig> PayParamsBizDetails<T> queryPayParams(String paymentMethod, Integer tenantId, Long franchiseeId) throws WechatPayException, AliPayException {
        
        return commonQuery(payConfigFactory.getStrategy(paymentMethod), paymentMethod, tenantId, franchiseeId);
    }
    
    @Override
    public <T extends BasePayConfig> PayParamsBizDetails<T> queryPrecisePayParams(String paymentMethod, Integer tenantId, Long franchiseeId)
            throws WechatPayException, AliPayException {
        
        return commonQuery(payConfigFactory.getPreciseStrategy(paymentMethod), paymentMethod, tenantId, franchiseeId);
    }
    
    
    /**
     * 统一查询
     *
     * @param payConfigStrategy
     * @param paymentMethod
     * @param tenantId
     * @param franchiseeId
     * @author caobotao.cbt
     * @date 2024/7/18 14:40
     */
    private <T extends BasePayConfig> PayParamsBizDetails<T> commonQuery(PayConfigFactory.PayConfigStrategy<T> payConfigStrategy, String paymentMethod, Integer tenantId,
            Long franchiseeId) throws WechatPayException, AliPayException {
        if (Objects.isNull(payConfigStrategy)) {
            log.warn("PayParamsBizServiceImpl.commonQuery WARN! paymentMethod:{} is not found", paymentMethod);
            return null;
        }
        T config = payConfigStrategy.execute(tenantId, franchiseeId);
        PayParamsBizDetails payParamsBizDetails = new PayParamsBizDetails();
        payParamsBizDetails.setPaymentMethod(paymentMethod);
        payParamsBizDetails.setPayParamConfig(config);
        return payParamsBizDetails;
    }
    
    
}
