package com.xiliulou.electricity.service.handler;

import com.xiliulou.electricity.bo.AuthPayStatusBO;
import com.xiliulou.electricity.bo.FreeDepositOrderStatusBO;
import com.xiliulou.electricity.query.FreeDepositAuthToPayQuery;
import com.xiliulou.electricity.query.FreeDepositAuthToPayStatusQuery;
import com.xiliulou.electricity.query.FreeDepositCancelAuthToPayQuery;
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
     * 代扣状态
     *
     * @param query query
     * @return AuthPayStatusBO
     */
    AuthPayStatusBO queryAuthToPayStatus(FreeDepositAuthToPayStatusQuery query);
    
    /**
     * 取消代扣，目前只有拍小组有
     *
     * @param query query
     * @return Boolean
     */
    Boolean cancelAuthPay(FreeDepositCancelAuthToPayQuery query);
}
