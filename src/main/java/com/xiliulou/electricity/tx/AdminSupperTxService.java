package com.xiliulou.electricity.tx;

import com.xiliulou.electricity.mapper.BatteryOtherPropertiesMapper;
import com.xiliulou.electricity.mapper.ElectricityBatteryMapper;
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
    
    @Transactional(rollbackFor = Exception.class)
    public void delBatteryBySnList(Integer tenantId, List<String> batterySnList) {
        electricityBatteryMapper.batchDeleteBySnList(tenantId, batterySnList);
        batteryOtherPropertiesMapper.batchDeleteBySnList(tenantId, batterySnList);
    }
}
