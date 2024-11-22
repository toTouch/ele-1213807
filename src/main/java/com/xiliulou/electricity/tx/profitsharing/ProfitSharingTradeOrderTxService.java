/**
 * Create date: 2024/8/28
 */

package com.xiliulou.electricity.tx.profitsharing;

import com.xiliulou.electricity.entity.profitsharing.ProfitSharingOrder;
import com.xiliulou.electricity.entity.profitsharing.ProfitSharingOrderDetail;
import com.xiliulou.electricity.entity.profitsharing.ProfitSharingTradeMixedOrder;
import com.xiliulou.electricity.entity.profitsharing.ProfitSharingTradeOrder;
import com.xiliulou.electricity.enums.profitsharing.ProfitSharingTradeMixedOrderStateEnum;
import com.xiliulou.electricity.enums.profitsharing.ProfitSharingTradeOderProcessStateEnum;
import com.xiliulou.electricity.mapper.profitsharing.ProfitSharingOrderDetailMapper;
import com.xiliulou.electricity.mapper.profitsharing.ProfitSharingOrderMapper;
import com.xiliulou.electricity.mapper.profitsharing.ProfitSharingTradeMixedOrderMapper;
import com.xiliulou.electricity.mapper.profitsharing.ProfitSharingTradeOrderMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * description:
 *
 * @author caobotao.cbt
 * @date 2024/8/28 10:46
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class ProfitSharingTradeOrderTxService {
    
    
    @Resource
    private ProfitSharingTradeOrderMapper profitSharingTradeOrderMapper;
    
    
    @Resource
    private ProfitSharingTradeMixedOrderMapper profitSharingTradeMixedOrderMapper;
    
    
    @Resource
    private ProfitSharingOrderMapper profitSharingOrderMapper;
    
    @Resource
    private ProfitSharingOrderDetailMapper profitSharingOrderDetailMapper;
    
    
    /**
     * 成功状态更新
     *
     * @param mixedOrder
     * @param successList
     * @author caobotao.cbt
     * @date 2024/9/6 09:21
     */
    public void updateSuccessStatus(ProfitSharingTradeMixedOrder mixedOrder, List<Long> successList) {
        long time = System.currentTimeMillis();
        mixedOrder.setUpdateTime(time);
        mixedOrder.setState(ProfitSharingTradeMixedOrderStateEnum.COMPLETE.getCode());
        profitSharingTradeMixedOrderMapper.updateStatusById(mixedOrder);
        profitSharingTradeOrderMapper.batchUpdateStateByIds(successList, ProfitSharingTradeOderProcessStateEnum.SUCCESS.getCode(), time, null);
    }
    
    
    /**
     * 状态更新
     *
     * @param mixedOrder
     * @param successList
     * @param lapsedTradeOrderIds
     * @param remark
     * @author caobotao.cbt
     * @date 2024/9/6 09:25
     */
    public void updateStatus(ProfitSharingTradeMixedOrder mixedOrder, List<Long> successList, List<Long> lapsedTradeOrderIds, String remark) {
        long time = System.currentTimeMillis();
        mixedOrder.setUpdateTime(time);
        profitSharingTradeMixedOrderMapper.updateStatusById(mixedOrder);
        
        if (CollectionUtils.isNotEmpty(lapsedTradeOrderIds)) {
            profitSharingTradeOrderMapper.batchUpdateStateByIds(lapsedTradeOrderIds, ProfitSharingTradeOderProcessStateEnum.LAPSED.getCode(), time, remark);
        }
        
        if (CollectionUtils.isNotEmpty(successList)) {
            profitSharingTradeOrderMapper.batchUpdateStateByIds(successList, ProfitSharingTradeOderProcessStateEnum.SUCCESS.getCode(), time, remark);
        }
    }
    
    
    /**
     * 分账接收方配置错误error更新
     *
     * @param mixedOrder
     * @param tradeOrderIds
     * @param insertProfitSharingOrderMap
     * @author caobotao.cbt
     * @date 2024/9/6 09:17
     */
    public void updateByReceiversConfigError(ProfitSharingTradeMixedOrder mixedOrder, List<Long> tradeOrderIds,
            Map<ProfitSharingOrder, ProfitSharingOrderDetail> insertProfitSharingOrderMap) {
        long time = System.currentTimeMillis();
        mixedOrder.setState(ProfitSharingTradeMixedOrderStateEnum.COMPLETE.getCode());
        profitSharingTradeMixedOrderMapper.updateStatusById(mixedOrder);
        profitSharingTradeOrderMapper.batchUpdateStateByIds(tradeOrderIds, ProfitSharingTradeOderProcessStateEnum.SUCCESS.getCode(), time, "分账接收方未配置");
        profitSharingOrderMapper.batchInsert(new ArrayList<>(insertProfitSharingOrderMap.keySet()));
        insertProfitSharingOrderMap.forEach((k, v) -> v.setProfitSharingOrderId(k.getId()));
        profitSharingOrderDetailMapper.batchInsert(new ArrayList<>(insertProfitSharingOrderMap.values()));
    }
    
}
