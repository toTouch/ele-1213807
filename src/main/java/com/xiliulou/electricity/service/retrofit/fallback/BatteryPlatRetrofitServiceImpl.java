package com.xiliulou.electricity.service.retrofit.fallback;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.dto.bms.BatteryInfoDto;
import com.xiliulou.electricity.dto.bms.BatteryTrackDto;
import com.xiliulou.electricity.service.retrofit.BatteryPlatRetrofitService;
import com.xiliulou.electricity.web.query.battery.*;
import com.xiliulou.electricity.web.query.jt808.Jt808DeviceControlRequest;
import com.xiliulou.electricity.web.query.jt808.Jt808GetInfoRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @author : eclair
 * @date : 2023/4/12 16:29
 */
@Service
public class BatteryPlatRetrofitServiceImpl implements BatteryPlatRetrofitService {

    @Override
    public R batchSave(Map<String, String> headers, BatteryBatchOperateQuery request) {
        return R.fail("200005", "服务调用出错,请稍后重试");
    }

    @Override
    public R batchDelete(Map<String, String> headers, BatteryBatchOperateQuery request) {
        return R.fail("200005", "服务调用出错,请稍后重试");
    }

    @Override
    public R modifyBatterySn(Map<String, String> headers, BatteryModifyQuery request) {
        return R.fail("200005", "服务调用出错,请稍后重试");
    }

    @Override
    public R<BatteryInfoDto> queryBatteryInfo(Map<String, String> headers, BatteryInfoQuery batteryInfoQuery) {
        return R.fail("200005", "服务调用出错,请稍后重试");
    }


    @Override
    public R<List<BatteryTrackDto>> queryBatteryTrack(Map<String, String> headers, BatteryLocationTrackQuery batteryLocationTrackQuery) {
        return R.fail("200005", "服务调用出错,请稍后重试");
    }
    @Override
    public R changeBatterySoc(Map<String, String> headers, BatteryChangeSocQuery batteryChangeSocQuery) {
        return R.fail("200005", "服务调用出错,请稍后重试");
    }
}
