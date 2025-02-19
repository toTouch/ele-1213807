package com.xiliulou.electricity.service.impl.battery;

import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.utils.TimeUtils;
import com.xiliulou.electricity.entity.ElectricityBattery;
import com.xiliulou.electricity.entity.battery.BatteryLabelRecord;
import com.xiliulou.electricity.service.battery.BatteryLabelRecordService;
import com.xiliulou.rocketmq5.service.RocketMq5ProducerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static com.xiliulou.electricity.mq.constant.MqProducerConstant.BATTERY_LABEL_RECORD_TOPIC;

/**
 * @author SJP
 * @date 2025-02-14 15:43
 **/
@Slf4j
@Service
@RequiredArgsConstructor
public class BatteryLabelRecordServiceImpl implements BatteryLabelRecordService {

    private final RocketMq5ProducerService rocketMq5ProducerService;
    
    
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
        
        rocketMq5ProducerService.sendAsyncMessage(BATTERY_LABEL_RECORD_TOPIC, JsonUtil.toJson(record));
    }
}
