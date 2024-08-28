package com.xiliulou.electricity.service.profitsharing;


import com.xiliulou.electricity.entity.profitsharing.ProfitSharingOrder;
import com.xiliulou.electricity.entity.profitsharing.ProfitSharingTradeMixedOrder;
import com.xiliulou.electricity.entity.profitsharing.ProfitSharingTradeOrder;
import com.xiliulou.electricity.mq.model.ProfitSharingTradeOrderRefund;

/**
 * 分账订单表(profitSharingOrder)表服务接口
 *
 * @author maxiaodong
 * @since 2024-08-22 16:58:56
 */
public interface ProfitSharingOrderService {
    
    boolean existsUnfreezeByThirdOrderNo(String thirdOrderNo);
    
    int insert(ProfitSharingOrder profitSharingOrder);
    
    void doUnFreeze(ProfitSharingTradeOrder profitSharingTradeOrder, ProfitSharingTradeOrderRefund profitSharingTradeOrderRefund,
            ProfitSharingTradeMixedOrder profitSharingTradeMixedOrder);
}
