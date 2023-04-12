package com.xiliulou.electricity.service.retrofit.fallback;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.service.retrofit.BatteryPlatRetrofitService;
import com.xiliulou.electricity.web.query.battery.BatteryBatchOperateQuery;
import com.xiliulou.electricity.web.query.battery.BatteryModifyQuery;
import com.xiliulou.electricity.web.query.jt808.Jt808DeviceControlRequest;
import com.xiliulou.electricity.web.query.jt808.Jt808GetInfoRequest;
import org.springframework.stereotype.Service;

/**
 * @author : eclair
 * @date : 2023/4/12 16:29
 */
@Service
public class BatteryPlatRetrofitServiceImpl implements BatteryPlatRetrofitService {
    @Override
    public R batchSave(BatteryBatchOperateQuery request) {
        return null;
    }

    @Override
    public R batchDelete(BatteryBatchOperateQuery request) {
        return null;
    }

    @Override
    public R modifyBatterySn(BatteryModifyQuery request) {
        return null;
    }
}
