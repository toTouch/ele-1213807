package com.xiliulou.electricity.service.handler.impl;

import com.xiliulou.core.exception.CustomBusinessException;
import com.xiliulou.electricity.bo.FreeDepositOrderStatusBO;
import com.xiliulou.electricity.query.FreeDepositOrderRequest;
import com.xiliulou.electricity.query.FreeDepositOrderStatusQuery;
import com.xiliulou.electricity.service.handler.AbstractCommonFreeDeposit;
import com.xiliulou.electricity.service.handler.BaseFreeDepositService;
import com.xiliulou.pay.deposit.fengyun.service.FyDepositService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;

/**
 * @ClassName: fyFreeDepositOrderServiceImpl
 * @description:
 * @author: renhang
 * @create: 2024-08-22 15:29
 */
@Service("fyFreeDepositOrderServiceImpl")
@Slf4j
public class FyBaseFreeDepositOrderServiceImpl extends AbstractCommonFreeDeposit implements BaseFreeDepositService {
    
    @Resource
    private FyDepositService fyDepositService;
    
    @Override
    public Triple<Boolean, String, Object> freeDepositOrder(FreeDepositOrderRequest request) {
        
        try {
            Map<String, Object> map = fyDepositService.authPay(buildFyAuthPayRequest(request));
        } catch (Exception e) {
            throw new CustomBusinessException("");
        }
        
        return Triple.of(true, null, null);
    }
    
    @Override
    public FreeDepositOrderStatusBO queryFreeDepositOrderStatus(FreeDepositOrderStatusQuery query) {
        return null;
    }
    
    @Override
    public Triple<Boolean, String, Object> unFreezeDeposit(FreeDepositOrderStatusQuery query) {
        return null;
    }
    
    
}
