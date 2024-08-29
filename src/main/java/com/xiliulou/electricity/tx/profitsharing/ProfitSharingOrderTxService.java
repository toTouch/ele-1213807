/**
 * Copyright(c) 2018 Sunyur.com, All Rights Reserved. Author: sunyur Create date: 2024/8/28
 */

package com.xiliulou.electricity.tx.profitsharing;

import com.xiliulou.electricity.entity.profitsharing.ProfitSharingOrder;
import com.xiliulou.electricity.entity.profitsharing.ProfitSharingOrderDetail;
import com.xiliulou.electricity.entity.profitsharing.ProfitSharingTradeMixedOrder;
import com.xiliulou.electricity.mapper.profitsharing.ProfitSharingOrderDetailMapper;
import com.xiliulou.electricity.mapper.profitsharing.ProfitSharingOrderMapper;
import com.xiliulou.electricity.mapper.profitsharing.ProfitSharingTradeMixedOrderMapper;
import com.xiliulou.electricity.mapper.profitsharing.ProfitSharingTradeOrderMapper;
import com.xiliulou.electricity.task.profitsharing.AbstractProfitSharingTradeOrderTask;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * description:
 *
 * @author caobotao.cbt
 * @date 2024/8/28 10:46
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class ProfitSharingOrderTxService {
    
    
    @Resource
    private ProfitSharingTradeOrderMapper profitSharingTradeOrderMapper;
    
    
    @Resource
    private ProfitSharingTradeMixedOrderMapper profitSharingTradeMixedOrderMapper;
    
    
    @Resource
    private ProfitSharingOrderMapper profitSharingOrderMapper;
    
    @Resource
    private ProfitSharingOrderDetailMapper profitSharingOrderDetailMapper;
    
    
    public void save(List<Long> tradeOrderIds, Integer tradeOrderState, String remark, Map<ProfitSharingOrder, ProfitSharingOrderDetail> insertProfitSharingOrderMap) {
        long time = System.currentTimeMillis();
        profitSharingTradeOrderMapper.batchUpdateStateByIds(tradeOrderIds, tradeOrderState, time, remark);
        profitSharingOrderMapper.batchInsert(new ArrayList<>(insertProfitSharingOrderMap.keySet()));
        insertProfitSharingOrderMap.forEach((k, v) -> v.setProfitSharingOrderId(k.getId()));
        profitSharingOrderDetailMapper.batchInsert(new ArrayList<>(insertProfitSharingOrderMap.values()));
    }
    
    public void insert(List<AbstractProfitSharingTradeOrderTask.ProfitSharingCheckModel> checkModels) {
        
        List<ProfitSharingOrder> profitSharingOrders = checkModels.stream().map(v -> v.getProfitSharingOrder()).collect(Collectors.toList());
        profitSharingOrderMapper.batchInsert(profitSharingOrders);
        List<ProfitSharingOrderDetail> details = new ArrayList<>();
        checkModels.forEach(profitSharingCheckModel -> {
            ProfitSharingOrder profitSharingOrder = profitSharingCheckModel.getProfitSharingOrder();
            profitSharingCheckModel.getProfitSharingDetailsCheckModels().forEach(detailsCheckModel -> {
                ProfitSharingOrderDetail profitSharingOrderDetail = detailsCheckModel.getProfitSharingOrderDetail();
                profitSharingOrderDetail.setProfitSharingOrderId(profitSharingOrder.getId());
                details.add(profitSharingOrderDetail);
            });
        });
        profitSharingOrderDetailMapper.batchInsert(details);
    }
    
    
    public void update(List<AbstractProfitSharingTradeOrderTask.ProfitSharingCheckModel> successList) {
        successList.forEach(profitSharingCheckModel -> {
            profitSharingOrderMapper.update(profitSharingCheckModel.getProfitSharingOrder());
            profitSharingCheckModel.getProfitSharingDetailsCheckModels().forEach(details -> {
                profitSharingOrderDetailMapper.update(details.getProfitSharingOrderDetail());
            });
            
        });
    }
}
