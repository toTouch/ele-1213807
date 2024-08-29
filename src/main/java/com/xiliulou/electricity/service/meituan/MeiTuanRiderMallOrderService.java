package com.xiliulou.electricity.service.meituan;

import com.xiliulou.electricity.entity.meituan.MeiTuanRiderMallOrder;

/**
 * @author HeYafeng
 * @description 美团骑手商城订单
 * @date 2024/8/28 18:05:30
 */
public interface MeiTuanRiderMallOrderService {
    
    void handelFetchOrders(String sessionId, Long startTime, Integer recentDay);
    
    MeiTuanRiderMallOrder queryByOrderIdAndPhone(String orderId, String phone);
}
