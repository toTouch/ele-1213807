package com.xiliulou.electricity.service.battery;

import com.xiliulou.electricity.entity.ElectricityBattery;

/**
 * @author: SJP
 * @create: 2025-02-14 15:41
 **/
public interface BatteryLabelRecordService {

    void sendRecord(ElectricityBattery battery, Long uid, Integer newLabel, Long updateTime);

}
