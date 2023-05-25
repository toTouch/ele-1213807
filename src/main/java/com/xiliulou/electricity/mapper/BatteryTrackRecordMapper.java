package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.BatteryTrackRecord;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * (BatteryTrackRecord)表数据库访问层
 *
 * @author makejava
 * @since 2023-01-03 16:24:37
 */
public interface BatteryTrackRecordMapper extends BaseMapper<BatteryTrackRecord> {

    
    List<BatteryTrackRecord> queryTrackRecordByCondition(@Param("sn") String sn, @Param("size") Integer size,
            @Param("offset") Integer offset, @Param("startTime") Long startTime, @Param("endTime") Long endTime);

    int insertBatch(List<BatteryTrackRecord> tempSaveBatteryTrackRecordList);
}
