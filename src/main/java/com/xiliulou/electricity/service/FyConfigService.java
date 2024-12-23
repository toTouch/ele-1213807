package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.FyConfig;
import com.xiliulou.electricity.request.fy.FyConfigRequest;
import org.apache.commons.lang3.tuple.Pair;


public interface FyConfigService {
    
    /**
     * 通过tenantId查询单条数据从数据库
     *
     * @param tenantId 租户id
     * @return 实例对象
     */
    FyConfig queryByTenantIdFromDB(Integer tenantId);
    
    /**
     * 通过tenantId查询单条数据从缓存
     *
     * @param tenantId 租户id
     * @return 实例对象
     */
    FyConfig queryByTenantIdFromCache(Integer tenantId);
    
    /**
     * 通过tenantId创建或修改配置
     *
     * @param tenantId 租户
     * @param params 入参
     * @return 实例对象
     */
    Pair<Boolean, String> saveOrUpdate(Integer tenantId, FyConfigRequest params);
}
