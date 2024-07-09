package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.AlipayAppConfig;

/**
 * 支付宝小程序配置(AlipayAppConfig)表服务接口
 *
 * @author zzlong
 * @since 2024-07-08 16:45:19
 */
public interface AlipayAppConfigService {
    
    AlipayAppConfig queryByAppId(String appId);
    
    AlipayAppConfig queryByIdFromCache(Long id);
    
    Integer update(AlipayAppConfig alipayAppConfig);
    
    Integer deleteById(Long id);
    
    AlipayAppConfig queryByTenantId(Integer tenantId);
}
