package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.WechatPaymentCertificate;
import org.apache.ibatis.annotations.Param;

/**
 * @author Mr. wang
 * @date: 2023年5月16日 desc: 微信支付证书内容mapper层
 */
public interface WechatPaymentCertificateMapper extends BaseMapper<WechatPaymentCertificate> {
    
    
    /**
     * 根据id更新证书
     *
     * @param certificate
     * @author caobotao.cbt
     * @date 2024/6/13 14:13
     */
    int updateByIdAndTenantId(WechatPaymentCertificate certificate);
    
    /**
     * 根据租户+加盟商id查询
     *
     * @param tenantId
     * @param franchiseeId
     * @author caobotao.cbt
     * @date 2024/6/12 19:57
     */
    WechatPaymentCertificate selectByTenantIdAndFranchiseeId(@Param("tenantId") Integer tenantId, @Param("franchiseeId") Long franchiseeId);
    
    
    /**
     * 逻辑删除
     *
     * @author caobotao.cbt
     * @date 2024/6/12 13:51
     */
    int logicalDeleteByPayParamsId(@Param("payParamsId") Long payParamsId, @Param("tenantId") Integer tenantId);
}
