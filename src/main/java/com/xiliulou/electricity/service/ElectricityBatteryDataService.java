package com.xiliulou.electricity.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.dto.bms.BatteryInfoDto;
import com.xiliulou.electricity.entity.ElectricityBattery;
import com.xiliulou.electricity.entity.Tenant;
import com.xiliulou.electricity.query.ElectricityBatteryDataQuery;
import com.xiliulou.electricity.web.query.battery.BatteryInfoQuery;


public interface ElectricityBatteryDataService extends IService<ElectricityBattery> {
    
    R selectAllBatteryPageData(ElectricityBatteryDataQuery electricityBatteryQuery);
    
    R selectAllBatteryDataCount(ElectricityBatteryDataQuery electricityBatteryQuery);
    
    R updateGuessUserInfo(Long id);
    
    R selectInCabinetBatteryPageData(ElectricityBatteryDataQuery electricityBatteryQuery);
    
    R selectInCabinetBatteryDataCount(ElectricityBatteryDataQuery electricityBatteryQuery);
    
    R selectPendingRentalBatteryPageData(ElectricityBatteryDataQuery electricityBatteryQuery);
    
    R selectPendingRentalBatteryDataCount(ElectricityBatteryDataQuery electricityBatteryQuery);
    
    R selectLeasedBatteryPageData(ElectricityBatteryDataQuery electricityBatteryQuery);
    
    R selectLeasedBatteryDataCount(ElectricityBatteryDataQuery electricityBatteryQuery);
    
    R selectStrayBatteryPageData(ElectricityBatteryDataQuery electricityBatteryQuery);
    
    R selectStrayBatteryDataCount(ElectricityBatteryDataQuery electricityBatteryQuery);
    
    R selectOverdueBatteryPageData(ElectricityBatteryDataQuery electricityBatteryQuery);
    
    R selectOverdueBatteryDataCount(ElectricityBatteryDataQuery electricityBatteryQuery);
    
    R selectOverdueCarBatteryPageData(ElectricityBatteryDataQuery electricityBatteryQuery);
    
    R selectOverdueCarBatteryDataCount(ElectricityBatteryDataQuery electricityBatteryQuery);
    
    BatteryInfoDto callBatteryServiceQueryBatteryInfo(BatteryInfoQuery batteryInfoQuery, Tenant tenant);
    
    R queryStockBatteryPageData(ElectricityBatteryDataQuery electricityBatteryQuery);
    
    R queryStockBatteryPageDataCount(ElectricityBatteryDataQuery electricityBatteryQuery);
    
    
}