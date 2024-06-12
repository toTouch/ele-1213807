package com.xiliulou.electricity.service.impl;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.google.common.collect.Maps;
import com.xiliulou.core.utils.DataUtil;
import com.xiliulou.electricity.entity.ElectricityPayParams;
import com.xiliulou.electricity.entity.Tenant;
import com.xiliulou.electricity.entity.WechatPaymentCertificate;
import com.xiliulou.electricity.mapper.WechatPaymentCertificateMapper;
import com.xiliulou.electricity.service.ElectricityPayParamsService;
import com.xiliulou.electricity.service.TenantService;
import com.xiliulou.pay.weixinv3.common.request.WechatCommonParam;
import com.xiliulou.pay.weixinv3.franchisee.request.WechatV3FranchiseeMerchantLoadRequest;
import com.xiliulou.pay.weixinv3.franchisee.service.WechatV3FranchiseeMerchantLoadAndUpdateCertificateService;
import com.xiliulou.pay.weixinv3.service.WechatV3CommonService;
import com.xiliulou.pay.weixinv3.service.WechatV3MerchantLoadAndUpdateCertificateService;
import com.xiliulou.pay.weixinv3.util.WechatCertificateUtils;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.math.BigInteger;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;


@Service
@Slf4j
public class DbWechatV3FraniseeMerchantLoadAndUpdateCertificateServiceImpl implements WechatV3FranchiseeMerchantLoadAndUpdateCertificateService {
    
    @Autowired
    WechatV3CommonService wechatV3CommonService;
    
    @Autowired
    TenantService tenantService;
    
    @Autowired
    ElectricityPayParamsService electricityPayParamsService;
    
    @Autowired
    private WechatPaymentCertificateMapper wechatPaymentCertificateMapper;
    
    LoadingCache<Integer, HashMap<BigInteger, X509Certificate>> wechatCertificateCache = null;
    
    @PostConstruct
    public void init() {
        wechatCertificateCache = Caffeine.newBuilder().refreshAfterWrite(9, TimeUnit.HOURS).build(new CacheLoader<Integer, HashMap<BigInteger, X509Certificate>>() {
            @Nullable
            @Override
            public HashMap<BigInteger, X509Certificate> load(@NonNull Integer tenantId) throws Exception {
                Tenant tenantEntity = tenantService.queryByIdFromCache(tenantId);
                if (Objects.isNull(tenantEntity)) {
                    log.error("DB WECHAT CERTIFICATE ERROR! not found tenantId! tenantId={}", tenantId);
                    return null;
                }
                
                List<X509Certificate> wechatPlatformCertificate = wechatV3CommonService.getWechatPlatformCertificate(tenantId);
                if (!DataUtil.collectionIsUsable(wechatPlatformCertificate)) {
                    return null;
                }
                
                HashMap<BigInteger, X509Certificate> result = Maps.newHashMap();
                wechatPlatformCertificate.forEach(e -> {
                    result.put(e.getSerialNumber(), e);
                });
                return result;
            }
        });
    }
    
    
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
    
    }
    
    @Override
    public void refreshMerchantInfo(WechatV3FranchiseeMerchantLoadRequest request) {
    
    }
}
