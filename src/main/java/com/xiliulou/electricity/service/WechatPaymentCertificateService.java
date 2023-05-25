package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.WechatPaymentCertificate;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author wangchen
 * @date 2023年5月17日
 * desc: 微信支付证书服务接口
 */
public interface WechatPaymentCertificateService {
    
    /**
     * 根据租户id查询微信支付证书内容
     * @param tenantId tenantId
     * @return WechatPaymentCertificate
     */
    WechatPaymentCertificate selectByTenantId(Integer tenantId);
    
    /**
     * 根据租户id更新证书信息
     * @param certificate certificate
     * @return int
     */
    int updateByTenantId(WechatPaymentCertificate certificate);
    
    /**
     * 插入证书内容
     * @param certificate certificate
     * @return int
     */
    int insert(WechatPaymentCertificate certificate);
    
    /**
     * 新增或更新证书信息
     * @param certificate 支付证书信息
     */
    void saveOrUpdateWeChatPaymentCertificate(WechatPaymentCertificate certificate);
    
    /**
     * 处理支付证书文件
     *
     * @param file     证书文件
     * @param tenantId 租户id
     * @throws Exception 异常
     */
    void handleCertificateFile(MultipartFile file, Integer tenantId) throws Exception;
}
