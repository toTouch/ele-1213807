package com.xiliulou.electricity.service.retrofit;

import com.github.lianjiatech.retrofit.spring.boot.core.RetrofitClient;
import com.github.lianjiatech.retrofit.spring.boot.degrade.sentinel.SentinelDegrade;
import com.github.lianjiatech.retrofit.spring.boot.interceptor.Intercept;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.request.user.FeatureSortReq;
import com.xiliulou.electricity.service.retrofit.fallback.AuxRetrofitServiceImpl;
import com.xiliulou.electricity.service.retrofit.interceptor.MeituanServiceInterceptor;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * @author HeYafeng
 * @date 2024/12/26 10:07:44
 */
@RetrofitClient(serviceId = "saas-aux", fallback = AuxRetrofitServiceImpl.class)
@SentinelDegrade(count = 3, enable = true, timeWindow = 15, grade = 2)
@Intercept(handler = MeituanServiceInterceptor.class)
public interface AuxRetrofitService {
    
    @POST("/saas-aux/inner/user/featureSort/del")
    R deleteFeatureSort(@Body FeatureSortReq featureSortReq);
}
