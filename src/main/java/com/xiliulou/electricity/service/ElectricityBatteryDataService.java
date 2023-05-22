package com.xiliulou.electricity.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.dto.bms.BatteryInfoDto;
import com.xiliulou.electricity.entity.ElectricityBattery;
import com.xiliulou.electricity.web.query.battery.BatteryInfoQuery;


public interface ElectricityBatteryDataService extends IService<ElectricityBattery> {

    R selectAllBatteryPageData(long offset, long size);

    R selectAllBatteryDataCount();

    R  selectInCabinetBatteryPageData(long offset, long size, String sn, Long franchiseeId, Integer electricityCabinetId);

    R selectInCabinetBatteryDataCount(String sn, Long franchiseeId, Integer electricityCabinetId);

    R selectPendingRentalBatteryPageData(long offset, long size, String sn, Long franchiseeId, Integer electricityCabinetId);

    R selectPendingRentalBatteryDataCount(String sn, Long franchiseeId, Integer electricityCabinetId);

    R  selectLeasedBatteryPageData(long offset, long size, String sn, Long franchiseeId, Integer electricityCabinetId);

    R selectLeasedBatteryDataCount(String sn, Long franchiseeId, Integer electricityCabinetId);

    R  selectStrayBatteryPageData(long offset, long size, String sn, Long franchiseeId, Integer electricityCabinetId);

    R selectStrayBatteryDataCount(String sn, Long franchiseeId, Integer electricityCabinetId);

    R  selectOverdueBatteryPageData(long offset, long size, String sn, Long franchiseeId, Integer electricityCabinetId);

    R selectOverdueBatteryDataCount(String sn, Long franchiseeId, Integer electricityCabinetId);

    BatteryInfoDto callBatteryServiceQueryBatteryInfo(BatteryInfoQuery batteryInfoQuery);
}