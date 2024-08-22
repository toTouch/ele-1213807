package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.FyConfig;


public interface FyConfigService {
    
    /**
     * 通过tenantId查询单条数据从数据库
     *
     * @param tenantId
     * @return 实例对象
     */
    FyConfig queryByTenantIdFromDB(Integer tenantId);
    
    /**
     * 通过tenantId查询单条数据从缓存
     *
     * @param tenantId
     * @return 实例对象
     */
    FyConfig queryByTenantIdFromCache(Integer tenantId);
    
    
}
