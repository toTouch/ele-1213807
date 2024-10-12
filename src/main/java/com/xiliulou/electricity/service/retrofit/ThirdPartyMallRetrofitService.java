package com.xiliulou.electricity.service.retrofit;

import com.github.lianjiatech.retrofit.spring.boot.core.RetrofitClient;
import com.github.lianjiatech.retrofit.spring.boot.degrade.sentinel.SentinelDegrade;
import com.github.lianjiatech.retrofit.spring.boot.interceptor.Intercept;
import com.xiliulou.electricity.request.thirdPartyMall.NotifyMeiTuanDeliverReq;
import com.xiliulou.electricity.service.retrofit.fallback.ThirdPartyMallRetrofitServiceImpl;
import com.xiliulou.electricity.dto.thirdMallParty.MtDTO;
import com.xiliulou.electricity.service.retrofit.interceptor.MeituanServiceInterceptor;
import retrofit2.http.Body;
import retrofit2.http.HeaderMap;
import retrofit2.http.POST;

import java.util.Map;

/**
 * @author HeYafeng
 * @date 2024/10/12 16:01:44
 */

@RetrofitClient(serviceId = "xiliulou-thirdmall", fallback = ThirdPartyMallRetrofitServiceImpl.class)
@SentinelDegrade(count = 3, enable = true, timeWindow = 15, grade = 2)
@Intercept(handler = MeituanServiceInterceptor.class)
public interface ThirdPartyMallRetrofitService {
    
    @POST("/inner/meiTuan/notifyDeliver")
    MtDTO notifyMeiTuanDeliver(@HeaderMap Map<String, String> headers, @Body NotifyMeiTuanDeliverReq notifyMeiTuanDeliverReq);
}
