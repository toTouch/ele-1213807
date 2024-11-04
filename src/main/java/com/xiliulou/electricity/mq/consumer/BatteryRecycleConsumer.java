package com.xiliulou.electricity.mq.consumer;

import cn.hutool.core.util.IdUtil;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.CommonConstant;
import com.xiliulou.electricity.dto.BatteryRecycleDelayDTO;
import com.xiliulou.electricity.entity.ElectricityCabinetBox;
import com.xiliulou.electricity.entity.batteryrecycle.BatteryRecycleRecord;
import com.xiliulou.electricity.enums.batteryrecycle.BatteryRecycleStatusEnum;
import com.xiliulou.electricity.mq.constant.MqConsumerConstant;
import com.xiliulou.electricity.mq.constant.MqProducerConstant;
import com.xiliulou.electricity.service.ElectricityCabinetBoxService;
import com.xiliulou.electricity.service.batteryRecycle.BatteryRecycleRecordService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;

/**
 * @author maxiaodong
 * @date 2024/8/27 13:50
 * @desc
 */
@Slf4j
@Component
@RocketMQMessageListener(topic = MqProducerConstant.BATTERY_RECYCLE_TOPIC_NAME, consumerGroup = MqConsumerConstant.BATTERY_RECYCLE_GROUP, consumeThreadMax = 5)
public class BatteryRecycleConsumer implements RocketMQListener<String> {
    @Resource
    private ElectricityCabinetBoxService electricityCabinetBoxService;
    
    @Resource
    private BatteryRecycleRecordService batteryRecycleRecordService;
    
    @Resource
    private RedisService redisService;
    
   /* public void onMessage(String message) {
        log.info("BATTERY RECYCLE CONSUMER INFO!received msg={}", message);
        
        MDC.put(CommonConstant.TRACE_ID, IdUtil.fastSimpleUUID());
    
        BatteryRecycleDelayDTO batteryRecycleDelayDTO = null;
        
        try {
            batteryRecycleDelayDTO = JsonUtil.fromJson(message, BatteryRecycleDelayDTO.class);
            List<ElectricityCabinetBox> electricityCabinetBoxList = electricityCabinetBoxService.listNotUsableBySn(batteryRecycleDelayDTO.getSn(), batteryRecycleDelayDTO.getCabinetId(), batteryRecycleDelayDTO.getCellNo());
            if (ObjectUtils.isEmpty(electricityCabinetBoxList)) {
                log.info("BATTERY RECYCLE CONSUMER INFO! disable cell is invalid msg={}", message);
                return;
            }
    
            BatteryRecycleRecord batteryRecycleRecord = new BatteryRecycleRecord();
            batteryRecycleRecord.setId(batteryRecycleDelayDTO.getRecycleId());
            batteryRecycleRecord.setStatus(BatteryRecycleStatusEnum.LOCK.getCode());
            batteryRecycleRecord.setElectricityCabinetId(batteryRecycleDelayDTO.getCabinetId());
            batteryRecycleRecord.setCellNo(batteryRecycleDelayDTO.getCellNo());
            batteryRecycleRecord.setUpdateTime(System.currentTimeMillis());
            batteryRecycleRecordService.updateById(batteryRecycleRecord);
        } catch (Exception e) {
            log.error("BATTERY RECYCLE CONSUMER ERROR!msg={}", message, e);
        } finally {
            MDC.clear();
        }
    }*/
    
    public void onMessage(String message) {
        log.info("BATTERY RECYCLE CONSUMER INFO!received msg={}", message);
        
        MDC.put(CommonConstant.TRACE_ID, IdUtil.fastSimpleUUID());
        
        BatteryRecycleDelayDTO batteryRecycleDelayDTO = null;
        
        try {
            batteryRecycleDelayDTO = JsonUtil.fromJson(message, BatteryRecycleDelayDTO.class);
            
            // 下发指令相应成功修改回收记录为已锁仓
            String result = redisService.get(CacheConstant.ELE_OPERATOR_CACHE_KEY + batteryRecycleDelayDTO.getSessionId());
            if (!StringUtils.isEmpty(result)) {
                Map<String, Object> map = JsonUtil.fromJson(result, Map.class);
                String value = map.get("success").toString();
                if ("true".equalsIgnoreCase(value)) {
                    BatteryRecycleRecord batteryRecycleRecord = new BatteryRecycleRecord();
                    batteryRecycleRecord.setId(batteryRecycleDelayDTO.getRecycleId());
                    batteryRecycleRecord.setStatus(BatteryRecycleStatusEnum.LOCK.getCode());
                    batteryRecycleRecord.setElectricityCabinetId(batteryRecycleDelayDTO.getCabinetId());
                    batteryRecycleRecord.setCellNo(batteryRecycleDelayDTO.getCellNo());
                    batteryRecycleRecord.setUpdateTime(System.currentTimeMillis());
                    batteryRecycleRecordService.updateById(batteryRecycleRecord);
                    return;
                }
            }
            
            // 下发禁用指令失败
            // 禁用格口启用
            ElectricityCabinetBox electricityCabinetBoxUpdate = new ElectricityCabinetBox();
            electricityCabinetBoxUpdate.setElectricityCabinetId(batteryRecycleDelayDTO.getCabinetId());
            electricityCabinetBoxUpdate.setCellNo(batteryRecycleDelayDTO.getCellNo());
            electricityCabinetBoxUpdate.setUsableStatus(ElectricityCabinetBox.ELECTRICITY_CABINET_BOX_USABLE);
            electricityCabinetBoxUpdate.setUpdateTime(System.currentTimeMillis());
            electricityCabinetBoxService.modifyCellByCellNo(electricityCabinetBoxUpdate);
        } catch (Exception e) {
            log.error("BATTERY RECYCLE CONSUMER ERROR!msg={}", message, e);
        } finally {
            MDC.clear();
        }
    }
}
