package com.xiliulou.electricity.service.impl.profitsharing;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.electricity.bo.profitsharing.ProfitSharingOrderTypeUnfreezeBO;
import com.xiliulou.electricity.bo.wechat.WechatPayParamsDetails;
import com.xiliulou.electricity.constant.DateFormatConstant;
import com.xiliulou.electricity.converter.ElectricityPayParamsConverter;
import com.xiliulou.electricity.entity.profitsharing.ProfitSharingOrder;
import com.xiliulou.electricity.entity.profitsharing.ProfitSharingOrderDetail;
import com.xiliulou.electricity.entity.profitsharing.ProfitSharingStatistics;
import com.xiliulou.electricity.entity.profitsharing.ProfitSharingTradeMixedOrder;
import com.xiliulou.electricity.enums.profitsharing.ProfitSharingBusinessTypeEnum;
import com.xiliulou.electricity.enums.profitsharing.ProfitSharingOrderDetailStatusEnum;
import com.xiliulou.electricity.enums.profitsharing.ProfitSharingOrderDetailUnfreezeStatusEnum;
import com.xiliulou.electricity.service.TenantService;
import com.xiliulou.electricity.service.WechatPayParamsBizService;
import com.xiliulou.electricity.service.profitsharing.ProfitSharingOrderBizService;
import com.xiliulou.electricity.service.profitsharing.ProfitSharingOrderDetailService;
import com.xiliulou.electricity.service.profitsharing.ProfitSharingOrderService;
import com.xiliulou.electricity.service.profitsharing.ProfitSharingStatisticsService;
import com.xiliulou.electricity.service.profitsharing.ProfitSharingTradeMixedOrderService;
import com.xiliulou.electricity.utils.DateUtils;
import com.xiliulou.pay.base.exception.ProfitSharingException;
import com.xiliulou.pay.profitsharing.ProfitSharingServiceAdapter;
import com.xiliulou.pay.profitsharing.request.wechat.WechatProfitSharingQueryOrderRequest;
import com.xiliulou.pay.profitsharing.response.wechat.ReceiverResp;
import com.xiliulou.pay.profitsharing.response.wechat.WechatProfitSharingQueryOrderResp;
import com.xiliulou.pay.weixinv3.exception.WechatPayException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.xiliulou.electricity.constant.CacheConstant.PROFIT_SHARING_STATISTICS_LOCK_KEY;

/**
 * @author maxiaodong
 * @date 2024/8/28 14:29
 * @desc
 */
@Service
@Slf4j
public class ProfitSharingOrderBizServiceImpl implements ProfitSharingOrderBizService {
    @Resource
    private TenantService tenantService;
    
    @Resource
    private ProfitSharingOrderService profitSharingOrderService;
    
    @Resource
    private ProfitSharingOrderDetailService profitSharingOrderDetailService;
    
    @Resource
    private ProfitSharingTradeMixedOrderService profitSharingTradeMixedOrderService;
    
    @Resource
    private WechatPayParamsBizService wechatPayParamsBizService;
    
    @Resource
    private ProfitSharingServiceAdapter profitSharingServiceAdapter;
    
    @Resource
    private ProfitSharingStatisticsService profitSharingStatisticsService;
    
    @Resource
    private RedisService redisService;
    
    private DateTimeFormatter formatter = new DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .append(DateTimeFormatter.ISO_LOCAL_DATE)
            .appendLiteral('T')
            .append(DateTimeFormatter.ISO_LOCAL_TIME)
            .appendLiteral("+08:00").toFormatter();
   
    
    @Override
    public void doUnfreezeTask() {
        Integer startTenantId = 0;
        Integer size = 200;
        
        while (true) {
            List<Integer> tenantIds = tenantService.queryIdListByStartId(startTenantId, size);
            if (CollectionUtils.isEmpty(tenantIds)) {
                break;
            }
            
            dealWithTenantIds(tenantIds);
            
            startTenantId = tenantIds.get(tenantIds.size() - 1);
        }
    }
    
