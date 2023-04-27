package com.xiliulou.electricity.service.retrofit.fallback;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.service.retrofit.Jt808RetrofitService;
import com.xiliulou.electricity.web.query.jt808.Jt808DeviceControlRequest;
import com.xiliulou.electricity.web.query.jt808.Jt808GetInfoRequest;
import org.springframework.stereotype.Service;

/**
 * @author : eclair
 * @date : 2022/12/29 09:36
 */
@Service
public class Jt808RetrofitFallBackImpl implements Jt808RetrofitService {
    
    @Override
    public R getInfo(Jt808GetInfoRequest request) {
        return R.fail("200005","服务调用出错");
    }
    
    @Override
    public R controlDevice(Jt808DeviceControlRequest request) {
        return R.fail("200005","服务调用出错");
    }
}
