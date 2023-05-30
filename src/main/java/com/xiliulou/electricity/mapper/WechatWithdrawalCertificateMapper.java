package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.WechatWithdrawalCertificate;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author wangchen
 * @date 2023年5月22日
 * desc: 微信提现证书mapper层
 */
public interface WechatWithdrawalCertificateMapper extends BaseMapper<WechatWithdrawalCertificate> {
    
    /**
     * 根据租户id查询提现证书
     * @param tenantId tenantId
     * @return WechatWithdrawalCertificate
     */
    WechatWithdrawalCertificate selectByTenantId(@Param("tenantId") Integer tenantId);
    
    /**
     * 根据租户id更新提现证书信息
     * @param certificate certificate
     * @return int
     */
    int updateByTenantId(WechatWithdrawalCertificate certificate);
    
    /**
     * 由租户id列表查询证书列表
     *
     * @param tenantIds 租户id
     * @return {@link List}<{@link WechatWithdrawalCertificate}>
     */
    List<WechatWithdrawalCertificate> listCertificateInTenantIds(@Param("tenantIds") List<Integer> tenantIds);
}
