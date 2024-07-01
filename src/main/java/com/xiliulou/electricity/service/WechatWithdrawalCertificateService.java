package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.WechatWithdrawalCertificate;
import com.xiliulou.electricity.query.WechatWithdrawalCertificateQueryModel;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @author wangchen
 * @date 2023年5月22日 desc: 微信提现证书服务类
 */
public interface WechatWithdrawalCertificateService {

    
    /**
     * 根据支付配置id查询
     *
     * @param tenantId
     * @param franchiseeId
     * @author caobotao.cbt
     * @date 2024/6/12 19:47
     */
    WechatWithdrawalCertificate queryByTenantIdAndFranchiseeId(Integer tenantId, Long franchiseeId);
    
    /**
     * 处理提现证书文件
     *
     * @param file     提现证书
     */
    void handleCertificateFile(MultipartFile file, WechatWithdrawalCertificate certificate);
    
    /**
     * 保存或更新提现证书信息
     *
     * @param certificate certificate
     */
    void saveOrUpdateWechatWithdrawalCertificate(WechatWithdrawalCertificate certificate);
    
    
    
    /**
     * 查询证书
     *
     * @param reqs
     * @author caobotao.cbt
     * @date 2024/6/13 14:37
     */
    List<WechatWithdrawalCertificate> listCertificate(List<WechatWithdrawalCertificateQueryModel> reqs);
}
