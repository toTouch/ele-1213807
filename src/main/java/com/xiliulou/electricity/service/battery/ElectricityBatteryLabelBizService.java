package com.xiliulou.electricity.service.battery;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.request.battery.BatteryLabelBatchUpdateRequest;

/**
 * @author: SJP
 * @Desc:
 * @create: 2025-02-20 17:54
 **/
public interface ElectricityBatteryLabelBizService {
    
    R updateRemark(String sn, String remark);
    
    R batchUpdate(BatteryLabelBatchUpdateRequest request);
}
