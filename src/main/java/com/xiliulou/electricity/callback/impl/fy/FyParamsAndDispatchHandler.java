package com.xiliulou.electricity.callback.impl.fy;


import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.dto.callback.CallbackContext;
import com.xiliulou.electricity.dto.callback.FyParams;
import com.xiliulou.electricity.enums.FreeBusinessTypeEnum;
import com.xiliulou.pay.deposit.fengyun.config.FengYunConfig;
import com.xiliulou.pay.deposit.fengyun.utils.FyAesUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
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
@AllArgsConstructor
public class FyParamsAndDispatchHandler implements FySupport<Map<String,Object>> {
    
    private final Map<String,Integer> BUSINESS_MAP = Map.of("PAY", FreeBusinessTypeEnum.AUTH_PAY.getCode(),"UNFREEZE",FreeBusinessTypeEnum.UNFREE.getCode());
    
    
    private final FengYunConfig fengYunConfig;
    
    @Override
    public int order() {
        return Integer.MIN_VALUE;
    }
    
    @Override
    public CallbackContext<?> handler(CallbackContext<Map<String, Object>> callbackContext) {
        Map<String, Object> params = callbackContext.getParams();
        
        if (!params.containsKey("bizContent")){
            log.warn("no bizContent found in params: params[{}]", JsonUtil.toJson(params));
            return null;
        }
        
        String o = (String) params.get("bizContent");
        try {
            String aesKey = fengYunConfig.getAesKey();
            String decrypt = FyAesUtil.decrypt(o, ObjectUtils.defaultIfNull(aesKey,"RyiQwkaIB2AMvmpJk5RG1g=="));
            log.info("found the free order params from fy. bizContent = {}, params = {}", o, decrypt);
            if (FreeBusinessTypeEnum.FREE.getCode().equals(callbackContext.getBusiness())){
                FyParams.FreeDeposit fyParams = JsonUtil.fromJson(decrypt, FyParams.FreeDeposit.class);
                if (Objects.isNull(fyParams)){
                    log.warn("pxz callback {} params is illegal : {}",callbackContext.getBusiness(), decrypt);
                    return null;
                }
                return CallbackContext.builder()
                        .business(callbackContext.getBusiness())
                        .channel(callbackContext.getChannel())
                        .params(fyParams)
                        .next(Boolean.TRUE)
                        .tenantId(callbackContext.getTenantId())
                        .type(callbackContext.getType())
                        .build();
            }
            FyParams.AuthPayOrUnfree fyParams = JsonUtil.fromJson(decrypt, FyParams.AuthPayOrUnfree.class);
            if (Objects.isNull(fyParams)){
                log.warn("pxz callback {} params is illegal : {}",callbackContext.getBusiness(), decrypt);
                return null;
            }
            return CallbackContext.builder()
                    .business(BUSINESS_MAP.get(fyParams.getTradeType()))
                    .channel(callbackContext.getChannel())
                    .params(fyParams)
                    .next(Boolean.TRUE)
                    .tenantId(callbackContext.getTenantId())
                    .type(callbackContext.getType())
                    .build();
        } catch (Exception e) {
            log.error("decrypt params : {} error",callbackContext.getParams() , e);
            return null;
        }
    }
    
}
