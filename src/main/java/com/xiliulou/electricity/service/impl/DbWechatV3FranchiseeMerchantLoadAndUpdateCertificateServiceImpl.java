package com.xiliulou.electricity.service.impl;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.google.common.collect.Maps;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.utils.DataUtil;
import com.xiliulou.electricity.entity.ElectricityPayParams;
import com.xiliulou.electricity.entity.Tenant;
import com.xiliulou.electricity.entity.WechatPaymentCertificate;
import com.xiliulou.electricity.mapper.WechatPaymentCertificateMapper;
import com.xiliulou.electricity.service.ElectricityPayParamsService;
import com.xiliulou.electricity.service.TenantService;
import com.xiliulou.electricity.service.WechatPaymentCertificateService;
import com.xiliulou.pay.weixinv3.common.request.GetWechatPlatformCertificateRequest;
import com.xiliulou.pay.weixinv3.common.request.WechatCommonParam;
import com.xiliulou.pay.weixinv3.franchisee.request.WechatV3FranchiseeMerchantLoadRequest;
import com.xiliulou.pay.weixinv3.franchisee.service.WechatV3CommonFranchiseeService;
import com.xiliulou.pay.weixinv3.franchisee.service.WechatV3FranchiseeMerchantLoadAndUpdateCertificateService;
import com.xiliulou.pay.weixinv3.service.WechatV3CommonService;
import com.xiliulou.pay.weixinv3.service.WechatV3MerchantLoadAndUpdateCertificateService;
import com.xiliulou.pay.weixinv3.util.WechatCertificateUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.math.BigInteger;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.xiliulou.electricity.constant.CacheConstant.PAYMENT_CERTIFICATE_KEY;
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
    
    @Autowired
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
        ElectricityPayParams payParams = electricityPayParamsService.queryCacheByTenantIdAndFranchiseeId(request.getTenantId(), request.getFranchiseeId());
        
        // 缓存查询
        // TODO: 2024/6/12 缓存是循环 需要调整
        List<X509Certificate> cacheList = redisService.getWithList(buildCacheKey(request.getTenantId(), request.getFranchiseeId()), X509Certificate.class);
        PrivateKey privateKey = null;
        if (CollectionUtils.isEmpty(cacheList)) {
            WechatPaymentCertificate wechatPaymentCertificate = wechatPaymentCertificateService
                    .queryByTenantIdAndFranchiseeId(payParams.getTenantId(), payParams.getFranchiseeId());
            privateKey = WechatCertificateUtils.transferCertificateContent(wechatPaymentCertificate.getCertificateContent());
            GetWechatPlatformCertificateRequest builder = GetWechatPlatformCertificateRequest.builder().merchantId(payParams.getWechatMerchantId())
                    .merchantCertificateSerialNo(payParams.getWechatMerchantCertificateSno()).merchantApiV3Key(payParams.getWechatV3ApiKey()).privateKey(privateKey).build();
            cacheList = wechatV3CommonFranchiseeService.getWechatPlatformCertificate(builder);
            if (CollectionUtils.isNotEmpty(cacheList)) {
                // TODO: 2024/6/12 缓存是循环 需要调整
                //                redisService.saveWithList();
            }
        }
        HashMap<BigInteger, X509Certificate> wechatPlatformCertificateMap = Maps.newHashMap();
        
        Optional.ofNullable(cacheList).orElse(Collections.emptyList()).forEach(p -> {
            wechatPlatformCertificateMap.put(p.getSerialNumber(), p);
        });
        
        commonParam.setMerchantId(payParams.getWechatMerchantId());
        commonParam.setMerchantCertificateSerialNo(payParams.getWechatMerchantCertificateSno());
        commonParam.setPrivateKey(privateKey);
        commonParam.setMerchantApiV3Key(payParams.getWechatV3ApiKey());
        commonParam.setWechatPlatformCertificateMap(wechatPlatformCertificateMap);
    }
    
    @Override
    public void refreshMerchantInfo(WechatV3FranchiseeMerchantLoadRequest request) {
    
    }
    
    
    private String buildCacheKey(Integer tenantId, Long franchiseeId) {
        return String.format(WECHAT_CERTIFICATE_KEY, tenantId, franchiseeId);
    }
    
    
}
