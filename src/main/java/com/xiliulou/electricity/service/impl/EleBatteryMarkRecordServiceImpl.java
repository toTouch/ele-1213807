package com.xiliulou.electricity.service.impl;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.dto.EleBatteryMarkRecordDTO;
import com.xiliulou.electricity.entity.BatteryTrackRecord;
import com.xiliulou.electricity.mq.constant.MqProducerConstant;
import com.xiliulou.electricity.service.EleBatteryMarkRecordService;
import com.xiliulou.mq.service.RocketMqService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author maxiaodong
 * @date 2024/11/12 14:09
 * @desc
 */
@Service
@Slf4j
public class EleBatteryMarkRecordServiceImpl implements EleBatteryMarkRecordService {
    
    @Resource
    private RedisService redisService;
    
    @Resource
    private RocketMqService rocketMqService;
    
    @Override
    public void checkBatteryMark(BatteryTrackRecord batteryTrackRecord) {
        if (ObjectUtils.isEmpty(batteryTrackRecord)) {
            return;
        }
    
        if (!(Objects.equals(batteryTrackRecord.getType(), BatteryTrackRecord.TYPE_EXCHANGE_IN) || Objects.equals(batteryTrackRecord.getType(),
                BatteryTrackRecord.TYPE_EXCHANGE_OUT))) {
            return;
        }
    
        String key = String.format(CacheConstant.BATTERY_MARK_KEY, batteryTrackRecord.getEId(), batteryTrackRecord.getSn());
        // 拿出电池的时候放入缓存
        if (Objects.equals(batteryTrackRecord.getType(), BatteryTrackRecord.TYPE_EXCHANGE_OUT)) {
            redisService.set(key, batteryTrackRecord.getOrderId(), 10L, TimeUnit.MINUTES);
            return;
        }
    
        // 取出缓存
        String orderId = redisService.get(key);
        if (ObjectUtils.isEmpty(orderId)) {
            return;
        }
    
        redisService.delete(key);
    
        EleBatteryMarkRecordDTO dto = new EleBatteryMarkRecordDTO();
        dto.setBatterySn(batteryTrackRecord.getSn());
        dto.setRentOrderId(orderId);
        dto.setReturnOrderId(batteryTrackRecord.getOrderId());
        dto.setCabinetId(Objects.nonNull(batteryTrackRecord.getEId()) ? batteryTrackRecord.getEId().intValue() : NumberConstant.ZERO);
        dto.setCellNo(batteryTrackRecord.getENo());
        
        rocketMqService.sendAsyncMsg(MqProducerConstant.BATTERY_MARK_TOPIC, JsonUtil.toJson(dto));
    }
}
