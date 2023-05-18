package com.xiliulou.electricity.vo.api;

import com.xiliulou.electricity.dto.bms.BatteryInfoDto;
import com.xiliulou.electricity.entity.ElectricityBattery;
import lombok.Data;

@Data
public class EleBatteryDataVO {

   private ElectricityBattery electricityBattery;

   private BatteryInfoDto batteryInfoDto;

}
