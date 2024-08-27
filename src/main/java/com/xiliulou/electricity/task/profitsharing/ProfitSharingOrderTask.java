/**
 * Copyright(c) 2018 Sunyur.com, All Rights Reserved. Author: sunyur Create date: 2024/8/26
 */

package com.xiliulou.electricity.task.profitsharing;

import java.math.BigDecimal;

import com.google.api.client.util.Sets;
import com.google.common.collect.Maps;
import com.xiliulou.electricity.bo.wechat.WechatPayParamsDetails;
import com.xiliulou.electricity.domain.profitsharing.ProfitSharingTradeOrderThirdOrderNoDO;
import com.xiliulou.electricity.entity.profitsharing.ProfitSharingOrder;
import com.xiliulou.electricity.entity.profitsharing.ProfitSharingOrderDetail;
import com.xiliulou.electricity.entity.profitsharing.ProfitSharingReceiverConfig;
import com.xiliulou.electricity.entity.profitsharing.ProfitSharingTradeOrder;
import com.xiliulou.electricity.enums.BusinessType;
import com.xiliulou.electricity.enums.profitsharing.ProfitSharingBusinessTypeEnum;
import com.xiliulou.electricity.enums.profitsharing.ProfitSharingOrderDetailStatusEnum;
import com.xiliulou.electricity.enums.profitsharing.ProfitSharingOrderDetailUnfreezeStatusEnum;
import com.xiliulou.electricity.enums.profitsharing.ProfitSharingOrderStatusEnum;
import com.xiliulou.electricity.enums.profitsharing.ProfitSharingOrderTypeEnum;
import com.xiliulou.electricity.enums.profitsharing.ProfitSharingQueryDetailsEnum;
import com.xiliulou.electricity.enums.profitsharing.ProfitSharingTradeOderProcessStateEnum;
import com.xiliulou.electricity.query.profitsharing.ProfitSharingTradeOrderQueryModel;
import com.xiliulou.electricity.service.ElectricityPayParamsService;
import com.xiliulou.electricity.service.TenantService;
import com.xiliulou.electricity.service.WechatPayParamsBizService;
import com.xiliulou.electricity.service.profitsharing.ProfitSharingTradeOrderService;
import com.xiliulou.electricity.ttl.TtlTraceIdSupport;
import com.xiliulou.electricity.utils.OrderIdUtil;
import com.xiliulou.pay.weixinv3.exception.WechatPayException;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * description:
 *
 * @author caobotao.cbt
 * @date 2024/8/26 16:33
 */
@Slf4j
@Component
@JobHandler(value = "profitSharingOrderTask")
public class ProfitSharingOrderTask extends IJobHandler {
    
    @Resource
    private TenantService tenantService;
    
    @Resource
    private ProfitSharingTradeOrderService profitSharingTradeOrderService;
    
    @Resource
    private WechatPayParamsBizService wechatPayParamsBizService;
    
    public static final Integer SIZE = 200;
    
    @Override
    public ReturnT<String> execute(String param) throws Exception {
        TtlTraceIdSupport.set();
        Integer startTenantId = 0;
        try {
            while (true) {
                List<Integer> tenantIds = tenantService.queryIdListByStartId(0, SIZE);
                if (CollectionUtils.isEmpty(tenantIds)) {
                    break;
                }
                startTenantId = tenantIds.get(tenantIds.size() - 1);
                this.executeByTenantId(startTenantId);
            }
            
            return ReturnT.SUCCESS;
        } finally {
            TtlTraceIdSupport.clear();
        }
    }
    
    
    /**
     * 根据租户处理
     *
     * @param startTenantId
     * @author caobotao.cbt
     * @date 2024/8/26 16:44
     */
    private void executeByTenantId(Integer tenantId) {
        Long startId = 0L;
        ProfitSharingTradeOrderQueryModel queryModel = new ProfitSharingTradeOrderQueryModel();
        queryModel.setProcessState(ProfitSharingTradeOderProcessStateEnum.AWAIT.getCode());
        queryModel.setTenantId(tenantId);
        queryModel.setSize(SIZE);
        
        Map<String, WechatPayParamsDetails> tenantFranchiseePayParamMap = new HashMap<>();
        
        while (true) {
            queryModel.setStartId(startId);
            List<ProfitSharingTradeOrderThirdOrderNoDO> thirdOrderNoList = profitSharingTradeOrderService.queryThirdOrderNoListByParam(queryModel);
            if (CollectionUtils.isEmpty(thirdOrderNoList)) {
                break;
            }
            startId = thirdOrderNoList.get(thirdOrderNoList.size() - 1).getId();
            
            List<String> thirdOrderNos = thirdOrderNoList.stream().map(ProfitSharingTradeOrderThirdOrderNoDO::getThirdOrderNo).distinct().collect(Collectors.toList());
            List<ProfitSharingTradeOrder> tradeOrders = profitSharingTradeOrderService.queryListByThirdOrderNos(tenantId, thirdOrderNos);
            
            Map<String, List<ProfitSharingTradeOrder>> thirdOrderNoMap = Maps.newConcurrentMap();
            Set<Long> franchiseeIds = new HashSet<>();
            tradeOrders.forEach(order -> {
                thirdOrderNoMap.computeIfAbsent(order.getThirdOrderNo(), k -> new ArrayList<>()).add(order);
                franchiseeIds.add(order.getFranchiseeId());
            });
            
            // 查询构建支付配置
            this.queryBuildTenantFranchiseePayParamMap(tenantFranchiseePayParamMap, tenantId, franchiseeIds);
            
            thirdOrderNoMap.forEach((thirdOrderNo, orders) -> {
                // 处理第三方订单号
                executeByThirdOrderNo(tenantId, thirdOrderNo, orders, tenantFranchiseePayParamMap);
            });
        }
    }
    
