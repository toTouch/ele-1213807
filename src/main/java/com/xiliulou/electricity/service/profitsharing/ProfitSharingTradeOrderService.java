package com.xiliulou.electricity.service.profitsharing;

import com.xiliulou.electricity.entity.profitsharing.ProfitSharingOrder;
import com.xiliulou.electricity.domain.profitsharing.ProfitSharingTradeOrderThirdOrderNoDO;
import com.xiliulou.electricity.entity.profitsharing.ProfitSharingTradeOrder;
import com.xiliulou.electricity.query.profitsharing.ProfitSharingTradeOrderQueryModel;

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
    
    
    /**
     * 条件查询
     *
     * @param queryModel
     * @return
     * @author caobotao.cbt
     * @date 2024/8/26 17:11
     */
    List<ProfitSharingTradeOrderThirdOrderNoDO> queryThirdOrderNoListByParam(ProfitSharingTradeOrderQueryModel queryModel);
    
    
    /**
     * 根据第三方订单号查询
     *
     * @param tenantId
     * @param thirdOrderNos
     * @author caobotao.cbt
     * @date 2024/8/26 18:01
     */
    List<ProfitSharingTradeOrder> queryListByThirdOrderNos(Integer tenantId, List<String> thirdOrderNos);
    
    
    boolean existsNotRefundByThirdOrderNo(String thirdOrderNo, String orderNo);
}
