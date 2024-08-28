package com.xiliulou.electricity.service.profitsharing;

import com.xiliulou.electricity.entity.profitsharing.ProfitSharingTradeMixedOrder;

/**
 * 分账交易混合订单(TProfitSharingTradeMixedOrder)表服务接口
 *
 * @author makejava
 * @since 2024-08-27 19:19:18
 */
public interface ProfitSharingTradeMixedOrderService {
    
    
    /**
     * 新增数据
     *
     * @param tProfitSharingTradeMixedOrder 实例对象
     * @return 实例对象
     */
    void insert(ProfitSharingTradeMixedOrder tProfitSharingTradeMixedOrder);
    
    
    ProfitSharingTradeMixedOrder queryByThirdOrderNo(String thirdOrderNo);
}
