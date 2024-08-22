package com.xiliulou.electricity.service.handler.impl;

import com.xiliulou.electricity.query.FreeDepositOrderRequest;
import com.xiliulou.electricity.service.handler.CommonFreeDeposit;
import com.xiliulou.electricity.service.handler.FreeDepositService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.stereotype.Service;

/**
 * @ClassName: fyFreeDepositOrderServiceImpl
 * @description:
 * @author: renhang
 * @create: 2024-08-22 15:29
 */
@Service("fyFreeDepositOrderServiceImpl")
@Slf4j
public class FyFreeDepositOrderServiceImpl extends CommonFreeDeposit implements FreeDepositService {
    
    
    @Override
    public Triple<Boolean, String, Object> freeDepositOrder(FreeDepositOrderRequest request) {
        
        return Triple.of(true, null, null);
    }
}
