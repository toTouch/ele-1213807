/**
 * Copyright(c) 2018 Sunyur.com, All Rights Reserved. Author: sunyur Create date: 2024/7/16
 */

package com.xiliulou.electricity.service.impl.payconfig;

import com.xiliulou.core.base.enums.ChannelEnum;
import com.xiliulou.electricity.bo.base.BasePayConfig;
import com.xiliulou.electricity.bo.wechat.WechatPayParamsDetails;
import com.xiliulou.electricity.converter.ElectricityPayParamsConverter;
import com.xiliulou.electricity.entity.ElectricityPayParams;
import com.xiliulou.electricity.enums.profitsharing.ProfitSharingQueryDetailsEnum;
import com.xiliulou.electricity.service.AlipayAppConfigService;
import com.xiliulou.electricity.service.ElectricityPayParamsService;
import com.xiliulou.electricity.service.WechatPayParamsBizService;
import com.xiliulou.electricity.service.pay.PayConfigBizService;
import com.xiliulou.pay.base.exception.PayException;
import lombok.extern.slf4j.Slf4j;
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
    private AlipayAppConfigService alipayAppConfigService;
    
    @Resource
    private WechatPayParamsBizService wechatPayParamsBizService;
    
    @Resource
    private ElectricityPayParamsService electricityPayParamsService;
    
    
    @Override
    public BasePayConfig queryPayParams(String paymentChannel, Integer tenantId, Long franchiseeId, Set<ProfitSharingQueryDetailsEnum> queryProfitSharingConfig)
            throws PayException {
        
        if (Objects.isNull(tenantId) || Objects.isNull(franchiseeId) || StringUtils.isBlank(paymentChannel)) {
            log.error("PayConfigBizServiceImpl.queryPayParams paymentChannel:{},tenantId:{},franchiseeId:{}", paymentChannel, tenantId, franchiseeId);
            throw new PayException("参数错误");
        }
        
        if (ChannelEnum.WECHAT.getCode().equals(paymentChannel)) {
            // 微信
            return wechatPayParamsBizService.getDetailsByIdTenantIdAndFranchiseeId(tenantId, franchiseeId, queryProfitSharingConfig);
            
        } else if (ChannelEnum.ALIPAY.getCode().equals(paymentChannel)) {
            // 支付宝
            return alipayAppConfigService.queryByTenantIdAndFranchiseeId(tenantId, franchiseeId);
            
        } else {
            log.error("ERROR ! paymentChannel = {}, not supports", paymentChannel);
            throw new PayException("paymentChannel=" + paymentChannel + "，not supports");
        }
    }
    
    @Override
    public BasePayConfig queryPrecisePayParams(String paymentChannel, Integer tenantId, Long franchiseeId, Set<ProfitSharingQueryDetailsEnum> queryProfitSharingConfig)
            throws PayException {
        
        if (Objects.isNull(tenantId) || Objects.isNull(franchiseeId) || StringUtils.isBlank(paymentChannel)) {
            log.error("PayConfigBizServiceImpl.queryPrecisePayParams paymentChannel:{},tenantId:{},franchiseeId:{}", paymentChannel, tenantId, franchiseeId);
            throw new PayException("参数错误");
        }
        
        if (ChannelEnum.WECHAT.getCode().equals(paymentChannel)) {
            // 微信
            return wechatPayParamsBizService.getPreciseCacheByTenantIdAndFranchiseeId(tenantId, franchiseeId, queryProfitSharingConfig);
            
        } else if (ChannelEnum.ALIPAY.getCode().equals(paymentChannel)) {
            // 支付宝
            return alipayAppConfigService.queryPreciseByTenantIdAndFranchiseeId(tenantId, franchiseeId);
            
        } else {
            log.error("ERROR ! paymentChannel = {}, not supports", paymentChannel);
            throw new PayException("paymentChannel=" + paymentChannel + "，not supports");
        }
        
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
    
    @Override
    public BasePayConfig querySimplePrecisePayParams(String paymentChannel, Integer tenantId, Long franchiseeId) throws PayException {
        
        if (ChannelEnum.WECHAT.getCode().equals(paymentChannel)) {
            // 微信
            ElectricityPayParams payParams = electricityPayParamsService.queryPreciseCacheByTenantIdAndFranchiseeId(tenantId, franchiseeId);
            if (Objects.isNull(payParams)){
                return null;
            }
            WechatPayParamsDetails wechatPayParamsDetails = ElectricityPayParamsConverter.qryDoToDetails(payParams);
            return wechatPayParamsDetails;
        } else if (ChannelEnum.ALIPAY.getCode().equals(paymentChannel)) {
            // 支付宝
            return alipayAppConfigService.queryPreciseByTenantIdAndFranchiseeId(tenantId, franchiseeId);
        
        } else {
            log.error("ERROR ! paymentChannel = {}, not supports", paymentChannel);
            throw new PayException("paymentChannel=" + paymentChannel + "，not supports");
        }
    }
    
    
}
