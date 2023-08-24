package com.xiliulou.electricity.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.dto.bms.BatteryInfoDto;
import com.xiliulou.electricity.entity.ElectricityBattery;
import com.xiliulou.electricity.entity.Tenant;
import com.xiliulou.electricity.web.query.battery.BatteryInfoQuery;


public interface ElectricityBatteryDataService extends IService<ElectricityBattery> {

    R selectAllBatteryPageData(long offset, long size, String sn, Long franchiseeId, Integer electricityCabinetId, Long uid);

    R selectAllBatteryDataCount(String sn, Long franchiseeId, Integer electricityCabinetId, Long uid);

    R  selectInCabinetBatteryPageData(long offset, long size, String sn, Long franchiseeId, Integer electricityCabinetId, Long uid);

    R selectInCabinetBatteryDataCount(String sn, Long franchiseeId, Integer electricityCabinetId, Long uid);

    R selectPendingRentalBatteryPageData(long offset, long size, String sn, Long franchiseeId, Integer electricityCabinetId, Long uid);

    R selectPendingRentalBatteryDataCount(String sn, Long franchiseeId, Integer electricityCabinetId, Long uid);

    R  selectLeasedBatteryPageData(long offset, long size, String sn, Long franchiseeId, Integer electricityCabinetId, Long uid);

    R selectLeasedBatteryDataCount(String sn, Long franchiseeId, Integer electricityCabinetId, Long uid);

    R  selectStrayBatteryPageData(long offset, long size, String sn, Long franchiseeId, Integer electricityCabinetId, Long uid);

    R selectStrayBatteryDataCount(String sn, Long franchiseeId, Integer electricityCabinetId, Long uid);

    R  selectOverdueBatteryPageData(long offset, long size, String sn, Long franchiseeId, Integer electricityCabinetId, Long uid);

    R selectOverdueBatteryDataCount(String sn, Long franchiseeId, Integer electricityCabinetId, Long uid);

    BatteryInfoDto callBatteryServiceQueryBatteryInfo(BatteryInfoQuery batteryInfoQuery, Tenant tenant);

    R queryStockBatteryPageData(long offset, long size, String sn, Long franchiseeId, Integer electricityCabinetId);

    R queryStockBatteryPageDataCount(String sn, Long franchiseeId, Integer electricityCabinetId);


}