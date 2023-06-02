package com.xiliulou.electricity.service.impl;

import com.xiliulou.electricity.entity.WechatPaymentCertificate;
import com.xiliulou.electricity.mapper.WechatPaymentCertificateMapper;
import com.xiliulou.electricity.service.WechatPaymentCertificateService;
import com.xiliulou.pay.weixinv3.util.WechatCertificateUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Objects;

/**
 * @author wangchen
 */
@Service
@Slf4j
public class WechatPaymentCertificateServiceImpl implements WechatPaymentCertificateService {
    
    @Autowired
    private WechatPaymentCertificateMapper wechatPaymentCertificateMapper;
    
    @Override
    public WechatPaymentCertificate selectByTenantId(Integer tenantId) {
        return  wechatPaymentCertificateMapper.selectByTenantId(tenantId);
    }
    
    @Override
    public int updateByTenantId(WechatPaymentCertificate certificate) {
        return wechatPaymentCertificateMapper.updateByTenantId(certificate);
    }
    
    @Override
    public int insert(WechatPaymentCertificate certificate) {
        return wechatPaymentCertificateMapper.insert(certificate);
    }
    
    @Override
    public void saveOrUpdateWeChatPaymentCertificate(WechatPaymentCertificate certificate) {
        WechatPaymentCertificate wechatPaymentCertificate = selectByTenantId(certificate.getTenantId());
        if (Objects.nonNull(wechatPaymentCertificate)) {
            wechatPaymentCertificateMapper.updateByTenantId(certificate);
        } else {
            wechatPaymentCertificateMapper.insert(certificate);
        }
    }
    
    @Override
    public void handleCertificateFile(MultipartFile file, Integer tenantId) throws Exception {
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
                log.error("certificate content is empty, tenantId={}, fileName={}", tenantId, fileName);
                throw new Exception("证书内容为空!");
            }
            //存储微信支付证书
            WechatPaymentCertificate wechatPaymentCertificate = new WechatPaymentCertificate()
                    .setTenantId(tenantId)
                    .setCertificateContent(privateKey)
                    .setUploadTime(System.currentTimeMillis());
            saveOrUpdateWeChatPaymentCertificate(wechatPaymentCertificate);
        } catch (Exception e) {
            log.error("certificate get error, tenantId={}", tenantId);
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
    
    
}
