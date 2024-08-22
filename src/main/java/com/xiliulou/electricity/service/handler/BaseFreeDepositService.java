package com.xiliulou.electricity.service.handler;

import com.xiliulou.electricity.bo.FreeDepositOrderStatusBO;
import com.xiliulou.electricity.dto.FreeDepositOrderStatusQuery;
import com.xiliulou.electricity.dto.FreeDepositUserDTO;
import com.xiliulou.electricity.query.FreeDepositOrderRequest;
import org.apache.commons.lang3.tuple.Triple;

/**
 * @ClassName: BaseFreeDepositService
 * @description:
 * @author: renhang
 * @create: 2024-08-22 15:29
 */
public interface BaseFreeDepositService {
    
    
    /**
     * 冻结免押
     *
     * @param request
     * @return
     */
    Triple<Boolean, String, Object> freeDepositOrder(FreeDepositOrderRequest request);
    
    
    FreeDepositOrderStatusBO queryFreeDepositOrderStatus(FreeDepositOrderStatusQuery query);
}
