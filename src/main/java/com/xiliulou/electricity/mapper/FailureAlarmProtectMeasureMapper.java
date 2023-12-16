package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.FailureAlarm;
import com.xiliulou.electricity.entity.FailureAlarmProtectMeasure;
import com.xiliulou.electricity.vo.asset.AssetAllocateDetailVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * (City)故障告警设置
 *
 * @author maxiaodong
 * @since 2023-12-15 18:05:41
 */
public interface FailureAlarmProtectMeasureMapper extends BaseMapper<FailureAlarmProtectMeasure>{
    
    int batchInsert(@Param("list") List<FailureAlarmProtectMeasure> protectMeasures);
    
    List<FailureAlarmProtectMeasure> selectListByFailureAlarmIdList(@Param("failureAlarmIdList") List<Long> failureAlarmIdList);
    
    int deleteByFailureAlarmId(@Param("failureAlarmId") Long failureAlarmId);
}
