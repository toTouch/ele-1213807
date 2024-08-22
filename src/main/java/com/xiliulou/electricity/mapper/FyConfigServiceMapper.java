package com.xiliulou.electricity.mapper;

import com.xiliulou.electricity.entity.FyConfig;
import org.apache.ibatis.annotations.Param;

public interface FyConfigServiceMapper {
    
    
    FyConfig selectByTenantId(@Param("tenantId") Integer tenantId);
}
