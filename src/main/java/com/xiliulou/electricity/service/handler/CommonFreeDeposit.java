package com.xiliulou.electricity.service.handler;

import cn.hutool.core.util.StrUtil;
import com.xiliulou.core.exception.CustomBusinessException;
import com.xiliulou.electricity.entity.PxzConfig;
import com.xiliulou.electricity.query.FreeDepositOrderRequest;
import com.xiliulou.electricity.service.PxzConfigService;
import com.xiliulou.pay.deposit.paixiaozu.pojo.request.PxzCommonRequest;
import com.xiliulou.pay.deposit.paixiaozu.pojo.request.PxzFreeDepositOrderRequest;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * @ClassName: CommonFreeDeposit
 * @description:
 * @author: renhang
 * @create: 2024-08-22 15:24
 */

@Slf4j
public abstract class CommonFreeDeposit {
    
    @Resource
    private PxzConfigService pxzConfigService;
    
    public PxzCommonRequest<PxzFreeDepositOrderRequest> buildFreeDepositOrderPxzRequest(FreeDepositOrderRequest freeDepositOrderRequest) {
        PxzConfig pxzConfig = pxzConfigService.queryByTenantIdFromCache(freeDepositOrderRequest.getTenantId());
        if (Objects.isNull(pxzConfig) || StrUtil.isBlank(pxzConfig.getAesKey()) || StrUtil.isBlank(pxzConfig.getMerchantCode())) {
            throw new CustomBusinessException("免押功能未配置相关信息！请联系客服处理");
        }
        
        String orderId = freeDepositOrderRequest.getFreeDepositOrderId();
        PxzCommonRequest<PxzFreeDepositOrderRequest> query = new PxzCommonRequest<>();
        query.setAesSecret(pxzConfig.getAesKey());
        query.setDateTime(System.currentTimeMillis());
        query.setSessionId(orderId);
        query.setMerchantCode(pxzConfig.getMerchantCode());
        
        PxzFreeDepositOrderRequest request = new PxzFreeDepositOrderRequest();
        request.setPhone(freeDepositOrderRequest.getPhoneNumber());
        request.setSubject(freeDepositOrderRequest.getSubject());
        request.setRealName(freeDepositOrderRequest.getRealName());
        request.setIdNumber(freeDepositOrderRequest.getIdCard());
        request.setTransId(orderId);
        request.setTransAmt(freeDepositOrderRequest.getPayAmount().multiply(BigDecimal.valueOf(100)).intValue());
        query.setData(request);
        
        return query;
    }
}