    /**
     * 解冻账单查询任务
     */
    @Override
    public void doUnfreezeQueryTask() {
        Integer startTenantId = 0;
        Integer size = 200;
    
//        while (true) {
//            List<Integer> tenantIds = tenantService.queryIdListByStartId(startTenantId, size);
//            if (CollectionUtils.isEmpty(tenantIds)) {
//                break;
//            }
//
//            dealUnfreezeQueryWithTenantIds(tenantIds);
//
//            startTenantId = tenantIds.get(tenantIds.size() - 1);
//        }
    
        dealUnfreezeQueryWithTenantIds(new ArrayList<>(80));
    
    
    }
    
    private void dealUnfreezeQueryWithTenantIds(List<Integer> tenantIds) {
        tenantIds.stream().forEach(tenantId -> {
            Integer size = 200;
            Long startId = 0L;
            while (true) {
                List<ProfitSharingOrderTypeUnfreezeBO> profitSharingOrderTypeUnfreezeBOList = profitSharingOrderDetailService.listOrderTypeUnfreeze(tenantId, startId, size);
                if (ObjectUtils.isEmpty(profitSharingOrderTypeUnfreezeBOList)) {
                    break;
                }
        
                // 根据微信支付订单号处理
                dealUnfreezeQuery(tenantId, profitSharingOrderTypeUnfreezeBOList);
    
                startId += profitSharingOrderTypeUnfreezeBOList.get(profitSharingOrderTypeUnfreezeBOList.size() - 1).getId();
            }
        });
        
    }
    
