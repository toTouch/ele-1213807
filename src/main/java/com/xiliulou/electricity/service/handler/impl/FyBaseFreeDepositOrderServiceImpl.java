package com.xiliulou.electricity.service.handler.impl;

import com.xiliulou.electricity.bo.FreeDepositOrderStatusBO;
import com.xiliulou.electricity.query.FreeDepositOrderRequest;
import com.xiliulou.electricity.query.FreeDepositOrderStatusQuery;
import com.xiliulou.electricity.service.handler.AbstractCommonFreeDeposit;
import com.xiliulou.electricity.service.handler.BaseFreeDepositService;
import com.xiliulou.pay.deposit.fengyun.service.FyDepositService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;

/**
 * @ClassName: fyFreeDepositOrderServiceImpl
 * @description:
 * @author: renhang
 * @create: 2024-08-22 15:29
 */
@Service("fyFreeDepositOrderServiceImpl")
@Slf4j
public class FyBaseFreeDepositOrderServiceImpl extends AbstractCommonFreeDeposit implements BaseFreeDepositService {
    
    @Resource
    private FyDepositService fyDepositService;
    
    @Override
    public Triple<Boolean, String, Object> freeDepositOrder(FreeDepositOrderRequest request) {
        Map<String, Object> map = null;
        String orderId = request.getFreeDepositOrderId();
        try {
            map = fyDepositService.authPay(buildFyAuthPayRequest(request));
        } catch (Exception e) {
            log.error("FY ERROR! freeDepositOrder fail!  orderId={}", orderId, e);
            return Triple.of(false, "100401", "免押调用失败！");
        }
        
        Triple<Boolean, String, Object> resultCheck = FyResultCheck(map, orderId);
        if (!resultCheck.getLeft()) {
            return resultCheck;
        }
        // todo 蜂云结果返回
        
        return Triple.of(true, null, null);
    }
    
    @Override
    public FreeDepositOrderStatusBO queryFreeDepositOrderStatus(FreeDepositOrderStatusQuery query) {
        Map<String, Object> map = null;
        String orderId = query.getOrderId();
        try {
            map = fyDepositService.queryFreezeStatus(buildFyFreeDepositStatusRequest(query));
        } catch (Exception e) {
            log.error("FY ERROR! freeDepositOrder fail!  orderId={}", orderId, e);
            return null;
        }
        
        Triple<Boolean, String, Object> resultCheck = FyResultCheck(map, orderId);
        if (!resultCheck.getLeft()) {
            return null;
        }
        
        String authNo = (String) map.get("authNo");
        
        Integer authStatus = FyAuthStatusToPxzStatus((String) map.get("status"));
        
        return FreeDepositOrderStatusBO.builder().authNo(authNo).authStatus(authStatus).build();
    }
    
    @Override
    public Triple<Boolean, String, Object> unFreezeDeposit(FreeDepositOrderStatusQuery query) {
        Map<String, Object> map = null;
        String orderId = query.getOrderId();
        try {
            map = fyDepositService.handleFund(buildFyUnFreeRequest(query));
        } catch (Exception e) {
            log.error("FY ERROR! freeDepositOrder fail!  orderId={}", orderId, e);
            return null;
        }
        
    }
    
    
}
