package com.xiliulou.electricity.service.impl.battery;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.dto.battery.BatteryLabelModifyDto;
import com.xiliulou.electricity.entity.ElectricityBattery;
import com.xiliulou.electricity.entity.battery.ElectricityBatteryLabel;
import com.xiliulou.electricity.enums.battery.BatteryLabelEnum;
import com.xiliulou.electricity.mapper.battery.ElectricityBatteryLabelMapper;
import com.xiliulou.electricity.service.battery.ElectricityBatteryLabelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
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
            batteryLabels.add(
                    ElectricityBatteryLabel.builder().sn(battery.getSn()).tenantId(battery.getTenantId()).franchiseeId(battery.getFranchiseeId()).createTime(now).updateTime(now)
                            .build());
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
            String key = String.format(CacheConstant.PRE_MODIFY_BATTERY_LABEL, eId, cellNo, sn);
            String dtoStr = redisService.get(key);
            // 缓存内没有，不用做优先级校验
            if (StringUtils.isEmpty(dtoStr) || StringUtils.isBlank(dtoStr)) {
                redisService.saveWithString(key, labelModifyDto, 30L, TimeUnit.MINUTES);
                return;
            }
            
            BatteryLabelModifyDto dto = JsonUtil.fromJson(dtoStr, BatteryLabelModifyDto.class);
            Integer oldPreLabel = dto.getPreLabel();
            Integer newPreLabel = labelModifyDto.getPreLabel();
            // 新旧一样,刷新下时间
            if (Objects.equals(oldPreLabel, newPreLabel)) {
                redisService.expire(key, 30 * 60 * 1000L, false);
                return;
            }
            
            // 旧预修改标签是租借时，如果新预修改标签也属于租借则更新缓存，否则直接返回，租借的优先级更高
            Set<Integer> rentLabels = Set.of(BatteryLabelEnum.RENT_NORMAL.getCode(), BatteryLabelEnum.RENT_OVERDUE.getCode(), BatteryLabelEnum.RENT_LONG_TERM_UNUSED.getCode());
            if (rentLabels.contains(oldPreLabel) && !rentLabels.contains(newPreLabel)) {
                return;
            }
            
            redisService.saveWithString(key, labelModifyDto, 30L, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.error("BATTERY LABEL SET PRE LABEL ERROR! sn={}", sn, e);
        }
    }
    
    @Slave
    @Override
    public List<ElectricityBatteryLabel> listBySns(List<String> sns) {
        return electricityBatteryLabelMapper.selectListBySns(sns);
    }
}
