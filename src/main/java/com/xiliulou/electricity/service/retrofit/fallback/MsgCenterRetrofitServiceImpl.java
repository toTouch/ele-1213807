package com.xiliulou.electricity.service.retrofit.fallback;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.request.InitTenantSubscriptRequest;
import com.xiliulou.electricity.service.retrofit.MsgCenterRetrofitService;
import org.springframework.stereotype.Service;

/**
 * @author renhang
 */
@Service
public class MsgCenterRetrofitServiceImpl implements MsgCenterRetrofitService {
    
    
    @Override
    public R initTenantSubscriptMsg(InitTenantSubscriptRequest request) {
        return R.fail("200005", "服务调用出错");
    }
}
