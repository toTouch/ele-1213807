package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.ElectricityBattery;
import com.xiliulou.electricity.entity.ElectricityCabinet;

import java.util.List;

/**
 * 换电柜电池表(ElectricityBattery)表服务接口
 *
 * @author makejava
 * @since 2020-11-26 14:44:12
 */
public interface ElectricityBatteryService {


    R save(ElectricityBattery electricityBattery);
}