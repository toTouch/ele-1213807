package com.xiliulou.electricity.service.merchant;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.request.battery.BatteryLabelBatchUpdateRequest;

/**
 * @author: SJP
 * @Desc:
 * @create: 2025-02-21 17:27
 **/
public interface MerchantBizService {
    
    R<Integer> countReceived(Long uid);
    
    R receiveBattery(BatteryLabelBatchUpdateRequest request);
}
