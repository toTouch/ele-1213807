package com.xiliulou.electricity.mapper.batteryrecycle;


import com.xiliulou.electricity.entity.batteryrecycle.BatteryRecycleRecord;
import com.xiliulou.electricity.query.batteryRecycle.BatteryRecycleQueryModel;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 电池回收记录表(TBatteryRecycleRecord)表数据库访问层
 *
 * @author maxiaodong
 * @since 2024-10-30 10:47:47
 */
public interface BatteryRecycleRecordMapper {
    
    BatteryRecycleRecord selectListLastRecycleRecordByTime(@Param("startTime") long startTime,@Param("endTime") long endTime,@Param("tenantId") Integer tenantId);
    
    int batchInsert(List<BatteryRecycleRecord> batteryRecycleRecords);
    
    List<BatteryRecycleRecord> selectListByPage(BatteryRecycleQueryModel queryModel);
    
    Integer countTotal(BatteryRecycleQueryModel queryModel);
    
    BatteryRecycleRecord selectListFirstNotLockedRecord(@Param("tenantId") Integer tenantId);
    
    List<BatteryRecycleRecord> selectListNotLockedRecord(@Param("tenantId") Integer tenantId,@Param("maxId") Long maxId,@Param("size") Long size);
}

