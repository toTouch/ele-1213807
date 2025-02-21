package com.xiliulou.electricity.mapper.battery;

import com.xiliulou.electricity.entity.battery.ElectricityBatteryLabel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author: SJP
 * @Desc:
 * @create: 2025-02-18 15:03
 **/
@Mapper
public interface ElectricityBatteryLabelMapper {
    
    void insert(ElectricityBatteryLabel batteryLabel);
    
    void batchInsert(@Param("list") List<ElectricityBatteryLabel> batteryLabels);
    
    ElectricityBatteryLabel queryBySnAndTenantId(@Param("sn") String sn, @Param("tenantId") Integer tenantId);
    
    int updateById(ElectricityBatteryLabel batteryLabel);
    
    List<ElectricityBatteryLabel> selectListBySns(@Param("list") List<String> sns);
    
    int updateReceivedData(@Param("sn") String sn, @Param("updateTime") Long updateTime);
    
    Integer countReceived(@Param("receiverId") Long receiverId);
}
