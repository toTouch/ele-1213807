package com.xiliulou.electricity.service.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.electricity.entity.ElectricityPayParams;
import com.xiliulou.electricity.entity.WechatPaymentCertificate;
import com.xiliulou.electricity.service.ElectricityPayParamsService;
import com.xiliulou.electricity.service.TenantService;
import com.xiliulou.electricity.service.WechatPaymentCertificateService;
import com.xiliulou.pay.weixinv3.common.request.GetWechatPlatformCertificateRequest;
import com.xiliulou.pay.weixinv3.common.request.WechatCommonParam;
import com.xiliulou.pay.weixinv3.franchisee.request.WechatV3FranchiseeMerchantLoadRequest;
import com.xiliulou.pay.weixinv3.franchisee.service.WechatV3CommonFranchiseeService;
import com.xiliulou.pay.weixinv3.franchisee.service.WechatV3FranchiseeMerchantLoadAndUpdateCertificateService;
import com.xiliulou.pay.weixinv3.util.WechatCertificateUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationServiceException;
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


@Service
@Slf4j
public class DbWechatV3FranchiseeMerchantLoadAndUpdateCertificateServiceImpl implements WechatV3FranchiseeMerchantLoadAndUpdateCertificateService {
    
    @Resource
    WechatV3CommonFranchiseeService wechatV3CommonFranchiseeService;
    
    @Autowired
    TenantService tenantService;
    
    @Autowired
    ElectricityPayParamsService electricityPayParamsService;
    
    @Autowired
    private WechatPaymentCertificateService wechatPaymentCertificateService;
    
    @Resource
    private RedisService redisService;
    
    
    /**
     * 参数构建
     *
     * @param request
     * @param commonParam
     * @author caobotao.cbt
     * @date 2024/6/12 11:04
     */
    @Override
    public void buildWechatCommonParam(WechatV3FranchiseeMerchantLoadRequest request, WechatCommonParam commonParam) {
        
        try {
            ElectricityPayParams payParams = electricityPayParamsService.queryCacheByTenantIdAndFranchiseeId(request.getTenantId(), request.getFranchiseeId());
            if (Objects.isNull(payParams)) {
                throw new AuthenticationServiceException("未能查找到appId和appSecret！");
            }
            commonParam.setMerchantId(payParams.getWechatMerchantId());
            commonParam.setMerchantCertificateSerialNo(payParams.getWechatMerchantCertificateSno());
            commonParam.setMerchantApiV3Key(payParams.getWechatV3ApiKey());
            
            PrivateKey privateKey = this.getPrivateKey(payParams);
            if (Objects.isNull(privateKey)) {
                return;
            }
            commonParam.setPrivateKey(privateKey);
            
            HashMap<BigInteger, X509Certificate> wechatPlatformCertificateMap = this.getWechatPlatformCertificateMap(request, payParams, privateKey);
            commonParam.setWechatPlatformCertificateMap(wechatPlatformCertificateMap);
        } catch (Exception e) {
            throw new RuntimeException("获取商户支付配置失败！tenantId=" + request.getTenantId() + "franchiseeId=" + request.getFranchiseeId(), e);
        }
        
    }
    
    @Override
    public String getMerchantApiV3Key(WechatV3FranchiseeMerchantLoadRequest request) {
        try {
            ElectricityPayParams payParams = electricityPayParamsService.queryCacheByTenantIdAndFranchiseeId(request.getTenantId(), request.getFranchiseeId());
            if (Objects.isNull(payParams)) {
                throw new AuthenticationServiceException("未能查找到appId和appSecret！");
            }
            return payParams.getWechatV3ApiKey();
        } catch (Exception e) {
            throw new RuntimeException("获取商户apiV3密钥失败！tenantId=" + request.getTenantId() + "franchiseeId=" + request.getFranchiseeId(), e);
        }
    }
    
    
    @Override
    public void refreshMerchantInfo(WechatV3FranchiseeMerchantLoadRequest request) {
        String key = buildCacheKey(request.getTenantId(), request.getFranchiseeId());
        redisService.delete(key);
    }
    
    
    private String buildCacheKey(Integer tenantId, Long franchiseeId) {
        return String.format(WECHAT_CERTIFICATE_KEY, tenantId, franchiseeId);
    }
    
    /**
     * 支付证书获取
     *
     * @param request
     * @param payParams
     * @param privateKey
     * @author caobotao.cbt
     * @date 2024/6/13 09:30
     */
    private HashMap<BigInteger, X509Certificate> getWechatPlatformCertificateMap(WechatV3FranchiseeMerchantLoadRequest request, ElectricityPayParams payParams,
            PrivateKey privateKey) {
        
        String key = buildCacheKey(request.getTenantId(), request.getFranchiseeId());
        
        // 从缓存中获取证书
        Set<String> cacheSet = redisService.getAllElementsFromSet(key);
        
        List<String> cacheList = Lists.newArrayList(cacheSet);
        
        if (CollectionUtils.isNotEmpty(cacheList)) {
            // 缓存中存在，直接构建证书并返回
            return buildCertificatesFromStrings(cacheList);
        }
        
        // 调用微信接口获取证书
        GetWechatPlatformCertificateRequest builder = GetWechatPlatformCertificateRequest.builder().merchantId(payParams.getWechatMerchantId())
                .merchantCertificateSerialNo(payParams.getWechatMerchantCertificateSno()).merchantApiV3Key(payParams.getWechatV3ApiKey()).privateKey(privateKey).build();
        List<String> wechatPlatformCertificate = wechatV3CommonFranchiseeService.getWechatPlatformCertificate(builder);
        if (CollectionUtils.isEmpty(wechatPlatformCertificate)) {
            // 没有从微信获取到证书，返回空Map
            return new HashMap<>();
        }
        
        // 将证书添加到缓存
        redisService.addAllElementsFromSet(key, wechatPlatformCertificate);
        
        return buildCertificatesFromStrings(wechatPlatformCertificate);
    }
    
    
    private HashMap<BigInteger, X509Certificate> buildCertificatesFromStrings(List<String> certStrings) {
        List<X509Certificate> x509Certificates = wechatV3CommonFranchiseeService.buildWechatPlatformCertificate(certStrings);
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
}
