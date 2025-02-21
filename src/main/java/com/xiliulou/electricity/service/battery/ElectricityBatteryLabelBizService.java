package com.xiliulou.electricity.service.battery;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.ElectricityBattery;
import com.xiliulou.electricity.entity.battery.ElectricityBatteryLabel;
import com.xiliulou.electricity.request.battery.BatteryLabelBatchUpdateRequest;

/**
 * @author: SJP
 * @Desc:
 * @create: 2025-02-20 17:54
 **/
public interface ElectricityBatteryLabelBizService {
    
    R updateRemark(String sn, String remark);
    
    /**
     * 使用此方法时需要注意batteryLabel内设置的属性，只设置自己需要保存或需要更新的属性，后续代码会自动根据对象内的非null属性处理逻辑，设置了多余的属性会出现意料之外的数据修改
     *
     */
    void updateOrInsertBatteryLabel(ElectricityBattery battery, ElectricityBatteryLabel batteryLabel);
    
    R batchUpdate(BatteryLabelBatchUpdateRequest request);
}
