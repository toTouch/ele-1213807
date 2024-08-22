package com.xiliulou.electricity.service.handler.impl;

import com.xiliulou.electricity.bo.FreeDepositOrderStatusBO;
import com.xiliulou.electricity.query.FreeDepositOrderRequest;
import com.xiliulou.electricity.query.FreeDepositOrderStatusQuery;
import com.xiliulou.electricity.service.handler.BaseFreeDepositService;
import com.xiliulou.electricity.service.handler.CommonFreeDeposit;
import com.xiliulou.pay.deposit.fengyun.service.FyDepositService;
import com.xiliulou.pay.deposit.paixiaozu.pojo.rsp.PxzQueryOrderRsp;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @ClassName: fyFreeDepositOrderServiceImpl
 * @description:
 * @author: renhang
 * @create: 2024-08-22 15:29
 */
@Service("fyFreeDepositOrderServiceImpl")
@Slf4j
public class FyBaseFreeDepositOrderServiceImpl extends CommonFreeDeposit implements BaseFreeDepositService {
    
    
    @Resource
    private FyDepositService fyDepositService;
    
    @Override
    public Triple<Boolean, String, Object> freeDepositOrder(FreeDepositOrderRequest request) {
        
        
        fyDepositService.authPay();
        
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
