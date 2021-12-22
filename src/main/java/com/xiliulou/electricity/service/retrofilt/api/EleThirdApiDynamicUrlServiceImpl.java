package com.xiliulou.electricity.service.retrofilt.api;

import com.xiliulou.core.http.retrofit.RetrofitDynamicUrlService;
import com.xiliulou.electricity.entity.ThirdCallBackUrl;
import com.xiliulou.electricity.service.ThirdCallBackUrlService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * @author : eclair
 * @date : 2021/8/4 3:23 下午
 */
@Service
@Slf4j
public class EleThirdApiDynamicUrlServiceImpl implements RetrofitDynamicUrlService {
    @Autowired
    ThirdCallBackUrlService thirdCallBackUrlService;

    @Override
    public String getRealUrl(Integer tenantId, Integer urlType) {
        ThirdCallBackUrl thirdCallBackUrl =
                thirdCallBackUrlService.queryByTenantIdFromCache(tenantId);
        if (Objects.isNull(thirdCallBackUrl)) {
            log.error("CUPBOARD THIRD API DYNAMIC URL ERROR! not found url! tenantId={}", tenantId);
            return null;
        }

        switch (urlType) {
            case ThirdCallBackUrl.RENT_URL:
                return thirdCallBackUrl.getReturnUrl();
            case ThirdCallBackUrl.EXCHANGE_URL:
                return thirdCallBackUrl.getExchangeUrl();
            case ThirdCallBackUrl.RETURN_URL:
                return thirdCallBackUrl.getReturnUrl();
            default:
                log.error("CUPBOARD THIRD API DYNAMIC URL ERROR!  url type illegal! tenantId={},type={}", tenantId, urlType);
                return null;

        }
    }
}
