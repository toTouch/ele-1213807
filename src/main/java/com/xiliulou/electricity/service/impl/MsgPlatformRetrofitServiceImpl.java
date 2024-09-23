package com.xiliulou.electricity.service.impl;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.request.SendMessageRequest;
import com.xiliulou.electricity.service.MsgPlatformRetrofitService;
import org.springframework.stereotype.Service;

/**
 * @author zzlong
 */
@Service
public class MsgPlatformRetrofitServiceImpl implements MsgPlatformRetrofitService {
    
    @Override
    public R sendMessage(SendMessageRequest request) {
        return R.fail("200005", "服务调用出错,请稍后重试");
    }
}
