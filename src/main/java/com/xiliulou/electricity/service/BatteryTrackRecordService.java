package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.BatteryTrackRecord;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

/**
 * (BatteryTrackRecord)表服务接口
 *
 * @author makejava
 * @since 2023-01-03 16:24:37
 */
public interface BatteryTrackRecordService {

    /**
     * 新增数据
     *
     * @param batteryTrackRecord 实例对象
     * @return 实例对象
     */
    BatteryTrackRecord putBatteryTrackQueue(BatteryTrackRecord batteryTrackRecord);

    Pair<Boolean, Object> queryTrackRecord(String sn, Integer size, Integer offset, Long startTime, Long endTime);

    int insertBatch(List<BatteryTrackRecord> tempSaveBatteryWarnList);
}
