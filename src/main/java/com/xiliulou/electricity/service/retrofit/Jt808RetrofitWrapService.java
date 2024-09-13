package com.xiliulou.electricity.service.retrofit;


import com.xiliulou.core.web.R;
import com.xiliulou.electricity.config.jt808.Jt808Configuration;
import com.xiliulou.electricity.vo.Jt808DeviceInfoVo;
import com.xiliulou.electricity.web.query.jt808.Jt808DeviceControlRequest;
import com.xiliulou.electricity.web.query.jt808.Jt808GetInfoRequest;
import org.springframework.stereotype.Service;
import retrofit2.http.POST;

import javax.annotation.Resource;

/**
 * @author : eclair
 * @date : 2022/12/29 09:36
 */
// TODO: 2024/9/13 JT808 测试环境临时代码解决无法调用成功问题
@Service
public class Jt808RetrofitWrapService {
    
    @Resource
    private Jt808RetrofitService jt808RetrofitService;
    
    @Resource
    private Jt808Configuration jt808Configuration;

    public R<Jt808DeviceInfoVo> getInfo(Jt808GetInfoRequest request){
        if (jt808Configuration.getClose()){
            return R.ok(new Jt808DeviceInfoVo());
        }
        return jt808RetrofitService.getInfo(request);
    }
    
    
    public R controlDevice(Jt808DeviceControlRequest request){
        if (jt808Configuration.getClose()){
            return R.ok();
        }
        return jt808RetrofitService.controlDevice(request);
    }
}
