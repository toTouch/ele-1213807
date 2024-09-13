package com.xiliulou.electricity.service.meituan;

import com.xiliulou.electricity.entity.meituan.MeiTuanRiderMallOrder;
import com.xiliulou.electricity.query.meituan.OrderQuery;
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
    
    MeiTuanRiderMallOrder queryByOrderId(String orderId, String phone, Long uid, Integer tenantId);
    
    List<OrderVO> listOrders(OrderQuery query);
    
    List<MeiTuanRiderMallOrder> listOrdersByPhone(OrderQuery query);
    
    Triple<Boolean, String, Object> createBatteryMemberCardOrder(OrderQuery query);
    
    /**
     * 美团骑手商城限制提单校验
     */
    LimitTradeVO meiTuanLimitTradeCheck(String providerSkuId, String phone);
    
}
