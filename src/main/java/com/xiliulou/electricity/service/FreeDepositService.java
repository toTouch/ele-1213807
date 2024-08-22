package com.xiliulou.electricity.service;

import com.xiliulou.electricity.bo.FreeDepositOrderStatusBO;
import com.xiliulou.electricity.dto.FreeDepositOrderStatusDTO;
import com.xiliulou.electricity.dto.FreeDepositOrderStatusQuery;
import com.xiliulou.electricity.dto.FreeDepositUserDTO;
import com.xiliulou.electricity.query.FreeDepositOrderRequest;
import org.apache.commons.lang3.tuple.Triple;

public interface FreeDepositService {
    
    /**
     * 查询是否进行过免押
     *
     * @param freeDepositUserDTO
     * @return
     */
    Triple<Boolean, String, Object> checkExistSuccessFreeDepositOrder(FreeDepositUserDTO freeDepositUserDTO);
    
    /**
     * 查询免押具体状态
     *
     * @param query
     * @return
     */
    FreeDepositOrderStatusBO getFreeDepositOrderStatus(FreeDepositOrderStatusQuery query);
    
    /**
     * 冻结免押
     *
     * @param request
     * @return
     */
    Triple<Boolean, String, Object> freeDepositOrder(FreeDepositOrderRequest request);
    
    /**
     * 解冻免押
     *
     * @param depositOrderStatusDTO
     * @return
     */
    FreeDepositOrderStatusBO unFreezeDeposit(FreeDepositOrderStatusDTO depositOrderStatusDTO);
}
