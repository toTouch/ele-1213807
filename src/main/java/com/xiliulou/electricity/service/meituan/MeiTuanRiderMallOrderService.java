package com.xiliulou.electricity.service.meituan;

import com.xiliulou.electricity.entity.meituan.MeiTuanRiderMallOrder;
import com.xiliulou.electricity.query.meituan.OrderQuery;
import com.xiliulou.electricity.vo.meituan.OrderVO;

import java.util.List;

/**
 * @author HeYafeng
 * @description 美团骑手商城订单
 * @date 2024/8/28 18:05:30
 */
public interface MeiTuanRiderMallOrderService {
    
    void handelFetchOrderTask(String sessionId, Long startTime, Integer recentDay);
    
    void handelSyncOrderStatusTask(String sessionId, long startTime);
    
    MeiTuanRiderMallOrder queryByOrderId(String orderId, String phone, Long uid);
    
    List<OrderVO> listOrders(OrderQuery query);
    
    List<MeiTuanRiderMallOrder> listOrdersByUidAndPhone(OrderQuery query);
    
    void createBatteryMemberCardOrder(OrderQuery query);
    
    List<MeiTuanRiderMallOrder> listUnSyncedOrder(Integer tenantId);
}
