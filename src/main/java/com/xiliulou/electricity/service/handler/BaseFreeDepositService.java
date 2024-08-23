package com.xiliulou.electricity.service.handler;

import com.xiliulou.electricity.bo.FreeDepositOrderStatusBO;
import com.xiliulou.electricity.query.FreeDepositOrderRequest;
import com.xiliulou.electricity.query.FreeDepositOrderStatusQuery;
import com.xiliulou.electricity.query.UnFreeDepositOrderQuery;
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
    
    /**
     * 查询免押订单状态
     *
     * @param query
     * @return
     */
    FreeDepositOrderStatusBO queryFreeDepositOrderStatus(FreeDepositOrderStatusQuery query);
    
    /**
     * 解冻免押
     *
     * @param query
     * @return
     */
    Triple<Boolean, String, Object>  unFreezeDeposit(UnFreeDepositOrderQuery query);
}
