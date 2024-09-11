/**
 * Copyright(c) 2018 Sunyur.com, All Rights Reserved. Author: sunyur Create date: 2024/8/29
 */

package com.xiliulou.electricity.task.profitsharing;

import com.xiliulou.electricity.bo.base.BasePayConfig;
import com.xiliulou.electricity.entity.profitsharing.ProfitSharingOrder;
import com.xiliulou.electricity.entity.profitsharing.ProfitSharingOrderDetail;
import com.xiliulou.electricity.entity.profitsharing.ProfitSharingStatistics;
import com.xiliulou.electricity.enums.profitsharing.ProfitSharingOrderStatusEnum;
import com.xiliulou.electricity.exception.BizException;
import com.xiliulou.electricity.query.profitsharing.ProfitSharingOrderQueryModel;
import com.xiliulou.electricity.service.profitsharing.ProfitSharingOrderDetailService;
import com.xiliulou.electricity.service.profitsharing.ProfitSharingOrderService;
import com.xiliulou.electricity.service.profitsharing.ProfitSharingStatisticsService;
import com.xiliulou.electricity.task.profitsharing.base.AbstractProfitSharingTask;
import com.xiliulou.electricity.task.profitsharing.support.PayParamsQuerySupport;
import com.xiliulou.electricity.tx.profitsharing.ProfitSharingOrderTxService;
import com.xiliulou.electricity.utils.DateUtils;
import com.xiliulou.pay.profitsharing.ProfitSharingServiceAdapter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
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
    protected ProfitSharingStatisticsService profitSharingStatisticsService;
    
    @Resource
    private ProfitSharingOrderTxService profitSharingOrderTxService;
    
    
    /**
     * 已受理、处理中状态
     */
    private static final List<Integer> SUPPORT_STATUS = Arrays
            .asList(ProfitSharingOrderStatusEnum.PROFIT_SHARING_ACCEPT.getCode(), ProfitSharingOrderStatusEnum.PROFIT_SHARING_IN_PROCESS.getCode());
    
    /**
     * 根据租户处理
     *
     * @param tenantId
     * @author caobotao.cbt
     * @date 2024/8/26 16:44
     */
    @Override
    protected void executeByTenantId(Integer tenantId) {
        
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
                DealWithProfitSharingOrderModel orderModel = new DealWithProfitSharingOrderModel(order, curOrderDetails, BigDecimal.ZERO);
                this.dealWithByProfitSharingOrder(payParams, orderModel);
            });
        }
        
    }
    
    /**
     * 处理订单
     *
     * @param payParams
     * @param orderModel
     * @author caobotao.cbt
     * @date 2024/8/30 09:07
     */
    private void dealWithByProfitSharingOrder(T payParams, DealWithProfitSharingOrderModel orderModel) {
        
        try {
            this.queryBuildProfitSharingOrder(payParams, orderModel);
            
            BigDecimal failAmount = orderModel.getFailAmount();
            
            if (failAmount.compareTo(BigDecimal.ZERO) > 0) {
                
                // 存在失败金额 加锁执行
                this.profitSharingStatisticsTryLockExecute(payParams.getTenantId(), payParams.getFranchiseeId(),
                        () -> this.dealWithUpdateProfitSharingOrderAndStatistics(orderModel));
            } else {
                // 不存在失败金额
                profitSharingOrderTxService.update(orderModel.getOrder(), orderModel.curOrderDetails, null);
            }
            
            
        } catch (BizException e) {
            log.info("AbstractProfitSharingOrderQueryTask.dealWithByProfitSharingOrder BizException:", e);
        }
    }
    
    private ProfitSharingOrder dealWithUpdateProfitSharingOrderAndStatistics(DealWithProfitSharingOrderModel orderModel) {
        ProfitSharingOrder order = orderModel.getOrder();
        List<ProfitSharingOrderDetail> curOrderDetails = orderModel.getCurOrderDetails();
        BigDecimal failAmount = orderModel.getFailAmount();
        
        Long createTime = curOrderDetails.get(0).getCreateTime();
        String monthDate = DateUtils.getMonthDate(createTime);
        // 存在失败金额
        ProfitSharingStatistics sharingStatistics = profitSharingStatisticsService
                .queryByTenantIdAndFranchiseeIdAndStatisticsTime(order.getTenantId(), order.getFranchiseeId(), monthDate);
        sharingStatistics.setTotalAmount(failAmount);
        profitSharingOrderTxService.update(order, curOrderDetails, sharingStatistics);
        
        return order;
    }
    
    /**
     * 查询以及构建
     *
     * @param payParams
     * @param orderModel
     * @author caobotao.cbt
     * @date 2024/8/30 09:10
     */
    protected abstract void queryBuildProfitSharingOrder(T payParams, DealWithProfitSharingOrderModel orderModel);
    
    
    @Data
    @AllArgsConstructor
    public static class DealWithProfitSharingOrderModel {
        
        private ProfitSharingOrder order;
        
        private List<ProfitSharingOrderDetail> curOrderDetails;
        
        private BigDecimal failAmount;
        
        /**
         * 失败金额累计
         *
         * @param profitSharingAmount
         * @author caobotao.cbt
         * @date 2024/9/4 18:08
         */
        public void addFailAmount(BigDecimal profitSharingAmount) {
            this.failAmount = this.failAmount.add(profitSharingAmount);
        }
    }
}
