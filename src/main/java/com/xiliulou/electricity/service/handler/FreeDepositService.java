package com.xiliulou.electricity.service.handler;

import com.xiliulou.electricity.query.FreeDepositOrderRequest;
import org.apache.commons.lang3.tuple.Triple;

/**
 * @ClassName: PxzFreeDepositOrderServiceImpl
 * @description:
 * @author: renhang
 * @create: 2024-08-22 15:29
 */
public interface FreeDepositService {
    
    
    /**
     * 冻结免押
     *
     * @param request
     * @return
     */
    Triple<Boolean, String, Object> freeDepositOrder(FreeDepositOrderRequest request);
}
