package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.ElectricityCabinetExtra;

/**
 * @author HeYafeng
 * @description 柜机扩展表接口
 * @date 2024/4/23 13:56:21
 */
public interface ElectricityCabinetExtraService {
    
    ElectricityCabinetExtra queryByEid(Long eid, Integer tenantId);
    
    Integer insertOrUpdate(ElectricityCabinetExtra electricityCabinetExtra);
    
    Integer insertOne(ElectricityCabinetExtra electricityCabinetExtra);
    
    Integer update(ElectricityCabinetExtra electricityCabinetExtra);
}
