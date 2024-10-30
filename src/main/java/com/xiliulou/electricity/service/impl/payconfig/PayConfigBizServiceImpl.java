/**
 * Copyright(c) 2018 Sunyur.com, All Rights Reserved. Author: sunyur Create date: 2024/7/16
 */

package com.xiliulou.electricity.service.impl.payconfig;

import com.xiliulou.electricity.bo.base.BasePayConfig;
import com.xiliulou.electricity.enums.profitsharing.ProfitSharingQueryDetailsEnum;
import com.xiliulou.electricity.service.pay.PayConfigBizService;
import com.xiliulou.pay.alipay.exception.AliPayException;
import com.xiliulou.pay.base.exception.PayException;
import com.xiliulou.pay.weixinv3.exception.WechatPayException;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.stereotype.Service;
import shaded.org.apache.commons.lang3.StringUtils;

import javax.annotation.Resource;
import java.util.Objects;
import java.util.Set;

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
    public BasePayConfig queryPayParams(String paymentChannel, Integer tenantId, Long franchiseeId, Set<ProfitSharingQueryDetailsEnum> queryProfitSharingConfig)
            throws PayException {
        
        if (Objects.isNull(tenantId) || Objects.isNull(franchiseeId) || StringUtils.isBlank(paymentChannel)) {
            log.error("PayConfigBizServiceImpl.queryPayParams paymentChannel:{},tenantId:{},franchiseeId:{}", paymentChannel, tenantId, franchiseeId);
            throw new PayException("参数错误");
        }
        
        return commonQuery(payConfigFactory.getStrategy(paymentChannel), paymentChannel, tenantId, franchiseeId, queryProfitSharingConfig);
    }
    
    @Override
    public BasePayConfig queryPrecisePayParams(String paymentChannel, Integer tenantId, Long franchiseeId, Set<ProfitSharingQueryDetailsEnum> queryProfitSharingConfig)
            throws PayException {
        
        if (Objects.isNull(tenantId) || Objects.isNull(franchiseeId) || StringUtils.isBlank(paymentChannel)) {
            log.error("PayConfigBizServiceImpl.queryPrecisePayParams paymentChannel:{},tenantId:{},franchiseeId:{}", paymentChannel, tenantId, franchiseeId);
            throw new PayException("参数错误");
        }
        
        return commonQuery(payConfigFactory.getPreciseStrategy(paymentChannel), paymentChannel, tenantId, franchiseeId, queryProfitSharingConfig);
    }
    
    @Override
    public boolean checkConfigConsistency(String paymentChannel, Integer tenantId, Long franchiseeId, String thirdPartyMerchantId) {
        try {
            
            BasePayConfig config = queryPrecisePayParams(paymentChannel, tenantId, franchiseeId, null);
            
            return Objects.nonNull(config) && Objects.equals(config.getThirdPartyMerchantId(), thirdPartyMerchantId);
        } catch (PayException e) {
            log.warn("PayConfigBizServiceImpl.checkConfigConsistency WARN!  PayException:", e);
            return false;
        }
    }
    
    
    /**
     * 统一查询
     *
     * @param payConfigStrategy
     * @param paymentChannel
     * @param tenantId
     * @param franchiseeId
     * @author caobotao.cbt
     * @date 2024/7/18 14:40
     */
    private BasePayConfig commonQuery(PayConfigFactory.PayConfigStrategy payConfigStrategy, String paymentChannel, Integer tenantId, Long franchiseeId,
            Set<ProfitSharingQueryDetailsEnum> queryProfitSharingConfig) throws PayException {
        if (Objects.isNull(payConfigStrategy)) {
            log.warn("PayParamsBizServiceImpl.commonQuery WARN! paymentChannel:{} is not found", paymentChannel);
            return null;
        }
        return payConfigStrategy.execute(tenantId, franchiseeId, queryProfitSharingConfig);
    }
    
    
}
