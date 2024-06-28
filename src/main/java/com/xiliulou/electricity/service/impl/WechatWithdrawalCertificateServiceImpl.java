package com.xiliulou.electricity.service.impl;

import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.entity.WechatWithdrawalCertificate;
import com.xiliulou.electricity.mapper.WechatWithdrawalCertificateMapper;
import com.xiliulou.electricity.query.WechatWithdrawalCertificateQueryModel;
import com.xiliulou.electricity.service.WechatWithdrawalCertificateService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
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
    public WechatWithdrawalCertificate queryByTenantIdAndFranchiseeId(Integer tenantId, Long franchiseeId) {
        
        return wechatWithdrawalCertificateMapper.selectByTenantIdAndFranchiseeId(tenantId, franchiseeId);
    }
    
    @Override
    public void handleCertificateFile(MultipartFile file, WechatWithdrawalCertificate certificate) {
        InputStream inputStream = null;
        
        try {
            inputStream = file.getInputStream();
            byte[] data = new byte[inputStream.available()];
            int read = inputStream.read(data, 0, inputStream.available());
            if (read > 0) {
                certificate.setCertificateValue(data);
                certificate.setUploadTime(System.currentTimeMillis());
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
        WechatWithdrawalCertificate wechatWithdrawalCertificate = wechatWithdrawalCertificateMapper
                .selectByTenantIdAndFranchiseeId(certificate.getTenantId(), certificate.getFranchiseeId());
        if (Objects.nonNull(wechatWithdrawalCertificate)) {
            certificate.setId(wechatWithdrawalCertificate.getId());
            wechatWithdrawalCertificateMapper.updateByIdAndTenantId(certificate);
        } else {
            wechatWithdrawalCertificateMapper.insert(certificate);
        }
    }
    
    
    @Slave
    @Override
    public List<WechatWithdrawalCertificate> listCertificate(List<WechatWithdrawalCertificateQueryModel> reqs) {
        
        List<WechatWithdrawalCertificate> certificates = wechatWithdrawalCertificateMapper.selectListByTenantIdAndFranchiseeId(reqs);
        
        return Optional.ofNullable(certificates).orElse(Collections.emptyList());
    }
}
