package com.xiliulou.electricity.tx;

import com.xiliulou.electricity.mapper.BatteryOtherPropertiesMapper;
import com.xiliulou.electricity.mapper.ElectricityBatteryMapper;
import com.xiliulou.electricity.mapper.battery.ElectricityBatteryLabelMapper;
import com.xiliulou.electricity.service.battery.ElectricityBatteryLabelService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author: Ant
 * @Date 2024/4/22
 * @Description:
 **/
@Component
public class AdminSupperTxService {
    
    @Resource
    private BatteryOtherPropertiesMapper batteryOtherPropertiesMapper;
    
    @Resource
    private ElectricityBatteryMapper electricityBatteryMapper;
    
    @Resource
    private ElectricityBatteryLabelMapper electricityBatteryLabelMapper;
    
    @Transactional(rollbackFor = Exception.class)
    public void delBatteryBySnList(Integer tenantId, List<String> batterySnList) {
        electricityBatteryMapper.batchDeleteBySnList(tenantId, batterySnList);
        batteryOtherPropertiesMapper.batchDeleteBySnList(tenantId, batterySnList);
        // 删除电池标签关联数据
        electricityBatteryLabelMapper.batchDeleteBySnList(tenantId, batterySnList);
    }
}
