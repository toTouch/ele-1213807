/**
 * Copyright(c) 2018 Sunyur.com, All Rights Reserved. Author: sunyur Create date: 2024/6/14
 */

package com.xiliulou.electricity.service.impl;

import com.google.common.collect.Maps;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.bo.wechat.WechatPayParamsDetails;
import com.xiliulou.electricity.converter.ElectricityPayParamsConverter;
import com.xiliulou.electricity.entity.ElectricityPayParams;
import com.xiliulou.electricity.entity.WechatPaymentCertificate;
import com.xiliulou.electricity.entity.payparams.WechatCertificateCacheEntity;
import com.xiliulou.electricity.entity.profitsharing.ProfitSharingConfig;
import com.xiliulou.electricity.entity.profitsharing.ProfitSharingReceiverConfig;
import com.xiliulou.electricity.enums.profitsharing.ProfitSharingQueryDetailsEnum;
import com.xiliulou.electricity.service.ElectricityPayParamsService;
import com.xiliulou.electricity.service.WechatPayParamsBizService;
import com.xiliulou.electricity.service.WechatPaymentCertificateService;
import com.xiliulou.electricity.service.profitsharing.ProfitSharingConfigService;
import com.xiliulou.electricity.service.profitsharing.ProfitSharingReceiverConfigService;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.concurrent.TimeUnit;

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
    private ProfitSharingReceiverConfigService profitSharingReceiverConfigService;
    
    @Resource
    private ProfitSharingConfigService profitSharingConfigService;
    
    @Resource
    private RedisService redisService;
    
    /**
     * 缓存过期时间-毫秒（2小时）
     */
    public static final Long CACHE_TIME_OUT = 3600000L * 2L;
    
    
    @Override
    public WechatPayParamsDetails getDetailsByIdTenantIdAndFranchiseeId(Integer tenantId, Long franchiseeId, Set<ProfitSharingQueryDetailsEnum> queryProfitSharingConfig)
            throws WechatPayException {
        try {
            WechatPayParamsDetails wechatPayParamsDetails = this.getDetailsByIdTenantIdAndFranchiseeId(tenantId, franchiseeId);
            if (Objects.isNull(wechatPayParamsDetails)) {
                return null;
            }
            if (CollectionUtils.isEmpty(queryProfitSharingConfig)) {
                return wechatPayParamsDetails;
            }
            this.buildProfitSharing(queryProfitSharingConfig, wechatPayParamsDetails);
            return wechatPayParamsDetails;
        } catch (Exception e) {
            log.warn("WechatPayParamsBizServiceImpl.getDetailsByIdTenantIdAndFranchiseeId :", e);
            throw new WechatPayException("支付配置获取失败!");
        }
        
        
    }
    
    
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
    public WechatPayParamsDetails getPreciseCacheByTenantIdAndFranchiseeId(Integer tenantId, Long franchiseeId, Set<ProfitSharingQueryDetailsEnum> queryProfitSharingConfig)
            throws WechatPayException {
        try {
            ElectricityPayParams payParams = electricityPayParamsService.queryPreciseCacheByTenantIdAndFranchiseeId(tenantId, franchiseeId);
            if (Objects.isNull(payParams)) {
                return null;
            }
            
            WechatPayParamsDetails wechatPayParamsDetails = this.buildWechatPayParamsDetails(payParams);
            
            this.buildProfitSharing(queryProfitSharingConfig, wechatPayParamsDetails);
            
            return wechatPayParamsDetails;
        } catch (Exception e) {
            log.warn("WechatPayParamsBizServiceImpl.getPreciseCacheByTenantIdAndFranchiseeId :", e);
            throw new WechatPayException("支付配置获取失败!");
        }
    }
    
    @Override
    public List<WechatPayParamsDetails> queryListPreciseCacheByTenantIdAndFranchiseeIds(Integer tenantId, Set<Long> franchiseeIds,
            Set<ProfitSharingQueryDetailsEnum> queryProfitSharingConfig) throws WechatPayException {
        try {
            List<ElectricityPayParams> electricityPayParams = electricityPayParamsService.queryListPreciseCacheByTenantIdAndFranchiseeId(tenantId, franchiseeIds);
            if (CollectionUtils.isEmpty(electricityPayParams)) {
                return null;
            }
            // 批量构建
            List<WechatPayParamsDetails> wechatPayParamsDetailsList = ElectricityPayParamsConverter.qryDoToDetailsList(electricityPayParams);
            
            // 批量构建私钥
            this.batchBuildPrivateKey(tenantId, wechatPayParamsDetailsList);
            
            // 批量构建证书
            this.batchBuildWechatPlatformCertificateMap(wechatPayParamsDetailsList);
            
            //批量构建分账配置
            this.batchBuildProfitSharing(queryProfitSharingConfig, tenantId, wechatPayParamsDetailsList);
            
            return wechatPayParamsDetailsList;
        } catch (Exception e) {
            log.warn("WechatPayParamsBizServiceImpl.queryListPreciseCacheByTenantIdAndFranchiseeIds :", e);
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
     * 批量构建私钥
     *
     * @param wechatPayParamsDetailsList
     * @author caobotao.cbt
     * @date 2024/8/27 14:33
     */
    private void batchBuildPrivateKey(Integer tenantId, List<WechatPayParamsDetails> wechatPayParamsDetailsList) {
        List<Long> franchiseeIds = wechatPayParamsDetailsList.stream().map(WechatPayParamsDetails::getFranchiseeId).collect(Collectors.toList());
        List<WechatPaymentCertificate> certificates = wechatPaymentCertificateService.queryListByTenantIdAndFranchiseeIds(tenantId, franchiseeIds);
        if (CollectionUtils.isEmpty(certificates)) {
            return;
        }
        Map<Long, WechatPaymentCertificate> map = certificates.stream().collect(Collectors.toMap(WechatPaymentCertificate::getFranchiseeId, Function.identity(), (k1, k2) -> k1));
        wechatPayParamsDetailsList.forEach(payParamsDetails -> {
            WechatPaymentCertificate wechatPaymentCertificate = map.get(payParamsDetails.getFranchiseeId());
            if (Objects.isNull(wechatPaymentCertificate)) {
                return;
            }
            payParamsDetails.setPrivateKey(WechatCertificateUtils.transferCertificateContent(wechatPaymentCertificate.getCertificateContent()));
        });
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
        WechatCertificateCacheEntity entity = null;
        if (StringUtils.isNotBlank(cacheStr)) {
            entity = JsonUtil.fromJson(cacheStr, WechatCertificateCacheEntity.class);
            details.setWechatPlatformCertificateMap(buildCertificatesFromStrings(entity.getCertificates()));
            return;
        }
        
        List<String> wechatPlatformCertificate = this.queryWechatCertificate(details);
        
        if (CollectionUtils.isEmpty(wechatPlatformCertificate)) {
            // 没有从微信获取到证书，返回空Map
            return;
        }
        details.setWechatPlatformCertificateMap(buildCertificatesFromStrings(wechatPlatformCertificate));
        
        // 将证书添加到缓存
        redisService.set(key, JsonUtil.toJson(new WechatCertificateCacheEntity(details.getTenantId(), details.getFranchiseeId(), wechatPlatformCertificate)), CACHE_TIME_OUT,
                TimeUnit.MILLISECONDS);
        
    }
    
    
    /**
     * 查询微信证书
     *
     * @param details
     * @author caobotao.cbt
     * @date 2024/8/27 14:13
     */
    private List<String> queryWechatCertificate(WechatPayParamsDetails details) {
        // 调用微信接口获取证书
        WechatV3CommonRequest build = WechatV3CommonRequest.builder().merchantId(details.getWechatMerchantId())
                .merchantCertificateSerialNo(details.getWechatMerchantCertificateSno()).merchantApiV3Key(details.getWechatV3ApiKey()).privateKey(details.getPrivateKey()).build();
        return wechatV3CommonInvokeService.getWechatPlatformCertificate(build);
    }
    
    /**
     * 批量构建微信证书
     *
     * @param detailsList
     * @author caobotao.cbt
     * @date 2024/8/27 14:09
     */
    private void batchBuildWechatPlatformCertificateMap(List<WechatPayParamsDetails> detailsList) {
        List<String> keyList = detailsList.stream().map(d -> buildCacheKey(d.getTenantId(), d.getFranchiseeId())).collect(Collectors.toList());
        
        // 从缓存中获取证书
        List<WechatCertificateCacheEntity> cacheList = redisService.multiJsonGet(keyList, WechatCertificateCacheEntity.class);
        
        Map<String, WechatCertificateCacheEntity> cacheMap = Optional.ofNullable(cacheList).orElse(Collections.emptyList()).stream().filter(Objects::nonNull)
                .collect(Collectors.toMap(cache -> buildCacheKey(cache.getTenantId(), cache.getFranchiseeId()), Function.identity(), (k1, k2) -> k1));
        
        Map<String, String> cacheSaveMap = Maps.newHashMap();
        
        detailsList.forEach(details -> {
            
            WechatCertificateCacheEntity wechatCertificateCacheEntity = cacheMap.get(buildCacheKey(details.getTenantId(), details.getFranchiseeId()));
            
            if (Objects.nonNull(wechatCertificateCacheEntity)) {
                details.setWechatPlatformCertificateMap(buildCertificatesFromStrings(wechatCertificateCacheEntity.getCertificates()));
                return;
            }
            
            // 调用微信接口获取证书
            List<String> wechatPlatformCertificate = this.queryWechatCertificate(details);
            details.setWechatPlatformCertificateMap(buildCertificatesFromStrings(wechatPlatformCertificate));
            
            //缓存添加
            cacheSaveMap.put(buildCacheKey(details.getTenantId(), details.getFranchiseeId()),
                    JsonUtil.toJson(new WechatCertificateCacheEntity(details.getTenantId(), details.getFranchiseeId(), wechatPlatformCertificate)));
        });
        
        if (cacheSaveMap.isEmpty()) {
            return;
        }
        // 将证书添加到缓存
        redisService.multiSet(cacheSaveMap, CACHE_TIME_OUT, TimeUnit.MILLISECONDS);
    }
    
    
    private HashMap<BigInteger, X509Certificate> buildCertificatesFromStrings(List<String> certStrings) {
        List<X509Certificate> x509Certificates = wechatV3CommonInvokeService.buildWechatPlatformCertificate(certStrings);
        HashMap<BigInteger, X509Certificate> map = Maps.newHashMapWithExpectedSize(x509Certificates.size());
        x509Certificates.forEach(c -> map.put(c.getSerialNumber(), c));
        return map;
    }
    
    /**
     * 私钥获取
     *
     * @param payParams
     * @author caobotao.cbt
     * @date 2024/8/27 14:17
     */
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
    
    /**
     * 构建分账相关
     *
     * @param queryProfitSharingConfig
     * @param wechatPayParamsDetails
     * @author caobotao.cbt
     * @date 2024/8/27 10:29
     */
    private void buildProfitSharing(Set<ProfitSharingQueryDetailsEnum> queryProfitSharingConfig, WechatPayParamsDetails wechatPayParamsDetails) {
        if (CollectionUtils.isEmpty(queryProfitSharingConfig)) {
            return;
        }
        
        if (!queryProfitSharingConfig.contains(ProfitSharingQueryDetailsEnum.PROFIT_SHARING_CONFIG) && !queryProfitSharingConfig
                .contains(ProfitSharingQueryDetailsEnum.PROFIT_SHARING_CONFIG_AND_RECEIVER_CONFIG)) {
            return;
        }
        
        // 查询分账方配置
        ProfitSharingConfig profitSharingConfig = profitSharingConfigService.queryByPayParamsIdFromCache(wechatPayParamsDetails.getTenantId(), wechatPayParamsDetails.getId());
        wechatPayParamsDetails.setProfitSharingConfig(profitSharingConfig);
        
        if (Objects.isNull(profitSharingConfig) || !queryProfitSharingConfig.contains(ProfitSharingQueryDetailsEnum.PROFIT_SHARING_CONFIG_AND_RECEIVER_CONFIG)) {
            // 分账方配置不存在或者无需查询接收方
            return;
        }
        
        //查询分账接收方信息
        List<ProfitSharingReceiverConfig> profitSharingReceiverConfigs = profitSharingReceiverConfigService
                .queryListByProfitSharingConfigId(profitSharingConfig.getTenantId(), profitSharingConfig.getId());
        
        wechatPayParamsDetails.setProfitSharingReceiverConfigs(profitSharingReceiverConfigs);
        
    }
    
    
    private void batchBuildProfitSharing(Set<ProfitSharingQueryDetailsEnum> queryProfitSharingConfig, Integer tenantId, List<WechatPayParamsDetails> wechatPayParamsDetailsList) {
        
        if (CollectionUtils.isEmpty(queryProfitSharingConfig)) {
            return;
        }
        
        if (!queryProfitSharingConfig.contains(ProfitSharingQueryDetailsEnum.PROFIT_SHARING_CONFIG) && !queryProfitSharingConfig
                .contains(ProfitSharingQueryDetailsEnum.PROFIT_SHARING_CONFIG_AND_RECEIVER_CONFIG)) {
            return;
        }
        
        List<Integer> payParamIds = wechatPayParamsDetailsList.stream().map(WechatPayParamsDetails::getId).collect(Collectors.toList());
        List<ProfitSharingConfig> sharingConfigs = profitSharingConfigService.queryListByPayParamsIdsFromCache(tenantId, payParamIds);
        if (CollectionUtils.isEmpty(sharingConfigs)) {
            return;
        }
        
        Map<Integer, ProfitSharingConfig> payParamIdMap = Maps.newHashMap();
        
        List<Long> profitSharingConfigIds = new ArrayList<>();
        
        sharingConfigs.forEach(sharingConfig -> {
            profitSharingConfigIds.add(sharingConfig.getId());
            payParamIdMap.put(sharingConfig.getPayParamId(), sharingConfig);
        });
        
        if (!queryProfitSharingConfig.contains(ProfitSharingQueryDetailsEnum.PROFIT_SHARING_CONFIG_AND_RECEIVER_CONFIG)) {
            // 分账方配置不存在或者无需查询接收方
            wechatPayParamsDetailsList.forEach(payParamsDetails -> payParamsDetails.setProfitSharingConfig(payParamIdMap.get(payParamsDetails.getId())));
            return;
        }
        
        List<ProfitSharingReceiverConfig> profitSharingReceiverConfigs = profitSharingReceiverConfigService.queryListByProfitSharingConfigIds(tenantId, profitSharingConfigIds);
        Map<Long, List<ProfitSharingReceiverConfig>> receiverConfigMap = Optional.ofNullable(profitSharingReceiverConfigs).orElse(Collections.emptyList()).stream()
                .collect(Collectors.groupingBy(ProfitSharingReceiverConfig::getProfitSharingConfigId));
        
        wechatPayParamsDetailsList.forEach(payParamsDetails -> {
            ProfitSharingConfig profitSharingConfig = payParamIdMap.get(payParamsDetails.getId());
            if (Objects.isNull(profitSharingConfig)) {
                return;
            }
            payParamsDetails.setProfitSharingConfig(profitSharingConfig);
            payParamsDetails.setProfitSharingReceiverConfigs(receiverConfigMap.get(profitSharingConfig.getId()));
        });
    }
    
}