    private void dealUnfreezeQuery(Integer tenantId, List<ProfitSharingOrderTypeUnfreezeBO> profitSharingOrderTypeUnfreezeBOList) {
        Set<Long> franchiseeIdList = profitSharingOrderTypeUnfreezeBOList.parallelStream().map(ProfitSharingOrderTypeUnfreezeBO::getFranchiseeId)
                .collect(Collectors.toSet());
    
        // 批量精确查询支付配置
        try {
            List<WechatPayParamsDetails> wechatPayParamsDetails = wechatPayParamsBizService.queryListPreciseCacheByTenantIdAndFranchiseeIds(tenantId, franchiseeIdList, null);
            if (CollectionUtils.isEmpty(wechatPayParamsDetails)) {
                log.info("deal unfreeze query info, wechatPayParamsDetails is null, tenantId = {}, franchiseeIds = {}", tenantId, franchiseeIdList);
                return;
            }
    
            Map<Long, WechatPayParamsDetails> wechatPayParamsDetailsMap = wechatPayParamsDetails.stream().collect(
                    Collectors.toMap(WechatPayParamsDetails::getFranchiseeId, Function.identity(), (wechatPayParamsDetails1, wechatPayParamsDetails2) -> wechatPayParamsDetails1));
    
            profitSharingOrderTypeUnfreezeBOList.stream().forEach(profitSharingOrderTypeUnfreezeBO -> {
                if (!wechatPayParamsDetailsMap.containsKey(profitSharingOrderTypeUnfreezeBO.getFranchiseeId())) {
                    log.info("deal unfreeze query info, wechatPayParamsDetailsMap is null, tenantId = {}, franchiseeId = {}", tenantId, profitSharingOrderTypeUnfreezeBO.getFranchiseeId());
                    return;
                }
    
                WechatPayParamsDetails wechatPayParamsDetail = wechatPayParamsDetailsMap.get(profitSharingOrderTypeUnfreezeBO.getFranchiseeId());
                
                // 调用查询接口
                WechatProfitSharingQueryOrderRequest unfreezeRequest = new WechatProfitSharingQueryOrderRequest();
                unfreezeRequest.setCommonParam(ElectricityPayParamsConverter.optWechatProfitSharingCommonRequest(wechatPayParamsDetail));
                unfreezeRequest.setOutOrderNo(profitSharingOrderTypeUnfreezeBO.getOrderNo());
                unfreezeRequest.setTransactionId(profitSharingOrderTypeUnfreezeBO.getThirdTradeOrderNo());
    
                log.info("deal unfreeze query info!, thirdTradeOrderNo={}", profitSharingOrderTypeUnfreezeBO.getThirdTradeOrderNo());
    
                try {
                    WechatProfitSharingQueryOrderResp unfreeze = (WechatProfitSharingQueryOrderResp) profitSharingServiceAdapter.query(unfreezeRequest);
                    log.info("PROFIT SHARING UNFREEZE INFO!unfreeze query end, thirdTradeOrderNo={}, response={}", profitSharingOrderTypeUnfreezeBO.getThirdTradeOrderNo(), unfreeze);
                    if (Objects.isNull(unfreeze)) {
                        return;
                    }
                    
                    // 主表分账状态
                    Integer orderStatus = null;
                    if ("PROCESSING".equals(unfreeze.getState())) {
                        orderStatus = ProfitSharingOrderDetailStatusEnum.IN_PROCESS.getCode();
                    } else if ("FINISHED".equals(unfreeze.getState())) {
                        orderStatus = ProfitSharingOrderDetailStatusEnum.COMPLETE.getCode();
                    }
    
                    // 明细表分账状态
                    Integer orderDetailStatus = null;
                    Long finishTime = null;
                    String failReason = null;
                    Integer unfreezeStatus = null;
                    BigDecimal amount = null;
                    
                    // 是否回滚金额
                    boolean isRollbackAmount = false;
                    
                    if (ObjectUtils.isNotEmpty(unfreeze.getReceivers())) {
                        ReceiverResp receiverResp = unfreeze.getReceivers().get(0);
                        if (StringUtils.isNotEmpty(receiverResp.getFinishTime())) {
                            LocalDateTime localDateTime = LocalDateTime.parse(receiverResp.getFinishTime(), formatter);
                            ZonedDateTime zonedDateTime = localDateTime.atZone(ZoneId.systemDefault()); // 转换为系统默认时区的ZonedDateTime
                            finishTime = zonedDateTime.toEpochSecond();
                        }
    
                        if ("PENDING".equals(receiverResp.getResult())) {
                            orderStatus = ProfitSharingOrderDetailStatusEnum.IN_PROCESS.getCode();
                        } else if ("SUCCESS".equals(unfreeze.getState())) {
                            orderStatus = ProfitSharingOrderDetailStatusEnum.COMPLETE.getCode();
                            unfreezeStatus = ProfitSharingOrderDetailUnfreezeStatusEnum.SUCCESS.getCode();
                            isRollbackAmount = true;
                        } else if ("CLOSED".equals(unfreeze.getState())) {
                            orderStatus = ProfitSharingOrderDetailStatusEnum.FAIL.getCode();
                            unfreezeStatus = ProfitSharingOrderDetailUnfreezeStatusEnum.FAIL.getCode();
                        }
                        
                        if (StringUtils.isNotEmpty(receiverResp.getFailReason())) {
                            failReason = receiverResp.getFailReason();
                        }
                        
                        if (Objects.nonNull(receiverResp.getAmount())) {
                            amount = new BigDecimal(receiverResp.getAmount()).divide(new BigDecimal(100));
                        }
                    }
    
                    // 修改分账主表的状态
                    ProfitSharingOrder profitSharingOrderUpdate = new ProfitSharingOrder();
                    profitSharingOrderUpdate.setId(profitSharingOrderTypeUnfreezeBO.getId());
                    profitSharingOrderUpdate.setUpdateTime(System.currentTimeMillis());
                    profitSharingOrderUpdate.setStatus(orderStatus);
                    profitSharingOrderService.updateUnfreezeOrderById(profitSharingOrderUpdate);
                    
                    // 修改分账明细表的状态
                    ProfitSharingOrderDetail profitSharingOrderDetailUpdate = new ProfitSharingOrderDetail();
                    profitSharingOrderDetailUpdate.setId(profitSharingOrderTypeUnfreezeBO.getDetailId());
                    profitSharingOrderDetailUpdate.setUpdateTime(System.currentTimeMillis());
                    profitSharingOrderDetailUpdate.setStatus(orderDetailStatus);
                    profitSharingOrderDetailUpdate.setFinishTime(finishTime);
                    profitSharingOrderDetailUpdate.setUnfreezeStatus(unfreezeStatus);
                    profitSharingOrderDetailUpdate.setFailReason(failReason);
                    profitSharingOrderDetailUpdate.setProfitSharingAmount(amount);
                    profitSharingOrderDetailService.updateUnfreezeOrderById(profitSharingOrderDetailUpdate);
                    
                    // 回滚余额
                    if (isRollbackAmount) {
                        rollbackAmount(profitSharingOrderTypeUnfreezeBO, tenantId);
                    }
                    
                } catch (ProfitSharingException e) {
                    log.error("deal unfreeze query info error!orderNo = {}, thirdTradeNo = {}", profitSharingOrderTypeUnfreezeBO.getOrderNo(), profitSharingOrderTypeUnfreezeBO.getThirdTradeOrderNo(), e);
                }
            });
        } catch (WechatPayException e) {
            log.error("deal unfreeze query info error!", e);
        }
    
    }
    
