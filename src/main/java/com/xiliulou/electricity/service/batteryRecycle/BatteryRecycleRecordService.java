package com.xiliulou.electricity.service.batteryRecycle;

import com.xiliulou.electricity.entity.batteryrecycle.BatteryRecycleRecord;
import com.xiliulou.electricity.request.batteryrecycle.BatteryRecycleCancelRequest;
import com.xiliulou.electricity.request.batteryrecycle.BatteryRecycleSaveOrUpdateRequest;
import com.xiliulou.electricity.request.batteryrecycle.BatteryRecyclePageRequest;
import com.xiliulou.electricity.vo.recycle.BatteryRecycleVO;
import org.apache.commons.lang3.tuple.Triple;

import java.util.List;

/**
 * 电池回收记录表(TBatteryRecycleRecord)表服务接口
 *
 * @author maxiaodong
 * @since 2024-10-30 10:47:56
 */
public interface BatteryRecycleRecordService {
    
    Triple<Boolean, String, Object> save(BatteryRecycleSaveOrUpdateRequest saveRequest, Long uid);
    
    List<BatteryRecycleVO> listByPage(BatteryRecyclePageRequest request);
    
    Integer countTotal(BatteryRecyclePageRequest request);
    
    BatteryRecycleRecord listFirstNotLockedRecord(Integer tenantId);
    
    List<BatteryRecycleRecord> listNotLockedRecord(Integer tenantId, Long maxId, Long size);
    
    Integer updateById(BatteryRecycleRecord batteryRecycleRecord);

    Triple<Boolean, String, Object> cancel(BatteryRecycleCancelRequest request, List<BatteryRecycleRecord> batteryRecycleRecords);

    List<BatteryRecycleRecord> listBySnList(BatteryRecycleCancelRequest request);
}
