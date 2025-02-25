package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.ElectricityConfigExtra;

/**
 * @author HeYafeng
 * @date 2025/2/12 17:07:24
 */
public interface ElectricityConfigExtraService {
    
    ElectricityConfigExtra queryByTenantIdFromCache(Integer tenantId);
    
    ElectricityConfigExtra queryByTenantId(Integer tenantId);
    
    Integer insert(ElectricityConfigExtra electricityConfigExtra);
    
    Integer update(ElectricityConfigExtra electricityConfigExtra);
    
}
