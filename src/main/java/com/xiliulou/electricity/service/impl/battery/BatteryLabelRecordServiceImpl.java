package com.xiliulou.electricity.service.impl.battery;

import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.utils.TimeUtils;
import com.xiliulou.db.dynamic.annotation.DS;
import com.xiliulou.electricity.entity.ElectricityBattery;
import com.xiliulou.electricity.entity.battery.BatteryLabelRecord;
import com.xiliulou.electricity.mapper.battery.BatteryLabelRecordMapper;
import com.xiliulou.electricity.request.battery.BatteryLabelRecordRequest;
import com.xiliulou.electricity.service.battery.BatteryLabelRecordService;
import com.xiliulou.mq.service.RocketMqService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

import static com.xiliulou.electricity.mq.constant.MqProducerConstant.BATTERY_LABEL_RECORD_TOPIC;

/**
 * @author SJP
 * @date 2025-02-14 15:43
 **/
@Slf4j
@Service
@RequiredArgsConstructor
public class BatteryLabelRecordServiceImpl implements BatteryLabelRecordService {

    private final RocketMqService rocketMqService;
    
    private final BatteryLabelRecordMapper batteryLabelRecordMapper;
    
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    
    @Override
    public void sendRecord(ElectricityBattery battery, Long uid, Integer newLabel, Long updateTime) {
        BatteryLabelRecord record = new BatteryLabelRecord();
        record.setSn(battery.getSn());
        record.setOldLabel(battery.getLabel());
        record.setNewLabel(newLabel);
        record.setOperatorUid(uid);
        record.setTenantId(battery.getTenantId());
        record.setFranchiseeId(battery.getFranchiseeId());
        record.setExchangeTime(TimeUtils.convertToStandardFormatTime(updateTime));
        
        rocketMqService.sendAsyncMsg(BATTERY_LABEL_RECORD_TOPIC, JsonUtil.toJson(record));
    }
    
    @Override
    @DS(value = "clickhouse")
    public List<BatteryLabelRecord> listPage(BatteryLabelRecordRequest request) {
        transformQueryCondition(request);
        return batteryLabelRecordMapper.listPage(request);
    }
    
    @Override
    @DS(value = "clickhouse")
    public Long countAll(BatteryLabelRecordRequest request) {
        transformQueryCondition(request);
        return batteryLabelRecordMapper.countAll(request);
    }
    
    private void transformQueryCondition(BatteryLabelRecordRequest request) {
        if (Objects.nonNull(request.getStartTime())) {
            LocalDateTime beginLocalDateTime = LocalDateTime.ofEpochSecond(request.getStartTime() / 1000, 0, ZoneOffset.ofHours(8));
            request.setStartTimeStr(formatter.format(beginLocalDateTime));
        }
        
        if (Objects.nonNull(request.getEndTime())) {
            LocalDateTime endLocalDateTime = LocalDateTime.ofEpochSecond(request.getEndTime() / 1000, 0, ZoneOffset.ofHours(8));
            request.setEndTimeStr(formatter.format(endLocalDateTime));
        }
    }
}
