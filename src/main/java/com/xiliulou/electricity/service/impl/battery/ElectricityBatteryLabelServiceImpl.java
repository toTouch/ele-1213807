package com.xiliulou.electricity.service.impl.battery;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.dto.battery.BatteryLabelModifyDto;
import com.xiliulou.electricity.entity.ElectricityBattery;
import com.xiliulou.electricity.entity.battery.ElectricityBatteryLabel;
import com.xiliulou.electricity.mapper.battery.ElectricityBatteryLabelMapper;
import com.xiliulou.electricity.service.battery.ElectricityBatteryLabelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author SJP
 * @date 2025-02-14 15:43
 **/
@Slf4j
@Service
@RequiredArgsConstructor
public class ElectricityBatteryLabelServiceImpl implements ElectricityBatteryLabelService {
    
    private final RedisService redisService;
    
    private final ElectricityBatteryLabelMapper electricityBatteryLabelMapper;
    
    
    @Override
    public void insert(ElectricityBattery battery) {
        Long now = System.currentTimeMillis();
        ElectricityBatteryLabel batteryLabel = ElectricityBatteryLabel.builder().sn(battery.getSn()).tenantId(battery.getTenantId()).franchiseeId(battery.getFranchiseeId())
                .createTime(now).updateTime(now).build();
        electricityBatteryLabelMapper.insert(batteryLabel);
    }
    
    @Override
    public void batchInsert(List<ElectricityBattery> batteries) {
        if (CollectionUtils.isEmpty(batteries)) {
            return;
        }
        
        List<ElectricityBatteryLabel> batteryLabels = new ArrayList<>();
        Long now = System.currentTimeMillis();
        for (ElectricityBattery battery : batteries) {
            batteryLabels.add(ElectricityBatteryLabel.builder().sn(battery.getSn()).tenantId(battery.getTenantId()).franchiseeId(battery.getFranchiseeId())
                    .createTime(now).updateTime(now).build());
        }
        electricityBatteryLabelMapper.batchInsert(batteryLabels);
    }
    
    @Override
    public int updateById(ElectricityBatteryLabel batteryLabel) {
        return electricityBatteryLabelMapper.updateById(batteryLabel);
    }
    
    @Override
    public void setPreLabel(Integer eId, String cellNo, String sn, BatteryLabelModifyDto labelModifyDto) {
        try {
            redisService.saveWithString(String.format(CacheConstant.PRE_MODIFY_BATTERY_LABEL, eId, cellNo, sn), labelModifyDto, 30L, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.error("BATTERY LABEL SET PRE LABEL ERROR! sn={}", sn, e);
        }
    }
}
