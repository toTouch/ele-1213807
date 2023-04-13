package com.xiliulou.electricity.service.retrofit;

import com.github.lianjiatech.retrofit.spring.boot.core.RetrofitClient;
import com.github.lianjiatech.retrofit.spring.boot.degrade.sentinel.SentinelDegrade;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.service.retrofit.fallback.BatteryPlatRetrofitServiceImpl;
import com.xiliulou.electricity.service.retrofit.fallback.Jt808RetrofitFallBackImpl;
import com.xiliulou.electricity.vo.Jt808DeviceInfoVo;
import com.xiliulou.electricity.web.query.battery.BatteryBatchOperateQuery;
import com.xiliulou.electricity.web.query.battery.BatteryModifyQuery;
import com.xiliulou.electricity.web.query.jt808.Jt808DeviceControlRequest;
import com.xiliulou.electricity.web.query.jt808.Jt808GetInfoRequest;
import retrofit2.http.Body;
import retrofit2.http.HeaderMap;
import retrofit2.http.POST;

import java.util.Map;

/**
 * @author : eclair
 * @date : 2022/12/29 09:36
 */
@RetrofitClient(serviceId = "xiliulou-battery-service", fallback = BatteryPlatRetrofitServiceImpl.class)
public interface BatteryPlatRetrofitService {

    @POST("/battery/inner/battery/batch/save")
    R batchSave(@HeaderMap Map<String, String> headers, @Body BatteryBatchOperateQuery request);

    @POST("/battery/inner/battery/batch/delete")
    R batchDelete(@HeaderMap Map<String, String> headers, @Body BatteryBatchOperateQuery request);


    @POST("/battery/inner/battery/modifye")
    R modifyBatterySn(@HeaderMap Map<String, String> headers, @Body BatteryModifyQuery request);
}
