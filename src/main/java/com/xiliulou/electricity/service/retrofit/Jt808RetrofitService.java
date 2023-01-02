package com.xiliulou.electricity.service.retrofit;

import com.github.lianjiatech.retrofit.spring.boot.core.RetrofitClient;
import com.github.lianjiatech.retrofit.spring.boot.degrade.sentinel.SentinelDegrade;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.service.retrofit.fallback.Jt808RetrofitFallBackImpl;
import com.xiliulou.electricity.vo.Jt808DeviceInfoVo;
import com.xiliulou.electricity.web.query.jt808.Jt808DeviceControlRequest;
import com.xiliulou.electricity.web.query.jt808.Jt808GetInfoRequest;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * @author : eclair
 * @date : 2022/12/29 09:36
 */
@RetrofitClient(serviceId = "jt808", fallback = Jt808RetrofitFallBackImpl.class)
@SentinelDegrade(count = 3, enable = true, timeWindow = 30, grade = 2)
public interface Jt808RetrofitService {
    
    @POST("/jt808/inner/api/device/info")
    R<Jt808DeviceInfoVo> getInfo(@Body Jt808GetInfoRequest request);
    
    @POST("/jt808/inner/api/device/operate/lock")
    R controlDevice(@Body Jt808DeviceControlRequest request);
}
