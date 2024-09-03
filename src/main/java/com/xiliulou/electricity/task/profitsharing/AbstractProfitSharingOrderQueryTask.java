/**
 * Copyright(c) 2018 Sunyur.com, All Rights Reserved. Author: sunyur Create date: 2024/8/29
 */

package com.xiliulou.electricity.task.profitsharing;

import com.xiliulou.electricity.bo.base.BasePayConfig;
import com.xiliulou.electricity.entity.profitsharing.ProfitSharingOrder;
import com.xiliulou.electricity.entity.profitsharing.ProfitSharingOrderDetail;
import com.xiliulou.electricity.enums.profitsharing.ProfitSharingOrderStatusEnum;
import com.xiliulou.electricity.query.profitsharing.ProfitSharingOrderQueryModel;
import com.xiliulou.electricity.service.profitsharing.ProfitSharingOrderDetailService;
import com.xiliulou.electricity.service.profitsharing.ProfitSharingOrderService;
import com.xiliulou.electricity.task.profitsharing.base.AbstractProfitSharingTask;
import com.xiliulou.electricity.task.profitsharing.support.PayParamsQuerySupport;
import com.xiliulou.electricity.tx.profitsharing.ProfitSharingOrderTxService;
import com.xiliulou.pay.profitsharing.ProfitSharingServiceAdapter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * description:
 *
 * @author caobotao.cbt
 * @date 2024/8/29 17:04
 */
@Slf4j
public abstract class AbstractProfitSharingOrderQueryTask<T extends BasePayConfig> extends AbstractProfitSharingTask<T> {
    
    @Resource
    private ProfitSharingOrderService profitSharingOrderService;
    
    @Resource
    private ProfitSharingOrderDetailService profitSharingOrderDetailService;
    
    @Resource
    protected ProfitSharingServiceAdapter profitSharingServiceAdapter;
    
    @Resource
    protected PayParamsQuerySupport payParamsQuerySupport;
    
    @Resource
    private ProfitSharingOrderTxService profitSharingOrderTxService;
    
    
    private static final List<Integer> SUPPORT_STATUS = Arrays
            .asList(ProfitSharingOrderStatusEnum.PROFIT_SHARING_ACCEPT.getCode(), ProfitSharingOrderStatusEnum.PROFIT_SHARING_ACCEPT.getCode());
    
    /**
     * 根据租户处理
     *
     * @param tenantId
     * @author caobotao.cbt
     * @date 2024/8/26 16:44
     */
    @Override
    protected void executeByTenantId(Integer tenantId) {
        
        log.info("AbstractProfitSharingOrderQueryTask.executeByTenantId tenantId:{} start", tenantId);
        
        ProfitSharingOrderQueryModel profitSharingOrderQueryModel = new ProfitSharingOrderQueryModel();
        profitSharingOrderQueryModel.setStartId(0L);
        profitSharingOrderQueryModel.setStatusList(SUPPORT_STATUS);
        profitSharingOrderQueryModel.setTenantId(tenantId);
        profitSharingOrderQueryModel.setSize(SIZE);
        Map<String, T> tenantFranchiseePayParamMap = new HashMap<>();
        
        while (true) {
            List<ProfitSharingOrder> orders = profitSharingOrderService.queryByIdGreaterThanAndOtherConditions(profitSharingOrderQueryModel);
            if (CollectionUtils.isEmpty(orders)) {
                break;
            }
            profitSharingOrderQueryModel.setStartId(orders.get(orders.size() - 1).getId());
            
            List<Long> profitSharingOrderIds = new ArrayList<>();
            Set<Long> franchiseeIds = new HashSet<>();
            
            orders.forEach(o -> {
                profitSharingOrderIds.add(o.getId());
                franchiseeIds.add(o.getFranchiseeId());
            });
            
            List<ProfitSharingOrderDetail> orderDetails = profitSharingOrderDetailService.queryListByProfitSharingOrderIds(tenantId, profitSharingOrderIds);
            
            Map<Long, List<ProfitSharingOrderDetail>> profitSharingOrderIdGPMap = Optional.ofNullable(orderDetails).orElse(Collections.emptyList()).stream()
                    .collect(Collectors.groupingBy(ProfitSharingOrderDetail::getProfitSharingOrderId));
            
            this.queryBuildTenantFranchiseePayParamMap(tenantFranchiseePayParamMap, tenantId, franchiseeIds);
            
            orders.forEach(order -> {
                List<ProfitSharingOrderDetail> curOrderDetails = profitSharingOrderIdGPMap.get(order.getId());
                T payParams = tenantFranchiseePayParamMap.get(payParamsQuerySupport.getPayParamMapKey(order.getTenantId(), order.getFranchiseeId()));
                this.dealWithByProfitSharingOrder(payParams, order, curOrderDetails);
            });
        }
        
        log.info("AbstractProfitSharingOrderQueryTask.executeByTenantId tenantId:{} end", tenantId);
        
    }
    
    /**
     * 处理订单
     *
     * @param payParams
     * @param order
     * @param curOrderDetails
     * @author caobotao.cbt
     * @date 2024/8/30 09:07
     */
    private void dealWithByProfitSharingOrder(T payParams, ProfitSharingOrder order, List<ProfitSharingOrderDetail> curOrderDetails) {
        Boolean flag = this.queryBuildProfitSharingOrder(payParams, order, curOrderDetails);
        if (!flag) {
            // 结果处理失败，等后续处理
            return;
        }
        
        profitSharingOrderTxService.update(order, curOrderDetails);
    }
    
    /**
     * 查询以及构建
     *
     * @param payParams
     * @param order
     * @param curOrderDetails
     * @author caobotao.cbt
     * @date 2024/8/30 09:10
     */
    protected abstract Boolean queryBuildProfitSharingOrder(T payParams, ProfitSharingOrder order, List<ProfitSharingOrderDetail> curOrderDetails);
    
}
