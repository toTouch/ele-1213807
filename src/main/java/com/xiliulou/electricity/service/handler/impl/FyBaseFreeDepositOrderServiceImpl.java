package com.xiliulou.electricity.service.handler.impl;

import com.xiliulou.electricity.bo.AuthPayStatusBO;
import com.xiliulou.electricity.bo.FreeDepositOrderStatusBO;
import com.xiliulou.electricity.dto.FreeDepositOrderDTO;
import com.xiliulou.electricity.enums.FreeDepositChannelEnum;
import com.xiliulou.electricity.query.FreeDepositAuthToPayQuery;
import com.xiliulou.electricity.query.FreeDepositAuthToPayStatusQuery;
import com.xiliulou.electricity.query.FreeDepositCancelAuthToPayQuery;
import com.xiliulou.electricity.query.FreeDepositOrderRequest;
import com.xiliulou.electricity.query.FreeDepositOrderStatusQuery;
import com.xiliulou.electricity.query.UnFreeDepositOrderQuery;
import com.xiliulou.electricity.service.FreeDepositOrderService;
import com.xiliulou.electricity.service.handler.AbstractCommonFreeDeposit;
import com.xiliulou.electricity.service.handler.BaseFreeDepositService;
import com.xiliulou.pay.deposit.fengyun.constant.FyConstants;
import com.xiliulou.pay.deposit.fengyun.pojo.response.FyAuthPayRsp;
import com.xiliulou.pay.deposit.fengyun.pojo.response.FyHandleFundRsp;
import com.xiliulou.pay.deposit.fengyun.pojo.response.FyQueryFreezeRsp;
import com.xiliulou.pay.deposit.fengyun.pojo.response.FyQueryHandleFundRsp;
import com.xiliulou.pay.deposit.fengyun.pojo.response.FyResult;
import com.xiliulou.pay.deposit.fengyun.service.FyDepositService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;

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
    
    @Resource
    private FreeDepositOrderService freeDepositOrderService;
    
    @Override
    public Triple<Boolean, String, Object> freeDepositOrder(FreeDepositOrderRequest request) {
        FyResult<FyAuthPayRsp> result = null;
        String orderId = request.getFreeDepositOrderId();
        try {
            result = fyDepositService.authPay(buildFyFreeDepositRequest(request));
        } catch (Exception e) {
            log.error("FY ERROR! freeDepositOrder fail!  orderId={}", orderId, e);
            return Triple.of(false, "100401", "免押调用失败！");
        }
        
        Triple<Boolean, String, Object> resultCheck = fyResultCheck(result, orderId);
        if (!resultCheck.getLeft()) {
            return resultCheck;
        }
        
        FreeDepositOrderDTO dto = FreeDepositOrderDTO.builder().channel(FreeDepositChannelEnum.FY.getChannel()).data(result.getFyResponse().getOutOrderNo()).build();
        
        return Triple.of(true, null, dto);
    }
    
    @Override
    public FreeDepositOrderStatusBO queryFreeDepositOrderStatus(FreeDepositOrderStatusQuery query) {
        FyResult<FyQueryFreezeRsp> result = null;
        String orderId = query.getOrderId();
        try {
            result = fyDepositService.queryFreezeStatus(buildFyFreeDepositStatusRequest(query));
        } catch (Exception e) {
            log.error("FY ERROR! queryFreeDepositOrderStatus fail! orderId={}", orderId, e);
            return null;
        }
        
        Triple<Boolean, String, Object> resultCheck = fyResultCheck(result, orderId);
        if (!resultCheck.getLeft()) {
            return null;
        }
        
        FyQueryFreezeRsp response = result.getFyResponse();
        Integer authNo = response.getAuthNo();
        String status = response.getStatus();
        
        Integer authStatus = fyAuthStatusToPxzStatus(status);
        
        return FreeDepositOrderStatusBO.builder().authNo(String.valueOf(authNo)).authStatus(authStatus).build();
    }
    
    @Override
    public Triple<Boolean, String, Object> unFreezeDeposit(UnFreeDepositOrderQuery query) {
        FyResult<FyHandleFundRsp> result = null;
        String orderId = query.getOrderId();
        try {
            result = fyDepositService.handleFund(buildFyUnFreeRequest(query));
        } catch (Exception e) {
            log.error("FY ERROR! freeDepositOrder fail!  orderId={}", orderId, e);
            return Triple.of(false, "100401", "免押解冻调用失败！");
        }
        
        Triple<Boolean, String, Object> resultCheck = fyResultCheck(result, orderId);
        if (!resultCheck.getLeft()) {
            return resultCheck;
        }
        
        return Triple.of(true, null, "解冻中，请稍后");
    }
    
    @Override
    public Triple<Boolean, String, Object> authToPay(FreeDepositAuthToPayQuery query) {
        FyResult<FyHandleFundRsp> result = null;
        String orderId = query.getOrderId();
        try {
            result = fyDepositService.handleFund(buildFyAuthPayRequest(query));
        } catch (Exception e) {
            log.error("FY ERROR! freeDepositOrder fail!  orderId={}", orderId, e);
            return Triple.of(false, "100401", "免押代扣调用失败！");
        }
        
        Triple<Boolean, String, Object> resultCheck = fyResultCheck(result, orderId);
        if (!resultCheck.getLeft()) {
            return resultCheck;
        }
        
        return Triple.of(true, null, "免押代扣中，请稍后");
    }
    
    @Override
    public AuthPayStatusBO queryAuthToPayStatus(FreeDepositAuthToPayStatusQuery query) {
        FyResult<FyQueryHandleFundRsp> result = null;
        String orderId = query.getOrderId();
        try {
            result = fyDepositService.queryHandleFundStatus(buildFyAuthPayStatusRequest(query));
        } catch (Exception e) {
            log.error("FY ERROR! freeDepositOrder fail!  orderId={}", orderId, e);
            return null;
        }
        
        Triple<Boolean, String, Object> resultCheck = fyResultCheck(result, orderId);
        if (!resultCheck.getLeft()) {
            return null;
        }
        
        FyQueryHandleFundRsp fyResponse = result.getFyResponse();
        
        if (Objects.equals(fyResponse.getTradeType(), FyConstants.HANDLE_FUND_TRADE_TYPE_PAY)) {
            Integer payStatusToPxzStatus = fyAuthPayStatusToPxzStatus(fyResponse.getStatus());
            return AuthPayStatusBO.builder().orderId(fyResponse.getThirdOrderNo()).orderStatus(payStatusToPxzStatus).build();
        }
        
        return null;
    }
    
    @Override
    public Boolean cancelAuthPay(FreeDepositCancelAuthToPayQuery query) {
        return false;
    }
    
    
}
