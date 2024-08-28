/**
 * Copyright(c) 2018 Sunyur.com, All Rights Reserved. Author: sunyur Create date: 2024/8/28
 */

package com.xiliulou.electricity.task.profitsharing;

import com.google.common.collect.Maps;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.bo.base.BasePayConfig;
import com.xiliulou.electricity.bo.wechat.WechatPayParamsDetails;
import com.xiliulou.electricity.entity.profitsharing.ProfitSharingOrder;
import com.xiliulou.electricity.entity.profitsharing.ProfitSharingOrderDetail;
import com.xiliulou.electricity.entity.profitsharing.ProfitSharingReceiverConfig;
import com.xiliulou.electricity.entity.profitsharing.ProfitSharingTradeMixedOrder;
import com.xiliulou.electricity.entity.profitsharing.ProfitSharingTradeOrder;
import com.xiliulou.electricity.enums.BusinessType;
import com.xiliulou.electricity.enums.profitsharing.ProfitSharingBusinessTypeEnum;
import com.xiliulou.electricity.enums.profitsharing.ProfitSharingOrderDetailStatusEnum;
import com.xiliulou.electricity.enums.profitsharing.ProfitSharingOrderDetailUnfreezeStatusEnum;
import com.xiliulou.electricity.enums.profitsharing.ProfitSharingOrderStatusEnum;
import com.xiliulou.electricity.enums.profitsharing.ProfitSharingOrderTypeEnum;
import com.xiliulou.electricity.enums.profitsharing.ProfitSharingQueryDetailsEnum;
import com.xiliulou.electricity.enums.profitsharing.ProfitSharingTradeMixedOrderStateEnum;
import com.xiliulou.electricity.enums.profitsharing.ProfitSharingTradeOderProcessStateEnum;
import com.xiliulou.electricity.enums.profitsharing.ProfitSharingTradeOderSupportRefundEnum;
import com.xiliulou.electricity.query.profitsharing.ProfitSharingTradeMixedOrderQueryModel;
import com.xiliulou.electricity.service.TenantService;
import com.xiliulou.electricity.service.WechatPayParamsBizService;
import com.xiliulou.electricity.service.profitsharing.ProfitSharingTradeMixedOrderService;
import com.xiliulou.electricity.service.profitsharing.ProfitSharingTradeOrderService;
import com.xiliulou.electricity.ttl.TtlTraceIdSupport;
import com.xiliulou.electricity.tx.profitsharing.ProfitSharingTradeOrderTxService;
import com.xiliulou.electricity.utils.OrderIdUtil;
import com.xiliulou.pay.base.enums.ChannelEnum;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
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
 * @date 2024/8/28 09:40
 */
public abstract class AbstractProfitSharingTradeOrderTask extends IJobHandler {
    
    @Resource
    private TenantService tenantService;
    
    @Resource
    private ProfitSharingTradeOrderService profitSharingTradeOrderService;
    
    @Resource
    private ProfitSharingTradeMixedOrderService profitSharingTradeMixedOrderService;
    
    @Resource
    private ProfitSharingTradeOrderTxService profitSharingTradeOrderTxService;
    
    public static final Integer SIZE = 200;
    
    
    
