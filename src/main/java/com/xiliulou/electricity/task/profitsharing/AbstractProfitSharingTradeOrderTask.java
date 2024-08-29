/**
 * Copyright(c) 2018 Sunyur.com, All Rights Reserved. Author: sunyur Create date: 2024/8/28
 */

package com.xiliulou.electricity.task.profitsharing;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.bo.base.BasePayConfig;
import com.xiliulou.electricity.entity.BatteryMembercardRefundOrder;
import com.xiliulou.electricity.entity.profitsharing.ProfitSharingConfig;
import com.xiliulou.electricity.entity.profitsharing.ProfitSharingOrder;
import com.xiliulou.electricity.entity.profitsharing.ProfitSharingOrderDetail;
import com.xiliulou.electricity.entity.profitsharing.ProfitSharingReceiverConfig;
import com.xiliulou.electricity.entity.profitsharing.ProfitSharingStatistics;
import com.xiliulou.electricity.entity.profitsharing.ProfitSharingTradeMixedOrder;
import com.xiliulou.electricity.entity.profitsharing.ProfitSharingTradeOrder;
import com.xiliulou.electricity.enums.BusinessType;
import com.xiliulou.electricity.enums.profitsharing.ProfitSharingBusinessTypeEnum;
import com.xiliulou.electricity.enums.profitsharing.ProfitSharingOrderDetailStatusEnum;
import com.xiliulou.electricity.enums.profitsharing.ProfitSharingOrderDetailUnfreezeStatusEnum;
import com.xiliulou.electricity.enums.profitsharing.ProfitSharingOrderStatusEnum;
import com.xiliulou.electricity.enums.profitsharing.ProfitSharingOrderTypeEnum;
import com.xiliulou.electricity.enums.profitsharing.ProfitSharingStatisticsTypeEnum;
import com.xiliulou.electricity.enums.profitsharing.ProfitSharingTradeMixedOrderStateEnum;
import com.xiliulou.electricity.enums.profitsharing.ProfitSharingTradeOderProcessStateEnum;
import com.xiliulou.electricity.enums.profitsharing.ProfitSharingTradeOderSupportRefundEnum;
import com.xiliulou.electricity.exception.BizException;
import com.xiliulou.electricity.query.profitsharing.ProfitSharingTradeMixedOrderQueryModel;
import com.xiliulou.electricity.service.BatteryMembercardRefundOrderService;
import com.xiliulou.electricity.service.TenantService;
import com.xiliulou.electricity.service.profitsharing.ProfitSharingStatisticsService;
import com.xiliulou.electricity.service.profitsharing.ProfitSharingTradeMixedOrderService;
import com.xiliulou.electricity.service.profitsharing.ProfitSharingTradeOrderService;
import com.xiliulou.electricity.ttl.TtlTraceIdSupport;
import com.xiliulou.electricity.tx.profitsharing.ProfitSharingOrderTxService;
import com.xiliulou.electricity.tx.profitsharing.ProfitSharingTradeOrderTxService;
import com.xiliulou.electricity.utils.OrderIdUtil;
import com.xiliulou.pay.profitsharing.ProfitSharingServiceAdapter;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.xiliulou.electricity.constant.CacheConstant.PROFIT_SHARING_STATISTICS_LOCK_KEY;

/**
 * description:
 *
 * @author caobotao.cbt
 * @date 2024/8/28 09:40
 */
@Slf4j
public abstract class AbstractProfitSharingTradeOrderTask<T extends BasePayConfig> extends IJobHandler {
    
    @Resource
    private TenantService tenantService;
    
    @Resource
    private ProfitSharingTradeOrderService profitSharingTradeOrderService;
    
    @Resource
    private ProfitSharingTradeMixedOrderService profitSharingTradeMixedOrderService;
    
    @Resource
    private ProfitSharingTradeOrderTxService profitSharingTradeOrderTxService;
    
    @Resource
    private BatteryMembercardRefundOrderService batteryMembercardRefundOrderService;
    
    @Resource
    private RedisService redisService;
    
    @Resource
    protected ProfitSharingServiceAdapter profitSharingServiceAdapter;
    
    @Resource
    private ProfitSharingStatisticsService profitSharingStatisticsService;
    
    @Resource
    private ProfitSharingOrderTxService profitSharingOrderTxService;
    
    
    public static final Integer SIZE = 200;
    
