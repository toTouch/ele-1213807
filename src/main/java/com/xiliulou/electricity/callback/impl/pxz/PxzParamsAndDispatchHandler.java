package com.xiliulou.electricity.callback.impl.pxz;


import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.dto.callback.CallbackContext;
import com.xiliulou.electricity.dto.callback.PxzParams;
import com.xiliulou.electricity.entity.PxzConfig;
import com.xiliulou.electricity.enums.FreeBusinessTypeEnum;
import com.xiliulou.electricity.service.PxzConfigService;
import com.xiliulou.pay.deposit.paixiaozu.PxzAesUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;

/**
 * <p>
 * Description: This class is FyParamsHandler!
 * </p>
 * <p>Project: saas-electricity</p>
 * <p>Copyright: Copyright (c) 2024</p>
 * <p>Company: xiliulou</p>
 *
 * @author <a href="mailto:wxblifeng@163.com">PeakLee</a>
 * @since V1.0 2024/8/27
 **/
@Slf4j
@Service
public class PxzParamsAndDispatchHandler implements PxzSupport<Map<String,Object>> {
    
    private final PxzConfigService pxzConfigService;
    
    public PxzParamsAndDispatchHandler(PxzConfigService pxzConfigService) {
        this.pxzConfigService = pxzConfigService;
    }
    
    @Override
    public int order() {
        return Integer.MIN_VALUE;
    }
    
    @Override
    public CallbackContext<?> handler(CallbackContext<Map<String, Object>> callbackContext) {
        PxzConfig pxzConfig = pxzConfigService.queryByTenantIdFromCache(callbackContext.getTenantId());
        if (Objects.isNull(pxzConfig)){
            log.warn("no information on free deposit configuration found: tenant[{}]", callbackContext.getTenantId());
            return null;
        }
        
        String data = (String) callbackContext.getParams().get("body");
        String encrypt = PxzAesUtils.encrypt(data, pxzConfig.getAesKey());
        
        if (FreeBusinessTypeEnum.FREE.getCode().equals(callbackContext.getBusiness())) {
            PxzParams.FreeOfCharge params = JsonUtil.fromJson(encrypt, PxzParams.FreeOfCharge.class);
            log.info("pxz callback params : {}", params);
            return CallbackContext.builder()
                    .business(params.getAuthStatus())
                    .channel(callbackContext.getChannel())
                    .params(params)
                    .tenantId(callbackContext.getTenantId())
                    .next(Boolean.TRUE)
                    .type(callbackContext.getType())
                    .build();
        }
        
        PxzParams.Withhold params = JsonUtil.fromJson(encrypt, PxzParams.Withhold.class);
        log.info("pxz callback params : {}", params);
        return CallbackContext.builder()
                .business(callbackContext.getBusiness())
                .channel(callbackContext.getChannel())
                .params(params)
                .next(Boolean.TRUE)
                .tenantId(callbackContext.getTenantId())
                .type(callbackContext.getType())
                .build();
        
    }
    
}