    /**
     * 将微信支付订单号下的所有分账明细失败的分账金额累加起来回滚至
     * 支付配置对应的月结分账额度表中 备注：只回滚业务类型为换电，保险，滞纳金。
     *
     * @param profitSharingOrderTypeUnfreezeBO
     * @param tenantId
     */
    private void rollbackAmount(ProfitSharingOrderTypeUnfreezeBO profitSharingOrderTypeUnfreezeBO, Integer tenantId) {
        List<ProfitSharingOrderDetail> profitSharingOrderDetailList = profitSharingOrderDetailService.listFailByThirdOrderNo(profitSharingOrderTypeUnfreezeBO.getThirdTradeOrderNo());
        if (ObjectUtils.isEmpty(profitSharingOrderDetailList)) {
            log.info("profit sharing rollback amount info,fail detail order is empty, thirdTradeOrderNo = {}", profitSharingOrderTypeUnfreezeBO.getThirdTradeOrderNo());
            return;
        }
    
        Long createTime = profitSharingOrderDetailList.get(0).getCreateTime();
        String monthDate = DateUtils.getMonthDate(createTime);
        
        BigDecimal rollbackAmount = profitSharingOrderDetailList.stream().map(ProfitSharingOrderDetail::getProfitSharingAmount).filter(Objects::nonNull)
                .collect(Collectors.reducing(BigDecimal.ZERO, BigDecimal::add));
        
        // 检测分账统计是否存在
        ProfitSharingStatistics profitSharingStatistics = profitSharingStatisticsService.queryByTenantIdAndFranchiseeIdAndStatisticsTime(tenantId, profitSharingOrderTypeUnfreezeBO.getFranchiseeId(), monthDate);
        if (Objects.isNull(profitSharingStatistics)) {
            log.warn("profit sharing rollback amount info,statistics is not exists,tenantId = {}, franchiseeId = {}, statisticsTime = {}", tenantId, profitSharingOrderTypeUnfreezeBO.getFranchiseeId(), monthDate);
            return;
        }
    
        String lockKey = String.format(PROFIT_SHARING_STATISTICS_LOCK_KEY, tenantId, profitSharingOrderTypeUnfreezeBO.getFranchiseeId());
        String clientId = UUID.randomUUID().toString();
        Boolean lock = redisService.tryLock(lockKey, clientId, 5L, 3, 1000L);
        if (!lock) {
            log.warn("PROFIT SHARING ROLLBACK AMOUNT WARN! GET LOCK FAIL lockKey:{}, id = {}, rollbackAmount = {}", lockKey, profitSharingStatistics.getId(), rollbackAmount);
            return;
        }
        
        try {
            profitSharingStatisticsService.subtractAmountById(profitSharingStatistics.getId(), rollbackAmount);
        } catch (Exception e) {
            log.error("profit sharing rollback amount info,update statistics error! id = {}, rollbackAmount = {}", profitSharingStatistics.getId(), rollbackAmount, e);
        } finally {
            redisService.releaseLockLua(lockKey, clientId);
        }
        
        
    }
    
    private void dealWithTenantIds(List<Integer> tenantIds) {
        // 两个月前的第一天
        long startTime = DateUtils.getBeforeMonthFirstDayTimestamp(DateFormatConstant.LAST_MONTH);
        tenantIds.stream().forEach(tenantId -> {
            Integer offset = 0;
            Integer size = 200;
            
            while (true) {
                List<String> thirdOrderNoList = profitSharingTradeMixedOrderService.listThirdOrderNoByTenantId(tenantId, startTime, offset, size);
                if (ObjectUtils.isEmpty(thirdOrderNoList)) {
                    break;
                }
                
                // 根据微信支付订单号处理
                dealWithThirdOrderNo(thirdOrderNoList);
                
                offset += size;
            }
        });
    }
    