    public static final Long ONE_DAY = 24L * 60L * 60L * 1000L;
    
    private static final List<Integer> IN_PROCESS_STATUS = Arrays
            .asList(BatteryMembercardRefundOrder.STATUS_INIT, BatteryMembercardRefundOrder.STATUS_REFUND, BatteryMembercardRefundOrder.STATUS_AUDIT);
    
    
    @Override
    public ReturnT<String> execute(String param) throws Exception {
        TtlTraceIdSupport.set();
        try {
            WechatProfitSharingTradeOrderTask.TaskParam taskParam = new WechatProfitSharingTradeOrderTask.TaskParam();
            if (StringUtils.isNotBlank(param)) {
                taskParam = JsonUtil.fromJson(param, WechatProfitSharingTradeOrderTask.TaskParam.class);
            }
            
            if (CollectionUtils.isNotEmpty(taskParam.getTenantIds())) {
                // 指定租户
                taskParam.getTenantIds().forEach(tid -> this.executeByTenantId(tid));
                return ReturnT.SUCCESS;
            }
            
            Integer startTenantId = 0;
            
            while (true) {
                // 查询租户
                List<Integer> tenantIds = tenantService.queryIdListByStartId(startTenantId, SIZE);
                
                if (CollectionUtils.isEmpty(tenantIds)) {
                    break;
                }
                startTenantId = tenantIds.get(tenantIds.size() - 1);
                // 处理租户分账
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
        
        Map<String, T> tenantFranchiseePayParamMap = new HashMap<>();
        
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
            
            //查询代发起分账的明细
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
    private void executeByThirdOrderNo(ProfitSharingTradeMixedOrder mixedOrder, List<ProfitSharingTradeOrder> orders, Map<String, T> tenantFranchiseePayParamMap) {
        
        T payConfig = tenantFranchiseePayParamMap.get(getPayParamMapKey(mixedOrder.getTenantId(), mixedOrder.getFranchiseeId()));
        
        // 支付配置校验
        if (!this.checkDisposeByPayConfig(payConfig, mixedOrder, orders)) {
            // 支付配置不存在
            return;
        }
        
        //分账接收方校验
        if (!this.checkDisposeByReceiver(payConfig, mixedOrder, orders)) {
            return;
        }
        
        // 支持退款的订单
        List<ProfitSharingTradeOrder> supportRefundOrderList = new ArrayList<>();
        // 不支持退款的订单
        List<ProfitSharingTradeOrder> notSupportRefundOrderList = new ArrayList<>();
        
        orders.forEach(profitSharingTradeOrder -> {
            if (ProfitSharingTradeOderSupportRefundEnum.YES.getCode().equals(profitSharingTradeOrder.getSupportRefund())) {
                supportRefundOrderList.add(profitSharingTradeOrder);
            } else {
                notSupportRefundOrderList.add(profitSharingTradeOrder);
            }
        });
        
        if (CollectionUtils.isNotEmpty(supportRefundOrderList)) {
            // 当前订单组存在支持退款的订单
            this.disposeByRefund(payConfig, mixedOrder, notSupportRefundOrderList, supportRefundOrderList);
            return;
        }
        
        
    }
    
    /**
     * 退款处理
     *
     * @author caobotao.cbt
     * @date 2024/8/28 14:12
     */
    private void disposeByRefund(T payConfig, ProfitSharingTradeMixedOrder mixedOrder, List<ProfitSharingTradeOrder> notSupportRefundOrderList,
            List<ProfitSharingTradeOrder> supportRefundOrderList) {
        
        // 判定是否到可退期限，获取当前可退订单期限最大的订单可退时间
        Long maxRefundTime = supportRefundOrderList.stream().map(v -> v.getPayTime() + ONE_DAY).max(Long::compare).orElse(0L);
        if (System.currentTimeMillis() < maxRefundTime) {
            // 当前的订单组只要有一条在可退期限内，则全部延期处理
            return;
        }
        
        // 支持退款的订单按照订单分类分组
        Map<Integer, List<ProfitSharingTradeOrder>> orderType = supportRefundOrderList.stream().collect(Collectors.groupingBy(ProfitSharingTradeOrder::getOrderType));
        
        // 允许分账的订单
        List<ProfitSharingTradeOrder> allowProfitSharingTradeOrders = new ArrayList<>(notSupportRefundOrderList);
        // 退款成功需要更新状态的订单
        List<Long> lapsedTradeOrderIds = new ArrayList<>();
        
        // 目前只有租金退款
        List<ProfitSharingTradeOrder> batteryPackageOrderList = orderType.get(ProfitSharingBusinessTypeEnum.BATTERY_PACKAGE.getCode());
        
        if (CollectionUtils.isNotEmpty(batteryPackageOrderList)) {
            
            // TODO: 2024/8/28 暂时只有租金退款，只会循环一次
            for (ProfitSharingTradeOrder profitSharingTradeOrder : batteryPackageOrderList) {
                BatteryMembercardRefundOrder refundOrder = batteryMembercardRefundOrderService.selectLatestByMembercardOrderNo(profitSharingTradeOrder.getOrderNo());
                
                if (Objects.isNull(refundOrder)) {
                    // 未发生退款,允许分账（前置已经判断过退款期限）
                    allowProfitSharingTradeOrders.add(profitSharingTradeOrder);
                    continue;
                }
                
                // 有退款
                if (BatteryMembercardRefundOrder.STATUS_SUCCESS.equals(refundOrder.getStatus())) {
                    // 退款成功,退款分账单更新状态为已失效
                    profitSharingTradeOrder.setProcessState(ProfitSharingTradeOderProcessStateEnum.LAPSED.getCode());
                    profitSharingTradeOrder.setRemark("已退款");
                    lapsedTradeOrderIds.add(profitSharingTradeOrder.getId());
                } else if (this.IN_PROCESS_STATUS.contains(refundOrder.getStatus())) {
                    // 当前订单在退款中间态，整个订单组延期处理
                    log.info("AbstractProfitSharingTradeOrderTask.disposeByRefund refundOrder:{},status:{}", refundOrder.getRefundOrderNo(), refundOrder.getStatus());
                    return;
                } else {
                    //其他状态
                    allowProfitSharingTradeOrders.add(profitSharingTradeOrder);
                }
            }
        }
        
        if (CollectionUtils.isEmpty(allowProfitSharingTradeOrders)) {
            // 将退款更新为已失效
            mixedOrder.setState(ProfitSharingTradeMixedOrderStateEnum.COMPLETE.getCode());
            profitSharingTradeOrderTxService.updateStatus(mixedOrder, null, lapsedTradeOrderIds, "已退款");
            return;
        }
        
        // 执行分账
        this.executeProfitSharing(payConfig, allowProfitSharingTradeOrders);
        
        mixedOrder.setState(ProfitSharingTradeMixedOrderStateEnum.COMPLETE.getCode());
        List<Long> successIds = allowProfitSharingTradeOrders.stream().map(ProfitSharingTradeOrder::getId).collect(Collectors.toList());
        profitSharingTradeOrderTxService.updateStatus(mixedOrder, successIds, lapsedTradeOrderIds, "已退款");
    }
    
    /**
     * @param payConfig
     * @param allowProfitSharingTradeOrders
     * @author caobotao.cbt
     * @date 2024/8/29 11:19
     */
    private List<ProfitSharingCheckModel> executeProfitSharing(T payConfig, List<ProfitSharingTradeOrder> allowProfitSharingTradeOrders) {
        
        // 分布式锁
        String lockKey = String.format(PROFIT_SHARING_STATISTICS_LOCK_KEY, payConfig.getTenantId(), payConfig.getFranchiseeId());
        String clientId = UUID.randomUUID().toString();
        Boolean lock = redisService.tryLock(lockKey, clientId, 5L, 3, 1000L);
        if (!lock) {
            log.warn("AbstractProfitSharingTradeOrderTask.executeProfitSharing WARN! ");
            throw new BizException("lock get error!");
        }
        
        try {
            // 校验分账信息
            List<ProfitSharingCheckModel> checkModels = this.checkProfitSharing(payConfig, allowProfitSharingTradeOrders);
            
            List<ProfitSharingCheckModel> successList = checkModels.stream().filter(ProfitSharingCheckModel::getIsSuccess).collect(Collectors.toList());
            
            if (CollectionUtils.isEmpty(successList)) {
                // 无成功的，全部失败，则需要解冻
                checkModels.forEach(checkModel -> {
                    ProfitSharingTradeOrder profitSharingTradeOrder = checkModel.getProfitSharingTradeOrder();
                    ProfitSharingOrder profitSharingOrder = this.buildErrorProfitSharingOrder(payConfig, profitSharingTradeOrder, ProfitSharingBusinessTypeEnum.SYSTEM.getCode());
                    checkModel.setProfitSharingOrder(profitSharingOrder);
                    checkModel.getProfitSharingDetailsCheckModels().forEach(detail -> {
                        ProfitSharingOrderDetail profitSharingOrderDetail = this
                                .buildErrorProfitSharingOrderDetail(payConfig, profitSharingTradeOrder, detail.getProfitSharingReceiverConfig(), detail.getErrorMsg(),
                                        ProfitSharingBusinessTypeEnum.SYSTEM.getCode(), ProfitSharingOrderDetailUnfreezeStatusEnum.PENDING.getCode());
                        detail.setProfitSharingOrderDetail(profitSharingOrderDetail);
                    });
                });
                profitSharingOrderTxService.insert(checkModels);
                return checkModels;
            }
            
            // 生成成功订单
            successList.forEach(profitSharingCheckModel -> {
                ProfitSharingTradeOrder profitSharingTradeOrder = profitSharingCheckModel.getProfitSharingTradeOrder();
                ProfitSharingOrder sharingOrder = this.initProfitSharingOrder(payConfig, profitSharingTradeOrder);
                profitSharingCheckModel.setProfitSharingOrder(sharingOrder);
                profitSharingCheckModel.getProfitSharingDetailsCheckModels().forEach(details -> {
                    ProfitSharingOrderDetail orderDetail = this
                            .initProfitSharingDetailsCheckModel(payConfig, profitSharingTradeOrder, details.getProfitSharingReceiverConfig(), details.getProfitSharingAmount());
                    details.setProfitSharingOrderDetail(orderDetail);
                });
            });
            
            profitSharingOrderTxService.insert(successList);
            
            this.order(payConfig, successList);
            
            this.orderPostProcessing(successList);
            
            return checkModels;
            
        } finally {
            redisService.releaseLockLua(lockKey, clientId);
        }
    }
    
    /**
     * name: <br/> description:
     *
     * @param successList
     * @author caobotao.cbt
     * @date 2024/8/29 14:57
     */
    protected void orderPostProcessing(List<ProfitSharingCheckModel> successList) {
        profitSharingOrderTxService.update(successList);
    }
    
    private ProfitSharingOrderDetail initProfitSharingDetailsCheckModel(T payConfig, ProfitSharingTradeOrder profitSharingTradeOrder, ProfitSharingReceiverConfig receiverConfig,
            BigDecimal profitSharingAmount) {
        long timeMillis = System.currentTimeMillis();
        ProfitSharingOrderDetail detail = new ProfitSharingOrderDetail();
        detail.setThirdTradeOrderNo(profitSharingTradeOrder.getThirdOrderNo());
        detail.setOrderDetailNo(OrderIdUtil.generateBusinessId(BusinessType.PROFIT_SHARING_ORDER_DETAIL, profitSharingTradeOrder.getUid()));
        detail.setProfitSharingReceiveAccount(receiverConfig.getAccount());
        detail.setScale(receiverConfig.getScale());
        detail.setProfitSharingAmount(profitSharingAmount);
        detail.setStatus(ProfitSharingOrderDetailStatusEnum.ACCEPT.getCode());
        detail.setUnfreezeStatus(ProfitSharingOrderDetailUnfreezeStatusEnum.DISPENSE_WITH.getCode());
        detail.setTenantId(profitSharingTradeOrder.getTenantId());
        detail.setFranchiseeId(profitSharingTradeOrder.getFranchiseeId());
        detail.setBusinessType(profitSharingTradeOrder.getOrderType());
        detail.setCreateTime(timeMillis);
        detail.setUpdateTime(timeMillis);
        detail.setOutAccountType(payConfig.getConfigType());
        return detail;
    }
    
    private ProfitSharingOrder initProfitSharingOrder(T payConfig, ProfitSharingTradeOrder profitSharingTradeOrder) {
        ProfitSharingOrder profitSharingOrder = new ProfitSharingOrder();
        
        profitSharingOrder.setThirdTradeOrderNo(profitSharingTradeOrder.getThirdOrderNo());
        profitSharingOrder.setOrderNo(OrderIdUtil.generateBusinessOrderId(BusinessType.PROFIT_SHARING_ORDER, profitSharingTradeOrder.getUid()));
        profitSharingOrder.setBusinessOrderNo(profitSharingTradeOrder.getOrderNo());
        profitSharingOrder.setAmount(profitSharingTradeOrder.getAmount());
        profitSharingOrder.setBusinessType(profitSharingTradeOrder.getOrderType());
        profitSharingOrder.setStatus(ProfitSharingOrderStatusEnum.PROFIT_SHARING_ACCEPT.getCode());
        profitSharingOrder.setType(ProfitSharingOrderTypeEnum.PROFIT_SHARING.getCode());
        profitSharingOrder.setTenantId(profitSharingTradeOrder.getTenantId());
        profitSharingOrder.setFranchiseeId(profitSharingTradeOrder.getFranchiseeId());
        profitSharingOrder.setOutAccountType(payConfig.getConfigType());
        profitSharingOrder.setThirdMerchantId(payConfig.getThirdPartyMerchantId());
        long timeMillis = System.currentTimeMillis();
        profitSharingOrder.setCreateTime(timeMillis);
        profitSharingOrder.setUpdateTime(timeMillis);
        return profitSharingOrder;
    }
    
    
    /**
     * 执行分账前校验
     *
     * @author caobotao.cbt
     * @date 2024/8/28 15:46
     */
    private List<ProfitSharingCheckModel> checkProfitSharing(T payConfig, List<ProfitSharingTradeOrder> profitSharingTradeOrderList) {
        
        // 当前分账月份
        String currentMonth = profitSharingStatisticsService.getCurrentMonth();
        
        
        ProfitSharingConfig profitSharingConfig = payConfig.getEnableProfitSharingConfig();
        
        List<ProfitSharingReceiverConfig> receiverConfigs = payConfig.getEnableProfitSharingReceiverConfigs();
        
        ProfitSharingStatistics profitSharingStatistics = this.getAndInitProfitSharingStatistics(payConfig.getTenantId(), payConfig.getFranchiseeId(), currentMonth);
        
        // 月分账总额
        BigDecimal totalAmount = profitSharingStatistics.getTotalAmount();
        
        // 月限额
        BigDecimal amountLimit = profitSharingConfig.getAmountLimit();
        
        List<ProfitSharingCheckModel> checkModels = Lists.newArrayList();
        
        for (ProfitSharingTradeOrder profitSharingTradeOrder : profitSharingTradeOrderList) {
            
            ProfitSharingCheckModel profitSharingCheckModel = new ProfitSharingCheckModel(profitSharingTradeOrder);
            checkModels.add(profitSharingCheckModel);
            
            // 当前交易金额
            BigDecimal amount = profitSharingTradeOrder.getAmount();
            
            // 单笔最大限额
            BigDecimal maxProfitSharingAmount = amount.multiply(profitSharingConfig.getScaleLimit());
            
            //计算分账总额
            BigDecimal profitSharingTotalAmount = BigDecimal.ZERO;
            Map<Long, BigDecimal> profitSharingAmountMap = Maps.newHashMapWithExpectedSize(receiverConfigs.size());
            for (ProfitSharingReceiverConfig receiverConfig : receiverConfigs) {
                BigDecimal profitSharingAmount = amount.multiply(receiverConfig.getScale());
                profitSharingAmountMap.put(receiverConfig.getId(), profitSharingAmount);
                profitSharingTotalAmount = profitSharingTotalAmount.add(profitSharingAmount);
            }
            
            if (profitSharingTotalAmount.compareTo(maxProfitSharingAmount) > 0) {
                
                //分账总额>单笔最大限额
                this.buildErrorProfitSharingCheckModel(receiverConfigs, profitSharingCheckModel, "分账余额不足", BigDecimal.ZERO);
                continue;
            }
            
            totalAmount = totalAmount.add(profitSharingTotalAmount);
            
            if (totalAmount.compareTo(amountLimit) > 0) {
                // 累计限额>最大限额
                this.buildErrorProfitSharingCheckModel(receiverConfigs, profitSharingCheckModel, "分账余额不足", BigDecimal.ZERO);
                continue;
            }
            
            for (ProfitSharingReceiverConfig receiverConfig : receiverConfigs) {
                profitSharingCheckModel.addProfitSharingDetails(receiverConfig, null, profitSharingAmountMap.get(receiverConfig.getId()));
            }
            
        }
        
        return checkModels;
    }
    
    
    /**
     * 构建错误
     *
     * @author caobotao.cbt
     * @date 2024/8/29 10:28
     */
    private void buildErrorProfitSharingCheckModel(List<ProfitSharingReceiverConfig> receiverConfigs, ProfitSharingCheckModel profitSharingCheckModel, String remark,
            BigDecimal profitSharingAmount) {
        profitSharingCheckModel.setIsSuccess(false);
        receiverConfigs.forEach(receiverConfig -> profitSharingCheckModel.addProfitSharingDetails(receiverConfig, remark, profitSharingAmount));
        
    }
    
    
    /**
     * 构建失败明细订单
     *
     * @param payConfig
     * @param profitSharingOrder
     * @param receiverConfig
     * @param failReason
     * @author caobotao.cbt
     * @date 2024/8/29 09:11
     */
    private ProfitSharingOrderDetail buildErrorProfitSharingOrderDetail(T payConfig, ProfitSharingTradeOrder profitSharingOrder, ProfitSharingReceiverConfig receiverConfig,
            String failReason, Integer businessType, Integer unfreezeStatus) {
        long time = System.currentTimeMillis();
        ProfitSharingOrderDetail profitSharingOrderDetail = new ProfitSharingOrderDetail();
        profitSharingOrderDetail.setThirdTradeOrderNo(profitSharingOrder.getThirdOrderNo());
        profitSharingOrderDetail.setOrderDetailNo(OrderIdUtil.generateBusinessOrderId(BusinessType.PROFIT_SHARING_ORDER_DETAIL, profitSharingOrder.getUid()));
        profitSharingOrderDetail.setProfitSharingReceiveAccount(receiverConfig.getAccount());
        profitSharingOrderDetail.setScale(receiverConfig.getScale());
        profitSharingOrderDetail.setProfitSharingAmount(BigDecimal.ZERO);
        profitSharingOrderDetail.setStatus(ProfitSharingOrderDetailStatusEnum.FAIL.getCode());
        profitSharingOrderDetail.setFailReason(failReason);
        profitSharingOrderDetail.setFinishTime(time);
        profitSharingOrderDetail.setUnfreezeStatus(unfreezeStatus);
        profitSharingOrderDetail.setTenantId(profitSharingOrder.getTenantId());
        profitSharingOrderDetail.setFranchiseeId(profitSharingOrder.getFranchiseeId());
        profitSharingOrderDetail.setCreateTime(time);
        profitSharingOrderDetail.setUpdateTime(time);
        profitSharingOrderDetail.setOutAccountType(payConfig.getConfigType());
        profitSharingOrderDetail.setBusinessType(businessType);
        return profitSharingOrderDetail;
    }
    
    /**
     * 构建分账订单
     *
     * @param payConfig
     * @param profitSharingTradeOrder
     * @author caobotao.cbt
     * @date 2024/8/28 18:10
     */
    private ProfitSharingOrder buildErrorProfitSharingOrder(T payConfig, ProfitSharingTradeOrder profitSharingTradeOrder, Integer businessType) {
        ProfitSharingOrder profitSharingOrder = new ProfitSharingOrder();
        profitSharingOrder.setThirdTradeOrderNo(profitSharingTradeOrder.getThirdOrderNo());
        profitSharingOrder.setOrderNo(OrderIdUtil.generateBusinessOrderId(BusinessType.PROFIT_SHARING_ORDER, profitSharingTradeOrder.getUid()));
        profitSharingOrder.setBusinessOrderNo(profitSharingTradeOrder.getOrderNo());
        profitSharingOrder.setAmount(profitSharingTradeOrder.getAmount());
        profitSharingOrder.setBusinessType(businessType);
        profitSharingOrder.setStatus(ProfitSharingOrderStatusEnum.PROFIT_SHARING_COMPLETE.getCode());
        profitSharingOrder.setType(ProfitSharingOrderTypeEnum.PROFIT_SHARING.getCode());
        profitSharingOrder.setTenantId(profitSharingTradeOrder.getTenantId());
        profitSharingOrder.setFranchiseeId(profitSharingTradeOrder.getFranchiseeId());
        profitSharingOrder.setOutAccountType(payConfig.getConfigType());
        profitSharingOrder.setThirdMerchantId(profitSharingTradeOrder.getThirdMerchantId());
        profitSharingOrder.setCreateTime(profitSharingTradeOrder.getCreateTime());
        profitSharingOrder.setUpdateTime(profitSharingTradeOrder.getUpdateTime());
        return profitSharingOrder;
    }
    
    
    /**
     * 获取当月分账总额
     *
     * @param tenantId
     * @param franchiseeId
     * @param currentMonth
     * @author caobotao.cbt
     * @date 2024/8/29 09:00
     */
    private ProfitSharingStatistics getAndInitProfitSharingStatistics(Integer tenantId, Long franchiseeId, String currentMonth) {
        ProfitSharingStatistics profitSharingStatistics = profitSharingStatisticsService.queryByTenantIdAndFranchiseeIdAndStatisticsTime(tenantId, franchiseeId, currentMonth);
        if (Objects.isNull(profitSharingStatistics)) {
            profitSharingStatistics = this.buildProfitSharingStatistics(tenantId, franchiseeId, currentMonth);
            profitSharingStatisticsService.insert(profitSharingStatistics);
        }
        return profitSharingStatistics;
    }
    
    /**
     * 构建统计
     *
     * @author caobotao.cbt
     * @date 2024/8/28 17:35
     */
    private ProfitSharingStatistics buildProfitSharingStatistics(Integer tenantId, Long franchiseeId, String currentMonth) {
        ProfitSharingStatistics profitSharingStatistics = new ProfitSharingStatistics();
        long time = System.currentTimeMillis();
        profitSharingStatistics.setTenantId(tenantId);
        profitSharingStatistics.setFranchiseeId(franchiseeId);
        profitSharingStatistics.setStatisticsType(ProfitSharingStatisticsTypeEnum.MONTH.getCode());
        profitSharingStatistics.setStatisticsTime(currentMonth);
        profitSharingStatistics.setTotalAmount(BigDecimal.ZERO);
        profitSharingStatistics.setCreateTime(time);
        profitSharingStatistics.setUpdateTime(time);
        return profitSharingStatistics;
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
    private boolean checkDisposeByReceiver(T payConfig, ProfitSharingTradeMixedOrder mixedOrder, List<ProfitSharingTradeOrder> orders) {
        
        // 获取可用的分账接收方数据
        List<ProfitSharingReceiverConfig> enableProfitSharingReceiverConfigs = payConfig.getEnableProfitSharingReceiverConfigs();
        if (CollectionUtils.isNotEmpty(enableProfitSharingReceiverConfigs)) {
            return true;
        }
        
        // 无可用分账接收方,生成分账订单和分账订单明细（用来解冻）
        List<Long> tradeOrderIds = new ArrayList<>(orders.size());
        
        Map<ProfitSharingOrder, ProfitSharingOrderDetail> insertMap = Maps.newHashMap();
        
        orders.forEach(profitSharingTradeOrder -> {
            tradeOrderIds.add(profitSharingTradeOrder.getId());
            
            ProfitSharingOrder profitSharingOrder = this.buildReceiverErrorProfitSharingOrder(mixedOrder.getThirdOrderNo(), profitSharingTradeOrder, payConfig);
            ProfitSharingOrderDetail profitSharingOrderDetail = this.buildReceiverErrorProfitSharingOrderDetail(profitSharingOrder, profitSharingTradeOrder);
            insertMap.put(profitSharingOrder, profitSharingOrderDetail);
            
        });
        
        profitSharingTradeOrderTxService.insert(tradeOrderIds, ProfitSharingTradeOderProcessStateEnum.SUCCESS.getCode(), "分账接收方未配置", insertMap);
        
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
    private boolean checkDisposeByPayConfig(T payConfig, ProfitSharingTradeMixedOrder mixedOrder, List<ProfitSharingTradeOrder> orders) {
        if (Objects.nonNull(payConfig)) {
            return true;
        }
        
        //支付参数缺失
        List<Long> tradeOrderIds = orders.stream().map(ProfitSharingTradeOrder::getId).collect(Collectors.toList());
        
        mixedOrder.setState(ProfitSharingTradeMixedOrderStateEnum.COMPLETE.getCode());
        
        profitSharingTradeOrderTxService.updateStatus(mixedOrder, null, tradeOrderIds, "支付配置不存在");
        
        return false;
    }
    
    /**
     * 无分账接收方明细构建
     *
     * @param profitSharingOrder
     * @param profitSharingTradeOrder
     * @author caobotao.cbt
     * @date 2024/8/29 16:07
     */
    private ProfitSharingOrderDetail buildReceiverErrorProfitSharingOrderDetail(ProfitSharingOrder profitSharingOrder, ProfitSharingTradeOrder profitSharingTradeOrder) {
        long time = System.currentTimeMillis();
        ProfitSharingOrderDetail profitSharingOrderDetail = new ProfitSharingOrderDetail();
        
        profitSharingOrderDetail.setThirdTradeOrderNo(profitSharingOrder.getThirdTradeOrderNo());
        profitSharingOrderDetail.setOrderDetailNo(OrderIdUtil.generateBusinessOrderId(BusinessType.PROFIT_SHARING_ORDER_DETAIL, profitSharingTradeOrder.getUid()));
        profitSharingOrderDetail.setScale(BigDecimal.ZERO);
        profitSharingOrderDetail.setProfitSharingAmount(BigDecimal.ZERO);
        profitSharingOrderDetail.setStatus(ProfitSharingOrderDetailStatusEnum.FAIL.getCode());
        profitSharingOrderDetail.setFailReason("分账接收方未配置");
        profitSharingOrderDetail.setFinishTime(time);
        profitSharingOrderDetail.setUnfreezeStatus(ProfitSharingOrderDetailUnfreezeStatusEnum.PENDING.getCode());
        profitSharingOrderDetail.setTenantId(profitSharingOrder.getTenantId());
        profitSharingOrderDetail.setFranchiseeId(profitSharingOrder.getFranchiseeId());
        profitSharingOrderDetail.setCreateTime(time);
        profitSharingOrderDetail.setUpdateTime(time);
        profitSharingOrderDetail.setOutAccountType(profitSharingOrder.getOutAccountType());
        profitSharingOrderDetail.setBusinessType(ProfitSharingBusinessTypeEnum.SYSTEM.getCode());
        
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
    private ProfitSharingOrder buildReceiverErrorProfitSharingOrder(String thirdOrderNo, ProfitSharingTradeOrder profitSharingTradeOrder, T basePayConfig) {
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
    protected abstract void queryBuildTenantFranchiseePayParamMap(Map<String, T> tenantFranchiseePayParamMap, Integer tenantId, Set<Long> franchiseeIds);
    
    
    /**
     * 发起订单
     *
     * @param payConfig
     * @param profitSharingModels
     * @author caobotao.cbt
     * @date 2024/8/28 19:28
     */
    protected abstract void order(T payConfig, List<ProfitSharingCheckModel> profitSharingModels);
    
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
    
    
    @Data
    public static class ProfitSharingCheckModel {
        
        public ProfitSharingCheckModel(ProfitSharingTradeOrder profitSharingTradeOrder) {
            this.profitSharingTradeOrder = profitSharingTradeOrder;
        }
        
        /**
         * 分账交易单
         */
        private ProfitSharingTradeOrder profitSharingTradeOrder;
        
        /**
         * 分账订单
         */
        private ProfitSharingOrder profitSharingOrder;
        
        
        /**
         * 分账明细校验
         */
        private List<ProfitSharingDetailsCheckModel> profitSharingDetailsCheckModels = new ArrayList<>();
        
        
        /**
         * 是否可发起分账
         */
        private Boolean isSuccess = true;
        
        
        public void addProfitSharingDetails(ProfitSharingReceiverConfig receiverConfig, String errMsg, BigDecimal profitSharingAmount) {
            this.profitSharingDetailsCheckModels.add(new ProfitSharingDetailsCheckModel(receiverConfig, errMsg, profitSharingAmount));
        }
    }
    
    
    @Data
    public static class ProfitSharingDetailsCheckModel {
        
        
        public ProfitSharingDetailsCheckModel(ProfitSharingReceiverConfig profitSharingReceiverConfig, String errorMsg, BigDecimal profitSharingAmount) {
            this.profitSharingReceiverConfig = profitSharingReceiverConfig;
            this.errorMsg = errorMsg;
            this.profitSharingAmount = profitSharingAmount;
        }
        
        /**
         * 接收方配置
         */
        private ProfitSharingReceiverConfig profitSharingReceiverConfig;
        
        /**
         * 分账金额
         */
        private BigDecimal profitSharingAmount;
        
        /**
         * 失败原因
         */
        private String errorMsg;
        
        
        /**
         * 分账订单明细
         */
        private ProfitSharingOrderDetail profitSharingOrderDetail;
    }
    
    
}
