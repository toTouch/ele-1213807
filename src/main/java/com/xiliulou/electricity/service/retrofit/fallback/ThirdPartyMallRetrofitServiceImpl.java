package com.xiliulou.electricity.service.retrofit.fallback;

import com.xiliulou.electricity.request.thirdParty.NotifyMeiTuanDeliverReq;
import com.xiliulou.electricity.service.retrofit.ThirdPartyMallRetrofitService;
import com.xiliulou.electricity.dto.thirdParty.MtDTO;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author HeYafeng
 * @date 2024/10/12 16:08:08
 */
@Service
public class ThirdPartyMallRetrofitServiceImpl implements ThirdPartyMallRetrofitService {
    
    @Override
    public MtDTO notifyMeiTuanDeliver(Map<String, String> headers, NotifyMeiTuanDeliverReq notifyMeiTuanDeliverReq) {
        return MtDTO.failError(MtDTO.USE_FAILED);
    }
}