    /**
     * 查询构建支付配置
     *
     * @param tenantFranchiseePayParamMap
     * @param tenantId
     * @param franchiseeIds
     * @author caobotao.cbt
     * @date 2024/8/27 11:06
     */
    private void queryBuildTenantFranchiseePayParamMap(Map<String, WechatPayParamsDetails> tenantFranchiseePayParamMap, Integer tenantId, Set<Long> franchiseeIds) {
        try {
            Set<Long> needQueryFranchiseeIds = new HashSet<>();
            franchiseeIds.forEach(franchiseeId -> {
                String payParamMapKey = getPayParamMapKey(tenantId, franchiseeId);
                if (!tenantFranchiseePayParamMap.containsKey(payParamMapKey)) {
                    needQueryFranchiseeIds.add(franchiseeId);
                }
            });
            
            if (CollectionUtils.isEmpty(needQueryFranchiseeIds)) {
                return;
            }
            
            List<WechatPayParamsDetails> wechatPayParamsDetailsList = wechatPayParamsBizService.queryListPreciseCacheByTenantIdAndFranchiseeIds(tenantId, needQueryFranchiseeIds,
                    Collections.singleton(ProfitSharingQueryDetailsEnum.PROFIT_SHARING_CONFIG_AND_RECEIVER_CONFIG));
            
            Map<Long, WechatPayParamsDetails> franchiseePayParamsMap = Optional.ofNullable(wechatPayParamsDetailsList).orElse(Collections.emptyList()).stream()
                    .collect(Collectors.toMap(WechatPayParamsDetails::getFranchiseeId, Function.identity(), (k1, k2) -> k1));
            
            needQueryFranchiseeIds.forEach(franchiseeId -> tenantFranchiseePayParamMap.put(getPayParamMapKey(tenantId, franchiseeId), franchiseePayParamsMap.get(franchiseeId)));
            
        } catch (Exception e) {
            log.info("ProfitSharingOrderTask.queryBuildTenantFranchiseePayParamMap Exception:", e);
        }
        
    }
    
    /**
     * 根据第三方单号处理
     *
     * @param tenantId
     * @param thirdOrderNo
     * @param orders
     * @param tenantFranchiseePayParamMap
     * @author caobotao.cbt
     * @date 2024/8/26 18:25
     */
    private void executeByThirdOrderNo(Integer tenantId, String thirdOrderNo, List<ProfitSharingTradeOrder> orders,
            Map<String, WechatPayParamsDetails> tenantFranchiseePayParamMap) {
        
        // 同一批次的支付订单,加盟商id一定一致
        Long franchiseeId = orders.stream().findFirst().get().getFranchiseeId();
        WechatPayParamsDetails wechatPayParamsDetails = tenantFranchiseePayParamMap.get(getPayParamMapKey(tenantId, franchiseeId));
        
        List<ProfitSharingTradeOrder> updateProfitSharingTradeOrderList = new ArrayList<>();
        
        List<ProfitSharingOrder> insertProfitSharingOrderList = new ArrayList<>();
    
        List<ProfitSharingOrderDetail> insertProfitSharingOrderDetailList = new ArrayList<>();
        
        //支付参数缺失
        if (Objects.isNull(wechatPayParamsDetails)) {
            orders.forEach(profitSharingTradeOrder -> {
                profitSharingTradeOrder.setProcessState(ProfitSharingTradeOderProcessStateEnum.FAIL.getCode());
                profitSharingTradeOrder.setRemark("支付配置不存在");
                updateProfitSharingTradeOrderList.add(profitSharingTradeOrder);
            });
            this.updateProfitSharingTradeOrder(updateProfitSharingTradeOrderList);
            return;
        }
        
        // 未配置分账接收方
        List<ProfitSharingReceiverConfig> receiverConfigs = wechatPayParamsDetails.getEnableProfitSharingReceiverConfigs();
        if (Objects.isNull(receiverConfigs)) {
            orders.forEach(profitSharingTradeOrder -> {
                profitSharingTradeOrder.setProcessState(ProfitSharingTradeOderProcessStateEnum.FAIL.getCode());
                profitSharingTradeOrder.setRemark("分账接收方未配置");
                updateProfitSharingTradeOrderList.add(profitSharingTradeOrder);
                ProfitSharingOrder profitSharingOrder = this.buildReceiverErrorProfitSharingOrder(thirdOrderNo, profitSharingTradeOrder, wechatPayParamsDetails);
                insertProfitSharingOrderList.add(profitSharingOrder);
                ProfitSharingOrderDetail profitSharingOrderDetail = this.buildReceiverErrorProfitSharingOrderDetail(profitSharingOrder,profitSharingTradeOrder);
                insertProfitSharingOrderDetailList.add(profitSharingOrderDetail);
            });
            // TODO: 2024/8/27 调用事务
//            tx.save(updateProfitSharingTradeOrderList,insertProfitSharingOrderList,insertProfitSharingOrderDetailList);
            return;
        }
        
        orders.forEach(order -> {
        
        
        });
        
    }
    
