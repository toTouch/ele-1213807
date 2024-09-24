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
    
    
    /**
     * 根据第三方订单号查询 + 渠道查询 + 处理状态
     *
     * @param tenantId
     * @param channel
     * @param thirdOrderNos
     * @author caobotao.cbt
     * @date 2024/8/26 18:01
     */
    List<ProfitSharingTradeOrder> queryListByThirdOrderNosAndChannelAndProcessState(Integer tenantId, Integer processState, String channel, List<String> thirdOrderNos);
    
    
    boolean existsNotRefundByThirdOrderNo(String thirdOrderNo, String orderNo);
    
    String queryOrderNoByThirdOrderNo(String thirdOrderNo);
    
    /**
     * 批量更新退款状态
     *
     * @param ids
     * @param processState
     * @param remark
     * @author caobotao.cbt
     * @date 2024/8/29 11:12
     */
    void batchUpdateStatus(List<Long> ids, Integer processState, String remark);
    
    List<ProfitSharingTradeOrder> listNotPaySuccessByOrderNo(String orderNo);
    
    int batchRemoveByIdList(List<Long> notPayTradeOrderIds);
}