    @Override
    public ReturnT<String> execute(String param) throws Exception {
        TtlTraceIdSupport.set();
        try {
            ProfitSharingOrderTask.TaskParam taskParam = new ProfitSharingOrderTask.TaskParam();
            if (StringUtils.isNotBlank(param)) {
                taskParam = JsonUtil.fromJson(param, ProfitSharingOrderTask.TaskParam.class);
            }
            
            if (CollectionUtils.isNotEmpty(taskParam.getTenantIds())) {
                // 指定租户
                taskParam.getTenantIds().forEach(tid -> this.executeByTenantId(tid));
                return ReturnT.SUCCESS;
            }
            
            Integer startTenantId = 0;
            
            while (true) {
                List<Integer> tenantIds = tenantService.queryIdListByStartId(startTenantId, SIZE);
                
                if (CollectionUtils.isEmpty(tenantIds)) {
                    break;
                }
                startTenantId = tenantIds.get(tenantIds.size() - 1);
                tenantIds.forEach(tid -> this.executeByTenantId(tid));
            }
            
            return ReturnT.SUCCESS;
        } finally {
            TtlTraceIdSupport.clear();
        }
    }
    
    
    /**
     * 根据租户处理
     *
     * @param tenantId
     * @author caobotao.cbt
     * @date 2024/8/26 16:44
     */
    private void executeByTenantId(Integer tenantId) {
        Long startId = 0L;
        ProfitSharingTradeMixedOrderQueryModel queryModel = new ProfitSharingTradeMixedOrderQueryModel();
        queryModel.setState(ProfitSharingTradeMixedOrderStateEnum.INIT.getCode());
        queryModel.setTenantId(tenantId);
        queryModel.setSize(SIZE);
        queryModel.setChannel(this.getChannel());
        
        Map<String, BasePayConfig> tenantFranchiseePayParamMap = new HashMap<>();
        
        while (true) {
            queryModel.setStartId(startId);
            // 查询聚合分账交易订单
            List<ProfitSharingTradeMixedOrder> mixedOrders = profitSharingTradeMixedOrderService.queryListByParam(queryModel);
            if (CollectionUtils.isEmpty(mixedOrders)) {
                break;
            }
            startId = mixedOrders.get(mixedOrders.size() - 1).getId();
            
            Map<String, ProfitSharingTradeMixedOrder> thirdOrderNoMixedOrderMap = mixedOrders.stream()
                    .collect(Collectors.toMap(ProfitSharingTradeMixedOrder::getThirdOrderNo, Function.identity()));
            
            //根据第三方订单号与渠道查询
            List<ProfitSharingTradeOrder> tradeOrders = profitSharingTradeOrderService
                    .queryListByThirdOrderNosAndChannelAndProcessState(tenantId, ProfitSharingTradeOderProcessStateEnum.AWAIT.getCode(), this.getChannel(),
                            new ArrayList<>(thirdOrderNoMixedOrderMap.keySet()));
            
            Set<Long> franchiseeIds = new HashSet<>();
            Map<String, List<ProfitSharingTradeOrder>> thirdOrderNoMap = new HashMap<>();
            tradeOrders.forEach(profitSharingTradeOrder -> {
                thirdOrderNoMap.computeIfAbsent(profitSharingTradeOrder.getThirdOrderNo(), k -> new ArrayList<>()).add(profitSharingTradeOrder);
                franchiseeIds.add(profitSharingTradeOrder.getFranchiseeId());
            });
            
            tradeOrders.stream().map(ProfitSharingTradeOrder::getFranchiseeId).collect(Collectors.toSet());
            
            // 查询构建支付配置
            this.queryBuildTenantFranchiseePayParamMap(tenantFranchiseePayParamMap, tenantId, franchiseeIds);
            
            // 处理第三方订单号
            thirdOrderNoMixedOrderMap.forEach((thirdOrderNo, mixedOrder) -> {
                List<ProfitSharingTradeOrder> curTradeOrders = thirdOrderNoMap.get(thirdOrderNo);
                
                if (CollectionUtils.isEmpty(curTradeOrders)) {
                    // 将当前聚合订单状态更新为已处理（补偿）
                    mixedOrder.setState(ProfitSharingTradeMixedOrderStateEnum.COMPLETE.getCode());
                    mixedOrder.setUpdateTime(System.currentTimeMillis());
                    profitSharingTradeMixedOrderService.updateStatusById(mixedOrder);
                    return;
                }
                
                // 处理分账交易订单
                executeByThirdOrderNo(mixedOrder, curTradeOrders, tenantFranchiseePayParamMap);
            });
        }
    }
    
    
    /**
     * 根据第三方单号处理
     *
     * @param mixedOrder
     * @param orders
     * @param tenantFranchiseePayParamMap
     * @author caobotao.cbt
     * @date 2024/8/26 18:25
     */
    private void executeByThirdOrderNo(ProfitSharingTradeMixedOrder mixedOrder, List<ProfitSharingTradeOrder> orders, Map<String, BasePayConfig> tenantFranchiseePayParamMap) {
        
        Integer tenantId = mixedOrder.getTenantId();
        Long franchiseeId = mixedOrder.getFranchiseeId();
        
        BasePayConfig payConfig = tenantFranchiseePayParamMap.get(getPayParamMapKey(tenantId, franchiseeId));
        
        // 支付配置校验
        if (!this.checkDisposeByPayConfig(payConfig, mixedOrder, orders)) {
            return;
        }
        
        //分账接收方校验
        if (!this.checkDisposeByReceiver(payConfig, mixedOrder, orders)) {
            return;
        }
        
        Optional<ProfitSharingTradeOrder> supportRefund = orders.stream().filter(o -> ProfitSharingTradeOderSupportRefundEnum.YES.getCode().equals(o.getSupportRefund()))
                .findFirst();
        
        if (supportRefund.isPresent()) {
            // 支持退款
            ProfitSharingTradeOrder profitSharingTradeOrder = supportRefund.get();
            // 判定退款是否已超过可退期限
//            if ()
            
        }
        
        orders.forEach(order -> {
        
        });
        
    }
    