    private ProfitSharingOrderDetail buildReceiverErrorProfitSharingOrderDetail(ProfitSharingOrder profitSharingOrder, ProfitSharingTradeOrder profitSharingTradeOrder) {
        ProfitSharingOrderDetail profitSharingOrderDetail = ProfitSharingOrderDetail.builder()
        		.thirdTradeOrderNo(profitSharingOrder.getThirdTradeOrderNo())
        		.orderDetailNo(OrderIdUtil.generateBusinessOrderId(BusinessType.PROFIT_SHARING_ORDER_DETAIL, profitSharingTradeOrder.getUid()))
        		.profitSharingAmount(BigDecimal.ZERO)
                .scale(BigDecimal.ZERO)
        		.status(ProfitSharingOrderDetailStatusEnum.FAIL.getCode())
        		.failReason("分账接收方未配置")
        		.finishTime(System.currentTimeMillis())
        		.unfreezeStatus(ProfitSharingOrderDetailUnfreezeStatusEnum.PENDING.getCode())
        		.tenantId(profitSharingOrder.getTenantId())
        		.franchiseeId(profitSharingOrder.getFranchiseeId())
        		.createTime(profitSharingOrder.getCreateTime())
        		.updateTime(profitSharingOrder.getUpdateTime())
        		.outAccountType(profitSharingOrder.getOutAccountType())
        		.businessType(profitSharingOrder.getBusinessType())
        		.build();
        return profitSharingOrderDetail;
    }
    
    /**
     * 无接收方配置失败订单构建
     *
     * @param thirdOrderNo
     * @param profitSharingTradeOrder
     * @param wechatPayParamsDetails
     * @author caobotao.cbt
     * @date 2024/8/27 17:20
     */
    private ProfitSharingOrder buildReceiverErrorProfitSharingOrder(String thirdOrderNo, ProfitSharingTradeOrder profitSharingTradeOrder,
            WechatPayParamsDetails wechatPayParamsDetails) {
        long time = System.currentTimeMillis();
        ProfitSharingOrder profitSharingOrder = new ProfitSharingOrder();
        profitSharingOrder.setThirdTradeOrderNo(thirdOrderNo);
        profitSharingOrder.setOrderNo(OrderIdUtil.generateBusinessOrderId(BusinessType.PROFIT_SHARING_ORDER, profitSharingTradeOrder.getUid()));
        profitSharingOrder.setBusinessOrderNo(profitSharingTradeOrder.getOrderNo());
        profitSharingOrder.setAmount(profitSharingTradeOrder.getAmount());
        profitSharingOrder.setBusinessType(ProfitSharingBusinessTypeEnum.SYSTEM.getCode());
        profitSharingOrder.setStatus(ProfitSharingOrderStatusEnum.PROFIT_SHARING_COMPLETE.getCode());
        profitSharingOrder.setType(ProfitSharingOrderTypeEnum.PROFIT_SHARING.getCode());
        profitSharingOrder.setTenantId(profitSharingTradeOrder.getTenantId());
        profitSharingOrder.setFranchiseeId(profitSharingTradeOrder.getFranchiseeId());
        profitSharingOrder.setOutAccountType(wechatPayParamsDetails.getConfigType());
        profitSharingOrder.setThirdMerchantId(wechatPayParamsDetails.getWechatMerchantId());
        profitSharingOrder.setCreateTime(time);
        profitSharingOrder.setUpdateTime(time);
        return profitSharingOrder;
    }
    
    /**
     * 订单状态更新
     *
     * @param updateList
     * @author caobotao.cbt
     * @date 2024/8/27 16:22
     */
    private void updateProfitSharingTradeOrder(List<ProfitSharingTradeOrder> updateList) {
    
    }
    
    
    private String getPayParamMapKey(Integer tenantId, Long franchiseeId) {
        return tenantId + "_" + franchiseeId;
    }
}
