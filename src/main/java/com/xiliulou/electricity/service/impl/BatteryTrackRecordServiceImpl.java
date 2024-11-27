package com.xiliulou.electricity.service.impl;

import com.xiliulou.core.utils.TimeUtils;
import com.xiliulou.db.dynamic.annotation.DS;
import com.xiliulou.electricity.constant.OrderForBatteryConstants;
import com.xiliulou.electricity.entity.BatteryTrackRecord;
import com.xiliulou.electricity.entity.ElectricityBattery;
import com.xiliulou.electricity.enums.BusinessType;
import com.xiliulou.electricity.mapper.BatteryTrackRecordMapper;
import com.xiliulou.electricity.queue.BatteryTrackRecordBatchSaveQueueService;
import com.xiliulou.electricity.service.BatteryTrackRecordService;
import com.xiliulou.electricity.service.ElectricityBatteryService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.vo.BatteryTrackRecordVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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
     * 电池记录存放到队列
     *
     * @param batteryTrackRecord 实例对象
     * @return 实例对象
     */
    @Override
    public BatteryTrackRecord putBatteryTrackQueue(BatteryTrackRecord batteryTrackRecord) {
        batteryTrackRecordBatchSaveQueueService.putQueue(batteryTrackRecord);
        return batteryTrackRecord;
    }
    



    @Override
    @DS(value = "clickhouse")
    public Pair<Boolean, Object> queryTrackRecord(String sn, Integer size, Integer offset, Long startTime, Long endTime) {
        ElectricityBattery electricityBattery = electricityBatteryService.queryBySnFromDb(sn, TenantContextHolder.getTenantId());
        if (Objects.isNull(electricityBattery)) {
            return Pair.of(true, null);
        }
    
        List<BatteryTrackRecord> recordList = batteryTrackRecordMapper.queryTrackRecordByCondition(sn, size, offset, TimeUtils.convertToStandardFormatTime(startTime),
                TimeUtils.convertToStandardFormatTime(endTime));
    
        List<BatteryTrackRecordVO> list = recordList.stream().map(item -> {
            BatteryTrackRecordVO vo = new BatteryTrackRecordVO();
            BeanUtils.copyProperties(item, vo);
        
            String orderId = item.getOrderId();
            if (orderId.startsWith(BusinessType.EXCHANGE_BATTERY.getBusiness().toString())) {
                vo.setOrderType(OrderForBatteryConstants.TYPE_ELECTRICITY_CABINET_ORDER);
            } else if (orderId.startsWith(BusinessType.RENT_BATTERY.getBusiness().toString()) || orderId.startsWith(BusinessType.RETURN_BATTERY.getBusiness().toString())) {
                vo.setOrderType(OrderForBatteryConstants.TYPE_RENT_BATTERY_ORDER);
            }
        
            return vo;
        }).collect(Collectors.toList());
    
        return Pair.of(true, list);
    }

    @Override
    @DS(value = "clickhouse")
    public int insertBatch(List<BatteryTrackRecord> tempSaveBatteryTrackRecordList) {
        return batteryTrackRecordMapper.insertBatch(tempSaveBatteryTrackRecordList);
    }
}
