package com.xiliulou.electricity.service.impl;

import com.xiliulou.db.dynamic.annotation.DS;
import com.xiliulou.electricity.entity.BatteryTrackRecord;
import com.xiliulou.electricity.entity.ElectricityBattery;
import com.xiliulou.electricity.mapper.BatteryTrackRecordMapper;
import com.xiliulou.electricity.queue.BatteryTrackRecordBatchSaveQueueService;
import com.xiliulou.electricity.service.BatteryTrackRecordService;
import com.xiliulou.electricity.service.ElectricityBatteryService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

/**
 * (BatteryTrackRecord)表服务实现类
 *
 * @author makejava
 * @since 2023-01-03 16:24:37
 */
@Service("batteryTrackRecordService")
@Slf4j
public class BatteryTrackRecordServiceImpl implements BatteryTrackRecordService {
    
    @Resource
    private BatteryTrackRecordMapper batteryTrackRecordMapper;
    
    @Autowired
    ElectricityBatteryService electricityBatteryService;

    @Autowired
    BatteryTrackRecordBatchSaveQueueService batteryTrackRecordBatchSaveQueueService;

    
    /**
     * 新增数据
     *
     * @param batteryTrackRecord 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatteryTrackRecord insert(BatteryTrackRecord batteryTrackRecord) {
        batteryTrackRecordBatchSaveQueueService.putQueue(batteryTrackRecord);
        return batteryTrackRecord;
    }
    


    
    @Override
    @DS(value = "clickhouse")
    public Pair<Boolean, Object> queryTrackRecord(String sn, Integer size, Integer offset, Long startTime,
            Long endTime) {
        ElectricityBattery electricityBattery = electricityBatteryService.queryBySnFromDb(sn,
                TenantContextHolder.getTenantId());
        if (Objects.isNull(electricityBattery)) {
            return Pair.of(true, null);
        }
        
        
        return Pair.of(true,batteryTrackRecordMapper.queryTrackRecordByCondition(sn,size,offset,startTime,endTime));
    }

    @Override
    @DS(value = "clickhouse")
    public int insertBatch(List<BatteryTrackRecord> tempSaveBatteryTrackRecordList) {
        return batteryTrackRecordMapper.insertBatch(tempSaveBatteryTrackRecordList);
    }
}
