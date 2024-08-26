package com.xiliulou.electricity.service.callback.impl;

import com.xiliulou.core.exception.CustomBusinessException;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.constant.FreeDepositConstant;
import com.xiliulou.electricity.entity.FreeDepositAlipayHistory;
import com.xiliulou.electricity.entity.FreeDepositOrder;
import com.xiliulou.electricity.enums.FreeDepositChannelEnum;
import com.xiliulou.electricity.service.FreeDepositAlipayHistoryService;
import com.xiliulou.electricity.service.FreeDepositOrderService;
import com.xiliulou.electricity.service.callback.FreeDepositCallBackSerivce;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @ClassName: FreeDepositCallBackServiceImpl
 * @description:
 * @author: renhang
 * @create: 2024-08-25 12:59
 */
@Service
@Slf4j
public class FreeDepositCallBackServiceImpl implements FreeDepositCallBackSerivce {
    
    @Resource
    private FreeDepositOrderService freeDepositOrderService;
    
    @Resource
    private FreeDepositAlipayHistoryService freeDepositAlipayHistoryService;
    
    
    @Override
    public String authPayNotified(Integer channel, Map<String, Object> params) {
        
        if (Objects.equals(channel, FreeDepositChannelEnum.PXZ.getChannel())) {
            String orderId = (String) params.get("orderId");
            FreeDepositOrder freeDepositOrder = freeDepositOrderService.selectByOrderId(orderId);
            
            Map<String, Object> map = new HashMap<>(1);
            // 如果没有订单则确认成功
            if (Objects.isNull(freeDepositOrder)) {
                log.error("authPayNotified Error! freeDepositOrder is null, orderId is{}", orderId);
                map.put("respCode", FreeDepositConstant.AUTH_PXZ_SUCCESS_RSP);
                return JsonUtil.toJson(map);
            }
            Integer orderStatus = (Integer) params.get("orderStatus");
            if (Objects.equals(orderStatus, FreeDepositConstant.AUTH_PXZ_SUCCESS_RECEIVE)) {
                handlerAuthPaySuccess(freeDepositOrder);
                map.put("respCode", FreeDepositConstant.AUTH_PXZ_SUCCESS_RSP);
                return JsonUtil.toJson(map);
            }
            map.put("respCode", FreeDepositConstant.AUTH_PXZ_FAIL_RSP);
            return JsonUtil.toJson(map);
        }
        
        if (Objects.equals(channel, FreeDepositChannelEnum.FY.getChannel())) {
            // 蜂云只要有回调就一定是成功
            String orderId = (String) params.get("tradeNo");
            FreeDepositOrder freeDepositOrder = freeDepositOrderService.selectByOrderId(orderId);
            if (Objects.isNull(freeDepositOrder)) {
                log.error("authPayNotified Error! freeDepositOrder is null, orderId is{}", orderId);
                return FreeDepositConstant.AUTH_FY_SUCCESS_RSP;
            }
            handlerAuthPaySuccess(freeDepositOrder);
            return FreeDepositConstant.AUTH_FY_SUCCESS_RSP;
        }
        
        throw new CustomBusinessException("代扣回调异常");
    }
    
    
    private void handlerAuthPaySuccess(FreeDepositOrder freeDepositOrder) {
        // 更新免押订单状态
        FreeDepositOrder freeDepositOrderUpdate = new FreeDepositOrder();
        freeDepositOrderUpdate.setId(freeDepositOrder.getId());
        freeDepositOrderUpdate.setPayStatus(FreeDepositOrder.PAY_STATUS_DEAL_SUCCESS);
        freeDepositOrderUpdate.setUpdateTime(System.currentTimeMillis());
        freeDepositOrderService.update(freeDepositOrderUpdate);
        
        FreeDepositAlipayHistory freeDepositAlipayHistory = new FreeDepositAlipayHistory();
        freeDepositAlipayHistory.setOrderId(freeDepositOrder.getOrderId());
        freeDepositAlipayHistory.setPayStatus(freeDepositOrderUpdate.getPayStatus());
        freeDepositAlipayHistory.setUpdateTime(System.currentTimeMillis());
        freeDepositAlipayHistoryService.updateByOrderId(freeDepositAlipayHistory);
    }
}
