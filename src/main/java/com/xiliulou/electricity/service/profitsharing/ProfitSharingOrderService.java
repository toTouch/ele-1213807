package com.xiliulou.electricity.service.profitsharing;


import com.xiliulou.electricity.entity.profitsharing.ProfitSharingOrder;
import com.xiliulou.electricity.entity.profitsharing.ProfitSharingTradeMixedOrder;
import com.xiliulou.electricity.entity.profitsharing.ProfitSharingTradeOrder;
import com.xiliulou.electricity.mq.model.ProfitSharingTradeOrderRefund;
import com.xiliulou.electricity.query.profitsharing.ProfitSharingOrderQueryModel;
import com.xiliulou.pay.base.exception.ProfitSharingException;

import java.util.List;

/**
 * 分账订单表(profitSharingOrder)表服务接口
 *
 * @author maxiaodong
 * @since 2024-08-22 16:58:56
 */
public interface ProfitSharingOrderService {
    
    boolean existsUnfreezeByThirdOrderNo(String thirdOrderNo);
    
    int insert(ProfitSharingOrder profitSharingOrder);
    
    void doUnFreeze(ProfitSharingTradeMixedOrder profitSharingTradeMixedOrder) throws ProfitSharingException;
    
    List<String> listUnfreezeByThirdOrderNo(List<String> thirdOrderNoList);
    
    /**
     * 根据租户id+第三方单号查询
     *
     * @param tenantId
     * @param thirdOrderNos
     * @author caobotao.cbt
     * @date 2024/8/30 08:45
     */
    List<ProfitSharingOrder> queryListByThirdOrderNos(Integer tenantId, List<String> thirdOrderNos);
    
    /**
     * 条件查询
     *
     * @param profitSharingOrderQueryModel
     * @author caobotao.cbt
     * @date 2024/8/30 08:59
     */
    List<ProfitSharingOrder> queryByIdGreaterThanAndOtherConditions(ProfitSharingOrderQueryModel profitSharingOrderQueryModel);
    
}
