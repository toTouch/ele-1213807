package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.AlipayAppConfig;

/**
 * 支付宝小程序配置(AlipayAppConfig)表数据库访问层
 *
 * @author zzlong
 * @since 2024-07-08 16:45:19
 */
public interface AlipayAppConfigMapper extends BaseMapper<AlipayAppConfig> {
    
    AlipayAppConfig selectById(Long id);
    
    int update(AlipayAppConfig alipayAppConfig);
    
    int deleteById(Long id);
    
    AlipayAppConfig selectByAppId(String appId);
    
    AlipayAppConfig selectByTenantId(Integer tenantId);
}
