package com.xiliulou.electricity.service;

import com.xiliulou.electricity.bo.FreeDepositOrderStatusBO;
import com.xiliulou.electricity.dto.FreeDepositUserDTO;
import com.xiliulou.electricity.query.FreeDepositOrderRequest;
import com.xiliulou.electricity.query.FreeDepositOrderStatusQuery;
import com.xiliulou.electricity.query.UnFreeDepositOrderQuery;
import com.xiliulou.pay.deposit.paixiaozu.pojo.rsp.PxzQueryOrderRsp;
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
     * @param
     * @return
     */
    Triple<Boolean, String, Object> unFreezeDeposit(UnFreeDepositOrderQuery query);
    
//    Triple<Boolean, String, Object> authToPay(FreeDepositOrderStatusQuery query);
}
