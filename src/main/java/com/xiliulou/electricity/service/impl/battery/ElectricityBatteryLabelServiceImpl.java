package com.xiliulou.electricity.service.impl.battery;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.battery.BatteryLabelConstant;
import com.xiliulou.electricity.dto.battery.BatteryLabelModifyDto;
import com.xiliulou.electricity.entity.ElectricityBattery;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.entity.battery.ElectricityBatteryLabel;
import com.xiliulou.electricity.entity.merchant.Merchant;
import com.xiliulou.electricity.enums.battery.BatteryLabelEnum;
import com.xiliulou.electricity.mapper.battery.ElectricityBatteryLabelMapper;
import com.xiliulou.electricity.service.UserService;
import com.xiliulou.electricity.service.battery.ElectricityBatteryLabelService;
import com.xiliulou.electricity.service.merchant.MerchantService;
import com.xiliulou.electricity.vo.battery.ElectricityBatteryLabelVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
    
    private final UserService userService;
    
    private final MerchantService merchantService;
    
    
    @Override
    public void insert(ElectricityBatteryLabel batteryLabel) {
        electricityBatteryLabelMapper.insert(batteryLabel);
    }
    
    @Override
    public void insertWithBattery(ElectricityBattery battery) {
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
    
    @Slave
    @Override
    public ElectricityBatteryLabel queryBySnAndTenantId(String sn, Integer tenantId) {
        return electricityBatteryLabelMapper.queryBySnAndTenantId(sn, tenantId);
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
            Integer oldPreLabel = dto.getNewLabel();
            Integer newPreLabel = labelModifyDto.getNewLabel();
            // 新旧一样,刷新下时间
            if (Objects.equals(oldPreLabel, newPreLabel)) {
                redisService.expire(key, 30 * 60 * 1000L, false);
                return;
            }
            
            // 旧预修改标签是租借时，如果新预修改标签也属于租借则更新缓存，否则直接返回，租借的优先级更高
            if (Objects.nonNull(oldPreLabel) && BatteryLabelConstant.RENT_LABEL_SET.contains(oldPreLabel) && !BatteryLabelConstant.RENT_LABEL_SET.contains(newPreLabel)) {
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
    
    @Override
    public int deleteReceivedData(String sn) {
        return electricityBatteryLabelMapper.updateReceivedData(sn, System.currentTimeMillis());
    }
    
    @Slave
    public List<ElectricityBatteryLabelVO> listLabelVOBySns(List<String> sns, Map<String, Integer> snAndLabel) {
        List<ElectricityBatteryLabel> batteryLabels = electricityBatteryLabelMapper.selectListBySns(sns);
        if (CollectionUtils.isEmpty(batteryLabels)) {
            return Collections.emptyList();
        }
        
        return batteryLabels.parallelStream().map(batteryLabel -> {
            String sn = batteryLabel.getSn();
            
            ElectricityBatteryLabelVO vo = new ElectricityBatteryLabelVO();
            vo.setSn(sn);
            vo.setRemark(batteryLabel.getRemark());
            vo.setReceiverId(batteryLabel.getReceiverId());
            
            if (MapUtils.isEmpty(snAndLabel) || !snAndLabel.containsKey(sn)) {
                return vo;
            }
            
            Integer label = snAndLabel.get(sn);
            if (Objects.equals(label, BatteryLabelEnum.RECEIVED_ADMINISTRATORS.getCode())) {
                User user = userService.queryByUidFromCache(batteryLabel.getReceiverId());
                if (Objects.nonNull(user)) {
                    vo.setReceiverName(user.getName());
                }
            }
            
            if (Objects.equals(label, BatteryLabelEnum.RECEIVED_MERCHANT.getCode())) {
                Merchant merchant = merchantService.queryByIdFromCache(batteryLabel.getReceiverId());
                if (Objects.nonNull(merchant)) {
                    vo.setReceiverName(merchant.getName());
                }
            }
            
            return vo;
        }).collect(Collectors.toList());
    }
}
