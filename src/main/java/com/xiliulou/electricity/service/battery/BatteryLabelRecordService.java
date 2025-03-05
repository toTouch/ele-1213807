package com.xiliulou.electricity.service.battery;

import com.xiliulou.electricity.entity.ElectricityBattery;
import com.xiliulou.electricity.entity.battery.BatteryLabelRecord;
import com.xiliulou.electricity.request.battery.BatteryLabelRecordRequest;

import java.util.List;

/**
 * @author: SJP
 * @create: 2025-02-14 15:41
 **/
public interface BatteryLabelRecordService {

    void sendRecord(ElectricityBattery battery, Long operatorUid, Integer newLabel, Long updateTime, Long oldReceiverId, Long newReceiverId);
    
    List<BatteryLabelRecord> listPage(BatteryLabelRecordRequest request);
    
    Long countAll(BatteryLabelRecordRequest request);
}
