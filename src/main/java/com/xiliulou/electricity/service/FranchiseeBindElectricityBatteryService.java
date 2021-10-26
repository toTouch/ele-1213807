package com.xiliulou.electricity.service;
import com.xiliulou.electricity.entity.FranchiseeBindElectricityBattery;

import java.util.List;

/**
 * (ElectricityBatteryBind)表服务接口
 *
 * @author lxc
 * @since 2020-11-25 11:00:14
 */
public interface FranchiseeBindElectricityBatteryService {

    void deleteByFranchiseeId(Integer franchiseeId);

    void insert(FranchiseeBindElectricityBattery franchiseeBindElectricityBattery);

    List<FranchiseeBindElectricityBattery> queryByFranchiseeId(Long id);

	Integer queryCountByBattery(Long electricityBatteryId);

	FranchiseeBindElectricityBattery queryByBatteryId(Long id);

	FranchiseeBindElectricityBattery queryByBatteryIdAndFranchiseeId(Long batteryId,Long franchiseeId);
}
