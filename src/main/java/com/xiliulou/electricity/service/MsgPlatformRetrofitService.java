package com.xiliulou.electricity.service;

import com.github.lianjiatech.retrofit.spring.boot.core.RetrofitClient;
import com.github.lianjiatech.retrofit.spring.boot.degrade.sentinel.SentinelDegrade;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.request.SendMessageRequest;
import com.xiliulou.electricity.service.impl.MsgPlatformRetrofitServiceImpl;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import retrofit2.http.Body;
import retrofit2.http.POST;

@RetrofitClient(serviceId = "xiliulou-msg-service", fallback = MsgPlatformRetrofitServiceImpl.class)
@SentinelDegrade(count = 3, enable = true, timeWindow = 15, grade = 2)
public interface MsgPlatformRetrofitService {
    
    @POST("/xiliulou-msg/inner/send/message")
    R sendMessage(@Body SendMessageRequest request);
}
