package com.xiliulou.electricity.service.retrofit.fallback;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.request.user.FeatureSortReq;
import com.xiliulou.electricity.service.retrofit.AuxRetrofitService;
import org.springframework.stereotype.Service;

/**
 * @date 2024/12/26 10:07:44
 * @author HeYafeng
 */
@Service
public class AuxRetrofitServiceImpl implements AuxRetrofitService {
    
    @Override
    public R deleteFeatureSort(FeatureSortReq featureSortReq) {
        return R.fail("200005", "服务调用出错,请稍后重试");
    }
}
