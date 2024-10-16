package com.xiliulou.electricity.service;

import com.xiliulou.electricity.bo.AuthPayStatusBO;
import com.xiliulou.electricity.bo.FreeDepositOrderStatusBO;
import com.xiliulou.electricity.dto.FreeDepositUserDTO;
import com.xiliulou.electricity.query.FreeDepositAuthToPayQuery;
import com.xiliulou.electricity.query.FreeDepositAuthToPayStatusQuery;
import com.xiliulou.electricity.query.FreeDepositCancelAuthToPayQuery;
import com.xiliulou.electricity.query.FreeDepositOrderRequest;
import com.xiliulou.electricity.query.FreeDepositOrderStatusQuery;
import com.xiliulou.electricity.query.UnFreeDepositOrderQuery;
import org.apache.commons.lang3.tuple.Triple;

/**
 * @author renhanf
 * @date 2024/8/23
 */
public interface FreeDepositService {
    
    /**
     * 查询是否进行过免押
     *
     * @param freeDepositUserDTO 查询dto
     * @return Triple
     */
    Triple<Boolean, String, Object> checkExistSuccessFreeDepositOrder(FreeDepositUserDTO freeDepositUserDTO);
    
    /**
     * 查询免押具体状态
     *
     * @param query 查询入参
     * @return FreeDepositOrderStatusBO
     */
    FreeDepositOrderStatusBO getFreeDepositOrderStatus(FreeDepositOrderStatusQuery query);
    
    /**
     * 冻结免押
     *
     * @param request 查询入参
     * @return Triple
     */
    Triple<Boolean, String, Object> freeDepositOrder(FreeDepositOrderRequest request);
    
    /**
     * 解冻免押
     *
     * @param query 查询入参
     * @return Triple
     */
    Triple<Boolean, String, Object> unFreezeDeposit(UnFreeDepositOrderQuery query);
    
    /**
     * 免押代扣
     *
     * @param query 查询入参
     * @return Triple
     */
    Triple<Boolean, String, Object> authToPay(FreeDepositAuthToPayQuery query);
    
    
    /**
     * 代扣状态
     *
     * @param query 查询入参
     * @return AuthPayStatusBO
     */
    AuthPayStatusBO queryAuthToPayStatus(FreeDepositAuthToPayStatusQuery query);
    
    /**
     * 取消代扣
     *
     * @param query
     * @return
     */
    Boolean cancelAuthPay(FreeDepositCancelAuthToPayQuery query);
    
 
}
