package com.xiliulou.electricity.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.ElectricityBattery;
import com.xiliulou.electricity.query.ElectricityBatteryQuery;

import java.util.List;

/**
 * 换电柜电池表(ElectricityBattery)表服务接口
 *
 * @author makejava
 * @since 2020-11-26 14:44:12
 */
public interface ElectricityBatteryService extends IService<ElectricityBattery> {


    R saveElectricityBattery(ElectricityBattery electricityBattery);

    R update(ElectricityBattery electricityBattery);

    R getElectricityBatteryList(ElectricityBatteryQuery electricityBatteryQuery, Long offset, Long size);

    ElectricityBattery queryById(Long electricityBatteryId);

    R deleteElectricityBattery(Long id);

    ElectricityBattery queryByBindSn(String initElectricityBatterySn);

    List<ElectricityBattery> homeTwo(List<Long> batteryIdList);

    R getSelfBattery(Long uid);

    ElectricityBattery queryBySn(String oldElectricityBatterySn);


    void updateReport(ElectricityBattery newElectricityBattery);
}
