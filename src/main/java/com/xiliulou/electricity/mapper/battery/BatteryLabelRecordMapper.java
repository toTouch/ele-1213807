package com.xiliulou.electricity.mapper.battery;

import com.xiliulou.electricity.entity.battery.BatteryLabelRecord;
import com.xiliulou.electricity.request.battery.BatteryLabelRecordRequest;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author: SJP
 * @Desc:
 * @create: 2025-02-21 15:00
 **/
@Mapper
public interface BatteryLabelRecordMapper {
    
    List<BatteryLabelRecord> listPage(BatteryLabelRecordRequest request);
    
    Long countAll(BatteryLabelRecordRequest request);
}
