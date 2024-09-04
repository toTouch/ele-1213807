package com.xiliulou.electricity.service.meituan;

import com.xiliulou.electricity.entity.meituan.MeiTuanRiderMallConfig;
import com.xiliulou.electricity.entity.meituan.MeiTuanRiderMallOrder;
import com.xiliulou.electricity.query.meituan.OrderQuery;
import com.xiliulou.electricity.request.meituan.LimitTradeRequest;
import com.xiliulou.electricity.vo.meituan.LimitTradeVO;
import com.xiliulou.electricity.vo.meituan.OrderVO;
import org.apache.commons.lang3.tuple.Triple;

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
    
    Triple<Boolean, String, Object> createBatteryMemberCardOrder(OrderQuery query);
    
    List<MeiTuanRiderMallOrder> listAllUnSyncedOrder(Integer tenantId);
    
    List<MeiTuanRiderMallOrder> listUnSyncedOrder(Integer tenantId, Integer offset, Integer size);
    
    /**
     * 美团骑手商城限制提单校验
     */
    LimitTradeVO meiTuanLimitTradeCheck(LimitTradeRequest request, MeiTuanRiderMallConfig meiTuanRiderMallConfig);
    
}
