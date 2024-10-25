package com.xiliulou.electricity.mapper;

import com.xiliulou.electricity.entity.ServicePhone;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author HeYafeng
 * @date 2024/10/24 17:55:58
 */
public interface ServicePhoneMapper {
    
    List<ServicePhone> selectByTenantId(Integer tenantId);
    
    Integer batchInsert(@Param("list") List<ServicePhone> insertList);
    
    Integer update(ServicePhone servicePhone);
    
    List<ServicePhone> selectListByIds(@Param("ids") List<Long> ids);
    
    Integer insertOne(ServicePhone build);
    
    Integer updateByPhone(@Param("oldPhone") String oldPhone, @Param("newPhone") String newPhone, @Param("tenantId") Integer tenantId, @Param("updateTime") Long updateTime);
    
    ServicePhone selectByPhoneAndTenantId(@Param("newPhone") String newPhone, @Param("tenantId") Integer tenantId);
}
