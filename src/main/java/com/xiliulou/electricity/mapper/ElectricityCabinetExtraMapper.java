package com.xiliulou.electricity.mapper;

import com.xiliulou.electricity.entity.ElectricityCabinetExtra;
import org.apache.ibatis.annotations.Param;

/**
 * @author HeYafeng
 * @description 柜机扩展表Mapper
 * @date 2024/4/23 14:03:02
 */
public interface ElectricityCabinetExtraMapper {
    
    ElectricityCabinetExtra selectByEid(@Param("eid") Long eid, @Param("tenantId") Integer tenantId);
    
    Integer insertOne(ElectricityCabinetExtra electricityCabinetExtra);
    
    Integer update(ElectricityCabinetExtra electricityCabinetExtra);
}