    /**
     * 根据分账接收方校验
     *
     * @param payConfig
     * @param mixedOrder
     * @param orders
     * @author caobotao.cbt
     * @date 2024/8/28 11:23
     */
    private boolean checkDisposeByReceiver(BasePayConfig payConfig, ProfitSharingTradeMixedOrder mixedOrder, List<ProfitSharingTradeOrder> orders) {
        List<ProfitSharingReceiverConfig> enableProfitSharingReceiverConfigs = payConfig.getEnableProfitSharingReceiverConfigs();
        if (CollectionUtils.isNotEmpty(enableProfitSharingReceiverConfigs)) {
            return true;
        }
        
        List<Long> tradeOrderIds = new ArrayList<>(orders.size());
        
        Map<ProfitSharingOrder, ProfitSharingOrderDetail> insertMap = Maps.newHashMap();
        
        orders.forEach(profitSharingTradeOrder -> {
            tradeOrderIds.add(profitSharingTradeOrder.getId());
            
            ProfitSharingOrder profitSharingOrder = this.buildReceiverErrorProfitSharingOrder(mixedOrder.getThirdOrderNo(), profitSharingTradeOrder, payConfig);
            ProfitSharingOrderDetail profitSharingOrderDetail = this.buildReceiverErrorProfitSharingOrderDetail(profitSharingOrder, profitSharingTradeOrder);
            insertMap.put(profitSharingOrder, profitSharingOrderDetail);
            
        });
        
        profitSharingTradeOrderTxService.save(tradeOrderIds, ProfitSharingTradeOderProcessStateEnum.FAIL.getCode(), "分账接收方未配置", insertMap);
        
        return false;
    }
    
    /**
     * 根据支付配置校验
     *
     * @param payConfig
     * @param mixedOrder
     * @param orders
     * @author caobotao.cbt
     * @date 2024/8/28 11:23
     */
    private boolean checkDisposeByPayConfig(BasePayConfig payConfig, ProfitSharingTradeMixedOrder mixedOrder, List<ProfitSharingTradeOrder> orders) {
        if (Objects.nonNull(payConfig)) {
            return true;
        }
        
        //支付参数缺失
        List<Long> tradeOrderIds = orders.stream().map(ProfitSharingTradeOrder::getId).collect(Collectors.toList());
        
        mixedOrder.setState(ProfitSharingTradeMixedOrderStateEnum.COMPLETE.getCode());
        
        profitSharingTradeOrderTxService.updateStatus(mixedOrder, tradeOrderIds, ProfitSharingTradeOderProcessStateEnum.FAIL.getCode(), "支付配置不存在");
        
        return false;
    }
    
    private ProfitSharingOrderDetail buildReceiverErrorProfitSharingOrderDetail(ProfitSharingOrder profitSharingOrder, ProfitSharingTradeOrder profitSharingTradeOrder) {
        ProfitSharingOrderDetail profitSharingOrderDetail = ProfitSharingOrderDetail.builder().thirdTradeOrderNo(profitSharingOrder.getThirdTradeOrderNo())
                .orderDetailNo(OrderIdUtil.generateBusinessOrderId(BusinessType.PROFIT_SHARING_ORDER_DETAIL, profitSharingTradeOrder.getUid())).profitSharingAmount(BigDecimal.ZERO)
                .scale(BigDecimal.ZERO).status(ProfitSharingOrderDetailStatusEnum.FAIL.getCode()).failReason("分账接收方未配置").finishTime(System.currentTimeMillis())
                .unfreezeStatus(ProfitSharingOrderDetailUnfreezeStatusEnum.PENDING.getCode()).tenantId(profitSharingOrder.getTenantId())
                .franchiseeId(profitSharingOrder.getFranchiseeId()).createTime(profitSharingOrder.getCreateTime()).updateTime(profitSharingOrder.getUpdateTime())
                .outAccountType(profitSharingOrder.getOutAccountType()).businessType(profitSharingOrder.getBusinessType()).build();
        return profitSharingOrderDetail;
    }
    
    /**
     * 无接收方配置失败订单构建
     *
     * @param thirdOrderNo
     * @param profitSharingTradeOrder
     * @param basePayConfig
     * @author caobotao.cbt
     * @date 2024/8/27 17:20
     */
    private ProfitSharingOrder buildReceiverErrorProfitSharingOrder(String thirdOrderNo, ProfitSharingTradeOrder profitSharingTradeOrder, BasePayConfig basePayConfig) {
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
        profitSharingOrder.setOutAccountType(basePayConfig.getConfigType());
        profitSharingOrder.setThirdMerchantId(basePayConfig.getThirdPartyMerchantId());
        profitSharingOrder.setCreateTime(time);
        profitSharingOrder.setUpdateTime(time);
        return profitSharingOrder;
    }
    
    
    protected String getPayParamMapKey(Integer tenantId, Long franchiseeId) {
        return tenantId + "_" + franchiseeId;
    }
    
    
    /**
     * 构建支付配置
     *
     * @param tenantFranchiseePayParamMap
     * @param tenantId
     * @param franchiseeIds
     * @author caobotao.cbt
     * @date 2024/8/28 10:38
     */
    protected abstract void queryBuildTenantFranchiseePayParamMap(Map<String, BasePayConfig> tenantFranchiseePayParamMap, Integer tenantId, Set<Long> franchiseeIds);
    
    
    /**
     * 获取渠道
     *
     * @author caobotao.cbt
     * @date 2024/8/28 09:42
     */
    protected abstract String getChannel();
    
    @Data
    public static class TaskParam {
        
        /**
         * 租户id集合
         */
        private List<Integer> tenantIds;
        
        
        private String traceId;
        
    }
}
