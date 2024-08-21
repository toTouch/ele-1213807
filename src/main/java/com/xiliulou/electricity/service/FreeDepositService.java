package com.xiliulou.electricity.service;

import com.xiliulou.electricity.dto.FreeDepositUserDTO;
import org.apache.commons.lang3.tuple.Triple;

public interface FreeDepositService {
    
    Triple<Boolean, String, Object> checkExistSuccessFreeDepositOrder(Long uid, FreeDepositUserDTO freeDepositUserDTO);
    
    
    Triple<Boolean, String, Object> freeDepositOrder(String orderId);
}
