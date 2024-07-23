package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.WechatPaymentCertificate;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author wangchen
 * @date 2023年5月17日 desc: 微信支付证书服务接口
 */
public interface WechatPaymentCertificateService {
    
    
    /**
     * 新增或更新证书信息
     *
     * @param certificate 支付证书信息
     */
    void saveOrUpdateWeChatPaymentCertificate(WechatPaymentCertificate certificate);
    
    
    /**
     * 处理支付证书文件
     *
     * @param file
     * @param wechatPaymentCertificate
     * @author caobotao.cbt
     * @date 2024/6/13 10:46
     */
    void handleCertificateFile(MultipartFile file, WechatPaymentCertificate wechatPaymentCertificate) throws Exception;
    
    /**
     * 根据租户+加盟商id查询
     *
     * @param tenantId
     * @param franchiseeId
     * @author caobotao.cbt
     * @date 2024/6/12 19:47
     */
    WechatPaymentCertificate queryByTenantIdAndFranchiseeId(Integer tenantId, Long franchiseeId);
    
    
    /**
     * 缓存删除
     *
     * @param tenantId
     * @param franchiseeId
     * @author caobotao.cbt
     * @date 2024/6/13 14:24
     */
    void deleteCache(Integer tenantId, Long franchiseeId);
}
