package com.xiliulou.electricity.service.retrofit;

import com.github.lianjiatech.retrofit.spring.boot.core.RetrofitClient;
import com.github.lianjiatech.retrofit.spring.boot.degrade.sentinel.SentinelDegrade;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.request.InitTenantSubscriptRequest;
import com.xiliulou.electricity.service.retrofit.fallback.MsgCenterRetrofitServiceImpl;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * @author renhang
 */
@RetrofitClient(serviceId = "xiliulou-msg-service", fallback = MsgCenterRetrofitServiceImpl.class)
@SentinelDegrade(count = 3, enable = true, timeWindow = 15, grade = 2)
public interface MsgCenterRetrofitService {
    
    /**
     * 初始化租户订阅消息
     *
     * @param request  request
     * @return R
     */
    @POST("/out/init/tenantSubscript")
    R initTenantSubscriptMsg(@Body InitTenantSubscriptRequest request);
    
}
