package com.xiliulou.electricity.vo.api;

import com.xiliulou.electricity.dto.bms.BatteryInfoDto;
import com.xiliulou.electricity.vo.ElectricityBatteryDataVO;
import lombok.Data;

@Data
public class EleBatteryDataVO {

   private ElectricityBatteryDataVO electricityBatteryDataVO;

   private BatteryInfoDto batteryInfoDto;

}
