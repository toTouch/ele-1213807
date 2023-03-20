package com.xiliulou.electricity.service.impl;

import com.xiliulou.core.thread.XllThreadPoolExecutors;
import com.xiliulou.electricity.entity.ElectricityPayParams;
import com.xiliulou.electricity.entity.UserOauthBind;
import com.xiliulou.electricity.service.ElectricityPayParamsService;
import com.xiliulou.electricity.service.ShippingManagerService;
import com.xiliulou.electricity.service.UserOauthBindService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.pay.shipping.service.ShippingUploadService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.concurrent.ExecutorService;

/**
 * 小程序发货管理
 *
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-03-15-13:49
 */
@Slf4j
@Service
public class ShippingManagerServiceImpl implements ShippingManagerService {
    
    private ExecutorService shippingManagerExecutorService = XllThreadPoolExecutors
            .newFixedThreadPool("shippingUpload", 2, "ele_shipping");
    
    @Autowired
    ShippingUploadService shippingUploadService;
    
    @Autowired
    UserOauthBindService userOauthBindService;
    
    @Autowired
    ElectricityPayParamsService electricityPayParamsService;
    
    @Override
    public void uploadShippingInfo(Long uid, String phone, String orderNo, Integer tenantId) {
        
        //支付相关
        ElectricityPayParams electricityPayParams = electricityPayParamsService.queryFromCache(tenantId);
        if (Objects.isNull(electricityPayParams)) {
            log.error("SHIPPING ERROR! not found electricityPayParams,tenantId={}", tenantId);
            return;
        }
        
        if (StringUtils.isBlank(electricityPayParams.getMerchantMinProAppId()) || StringUtils
                .isBlank(electricityPayParams.getMerchantMinProAppSecert())) {
            log.error("SHIPPING ERROR! electricityPayParams is illegal,tenantId={}", tenantId);
            return;
        }
        
        UserOauthBind userOauthBind = userOauthBindService
                .queryByUserPhone(phone, UserOauthBind.SOURCE_WX_PRO, TenantContextHolder.getTenantId());
        if (Objects.isNull(userOauthBind)) {
            log.error("SHIPPING ERROR! userOauthBind is illegal,tenantId={},phone={}", tenantId, phone);
            return;
        }
        
        shippingManagerExecutorService.execute(() -> {
            shippingUploadService.shippingUploadInfo(userOauthBind.getThirdId(), orderNo,
                    electricityPayParams.getMerchantMinProAppId(), electricityPayParams.getMerchantMinProAppSecert());
        });
    }
}
