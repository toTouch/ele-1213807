package com.xiliulou.electricity.service.handler;

import com.xiliulou.electricity.bo.FreeDepositOrderStatusBO;
import com.xiliulou.electricity.query.FreeDepositAuthToPayQuery;
import com.xiliulou.electricity.query.FreeDepositOrderRequest;
import com.xiliulou.electricity.query.FreeDepositOrderStatusQuery;
import com.xiliulou.electricity.query.UnFreeDepositOrderQuery;
import org.apache.commons.lang3.tuple.Triple;

import java.util.Map;

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
     * @param request 请求入参
     * @return Triple
     */
    Triple<Boolean, String, Object> freeDepositOrder(FreeDepositOrderRequest request);
    
    /**
     * 查询免押订单状态
     *
     * @param query 请求入参
     * @return FreeDepositOrderStatusBO
     */
    FreeDepositOrderStatusBO queryFreeDepositOrderStatus(FreeDepositOrderStatusQuery query);
    
    /**
     * 解冻免押
     *
     * @param query 请求入参
     * @return Triple
     */
    Triple<Boolean, String, Object> unFreezeDeposit(UnFreeDepositOrderQuery query);
    
    /**
     * 免押支付
     *
     * @param query 请求入参
     * @return Triple
     */
    Triple<Boolean, String, Object> authToPay(FreeDepositAuthToPayQuery query);
    
    
    /**
     * 免押代扣回调
     *
     * @param business business
     * @param params map
     * @return
     */
    Object freeDepositNotified(Integer business, Map<String, Object> params);
}
