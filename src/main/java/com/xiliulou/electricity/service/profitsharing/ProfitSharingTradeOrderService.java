package com.xiliulou.electricity.service.profitsharing;

import com.xiliulou.electricity.entity.profitsharing.ProfitSharingTradeOrder;

import java.util.List;

/**
 * @author maxiaodong
 * @date 2024/8/23 17:04
 * @desc
 */
public interface ProfitSharingTradeOrderService {
    
    int batchInsert(List<ProfitSharingTradeOrder> profitSharingTradeOrderList);
    
    ProfitSharingTradeOrder queryByOrderNo(String orderNo);
    
    int updateById(ProfitSharingTradeOrder profitSharingUpdate);
    
    boolean existsNotRefundByThirdOrderNo(String thirdOrderNo, String orderNo);
}
