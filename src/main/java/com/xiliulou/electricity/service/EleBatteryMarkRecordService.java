package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.BatteryTrackRecord;

/**
 * 标记电池记录表(TEleBatteryMarkRecord)表服务接口
 *
 * @author maxiaodong
 * @since 2024-11-12 14:04:34
 */
public interface EleBatteryMarkRecordService {
    
    void checkBatteryMark(BatteryTrackRecord batteryTrackRecord);
}