    private void dealWithThirdOrderNo(List<String> thirdOrderNoList) {
        // 查询出存在解冻的订单
        List<String> unfreezeByThirdOrderNoList = profitSharingOrderService.listUnfreezeByThirdOrderNo(thirdOrderNoList);
    
        thirdOrderNoList.stream().forEach(thirdOrderNo -> {
            // 校验微信支付订单号是否已经存在解冻订单
            if (unfreezeByThirdOrderNoList.contains(thirdOrderNo)) {
                return;
            }
            
            // 不存在解冻待处理的明细
            boolean existsNotUnfreezeByThirdOrderNo = profitSharingOrderDetailService.existsNotUnfreezeByThirdOrderNo(thirdOrderNo);
            if (!existsNotUnfreezeByThirdOrderNo) {
                log.info("profit sharing unfreeze info,thirdOrderNo = {} is not exists unfreeze order detail", thirdOrderNo);
                return;
            }
            
            // 存在未处理完成的明细
            boolean existsNotCompleteByThirdOrderNo = profitSharingOrderDetailService.existsNotCompleteByThirdOrderNo(thirdOrderNo);
            if (existsNotCompleteByThirdOrderNo) {
                log.info("profit sharing unfreeze info,thirdOrderNo = {} is exists not complete order detail", thirdOrderNo);
                return;
            }
            
            // 存在分账明细失败的微信支付订单号
            boolean existsFail = profitSharingOrderDetailService.existsFailByThirdOrderNo(thirdOrderNo);
            if (existsFail) {
                // 默认状态解冻中
                Integer unfreezeStatus = ProfitSharingOrderDetailUnfreezeStatusEnum.IN_PROCESS.getCode();
                // 查询分账交易混合订单
                ProfitSharingTradeMixedOrder profitSharingTradeMixedOrder = profitSharingTradeMixedOrderService.queryByThirdOrderNo(thirdOrderNo);
                if (ObjectUtils.isEmpty(profitSharingTradeMixedOrder)) {
                    log.info("PROFIT SHARING UNFREEZE INFO!, profit sharing trade mixed order is not find, thirdOrderNo = {}",thirdOrderNo);
                    return;
                }
                
                // 调用解冻接口
                try {
                    profitSharingOrderService.doUnFreeze(profitSharingTradeMixedOrder);
                    // 修改分账明细失败的为解冻中
                } catch (ProfitSharingException e) {
                    log.error("PROFIT SHARING UNFREEZE ERROR!, thirdOrderNo = {}",thirdOrderNo, e);
                    
                    unfreezeStatus = ProfitSharingOrderDetailUnfreezeStatusEnum.LAPSED.getCode();
                }
    
                // 修改分账失败的明细解冻状态
                List<Integer> businessTypeList = new ArrayList<>();
                businessTypeList.add(ProfitSharingBusinessTypeEnum.BATTERY_PACKAGE.getCode());
                businessTypeList.add(ProfitSharingBusinessTypeEnum.INSURANCE.getCode());
                businessTypeList.add(ProfitSharingBusinessTypeEnum.BATTERY_SERVICE_FEE.getCode());
                businessTypeList.add(ProfitSharingBusinessTypeEnum.SYSTEM.getCode());
    
                profitSharingOrderDetailService.updateUnfreezeStatusByThirdOrderNo(thirdOrderNo, ProfitSharingOrderDetailStatusEnum.FAIL.getCode(), unfreezeStatus, businessTypeList, System.currentTimeMillis());
            }
    
            // 修改分账完成的明细的解冻状态为无需解冻
            List<Integer> businessTypeList = new ArrayList<>();
            businessTypeList.add(ProfitSharingBusinessTypeEnum.BATTERY_PACKAGE.getCode());
            businessTypeList.add(ProfitSharingBusinessTypeEnum.INSURANCE.getCode());
            businessTypeList.add(ProfitSharingBusinessTypeEnum.BATTERY_SERVICE_FEE.getCode());
            businessTypeList.add(ProfitSharingBusinessTypeEnum.SYSTEM.getCode());
    
            profitSharingOrderDetailService.updateUnfreezeStatusByThirdOrderNo(thirdOrderNo, ProfitSharingOrderDetailStatusEnum.COMPLETE.getCode(), ProfitSharingOrderDetailUnfreezeStatusEnum.DISPENSE_WITH.getCode(), businessTypeList, System.currentTimeMillis());
            
        });
    }
}
