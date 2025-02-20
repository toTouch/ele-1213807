package com.xiliulou.electricity.service.impl.battery;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.ElectricityBattery;
import com.xiliulou.electricity.entity.battery.ElectricityBatteryLabel;
import com.xiliulou.electricity.service.ElectricityBatteryService;
import com.xiliulou.electricity.service.battery.ElectricityBatteryLabelBizService;
import com.xiliulou.electricity.service.battery.ElectricityBatteryLabelService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * @author SJP
 * @date 2025-02-20 17:54
 **/
@Slf4j
@Service
@RequiredArgsConstructor
public class ElectricityBatteryLabelBizServiceImpl implements ElectricityBatteryLabelBizService {
    
    private final ElectricityBatteryLabelService electricityBatteryLabelService;
    
    private final ElectricityBatteryService electricityBatteryService;
    
    @Override
    public R updateRemark(String sn, String remark) {
        Integer tenantId = TenantContextHolder.getTenantId();
        
        ElectricityBatteryLabel batteryLabel = electricityBatteryLabelService.queryBySnAndTenantId(sn, tenantId);
        Long now = System.currentTimeMillis();
        
        if (Objects.isNull(batteryLabel)) {
            ElectricityBattery electricityBattery = electricityBatteryService.queryBySnFromDb(sn, tenantId);
            if (Objects.isNull(electricityBattery)) {
                log.warn("UPDATE REMARK WARN! electricityBattery is null, sn={}, tenantId={}", sn, tenantId);
                return R.fail("ELECTRICITY.0020", "未找到电池");
            }
            
            ElectricityBatteryLabel newBatteryLabel = ElectricityBatteryLabel.builder().sn(sn).remark(remark).tenantId(tenantId).franchiseeId(electricityBattery.getFranchiseeId())
                    .createTime(now).updateTime(now).build();
            electricityBatteryLabelService.insert(newBatteryLabel);
        } else {
            ElectricityBatteryLabel batteryLabelUpdate = new ElectricityBatteryLabel();
            batteryLabelUpdate.setId(batteryLabel.getId());
            batteryLabelUpdate.setRemark(remark);
            batteryLabelUpdate.setUpdateTime(now);
            electricityBatteryLabelService.updateById(batteryLabelUpdate);
        }
        
        return R.ok();
    }
}
