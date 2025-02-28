package com.xiliulou.electricity.mapper;

import com.xiliulou.electricity.entity.ElectricityConfigExtra;

/**
 * @author HeYafeng
 * @date 2025/2/12 17:13:18
 */
public interface ElectricityConfigExtraMapper {
    
    ElectricityConfigExtra selectByTenantId(Integer tenantId);
    
    Integer insert(ElectricityConfigExtra electricityConfigExtra);
    
    Integer update(ElectricityConfigExtra electricityConfigExtra);
}
