package com.xiliulou.electricity.service;

import com.xiliulou.electricity.bo.FreeDepositOrderStatusBO;
import com.xiliulou.electricity.dto.FreeDepositOrderStatusDTO;
import com.xiliulou.electricity.dto.FreeDepositUserDTO;
import com.xiliulou.electricity.query.FreeDepositOrderRequest;
import org.apache.commons.lang3.tuple.Triple;

public interface FreeDepositService {
    
    /**
     * 查询是否进行过免押
     * @param uid
     * @param freeDepositUserDTO
     * @return
     */
    Triple<Boolean, String, Object> checkExistSuccessFreeDepositOrder(Long uid, FreeDepositUserDTO freeDepositUserDTO);
    
    /**
     * 查询免押具体状态
     * @param depositOrderStatusDTO
     * @return
     */
    FreeDepositOrderStatusBO checkExistSuccessFreeDepositOrder(FreeDepositOrderStatusDTO depositOrderStatusDTO);
    
    
    Triple<Boolean, String, Object> freeDepositOrder(FreeDepositOrderRequest request);
}
