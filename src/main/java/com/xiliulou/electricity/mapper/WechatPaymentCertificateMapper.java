package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.WechatPaymentCertificate;
import org.apache.ibatis.annotations.Param;

/**
 * @author Mr. wang
 * @date: 2023年5月16日
 * desc: 微信支付证书内容mapper层
 */
public interface WechatPaymentCertificateMapper extends BaseMapper<WechatPaymentCertificate> {
    
    /**
     * 根据租户id查询证书
     * @param tenantId tenantId
     * @return WechatPaymentCertificate
     */
    WechatPaymentCertificate selectByTenantId(@Param("tenantId") Integer tenantId);
    
    /**
     * 根据租户id更新证书信息
     * @param certificate certificate
     * @return int
     */
    int updateByTenantId(WechatPaymentCertificate certificate);
}
