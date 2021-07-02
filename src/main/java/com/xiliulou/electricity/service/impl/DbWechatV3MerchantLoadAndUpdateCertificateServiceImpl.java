package com.xiliulou.electricity.service.impl;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.google.common.collect.Maps;
import com.xiliulou.core.utils.DataUtil;
import com.xiliulou.electricity.entity.ElectricityPayParams;
import com.xiliulou.electricity.entity.Tenant;
import com.xiliulou.electricity.service.ElectricityPayParamsService;
import com.xiliulou.electricity.service.TenantService;
import com.xiliulou.pay.weixinv3.service.WechatV3CommonService;
import com.xiliulou.pay.weixinv3.service.WechatV3MerchantLoadAndUpdateCertificateService;
import com.xiliulou.pay.weixinv3.util.WechatCertificateUtils;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.math.BigInteger;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author: eclair
 * @Date: 2021/2/26 09:41
 * @Description:
 */
@Service
@Slf4j
public class DbWechatV3MerchantLoadAndUpdateCertificateServiceImpl implements WechatV3MerchantLoadAndUpdateCertificateService {

    @Autowired
    WechatV3CommonService wechatV3CommonService;

    @Autowired
    TenantService tenantService;

    @Autowired
    ElectricityPayParamsService electricityPayParamsService;


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

    @Override
    public PrivateKey getMerchantCertificatePrivateKey(Integer tenantId) {
        try {
            ElectricityPayParams electricityPayParams = electricityPayParamsService.queryFromCache(tenantId);
            log.info("electricityPayParams is -->{}",electricityPayParams);
            return WechatCertificateUtils.getPrivateKey(electricityPayParams.getWechatMerchantPrivateKeyPath());
        } catch (Exception e) {
            throw new RuntimeException("获取私钥失败！tenantId=" + tenantId, e);
        }
    }

    @Override
    public String getMerchantCertificateSerialNo(Integer tenantId) {
        try {
            ElectricityPayParams electricityPayParams = electricityPayParamsService.queryFromCache(tenantId);
            return electricityPayParams.getWechatMerchantCertificateSno();
        } catch (Exception e) {
            throw new RuntimeException("获取商户证书序列号失败！tenantId=" + tenantId, e);
        }
    }

    @Override
    public HashMap<BigInteger, X509Certificate> getWechatPlatformCertificate(Integer tenantId) {
        return wechatCertificateCache.get(tenantId);
    }

    @Override
    public String getMerchantId(Integer tenantId) {
        try {
            ElectricityPayParams electricityPayParams = electricityPayParamsService.queryFromCache(tenantId);
            return electricityPayParams.getWechatMerchantId();
        } catch (Exception e) {
            throw new RuntimeException("获取商户id失败！tenantId=" + tenantId, e);
        }
    }

    @Override
    public String getMerchantApiV3Key(Integer tenantId) {
        try {
            ElectricityPayParams electricityPayParams = electricityPayParamsService.queryFromCache(tenantId);
            return electricityPayParams.getWechatV3ApiKey();
        } catch (Exception e) {
            throw new RuntimeException("获取商户apiV3密钥失败！tenantId=" + tenantId, e);
        }
    }
}
