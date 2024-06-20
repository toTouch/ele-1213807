package com.xiliulou.electricity.mapper;

import com.xiliulou.electricity.entity.ElectricityCabinetExtra;
import com.xiliulou.electricity.query.ElectricityCabinetBatchEditRentReturnCountQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author HeYafeng
 * @description 柜机扩展表Mapper
 * @date 2024/4/23 14:03:02
 */
public interface ElectricityCabinetExtraMapper {
    
    ElectricityCabinetExtra selectByEid(Long eid);
    
    Integer insertOne(ElectricityCabinetExtra electricityCabinetExtra);
    
    Integer update(ElectricityCabinetExtra electricityCabinetExtra);
    
    Integer updateMaxElectricityCabinetExtra(@Param("maxRetainBatteryCount") Integer maxRetainBatteryCount, @Param("id") Integer id, @Param("updateTime") Long updateTime);
    
    Integer updateMinElectricityCabinetExtra(@Param("minRetainBatteryCount") Integer minRetainBatteryCount, @Param("id") Integer id, @Param("updateTime") Long updateTime);
}
