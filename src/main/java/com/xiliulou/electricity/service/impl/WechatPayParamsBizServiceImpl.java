/**
 * Copyright(c) 2018 Sunyur.com, All Rights Reserved. Author: sunyur Create date: 2024/6/14
 */

package com.xiliulou.electricity.service.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.bo.wechat.WechatPayParamsDetails;
import com.xiliulou.electricity.converter.ElectricityPayParamsConverter;
import com.xiliulou.electricity.entity.ElectricityPayParams;
import com.xiliulou.electricity.entity.WechatPaymentCertificate;
import com.xiliulou.electricity.service.ElectricityPayParamsService;
import com.xiliulou.electricity.service.WechatPayParamsBizService;
import com.xiliulou.electricity.service.WechatPaymentCertificateService;
import com.xiliulou.pay.weixinv3.exception.WechatPayException;
import com.xiliulou.pay.weixinv3.util.WechatCertificateUtils;
import com.xiliulou.pay.weixinv3.v2.query.WechatV3CommonRequest;
import com.xiliulou.pay.weixinv3.v2.service.WechatV3CommonInvokeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigInteger;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static com.xiliulou.electricity.constant.CacheConstant.WECHAT_CERTIFICATE_KEY;

/**
 * description:
 *
 * @author caobotao.cbt
 * @date 2024/6/14 13:01
 */
@Slf4j
@Service
public class WechatPayParamsBizServiceImpl implements WechatPayParamsBizService {
    
    @Resource
    private ElectricityPayParamsService electricityPayParamsService;
    
    @Resource
    private WechatV3CommonInvokeService wechatV3CommonInvokeService;
    
    @Resource
    private WechatPaymentCertificateService wechatPaymentCertificateService;
    
    @Resource
    private RedisService redisService;
    
    @Override
    public WechatPayParamsDetails getDetailsByIdTenantIdAndFranchiseeId(Integer tenantId, Long franchiseeId) throws WechatPayException {
        try {
            ElectricityPayParams payParams = electricityPayParamsService.queryCacheByTenantIdAndFranchiseeId(tenantId, franchiseeId);
            if (Objects.isNull(payParams)) {
                return null;
            }
            
            return this.buildWechatPayParamsDetails(payParams);
        } catch (Exception e) {
            log.warn("WechatPayParamsBizServiceImpl.getDetailsByIdTenantIdAndFranchiseeId :", e);
            throw new WechatPayException("支付配置获取失败!");
        }
    }
    
    
    @Override
    public WechatPayParamsDetails getPreciseDetailsByIdTenantIdAndFranchiseeId(Integer tenantId, Long franchiseeId) throws WechatPayException {
        
        try {
            ElectricityPayParams payParams = electricityPayParamsService.queryPreciseCacheByTenantIdAndFranchiseeId(tenantId, franchiseeId);
            if (Objects.isNull(payParams)) {
                return null;
            }
            
            return this.buildWechatPayParamsDetails(payParams);
            
        } catch (Exception e) {
            log.warn("WechatPayParamsBizServiceImpl.getPreciseDetailsByIdTenantIdAndFranchiseeId :", e);
            throw new WechatPayException("支付配置获取失败!");
        }
    }
    
    
    @Override
    public void refreshMerchantInfo(Integer tenantId, Long franchiseeId) {
        String key = buildCacheKey(tenantId, franchiseeId);
        redisService.delete(key);
    }
    
    
    private String buildCacheKey(Integer tenantId, Long franchiseeId) {
        return String.format(WECHAT_CERTIFICATE_KEY, tenantId, franchiseeId);
    }
    
    /**
     * 构建证书
     *
     * @param details
     * @author caobotao.cbt
     * @date 2024/6/13 09:30
     */
    private void buildWechatPlatformCertificateMap(WechatPayParamsDetails details) {
        
        String key = buildCacheKey(details.getTenantId(), details.getFranchiseeId());
        
        // 从缓存中获取证书
        String cacheStr = redisService.get(key);
        List<String> cacheList = null;
        if (StringUtils.isNotBlank(cacheStr)) {
            cacheList = JsonUtil.fromJsonArray(cacheStr, String.class);
        }
        
        if (CollectionUtils.isNotEmpty(cacheList)) {
            // 缓存中存在，直接构建证书并返回
            details.setWechatPlatformCertificateMap(buildCertificatesFromStrings(cacheList));
            return;
        }
        
        // 调用微信接口获取证书
        WechatV3CommonRequest build = WechatV3CommonRequest.builder().merchantId(details.getWechatMerchantId())
                .merchantCertificateSerialNo(details.getWechatMerchantCertificateSno()).merchantApiV3Key(details.getWechatV3ApiKey()).privateKey(details.getPrivateKey()).build();
        List<String> wechatPlatformCertificate = wechatV3CommonInvokeService.getWechatPlatformCertificate(build);
        if (CollectionUtils.isEmpty(wechatPlatformCertificate)) {
            // 没有从微信获取到证书，返回空Map
            return;
        }
        
        // 将证书添加到缓存
        redisService.set(key, JsonUtil.toJson(wechatPlatformCertificate));
        
        details.setWechatPlatformCertificateMap(buildCertificatesFromStrings(wechatPlatformCertificate));
    }
    
    
    private HashMap<BigInteger, X509Certificate> buildCertificatesFromStrings(List<String> certStrings) {
        List<X509Certificate> x509Certificates = wechatV3CommonInvokeService.buildWechatPlatformCertificate(certStrings);
        HashMap<BigInteger, X509Certificate> map = Maps.newHashMapWithExpectedSize(x509Certificates.size());
        x509Certificates.forEach(c -> map.put(c.getSerialNumber(), c));
        return map;
    }
    
    private PrivateKey getPrivateKey(ElectricityPayParams payParams) {
        WechatPaymentCertificate wechatPaymentCertificate = wechatPaymentCertificateService.queryByTenantIdAndFranchiseeId(payParams.getTenantId(), payParams.getFranchiseeId());
        if (Objects.isNull(wechatPaymentCertificate)) {
            return null;
        }
        return WechatCertificateUtils.transferCertificateContent(wechatPaymentCertificate.getCertificateContent());
    }
    
    private WechatPayParamsDetails buildWechatPayParamsDetails(ElectricityPayParams payParams) {
        WechatPayParamsDetails wechatPayParamsDetails = ElectricityPayParamsConverter.qryDoToDetails(payParams);
        
        wechatPayParamsDetails.setPrivateKey(this.getPrivateKey(payParams));
        
        this.buildWechatPlatformCertificateMap(wechatPayParamsDetails);
        
        return wechatPayParamsDetails;
    }
}
