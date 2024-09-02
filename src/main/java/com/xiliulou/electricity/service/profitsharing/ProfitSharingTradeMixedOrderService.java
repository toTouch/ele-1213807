package com.xiliulou.electricity.service.profitsharing;

import com.xiliulou.electricity.entity.profitsharing.ProfitSharingTradeMixedOrder;
import com.xiliulou.electricity.query.profitsharing.ProfitSharingTradeMixedOrderQueryModel;

import java.util.List;

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
    
    /**
     * 列表查询
     *
     * @param queryModel
     * @author caobotao.cbt
     * @date 2024/8/28 08:56
     */
    List<ProfitSharingTradeMixedOrder> queryListByParam(ProfitSharingTradeMixedOrderQueryModel queryModel);
    
    /**
     * 状态更新
     *
     * @param mixedOrder
     * @author caobotao.cbt
     * @date 2024/8/28 09:58
     */
    void updateStatusById(ProfitSharingTradeMixedOrder mixedOrder);
    
    
    ProfitSharingTradeMixedOrder queryByThirdOrderNo(String thirdOrderNo);
    
    List<String> listThirdOrderNoByTenantId(Integer tenantId, long startTime, Integer offset, Integer size);
    
    int updateThirdOrderNoById(ProfitSharingTradeMixedOrder profitSharingTradeMixedOrderUpdate);
    
    ProfitSharingTradeMixedOrder queryById(Long profitSharingMixedOrderId);
}
