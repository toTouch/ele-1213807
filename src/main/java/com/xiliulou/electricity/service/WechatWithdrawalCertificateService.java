package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.WechatWithdrawalCertificate;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * @author wangchen
 * @date 2023年5月22日
 * desc: 微信提现证书服务类
 */
public interface WechatWithdrawalCertificateService {
    
    /**
     * 根据租户id查询提现证书信息
     *
     * @param tenantId 承租者id
     * @return {@link WechatWithdrawalCertificate}
     */
    WechatWithdrawalCertificate selectByTenantId(Integer tenantId);
    
    /**
     * 处理提现证书文件
     * @param file 提现证书
     * @param tenantId 租户id
     */
    void handleCertificateFile(MultipartFile file, Integer tenantId);
    
    /**
     * 保存或更新提现证书信息
     * @param certificate certificate
     */
    void saveOrUpdateWechatWithdrawalCertificate(WechatWithdrawalCertificate certificate);
    
    /**
     * 由租户id列表查询证书列表
     *
     * @param tenantIds 租户id
     * @return {@link Map}<{@link Integer}, {@link byte[]}>
     */
    Map<Integer, byte[]> listCertificateInTenantIds(List<Integer> tenantIds);
}
