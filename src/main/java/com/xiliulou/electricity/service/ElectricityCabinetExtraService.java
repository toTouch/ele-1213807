package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.ElectricityCabinetExtra;
import com.xiliulou.electricity.queryModel.EleCabinetExtraQueryModel;

/**
 * @author HeYafeng
 * @description 柜机扩展表接口
 * @date 2024/4/23 13:56:21
 */
public interface ElectricityCabinetExtraService {
    
    ElectricityCabinetExtra queryByEid(Long eid);
    
    ElectricityCabinetExtra queryByEidFromCache(Long eid);
    
    Integer insertOne(ElectricityCabinetExtra electricityCabinetExtra);
    
    Integer update(ElectricityCabinetExtra electricityCabinetExtra);
    
    
    Integer updateMaxElectricityCabinetExtra(Integer maxRetainBatteryCount, Integer returnTabType, Integer id);
    
    Integer updateMinElectricityCabinetExtra(Integer minRetainBatteryCount, Integer rentTabType, Integer id);
    
    void updateTabTypeCabinetExtra(EleCabinetExtraQueryModel extraQueryModel);
}
