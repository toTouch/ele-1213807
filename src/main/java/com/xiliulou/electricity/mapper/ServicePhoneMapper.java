package com.xiliulou.electricity.mapper;

import com.xiliulou.electricity.entity.ServicePhone;

/**
 * @author HeYafeng
 * @date 2024/10/24 17:55:58
 */
public interface ServicePhoneMapper {
    
    ServicePhone selectByTenantId(Integer tenantId);
    
    Integer update(ServicePhone servicePhone);
    
    Integer insertOne(ServicePhone build);
    
    Integer deleteById(Long id);
}
