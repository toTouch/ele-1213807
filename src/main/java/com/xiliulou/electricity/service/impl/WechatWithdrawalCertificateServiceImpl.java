package com.xiliulou.electricity.service.impl;

import com.xiliulou.electricity.entity.WechatWithdrawalCertificate;
import com.xiliulou.electricity.mapper.WechatWithdrawalCertificateMapper;
import com.xiliulou.electricity.service.WechatWithdrawalCertificateService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author wangchen
 * @date 2023年5月22日
 */
@Service
@Slf4j
public class WechatWithdrawalCertificateServiceImpl implements WechatWithdrawalCertificateService {
    
    @Autowired
    private WechatWithdrawalCertificateMapper wechatWithdrawalCertificateMapper;
    
    @Override
    public WechatWithdrawalCertificate selectByTenantId(Integer tenantId) {
        return wechatWithdrawalCertificateMapper.selectByTenantId(tenantId);
    }
    
    @Override
    public void handleCertificateFile(MultipartFile file, Integer tenantId) {
        InputStream inputStream = null;
        
        try {
            inputStream = file.getInputStream();
            byte[] data = new byte[inputStream.available()];
            int read = inputStream.read(data, 0, inputStream.available());
            if (read > 0) {
                WechatWithdrawalCertificate certificate = new WechatWithdrawalCertificate()
                        .setCertificateValue(data)
                        .setTenantId(tenantId)
                        .setUploadTime(System.currentTimeMillis());
                saveOrUpdateWechatWithdrawalCertificate(certificate);
            }
        } catch (IOException e) {
            log.error("read wechat withdrawal certificate error.", e);
        } finally {
            if (Objects.nonNull(inputStream)) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    log.error("close IO error.", e);
                }
            }
        }
    }
    
    @Override
    public void saveOrUpdateWechatWithdrawalCertificate(WechatWithdrawalCertificate certificate) {
        WechatWithdrawalCertificate wechatWithdrawalCertificate = wechatWithdrawalCertificateMapper.selectByTenantId(certificate.getTenantId());
        if (Objects.nonNull(wechatWithdrawalCertificate)) {
            wechatWithdrawalCertificateMapper.updateByTenantId(certificate);
        } else {
            wechatWithdrawalCertificateMapper.insert(certificate);
        }
    }
    
    @Override
    public Map<Integer, byte[]> listCertificateInTenantIds(List<Integer> tenantIds) {
        List<WechatWithdrawalCertificate> wechatWithdrawalCertificates = wechatWithdrawalCertificateMapper
                .listCertificateInTenantIds(tenantIds);
        if (CollectionUtils.isEmpty(wechatWithdrawalCertificates)) {
            return null;
        }
        return wechatWithdrawalCertificates.stream().collect(Collectors
                .toMap(WechatWithdrawalCertificate::getTenantId, WechatWithdrawalCertificate::getCertificateValue));
    }
}
