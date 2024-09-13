package com.xiliulou.electricity.service.impl;

import com.google.common.collect.Maps;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.entity.WechatPaymentCertificate;
import com.xiliulou.electricity.mapper.WechatPaymentCertificateMapper;
import com.xiliulou.electricity.service.WechatPayParamsBizService;
import com.xiliulou.electricity.service.WechatPaymentCertificateService;
import com.xiliulou.pay.weixinv3.util.WechatCertificateUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.xiliulou.electricity.constant.CacheConstant.PAYMENT_CERTIFICATE_KEY;

/**
 * @author wangchen
 */
@Service
@Slf4j
public class WechatPaymentCertificateServiceImpl implements WechatPaymentCertificateService {
    
    @Resource
    private WechatPaymentCertificateMapper wechatPaymentCertificateMapper;
    
    @Autowired
    private RedisService redisService;
    
    @Autowired
    private WechatPayParamsBizService wechatPayParamsBizService;
    
    @Override
    public void saveOrUpdateWeChatPaymentCertificate(WechatPaymentCertificate certificate) {
        WechatPaymentCertificate wechatPaymentCertificate = queryByTenantIdAndFranchiseeId(certificate.getTenantId(), certificate.getFranchiseeId());
        if (Objects.nonNull(wechatPaymentCertificate)) {
            certificate.setId(wechatPaymentCertificate.getId());
            wechatPaymentCertificateMapper.updateByIdAndTenantId(certificate);
            this.deleteCache(certificate.getTenantId(), certificate.getFranchiseeId());
        } else {
            wechatPaymentCertificateMapper.insert(certificate);
        }
    }
    
    
    @Override
    public void handleCertificateFile(MultipartFile file, WechatPaymentCertificate wechatPaymentCertificate) throws Exception {
        InputStream inputStream = null;
        InputStreamReader streamReader = null;
        BufferedReader reader = null;
        try {
            //支付证书
            String fileName = file.getOriginalFilename();
            inputStream = file.getInputStream();
            streamReader = new InputStreamReader(inputStream);
            reader = new BufferedReader(streamReader);
            String line;
            StringBuilder stringBuilder = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
            //解析文件内容
            String privateKey = WechatCertificateUtils.analysisCertificateToPrivateKey(stringBuilder.toString());
            if (StringUtils.isEmpty(privateKey)) {
                log.error("certificate content is empty, tenantId={}, fileName={}", wechatPaymentCertificate.getTenantId(), fileName);
                throw new Exception("证书内容为空!");
            }
            wechatPaymentCertificate.setCertificateContent(privateKey);
            wechatPaymentCertificate.setUploadTime(System.currentTimeMillis());
            //存储微信支付证书
            saveOrUpdateWeChatPaymentCertificate(wechatPaymentCertificate);
        } catch (Exception e) {
            log.error("certificate get error:", e);
            throw new Exception("证书内容获取失败，请重试！");
        } finally {
            try {
                if (Objects.nonNull(reader)) {
                    reader.close();
                }
                if (Objects.nonNull(streamReader)) {
                    streamReader.close();
                }
                if (Objects.nonNull(inputStream)) {
                    inputStream.close();
                }
            } catch (IOException e) {
                log.error("close IO error.", e);
            }
        }
    }
    
    @Override
    public WechatPaymentCertificate queryByTenantIdAndFranchiseeId(Integer tenantId, Long franchiseeId) {
        String cache = redisService.get(buildCacheKey(tenantId, franchiseeId));
        WechatPaymentCertificate wechatPaymentCertificate;
        if (StringUtils.isBlank(cache)) {
            wechatPaymentCertificate = wechatPaymentCertificateMapper.selectByTenantIdAndFranchiseeId(tenantId, franchiseeId);
            if (Objects.nonNull(wechatPaymentCertificate)) {
                redisService.set(buildCacheKey(tenantId, franchiseeId), JsonUtil.toJson(wechatPaymentCertificate));
            }
        } else {
            wechatPaymentCertificate = JsonUtil.fromJson(cache, WechatPaymentCertificate.class);
        }
        return wechatPaymentCertificate;
    }
    
    
    @Override
    public List<WechatPaymentCertificate> queryListByTenantIdAndFranchiseeIds(Integer tenantId, List<Long> franchiseeIds) {
        
        List<String> cacheKeys = franchiseeIds.stream().map(franchiseeId -> buildCacheKey(tenantId, franchiseeId)).collect(Collectors.toList());
        List<WechatPaymentCertificate> wechatPaymentCertificates = redisService.multiJsonGet(cacheKeys, WechatPaymentCertificate.class);
        
        Map<Long, WechatPaymentCertificate> existCacheMap = Optional.ofNullable(wechatPaymentCertificates).orElse(Collections.emptyList()).stream()
                .collect(Collectors.toMap(WechatPaymentCertificate::getFranchiseeId, Function.identity(), (k1, k2) -> k1));
        
        List<WechatPaymentCertificate> certificates = new ArrayList<>();
        List<Long> needQueryFranchiseeList = new ArrayList<>();
        
        franchiseeIds.forEach(franchiseeId -> {
            WechatPaymentCertificate wechatPaymentCertificate = existCacheMap.get(franchiseeId);
            if (Objects.isNull(wechatPaymentCertificate)) {
                needQueryFranchiseeList.add(franchiseeId);
            } else {
                certificates.add(wechatPaymentCertificate);
            }
        });
        
        if (CollectionUtils.isEmpty(needQueryFranchiseeList)) {
            return certificates;
        }
        
        List<WechatPaymentCertificate> dbList = wechatPaymentCertificateMapper.selectListByTenantIdAndFranchiseeIds(tenantId, needQueryFranchiseeList);
        if (CollectionUtils.isEmpty(dbList)) {
            return certificates;
        }
        
        Map<String, String> cacheSaveMap = Maps.newHashMap();
        dbList.forEach(certificate -> {
            cacheSaveMap.put(buildCacheKey(tenantId, certificate.getFranchiseeId()), JsonUtil.toJson(certificate));
            certificates.add(certificate);
        });
        
        redisService.multiSet(cacheSaveMap);
        
        return certificates;
    }
    
    @Override
    public void deleteCache(Integer tenantId, Long franchiseeId) {
        redisService.delete(buildCacheKey(tenantId, franchiseeId));
        wechatPayParamsBizService.refreshMerchantInfo(tenantId, franchiseeId);
    }
    
    
    private String buildCacheKey(Integer tenantId, Long franchiseeId) {
        return String.format(PAYMENT_CERTIFICATE_KEY, tenantId, franchiseeId);
    }
    
}
