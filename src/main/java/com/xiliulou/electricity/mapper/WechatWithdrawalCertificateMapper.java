package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.WechatPaymentCertificate;
import com.xiliulou.electricity.entity.WechatWithdrawalCertificate;
import com.xiliulou.electricity.query.WechatWithdrawalCertificateQueryModel;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author wangchen
 * @date 2023年5月22日 desc: 微信提现证书mapper层
 */
public interface WechatWithdrawalCertificateMapper extends BaseMapper<WechatWithdrawalCertificate> {
    
    
    /**
     * 根据id更新
     *
     * @param certificate
     * @author caobotao.cbt
     * @date 2024/6/13 14:13
     */
    int updateByIdAndTenantId(WechatWithdrawalCertificate certificate);
    
    /**
     * 逻辑删除
     *
     * @author caobotao.cbt
     * @date 2024/6/12 13:51
     */
    int logicalDeleteByPayParamsId(@Param("payParamsId") Long payParamsId, @Param("tenantId") Integer tenantId);
    
    /**
     * 根据租户+加盟商id查询
     *
     * @param tenantId
     * @param franchiseeId
     * @author caobotao.cbt
     * @date 2024/6/13 14:06
     */
    WechatWithdrawalCertificate selectByTenantIdAndFranchiseeId(@Param("tenantId") Integer tenantId, @Param("franchiseeId") Long franchiseeId);
    
    /**
     * 根据租户id+加盟商id批量查询
     *
     * @param list
     * @author caobotao.cbt
     * @date 2024/6/13 14:41
     */
    List<WechatWithdrawalCertificate> selectListByTenantIdAndFranchiseeId(@Param("list") List<WechatWithdrawalCertificateQueryModel> list);
}
