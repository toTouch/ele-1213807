package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.AlipayAppConfig;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 支付宝小程序配置(AlipayAppConfig)表数据库访问层
 *
 * @author zzlong
 * @since 2024-07-08 16:45:19
 */
public interface AlipayAppConfigMapper extends BaseMapper<AlipayAppConfig> {
    
    AlipayAppConfig selectById(Long id);
    
    int update(AlipayAppConfig alipayAppConfig);
    
    
    AlipayAppConfig selectOneByTenantId(Integer tenantId);
    
    AlipayAppConfig selectByAppId(String appId);
    
    AlipayAppConfig selectBySellerIdAndTenantId(@Param("sellerId") String sellerId, @Param("tenantId") Integer tenantId);
    
    /**
     * 根据租户id+加盟商id查询
     *
     * @param tenantId
     * @author caobotao.cbt
     * @date 2024/7/16 16:43
     */
    List<AlipayAppConfig> selectListByTenantIdAndFranchiseeIds(@Param("tenantId") Integer tenantId, @Param("franchiseeIds") List<Long> franchiseeIds);
    
    List<AlipayAppConfig> selectListByTenantId(Integer tenantId);
    
    List<AlipayAppConfig> selectByConfigType(@Param("tenantId") Integer tenantId, @Param("type") Integer type);
    
    Integer updateSyncByIds(@Param("alipayAppConfig") AlipayAppConfig alipayAppConfigUpdate, @Param("ids") List<Long> syncAlipayAppConfigIds);
    
}
