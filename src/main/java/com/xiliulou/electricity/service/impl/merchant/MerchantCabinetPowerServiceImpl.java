package com.xiliulou.electricity.service.impl.merchant;

import cn.hutool.core.date.DateUtil;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.thread.XllThreadPoolExecutorService;
import com.xiliulou.core.thread.XllThreadPoolExecutors;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.constant.merchant.MerchantPlaceBindConstant;
import com.xiliulou.electricity.constant.merchant.MerchantPlaceCabinetBindConstant;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.merchant.Merchant;
import com.xiliulou.electricity.entity.merchant.MerchantCabinetPowerMonthDetailPro;
import com.xiliulou.electricity.entity.merchant.MerchantPlace;
import com.xiliulou.electricity.entity.merchant.MerchantPlaceBind;
import com.xiliulou.electricity.entity.merchant.MerchantPlaceCabinetBind;
import com.xiliulou.electricity.request.merchant.MerchantCabinetPowerRequest;
import com.xiliulou.electricity.request.merchant.MerchantPlaceCabinetConditionRequest;
import com.xiliulou.electricity.request.merchant.MerchantPlaceConditionRequest;
import com.xiliulou.electricity.service.ElePowerService;
import com.xiliulou.electricity.service.ElectricityCabinetService;
import com.xiliulou.electricity.service.merchant.MerchantCabinetPowerMonthDetailProService;
import com.xiliulou.electricity.service.merchant.MerchantCabinetPowerMonthRecordProService;
import com.xiliulou.electricity.service.merchant.MerchantCabinetPowerService;
import com.xiliulou.electricity.service.merchant.MerchantPlaceBindService;
import com.xiliulou.electricity.service.merchant.MerchantPlaceCabinetBindService;
import com.xiliulou.electricity.service.merchant.MerchantPlaceService;
import com.xiliulou.electricity.service.merchant.MerchantService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.DateUtils;
import com.xiliulou.electricity.vo.EleSumPowerVO;
import com.xiliulou.electricity.vo.merchant.MerchantPlaceAndCabinetUserVO;
import com.xiliulou.electricity.vo.merchant.MerchantPlaceCabinetVO;
import com.xiliulou.electricity.vo.merchant.MerchantPlaceSelectVO;
import com.xiliulou.electricity.vo.merchant.MerchantPowerPeriodVO;
import com.xiliulou.electricity.vo.merchant.MerchantProCabinetPowerDetailVO;
import com.xiliulou.electricity.vo.merchant.MerchantProCabinetPowerVO;
import com.xiliulou.electricity.vo.merchant.MerchantProEidPowerListVO;
import com.xiliulou.electricity.vo.merchant.MerchantProLivePowerVO;
import com.xiliulou.electricity.vo.merchant.MerchantProPowerChargeLineDataVO;
import com.xiliulou.electricity.vo.merchant.MerchantProPowerLineDataVO;
import com.xiliulou.electricity.vo.merchant.MerchantProPowerLineVO;
import com.xiliulou.electricity.vo.merchant.MerchantProPowerVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author HeYafeng
 * @description 商户/场地/下 柜机电量/电费
 * @date 2024/2/20 19:14:12
 */
@Slf4j
@Service
public class MerchantCabinetPowerServiceImpl implements MerchantCabinetPowerService {
    
    XllThreadPoolExecutorService executorService = XllThreadPoolExecutors.newFixedThreadPool("HandleMerchantPowerForProPool", 3, "merchantPowerForProPoolThread",
            new LinkedBlockingQueue<>(10000));
    
    @Resource
    private MerchantPlaceBindService merchantPlaceBindService;
    
    @Resource
    private MerchantPlaceService merchantPlaceService;
    
    @Resource
    private MerchantPlaceCabinetBindService merchantPlaceCabinetBindService;
    
    @Resource
    private ElectricityCabinetService electricityCabinetService;
    
    @Resource
    private ElePowerService elePowerService;
    
    @Resource
    private MerchantCabinetPowerMonthRecordProService merchantCabinetPowerMonthRecordProService;
    
    @Resource
    private MerchantCabinetPowerMonthDetailProService merchantCabinetPowerMonthDetailProService;
    
    @Resource
    private MerchantService merchantService;
    
    @Resource
    private RedisService redisService;
    
    @Slave
    @Override
    public MerchantProPowerVO powerData(MerchantCabinetPowerRequest request) {
        log.info("执行powerData=======>");
        
        Merchant merchant = merchantService.queryByUid(request.getUid());
        if (Objects.isNull(merchant)) {
            log.warn("Merchant power for pro error, merchant not exist, uid={}", request.getUid());
            return null;
        }
        
        Integer tenantId = TenantContextHolder.getTenantId();
        if (Objects.nonNull(tenantId) && !Objects.equals(tenantId, merchant.getTenantId())) {
            log.warn("Merchant power for pro error, tenant not exist, uid={}", request.getUid());
            return null;
        }
        
        request.setMerchantId(merchant.getId());
        
        //获取要查询的柜机
        List<Long> cabinetIds = getStaticsCabinetIds(request);
        if (CollectionUtils.isEmpty(cabinetIds)) {
            return null;
        }
        
        MerchantProPowerVO vo = new MerchantProPowerVO();
        
        // 1.今日电量
        CompletableFuture<Void> todayPowerFuture = CompletableFuture.runAsync(() -> {
            log.info("执行今日电量开始=======>");
            MerchantPowerPeriodVO todayPower = getTodayPower(tenantId, merchant.getId(), cabinetIds);
            
            vo.setTodayPower(Objects.isNull(todayPower) ? NumberConstant.ZERO_D : todayPower.getPower());
            vo.setTodayCharge(Objects.isNull(todayPower) ? NumberConstant.ZERO_D : todayPower.getCharge());
            log.info("执行今日电量结束=======>{}", todayPower);
        }, executorService).exceptionally(e -> {
            log.error("Query merchant today power data error! uid={}", request.getUid(), e);
            return null;
        });
        
        // 2.昨日电量
        CompletableFuture<Void> yesterdayPowerFuture = CompletableFuture.runAsync(() -> {
            MerchantPowerPeriodVO yesterdayPower = getYesterdayPower(tenantId, merchant.getId(), cabinetIds);
            
            vo.setYesterdayPower(Objects.isNull(yesterdayPower) ? NumberConstant.ZERO_D : yesterdayPower.getPower());
            vo.setYesterdayCharge(Objects.isNull(yesterdayPower) ? NumberConstant.ZERO_D : yesterdayPower.getCharge());
            
        }, executorService).exceptionally(e -> {
            log.error("Query merchant yesterday power data error! uid={}", request.getUid(), e);
            return null;
        });
        
        // 3.本月电量
        CompletableFuture<Void> thisMonthPowerFuture = CompletableFuture.runAsync(() -> {
            MerchantPowerPeriodVO thisMonthPower = getThisMonthPower(tenantId, merchant.getId(), cabinetIds);
            
            vo.setThisMonthPower(Objects.isNull(thisMonthPower) ? NumberConstant.ZERO_D : thisMonthPower.getPower());
            vo.setThisMonthCharge(Objects.isNull(thisMonthPower) ? NumberConstant.ZERO_D : thisMonthPower.getCharge());
            
        }, executorService).exceptionally(e -> {
            log.error("Query merchant this month power data error! uid={}", request.getUid(), e);
            return null;
        });
        
        // 4.上月电量
        CompletableFuture<Void> lastMonthPowerFuture = CompletableFuture.runAsync(() -> {
            MerchantPowerPeriodVO lastMonthPower = getLastMonthPower(tenantId, merchant.getId(), cabinetIds);
            
            vo.setLastMonthPower(Objects.isNull(lastMonthPower) ? NumberConstant.ZERO_D : lastMonthPower.getPower());
            vo.setLastMonthCharge(Objects.isNull(lastMonthPower) ? NumberConstant.ZERO_D : lastMonthPower.getCharge());
            
        }, executorService).exceptionally(e -> {
            log.error("Query merchant last month power data error! uid={}", request.getUid(), e);
            return null;
        });
        
        // 5.累计电量
        CompletableFuture<Void> totalPowerFuture = CompletableFuture.runAsync(() -> {
            MerchantPowerPeriodVO totalPower = getTotalPower(tenantId, merchant.getId(), cabinetIds);
            vo.setTotalPower(totalPower.getPower());
            vo.setTotalCharge(totalPower.getCharge());
            
        }, executorService).exceptionally(e -> {
            log.error("Query merchant total power data error! uid={}", request.getUid(), e);
            return null;
        });
        
        // 等待所有线程执行完毕
        CompletableFuture<Void> resultComplete = CompletableFuture.allOf(todayPowerFuture, yesterdayPowerFuture, thisMonthPowerFuture, lastMonthPowerFuture, totalPowerFuture);
        
        try {
            resultComplete.get(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("Query merchant power data ERROR! uid={}", request.getUid(), e);
            return null;
        }
        
        return vo;
    }
    
    private MerchantPowerPeriodVO getTodayPower(Integer tenantId, Long merchantId, List<Long> cabinetIds) {
        
        log.info("执行getTodayPower...cabinetIds={}", cabinetIds);
        
        // 今日0点
        Long todayStartTime = DateUtils.getTimeAgoStartTime(NumberConstant.ZERO);
        // 当前时间
        long nowTime = System.currentTimeMillis();
        
        // 获取商户场地绑定记录
        List<MerchantPlaceBind> merchantPlaceBindList = getTodayMerchantPlaceBindList(merchantId, todayStartTime, nowTime);
        if (CollectionUtils.isEmpty(merchantPlaceBindList)) {
            log.warn("Merchant getTodayPower merchantPlaceBindList is empty! merchantId={}", merchantId);
            return null;
        }
        
        log.info("执行getTodayPower...cabinetIds={}, merchantPlaceBindList={}", cabinetIds, merchantPlaceBindList);
        
        // 封装结果集
        List<MerchantProLivePowerVO> resultList = new ArrayList<>();
        
        // 遍历场地
        for (MerchantPlaceBind merchantPlaceBind : merchantPlaceBindList) {
            Long placeId = merchantPlaceBind.getPlaceId();
            Long bindTime = merchantPlaceBind.getBindTime();
            Long unBindTime = merchantPlaceBind.getUnBindTime();
            
            log.info("执行getTodayPower...placeId={}, bindTime={}, unBindTime={}", placeId, bindTime, unBindTime);
            
            // 获取场地柜机绑定记录
            List<MerchantPlaceCabinetBind> placeCabinetBindList = getTodayPlaceCabinetBindList(placeId, bindTime, unBindTime);
            if (CollectionUtils.isEmpty(placeCabinetBindList)) {
                log.warn("Merchant getTodayPower placeCabinetBindList is empty! placeId={}, bindTime={}, unBindTime={}", placeId, bindTime, unBindTime);
                continue;
            }
            
            log.info("执行getTodayPower...placeId={}, placeCabinetBindList={}", placeId, placeCabinetBindList);
            
            // 遍历柜机
            List<CabinetPowerProRunnable> collect = cabinetIds.parallelStream().map(eid -> new CabinetPowerProRunnable(eid, elePowerService, placeCabinetBindList, tenantId))
                    .collect(Collectors.toList());
            
            log.info("执行getTodayPower...cabinetIds={}, collect={}", cabinetIds, collect);
            
            try {
                List<Future<MerchantProEidPowerListVO>> futureList = executorService.invokeAll(collect);
                if (CollectionUtils.isNotEmpty(futureList)) {
                    for (Future<MerchantProEidPowerListVO> future : futureList) {
                        MerchantProEidPowerListVO result = future.get();
                        List<MerchantProLivePowerVO> powerList = result.getPowerList();
                        resultList.addAll(powerList);
                    }
                }
            } catch (InterruptedException | ExecutionException e) {
                log.error("Merchant get lastMonth power for pro Exception occur!", e);
            }
        }
        
        // 封装数据
        MerchantPowerPeriodVO powerVO = new MerchantPowerPeriodVO();
        powerVO.setPower(resultList.stream().filter(Objects::nonNull).mapToDouble(MerchantProLivePowerVO::getPower).sum());
        powerVO.setCharge(resultList.stream().filter(Objects::nonNull).mapToDouble(MerchantProLivePowerVO::getCharge).sum());
        
        log.info("执行getTodayPower...cabinetIds={}, powerVO={}", cabinetIds, powerVO);
        
        return powerVO;
    }
    
    private List<MerchantProLivePowerVO> getTodayPowerForCabinetList(Integer tenantId, Long merchantId, List<Long> cabinetIds) {
        // 今日0点
        Long todayStartTime = DateUtils.getTimeAgoStartTime(NumberConstant.ZERO);
        // 当前时间
        long nowTime = System.currentTimeMillis();
        
        // 获取商户场地绑定记录
        List<MerchantPlaceBind> merchantPlaceBindList = getTodayMerchantPlaceBindList(merchantId, todayStartTime, nowTime);
        if (CollectionUtils.isEmpty(merchantPlaceBindList)) {
            return Collections.emptyList();
        }
        
        // 封装结果集
        List<MerchantProLivePowerVO> resultList = new ArrayList<>();
        
        // 遍历场地
        for (MerchantPlaceBind merchantPlaceBind : merchantPlaceBindList) {
            Long placeId = merchantPlaceBind.getPlaceId();
            Long bindTime = merchantPlaceBind.getBindTime();
            Long unBindTime = merchantPlaceBind.getUnBindTime();
            
            // 获取场地柜机绑定记录
            List<MerchantPlaceCabinetBind> placeCabinetBindList = getTodayPlaceCabinetBindList(placeId, bindTime, unBindTime);
            if (CollectionUtils.isEmpty(placeCabinetBindList)) {
                continue;
            }
            
            // 遍历柜机
            List<CabinetPowerProRunnable> collect = cabinetIds.parallelStream().map(eid -> new CabinetPowerProRunnable(eid, elePowerService, placeCabinetBindList, tenantId))
                    .collect(Collectors.toList());
            
            try {
                List<Future<MerchantProEidPowerListVO>> futureList = executorService.invokeAll(collect);
                if (CollectionUtils.isNotEmpty(futureList)) {
                    for (Future<MerchantProEidPowerListVO> future : futureList) {
                        MerchantProEidPowerListVO result = future.get();
                        List<MerchantProLivePowerVO> powerList = result.getPowerList();
                        resultList.addAll(powerList);
                    }
                }
            } catch (InterruptedException | ExecutionException e) {
                log.error("Merchant get lastMonth power for pro Exception occur!", e);
            }
        }
        
        // resultList 根据eid进行分组
        Map<Long, List<MerchantProLivePowerVO>> groupByEidMap = resultList.stream().filter(Objects::nonNull).collect(Collectors.groupingBy(MerchantProLivePowerVO::getEid));
        if (MapUtils.isEmpty(groupByEidMap)) {
            return Collections.emptyList();
        }
        
        List<MerchantProLivePowerVO> voList = new ArrayList<>();
        // 遍历groupByEidMap
        groupByEidMap.forEach((k, v) -> {
            double sumPower = v.stream().filter(Objects::nonNull).mapToDouble(MerchantProLivePowerVO::getPower).sum();
            double sumCharge = v.stream().filter(Objects::nonNull).mapToDouble(MerchantProLivePowerVO::getCharge).sum();
            
            MerchantProLivePowerVO powerVO = new MerchantProLivePowerVO();
            powerVO.setEid(k);
            powerVO.setPower(sumPower);
            powerVO.setCharge(sumCharge);
            
            voList.add(powerVO);
        });
        
        if (CollectionUtils.isEmpty(voList)) {
            return Collections.emptyList();
        }
        
        return voList;
    }
    
    private MerchantPowerPeriodVO getYesterdayPower(Integer tenantId, Long merchantId, List<Long> cabinetIds) {
        // 昨日0点
        Long yesterdayStartTime = DateUtils.getTimeAgoStartTime(NumberConstant.ONE);
        // 昨日23:59:59
        long yesterdayEndTime = DateUtils.getTimeAgoEndTime(NumberConstant.ONE);
        
        // 获取商户场地绑定记录
        List<MerchantPlaceBind> merchantPlaceBindList = getYesterdayMerchantPlaceBindList(merchantId, yesterdayStartTime, yesterdayEndTime);
        if (CollectionUtils.isEmpty(merchantPlaceBindList)) {
            return null;
        }
        
        // 封装结果集
        List<MerchantProLivePowerVO> resultList = new ArrayList<>();
        
        // 遍历场地
        for (MerchantPlaceBind merchantPlaceBind : merchantPlaceBindList) {
            Long placeId = merchantPlaceBind.getPlaceId();
            Long bindTime = merchantPlaceBind.getBindTime();
            Long unBindTime = merchantPlaceBind.getUnBindTime();
            
            // 获取场地柜机绑定记录
            List<MerchantPlaceCabinetBind> placeCabinetBindList = getYesterdayPlaceCabinetBindList(placeId, bindTime, unBindTime);
            if (CollectionUtils.isEmpty(placeCabinetBindList)) {
                continue;
            }
            
            // 遍历柜机
            List<CabinetPowerProRunnable> collect = cabinetIds.parallelStream().map(eid -> new CabinetPowerProRunnable(eid, elePowerService, placeCabinetBindList, tenantId))
                    .collect(Collectors.toList());
            
            try {
                List<Future<MerchantProEidPowerListVO>> futureList = executorService.invokeAll(collect);
                if (CollectionUtils.isNotEmpty(futureList)) {
                    for (Future<MerchantProEidPowerListVO> future : futureList) {
                        MerchantProEidPowerListVO result = future.get();
                        List<MerchantProLivePowerVO> powerList = result.getPowerList();
                        resultList.addAll(powerList);
                    }
                }
            } catch (InterruptedException | ExecutionException e) {
                log.error("Merchant get lastMonth power for pro Exception occur!", e);
            }
        }
        
        // 封装数据
        MerchantPowerPeriodVO powerVO = new MerchantPowerPeriodVO();
        powerVO.setPower(resultList.stream().filter(Objects::nonNull).mapToDouble(MerchantProLivePowerVO::getPower).sum());
        powerVO.setCharge(resultList.stream().filter(Objects::nonNull).mapToDouble(MerchantProLivePowerVO::getCharge).sum());
        
        return powerVO;
    }
    
    private MerchantPowerPeriodVO getThisMonthPower(Integer tenantId, Long merchantId, List<Long> cabinetIds) {
        // 本月第一天0点
        Long thisMonthStartTime = DateUtils.getBeforeMonthFirstDayTimestamp(NumberConstant.ZERO);
        long nowTime = System.currentTimeMillis();
        
        // 获取商户场地绑定记录
        List<MerchantPlaceBind> merchantPlaceBindList = getThisMonthMerchantPlaceBindList(merchantId, thisMonthStartTime, nowTime);
        if (CollectionUtils.isEmpty(merchantPlaceBindList)) {
            return null;
        }
        
        // 封装结果集
        List<MerchantProLivePowerVO> resultList = new ArrayList<>();
        
        // 遍历场地
        for (MerchantPlaceBind merchantPlaceBind : merchantPlaceBindList) {
            Long placeId = merchantPlaceBind.getPlaceId();
            Long bindTime = merchantPlaceBind.getBindTime();
            Long unBindTime = merchantPlaceBind.getUnBindTime();
            
            // 获取场地柜机绑定记录
            List<MerchantPlaceCabinetBind> placeCabinetBindList = getThisMonthPlaceCabinetBindList(placeId, bindTime, unBindTime);
            if (CollectionUtils.isEmpty(placeCabinetBindList)) {
                continue;
            }
            
            // 遍历柜机
            List<CabinetPowerProRunnable> collect = cabinetIds.parallelStream().map(eid -> new CabinetPowerProRunnable(eid, elePowerService, placeCabinetBindList, tenantId))
                    .collect(Collectors.toList());
            
            try {
                List<Future<MerchantProEidPowerListVO>> futureList = executorService.invokeAll(collect);
                if (CollectionUtils.isNotEmpty(futureList)) {
                    for (Future<MerchantProEidPowerListVO> future : futureList) {
                        MerchantProEidPowerListVO result = future.get();
                        List<MerchantProLivePowerVO> powerList = result.getPowerList();
                        resultList.addAll(powerList);
                    }
                }
            } catch (InterruptedException | ExecutionException e) {
                log.error("Merchant get lastMonth power for pro Exception occur!", e);
            }
        }
        
        // 封装数据
        MerchantPowerPeriodVO powerVO = new MerchantPowerPeriodVO();
        powerVO.setPower(resultList.stream().filter(Objects::nonNull).mapToDouble(MerchantProLivePowerVO::getPower).sum());
        powerVO.setCharge(resultList.stream().filter(Objects::nonNull).mapToDouble(MerchantProLivePowerVO::getCharge).sum());
        
        return powerVO;
    }
    
    private List<MerchantProCabinetPowerDetailVO> getLiveMonthPowerForCabinetDetail(Integer tenantId, Long merchantId, Long cabinetId, String monthDate, Long startTime,
            Long endTime) {
        // 获取商户场地绑定记录
        List<MerchantPlaceBind> merchantPlaceBindList = getThisMonthMerchantPlaceBindList(merchantId, startTime, endTime);
        if (CollectionUtils.isEmpty(merchantPlaceBindList)) {
            return Collections.emptyList();
        }
        
        // 获取场地柜机绑定记录
        List<MerchantPlaceCabinetBind> placeCabinetBindList = getThisMonthPlaceCabinetBindListForCabinetDetail(cabinetId, startTime, endTime);
        if (CollectionUtils.isEmpty(placeCabinetBindList)) {
            return Collections.emptyList();
        }
        
        // 结果集
        List<MerchantProCabinetPowerDetailVO> list = new ArrayList<>();
        
        for (MerchantPlaceCabinetBind placeCabinetBind : placeCabinetBindList) {
            Long eid = placeCabinetBind.getCabinetId();
            Long placeId = placeCabinetBind.getPlaceId();
            Long cabinetBindTime = placeCabinetBind.getBindTime();
            Long cabinetUnbindTime = placeCabinetBind.getUnBindTime();
            
            List<MerchantPlaceBind> merchantThePlaceBindList = merchantPlaceBindList.parallelStream()
                    .filter(merchantPlaceBind -> Objects.equals(merchantPlaceBind.getPlaceId(), placeId)).collect(Collectors.toList());
            
            if (CollectionUtils.isEmpty(merchantThePlaceBindList)) {
                continue;
            }
            
            // 商户和柜机的绑定状态
            int merchantCabinetBindStatus = MerchantPlaceBindConstant.UN_BIND;
            
            for (MerchantPlaceBind merchantPlaceBind : merchantThePlaceBindList) {
                Integer status = merchantPlaceBind.getType();
                Long placeBindTime = merchantPlaceBind.getBindTime();
                Long placeUnbindTime = merchantPlaceBind.getUnBindTime();
                
                // 绑定状态
                if (Objects.equals(status, MerchantPlaceBindConstant.BIND)) {
                    if (placeBindTime <= cabinetBindTime) {
                        merchantCabinetBindStatus = MerchantPlaceBindConstant.BIND;
                        
                        MerchantProCabinetPowerDetailVO detail = getCabinetPowerDetail(cabinetBindTime, cabinetUnbindTime, eid, placeId, tenantId, monthDate,
                                merchantCabinetBindStatus);
                        
                        list.add(detail);
                    }
                    
                    if (placeBindTime > cabinetBindTime && placeBindTime < cabinetUnbindTime) {
                        //商户未绑定柜机
                        MerchantProCabinetPowerDetailVO detail1 = getCabinetPowerDetail(cabinetBindTime, placeBindTime, eid, placeId, tenantId, monthDate,
                                merchantCabinetBindStatus);
                        
                        //商户绑定柜机
                        merchantCabinetBindStatus = MerchantPlaceBindConstant.BIND;
                        MerchantProCabinetPowerDetailVO detail2 = getCabinetPowerDetail(placeBindTime, cabinetUnbindTime, eid, placeId, tenantId, monthDate,
                                merchantCabinetBindStatus);
                        
                        list.add(detail1);
                        list.add(detail2);
                    }
                    
                    if (placeBindTime >= cabinetUnbindTime) {
                        MerchantProCabinetPowerDetailVO detail = getCabinetPowerDetail(cabinetBindTime, cabinetUnbindTime, eid, placeId, tenantId, monthDate,
                                merchantCabinetBindStatus);
                        
                        list.add(detail);
                    }
                }
            }
        }
        
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }
        
        return list;
    }
    
    private MerchantProCabinetPowerDetailVO getCabinetPowerDetail(Long startTime, Long endTime, Long eid, Long placeId, Integer tenantId, String monthDate,
            Integer merchantCabinetBindStatus) {
        // 查询电量
        EleSumPowerVO eleSumPowerVO = elePowerService.listByCondition(startTime, endTime, List.of(eid), tenantId);
        
        Integer bindStatus = MerchantPlaceBindConstant.UN_BIND;
        
        ElectricityCabinet electricityCabinet = electricityCabinetService.queryByIdFromCache(eid.intValue());
        
        return MerchantProCabinetPowerDetailVO.builder().monthDate(monthDate).cabinetName(Optional.ofNullable(electricityCabinet).orElse(new ElectricityCabinet()).getName())
                .sn(Optional.ofNullable(electricityCabinet).orElse(new ElectricityCabinet()).getSn())
                .power(Objects.isNull(eleSumPowerVO) ? NumberConstant.ZERO_D : eleSumPowerVO.getSumPower())
                .charge(Objects.isNull(eleSumPowerVO) ? NumberConstant.ZERO_D : eleSumPowerVO.getSumCharge()).startTime(startTime).endTime(endTime).placeId(placeId)
                .placeName(Optional.ofNullable(merchantPlaceService.queryByIdFromCache(placeId)).orElse(new MerchantPlace()).getName()).bindStatus(bindStatus)
                .bindStatus(merchantCabinetBindStatus).build();
    }
    
    private List<MerchantProLivePowerVO> getThisMonthPowerForCabinetList(Integer tenantId, Long merchantId, List<Long> cabinetIds) {
        // 本月第一天0点
        Long thisMonthStartTime = DateUtils.getBeforeMonthFirstDayTimestamp(NumberConstant.ZERO);
        long nowTime = System.currentTimeMillis();
        
        // 获取商户场地绑定记录
        List<MerchantPlaceBind> merchantPlaceBindList = getThisMonthMerchantPlaceBindList(merchantId, thisMonthStartTime, nowTime);
        if (CollectionUtils.isEmpty(merchantPlaceBindList)) {
            return Collections.emptyList();
        }
        
        // 封装结果集
        List<MerchantProLivePowerVO> resultList = new ArrayList<>();
        
        // 遍历场地
        for (MerchantPlaceBind merchantPlaceBind : merchantPlaceBindList) {
            Long placeId = merchantPlaceBind.getPlaceId();
            Long bindTime = merchantPlaceBind.getBindTime();
            Long unBindTime = merchantPlaceBind.getUnBindTime();
            
            // 获取场地柜机绑定记录
            List<MerchantPlaceCabinetBind> placeCabinetBindList = getThisMonthPlaceCabinetBindList(placeId, bindTime, unBindTime);
            if (CollectionUtils.isEmpty(placeCabinetBindList)) {
                continue;
            }
            
            // 遍历柜机
            List<CabinetPowerProRunnable> collect = cabinetIds.parallelStream().map(eid -> new CabinetPowerProRunnable(eid, elePowerService, placeCabinetBindList, tenantId))
                    .collect(Collectors.toList());
            
            try {
                List<Future<MerchantProEidPowerListVO>> futureList = executorService.invokeAll(collect);
                if (CollectionUtils.isNotEmpty(futureList)) {
                    for (Future<MerchantProEidPowerListVO> future : futureList) {
                        MerchantProEidPowerListVO result = future.get();
                        List<MerchantProLivePowerVO> powerList = result.getPowerList();
                        resultList.addAll(powerList);
                    }
                }
            } catch (InterruptedException | ExecutionException e) {
                log.error("Merchant get lastMonth power for pro Exception occur!", e);
            }
        }
        
        // resultList 根据eid进行分组
        Map<Long, List<MerchantProLivePowerVO>> groupByEidMap = resultList.stream().filter(Objects::nonNull).collect(Collectors.groupingBy(MerchantProLivePowerVO::getEid));
        if (MapUtils.isEmpty(groupByEidMap)) {
            return Collections.emptyList();
        }
        
        List<MerchantProLivePowerVO> voList = new ArrayList<>();
        // 遍历groupByEidMap
        groupByEidMap.forEach((k, v) -> {
            double sumPower = v.stream().filter(Objects::nonNull).mapToDouble(MerchantProLivePowerVO::getPower).sum();
            double sumCharge = v.stream().filter(Objects::nonNull).mapToDouble(MerchantProLivePowerVO::getCharge).sum();
            
            MerchantProLivePowerVO powerVO = new MerchantProLivePowerVO();
            powerVO.setEid(k);
            powerVO.setPower(sumPower);
            powerVO.setCharge(sumCharge);
            
            voList.add(powerVO);
        });
        
        if (CollectionUtils.isEmpty(voList)) {
            return Collections.emptyList();
        }
        
        return voList;
    }
    
    private MerchantPowerPeriodVO getLastMonthPower(Integer tenantId, Long merchantId, List<Long> cabinetIds) {
        //上月第一天0点
        Long lastMonthStartTime = DateUtils.getBeforeMonthFirstDayTimestamp(NumberConstant.ONE);
        // 上月最后一天23:59:59
        long lastMonthEndTime = DateUtils.getBeforeMonthLastDayTimestamp(NumberConstant.ONE);
        
        // 获取商户场地绑定记录
        List<MerchantPlaceBind> merchantPlaceBindList = getLastMonthMerchantPlaceBindList(merchantId, lastMonthStartTime, lastMonthEndTime);
        if (CollectionUtils.isEmpty(merchantPlaceBindList)) {
            return null;
        }
        
        // 封装结果集
        List<MerchantProLivePowerVO> resultList = new ArrayList<>();
        
        // 遍历场地
        for (MerchantPlaceBind merchantPlaceBind : merchantPlaceBindList) {
            Long placeId = merchantPlaceBind.getPlaceId();
            Long bindTime = merchantPlaceBind.getBindTime();
            Long unBindTime = merchantPlaceBind.getUnBindTime();
            
            // 获取场地柜机绑定记录
            List<MerchantPlaceCabinetBind> placeCabinetBindList = getLastMonthPlaceCabinetBindList(placeId, bindTime, unBindTime);
            if (CollectionUtils.isEmpty(placeCabinetBindList)) {
                continue;
            }
            
            // 遍历柜机
            List<CabinetPowerProRunnable> collect = cabinetIds.parallelStream().map(eid -> new CabinetPowerProRunnable(eid, elePowerService, placeCabinetBindList, tenantId))
                    .collect(Collectors.toList());
            
            try {
                List<Future<MerchantProEidPowerListVO>> futureList = executorService.invokeAll(collect);
                if (CollectionUtils.isNotEmpty(futureList)) {
                    for (Future<MerchantProEidPowerListVO> future : futureList) {
                        MerchantProEidPowerListVO result = future.get();
                        List<MerchantProLivePowerVO> powerList = result.getPowerList();
                        resultList.addAll(powerList);
                    }
                }
            } catch (InterruptedException | ExecutionException e) {
                log.error("Merchant get lastMonth power for pro Exception occur!", e);
            }
        }
        
        if (CollectionUtils.isEmpty(resultList)) {
            return null;
        }
        
        // 封装数据
        MerchantPowerPeriodVO powerVO = new MerchantPowerPeriodVO();
        powerVO.setPower(resultList.stream().filter(Objects::nonNull).mapToDouble(MerchantProLivePowerVO::getPower).sum());
        powerVO.setCharge(resultList.stream().filter(Objects::nonNull).mapToDouble(MerchantProLivePowerVO::getCharge).sum());
        
        return powerVO;
    }
    
    private List<MerchantProLivePowerVO> getLastMonthPowerForCabinetList(Integer tenantId, Long merchantId, List<Long> cabinetIds) {
        //上月第一天0点
        Long lastMonthStartTime = DateUtils.getBeforeMonthFirstDayTimestamp(NumberConstant.ONE);
        // 上月最后一天23:59:59
        long lastMonthEndTime = DateUtils.getBeforeMonthLastDayTimestamp(NumberConstant.ONE);
        
        // 获取商户场地绑定记录
        List<MerchantPlaceBind> merchantPlaceBindList = getLastMonthMerchantPlaceBindList(merchantId, lastMonthStartTime, lastMonthEndTime);
        if (CollectionUtils.isEmpty(merchantPlaceBindList)) {
            return Collections.emptyList();
        }
        
        // 封装结果集
        List<MerchantProLivePowerVO> resultList = new ArrayList<>();
        
        // 遍历场地
        for (MerchantPlaceBind merchantPlaceBind : merchantPlaceBindList) {
            Long placeId = merchantPlaceBind.getPlaceId();
            Long bindTime = merchantPlaceBind.getBindTime();
            Long unBindTime = merchantPlaceBind.getUnBindTime();
            
            // 获取场地柜机绑定记录
            List<MerchantPlaceCabinetBind> placeCabinetBindList = getLastMonthPlaceCabinetBindList(placeId, bindTime, unBindTime);
            if (CollectionUtils.isEmpty(placeCabinetBindList)) {
                continue;
            }
            
            // 遍历柜机
            List<CabinetPowerProRunnable> collect = cabinetIds.parallelStream().map(eid -> new CabinetPowerProRunnable(eid, elePowerService, placeCabinetBindList, tenantId))
                    .collect(Collectors.toList());
            
            try {
                List<Future<MerchantProEidPowerListVO>> futureList = executorService.invokeAll(collect);
                if (CollectionUtils.isNotEmpty(futureList)) {
                    for (Future<MerchantProEidPowerListVO> future : futureList) {
                        MerchantProEidPowerListVO result = future.get();
                        List<MerchantProLivePowerVO> powerList = result.getPowerList();
                        resultList.addAll(powerList);
                    }
                }
            } catch (InterruptedException | ExecutionException e) {
                log.error("Merchant get lastMonth power for pro Exception occur!", e);
            }
        }
        
        // resultList 根据eid进行分组
        Map<Long, List<MerchantProLivePowerVO>> groupByEidMap = resultList.stream().filter(Objects::nonNull).collect(Collectors.groupingBy(MerchantProLivePowerVO::getEid));
        if (MapUtils.isEmpty(groupByEidMap)) {
            return Collections.emptyList();
        }
        
        List<MerchantProLivePowerVO> voList = new ArrayList<>();
        // 遍历groupByEidMap
        groupByEidMap.forEach((k, v) -> {
            double sumPower = v.stream().filter(Objects::nonNull).mapToDouble(MerchantProLivePowerVO::getPower).sum();
            double sumCharge = v.stream().filter(Objects::nonNull).mapToDouble(MerchantProLivePowerVO::getCharge).sum();
            
            MerchantProLivePowerVO powerVO = new MerchantProLivePowerVO();
            powerVO.setEid(k);
            powerVO.setPower(sumPower);
            powerVO.setCharge(sumCharge);
            
            voList.add(powerVO);
        });
        
        if (CollectionUtils.isEmpty(voList)) {
            return Collections.emptyList();
        }
        
        return voList;
    }
    
    private List<MerchantPlaceCabinetBind> getTodayPlaceCabinetBindList(Long placeId, Long merchantPlaceBindTime, Long merchantPlaceUnbindTime) {
        List<MerchantPlaceCabinetBind> resultList = new ArrayList<>();
        List<MerchantPlaceCabinetBind> allPlaceCabinetBindList = new ArrayList<>();
        
        // 绑定状态记录
        MerchantPlaceCabinetConditionRequest placeCabinetBindRequest = MerchantPlaceCabinetConditionRequest.builder().placeId(placeId)
                .status(MerchantPlaceCabinetBindConstant.STATUS_BIND).build();
        List<MerchantPlaceCabinetBind> cabinetBindList = merchantPlaceCabinetBindService.listBindRecord(placeCabinetBindRequest);
        
        if (CollectionUtils.isNotEmpty(cabinetBindList)) {
            for (MerchantPlaceCabinetBind placeCabinetBind : cabinetBindList) {
                Long bindTime = placeCabinetBind.getBindTime();
    
                if (bindTime < merchantPlaceBindTime) {
                    placeCabinetBind.setBindTime(merchantPlaceBindTime);
                }
    
                // 给绑定状态记录赋值解绑时间：merchantPlaceUnbindTime
                placeCabinetBind.setUnBindTime(merchantPlaceUnbindTime);
    
                allPlaceCabinetBindList.add(placeCabinetBind);
            }
        }
        
        // 解绑状态记录
        MerchantPlaceCabinetConditionRequest placeCabinetUnbindRequest = MerchantPlaceCabinetConditionRequest.builder().placeId(placeId)
                .status(MerchantPlaceCabinetBindConstant.STATUS_UNBIND).build();
        List<MerchantPlaceCabinetBind> cabinetUnbindList = merchantPlaceCabinetBindService.listUnbindRecord(placeCabinetUnbindRequest);
        
        if (CollectionUtils.isNotEmpty(cabinetUnbindList)) {
            allPlaceCabinetBindList.addAll(cabinetUnbindList);
            
            // 先掐头去尾
            List<MerchantPlaceCabinetBind> stepOneList = new ArrayList<>();
            for (MerchantPlaceCabinetBind placeCabinetBind : allPlaceCabinetBindList) {
                Long bindTime = placeCabinetBind.getBindTime();
                Long unBindTime = placeCabinetBind.getUnBindTime();
                
                if (unBindTime < merchantPlaceBindTime) {
                    continue;
                }
                
                if (bindTime < merchantPlaceBindTime) {
                    placeCabinetBind.setBindTime(merchantPlaceBindTime);
                }
                
                stepOneList.add(placeCabinetBind);
            }
            
            // 再去除时间段子集
            for (MerchantPlaceCabinetBind current : stepOneList) {
                boolean isSubset = false;
                
                for (MerchantPlaceCabinetBind previous : resultList) {
                    if (current.getBindTime() >= (previous.getBindTime()) && current.getUnBindTime() <= (previous.getUnBindTime())) {
                        isSubset = true;
                        break;
                    }
                }
                
                if (!isSubset) {
                    resultList.add(current);
                }
            }
        }
        
        return resultList;
    }
    
    private List<MerchantPlaceCabinetBind> getYesterdayPlaceCabinetBindList(Long placeId, Long merchantPlaceBindTime, Long merchantPlaceUnbindTime) {
        List<MerchantPlaceCabinetBind> resultList = new ArrayList<>();
        List<MerchantPlaceCabinetBind> allPlaceCabinetBindList = new ArrayList<>();
        
        // 绑定状态记录
        MerchantPlaceCabinetConditionRequest placeCabinetBindRequest = MerchantPlaceCabinetConditionRequest.builder().placeId(placeId)
                .status(MerchantPlaceCabinetBindConstant.STATUS_BIND).startTime(merchantPlaceUnbindTime).build();
        List<MerchantPlaceCabinetBind> cabinetBindList = merchantPlaceCabinetBindService.listBindRecord(placeCabinetBindRequest);
        
        if (CollectionUtils.isNotEmpty(cabinetBindList)) {
            for (MerchantPlaceCabinetBind placeCabinetBind : cabinetBindList) {
                Long bindTime = placeCabinetBind.getBindTime();
    
                if (bindTime < merchantPlaceBindTime) {
                    placeCabinetBind.setBindTime(merchantPlaceBindTime);
                }
    
                // 给绑定状态记录赋值解绑时间：merchantPlaceUnbindTime
                placeCabinetBind.setUnBindTime(merchantPlaceUnbindTime);
    
                allPlaceCabinetBindList.add(placeCabinetBind);
            }
        }
        
        // 解绑状态记录
        MerchantPlaceCabinetConditionRequest placeCabinetUnbindRequest = MerchantPlaceCabinetConditionRequest.builder().placeId(placeId)
                .status(MerchantPlaceCabinetBindConstant.STATUS_UNBIND).build();
        List<MerchantPlaceCabinetBind> cabinetUnbindList = merchantPlaceCabinetBindService.listUnbindRecord(placeCabinetUnbindRequest);
        
        if (CollectionUtils.isNotEmpty(cabinetUnbindList)) {
            allPlaceCabinetBindList.addAll(cabinetUnbindList);
            
            // 先掐头去尾
            List<MerchantPlaceCabinetBind> stepOneList = new ArrayList<>();
            for (MerchantPlaceCabinetBind placeCabinetBind : allPlaceCabinetBindList) {
                Long bindTime = placeCabinetBind.getBindTime();
                Long unBindTime = placeCabinetBind.getUnBindTime();
                
                if (unBindTime < merchantPlaceBindTime) {
                    continue;
                }
                
                if (bindTime < merchantPlaceBindTime) {
                    placeCabinetBind.setBindTime(merchantPlaceBindTime);
                }
                
                if (unBindTime > merchantPlaceUnbindTime) {
                    placeCabinetBind.setUnBindTime(merchantPlaceUnbindTime);
                }
                
                stepOneList.add(placeCabinetBind);
            }
            
            // 再去除时间段子集
            for (MerchantPlaceCabinetBind current : stepOneList) {
                boolean isSubset = false;
                
                for (MerchantPlaceCabinetBind previous : resultList) {
                    if (current.getBindTime() >= (previous.getBindTime()) && current.getUnBindTime() <= (previous.getUnBindTime())) {
                        isSubset = true;
                        break;
                    }
                }
                
                if (!isSubset) {
                    resultList.add(current);
                }
            }
        }
        
        return resultList;
    }
    
    private List<MerchantPlaceCabinetBind> getThisMonthPlaceCabinetBindList(Long placeId, Long merchantPlaceBindTime, Long merchantPlaceUnbindTime) {
        List<MerchantPlaceCabinetBind> resultList = new ArrayList<>();
        List<MerchantPlaceCabinetBind> allPlaceCabinetBindList = new ArrayList<>();
        
        // 绑定状态记录
        MerchantPlaceCabinetConditionRequest placeCabinetBindRequest = MerchantPlaceCabinetConditionRequest.builder().placeId(placeId)
                .status(MerchantPlaceCabinetBindConstant.STATUS_BIND).build();
        List<MerchantPlaceCabinetBind> cabinetBindList = merchantPlaceCabinetBindService.listBindRecord(placeCabinetBindRequest);
        
        if (CollectionUtils.isNotEmpty(cabinetBindList)) {
            for (MerchantPlaceCabinetBind placeCabinetBind : cabinetBindList) {
                Long bindTime = placeCabinetBind.getBindTime();
    
                if (bindTime < merchantPlaceBindTime) {
                    placeCabinetBind.setBindTime(merchantPlaceBindTime);
                }
    
                // 给绑定状态记录赋值解绑时间：endTime
                placeCabinetBind.setUnBindTime(merchantPlaceUnbindTime);
    
                allPlaceCabinetBindList.add(placeCabinetBind);
            }
        }
        
        // 解绑状态记录
        MerchantPlaceCabinetConditionRequest placeCabinetUnbindRequest = MerchantPlaceCabinetConditionRequest.builder().placeId(placeId)
                .status(MerchantPlaceCabinetBindConstant.STATUS_UNBIND).build();
        List<MerchantPlaceCabinetBind> cabinetUnbindList = merchantPlaceCabinetBindService.listUnbindRecord(placeCabinetUnbindRequest);
        
        if (CollectionUtils.isNotEmpty(cabinetUnbindList)) {
            allPlaceCabinetBindList.addAll(cabinetUnbindList);
            
            // 先掐头去尾
            List<MerchantPlaceCabinetBind> stepOneList = new ArrayList<>();
            for (MerchantPlaceCabinetBind placeCabinetBind : allPlaceCabinetBindList) {
                Long bindTime = placeCabinetBind.getBindTime();
                Long unBindTime = placeCabinetBind.getUnBindTime();
                
                if (unBindTime < merchantPlaceBindTime) {
                    continue;
                }
                
                if (bindTime < merchantPlaceBindTime) {
                    placeCabinetBind.setBindTime(merchantPlaceBindTime);
                }
                
                stepOneList.add(placeCabinetBind);
            }
            
            // 再去除时间段子集
            for (MerchantPlaceCabinetBind current : stepOneList) {
                boolean isSubset = false;
                
                for (MerchantPlaceCabinetBind previous : resultList) {
                    if (current.getBindTime() >= (previous.getBindTime()) && current.getUnBindTime() <= (previous.getUnBindTime())) {
                        isSubset = true;
                        break;
                    }
                }
                
                if (!isSubset) {
                    resultList.add(current);
                }
            }
        }
        
        return resultList;
    }
    
    private List<MerchantPlaceCabinetBind> getThisMonthPlaceCabinetBindListForCabinetDetail(Long cabinetId, Long todayStartTime, Long nowTime) {
        List<MerchantPlaceCabinetBind> resultList = new ArrayList<>();
        List<MerchantPlaceCabinetBind> allPlaceCabinetBindList = new ArrayList<>();
        
        // 绑定状态记录
        MerchantPlaceCabinetConditionRequest placeCabinetBindRequest = MerchantPlaceCabinetConditionRequest.builder().cabinetIds(Set.of(cabinetId))
                .status(MerchantPlaceCabinetBindConstant.STATUS_BIND).build();
        List<MerchantPlaceCabinetBind> cabinetBindList = merchantPlaceCabinetBindService.listBindRecord(placeCabinetBindRequest);
        
        if (CollectionUtils.isNotEmpty(cabinetBindList)) {
            for (MerchantPlaceCabinetBind placeCabinetBind : cabinetBindList) {
                Long bindTime = placeCabinetBind.getBindTime();
    
                if (bindTime < todayStartTime) {
                    placeCabinetBind.setBindTime(todayStartTime);
                }
    
                // 给绑定状态记录赋值解绑时间：nowTime
                placeCabinetBind.setUnBindTime(nowTime);
    
                allPlaceCabinetBindList.add(placeCabinetBind);
            }
        }
        
        // 解绑状态记录
        MerchantPlaceCabinetConditionRequest placeCabinetUnbindRequest = MerchantPlaceCabinetConditionRequest.builder().cabinetIds(Set.of(cabinetId))
                .status(MerchantPlaceCabinetBindConstant.STATUS_UNBIND).build();
        List<MerchantPlaceCabinetBind> cabinetUnbindList = merchantPlaceCabinetBindService.listUnbindRecord(placeCabinetUnbindRequest);
        
        if (CollectionUtils.isNotEmpty(cabinetUnbindList)) {
            allPlaceCabinetBindList.addAll(cabinetUnbindList);
            
            // 先掐头去尾
            List<MerchantPlaceCabinetBind> stepOneList = new ArrayList<>();
            for (MerchantPlaceCabinetBind placeCabinetBind : allPlaceCabinetBindList) {
                Long bindTime = placeCabinetBind.getBindTime();
                Long unBindTime = placeCabinetBind.getUnBindTime();
                
                if (unBindTime < todayStartTime) {
                    continue;
                }
                
                if (bindTime < todayStartTime) {
                    placeCabinetBind.setBindTime(todayStartTime);
                }
                
                stepOneList.add(placeCabinetBind);
            }
            
            // 再去除时间段子集
            for (MerchantPlaceCabinetBind current : stepOneList) {
                boolean isSubset = false;
                
                for (MerchantPlaceCabinetBind previous : resultList) {
                    if (current.getBindTime() >= (previous.getBindTime()) && current.getUnBindTime() <= (previous.getUnBindTime())) {
                        isSubset = true;
                        break;
                    }
                }
                
                if (!isSubset) {
                    resultList.add(current);
                }
            }
        }
        
        return resultList;
    }
    
    private List<MerchantPlaceCabinetBind> getLastMonthPlaceCabinetBindList(Long placeId, Long merchantPlaceBindTime, Long merchantPlaceUnbindTime) {
        List<MerchantPlaceCabinetBind> resultList = new ArrayList<>();
        List<MerchantPlaceCabinetBind> allPlaceCabinetBindList = new ArrayList<>();
        
        // 绑定状态记录((bindTime<=merchantPlaceUnbindTime))
        MerchantPlaceCabinetConditionRequest placeCabinetBindRequest = MerchantPlaceCabinetConditionRequest.builder().placeId(placeId)
                .status(MerchantPlaceCabinetBindConstant.STATUS_BIND).startTime(merchantPlaceUnbindTime).build();
        List<MerchantPlaceCabinetBind> cabinetBindList = merchantPlaceCabinetBindService.listBindRecord(placeCabinetBindRequest);
        
        if (CollectionUtils.isNotEmpty(cabinetBindList)) {
            for (MerchantPlaceCabinetBind placeCabinetBind : cabinetBindList) {
                Long bindTime = placeCabinetBind.getBindTime();
                
                if (bindTime < merchantPlaceBindTime) {
                    placeCabinetBind.setBindTime(merchantPlaceBindTime);
                }
                
                // 给绑定状态记录赋值解绑时间：endTime
                placeCabinetBind.setUnBindTime(merchantPlaceUnbindTime);
                
                allPlaceCabinetBindList.add(placeCabinetBind);
            }
        }
        
        // 解绑状态记录
        MerchantPlaceCabinetConditionRequest placeCabinetUnbindRequest = MerchantPlaceCabinetConditionRequest.builder().placeId(placeId)
                .status(MerchantPlaceCabinetBindConstant.STATUS_UNBIND).build();
        List<MerchantPlaceCabinetBind> cabinetUnbindList = merchantPlaceCabinetBindService.listUnbindRecord(placeCabinetUnbindRequest);
        
        if (CollectionUtils.isNotEmpty(cabinetUnbindList)) {
            allPlaceCabinetBindList.addAll(cabinetUnbindList);
            
            // 先掐头去尾
            List<MerchantPlaceCabinetBind> stepOneList = new ArrayList<>();
            for (MerchantPlaceCabinetBind placeCabinetBind : allPlaceCabinetBindList) {
                Long bindTime = placeCabinetBind.getBindTime();
                Long unBindTime = placeCabinetBind.getUnBindTime();
                
                if (unBindTime < merchantPlaceBindTime) {
                    continue;
                }
                
                if (bindTime < merchantPlaceBindTime) {
                    placeCabinetBind.setBindTime(merchantPlaceBindTime);
                }
                
                if (unBindTime > merchantPlaceUnbindTime) {
                    placeCabinetBind.setUnBindTime(merchantPlaceUnbindTime);
                }
                
                stepOneList.add(placeCabinetBind);
            }
            
            // 再去除时间段子集
            for (MerchantPlaceCabinetBind current : stepOneList) {
                boolean isSubset = false;
                
                for (MerchantPlaceCabinetBind previous : resultList) {
                    if (current.getBindTime() >= (previous.getBindTime()) && current.getUnBindTime() <= (previous.getUnBindTime())) {
                        isSubset = true;
                        break;
                    }
                }
                
                if (!isSubset) {
                    resultList.add(current);
                }
            }
        }
        
        return resultList;
    }
    
    private List<MerchantPlaceBind> getTodayMerchantPlaceBindList(Long merchantId, Long todayStartTime, Long nowTime) {
        List<MerchantPlaceBind> resultList = new ArrayList<>();
        
        // 绑定状态记录
        MerchantPlaceConditionRequest merchantPlaceBindRequest = MerchantPlaceConditionRequest.builder().merchantId(merchantId).status(MerchantPlaceBindConstant.BIND).build();
        List<MerchantPlaceBind> merchantPlaceBindList = merchantPlaceBindService.listBindRecord(merchantPlaceBindRequest);
        
        if (CollectionUtils.isNotEmpty(merchantPlaceBindList)) {
            for (MerchantPlaceBind merchantPlaceBind : merchantPlaceBindList) {
                Long bindTime = merchantPlaceBind.getBindTime();
                
                if (bindTime < todayStartTime) {
                    merchantPlaceBind.setBindTime(todayStartTime);
                }
                
                // 给绑定状态记录赋值解绑时间：nowTime
                merchantPlaceBind.setUnBindTime(nowTime);
                
                resultList.add(merchantPlaceBind);
            }
        }
        
        // 解绑状态记录
        MerchantPlaceConditionRequest merchantPlaceUnbindRequest = MerchantPlaceConditionRequest.builder().merchantId(merchantId).status(MerchantPlaceBindConstant.UN_BIND).build();
        List<MerchantPlaceBind> merchantPlaceUnbindList = merchantPlaceBindService.listUnbindRecord(merchantPlaceUnbindRequest);
        
        if (CollectionUtils.isNotEmpty(merchantPlaceUnbindList)) {
            
            for (MerchantPlaceBind merchantPlaceUnbind : merchantPlaceUnbindList) {
                Long bindTime = merchantPlaceUnbind.getBindTime();
                Long unBindTime = merchantPlaceUnbind.getUnBindTime();
                
                if (unBindTime < todayStartTime) {
                    continue;
                }
                
                if (bindTime < todayStartTime) {
                    merchantPlaceUnbind.setBindTime(todayStartTime);
                }
                
                resultList.add(merchantPlaceUnbind);
            }
        }
        
        return resultList;
    }
    
    private List<MerchantPlaceBind> getYesterdayMerchantPlaceBindList(Long merchantId, Long yesterdayStartTime, Long yesterdayEndTime) {
        List<MerchantPlaceBind> resultList = new ArrayList<>();
        
        // 绑定状态记录
        MerchantPlaceConditionRequest merchantPlaceBindRequest = MerchantPlaceConditionRequest.builder().merchantId(merchantId).status(MerchantPlaceBindConstant.BIND)
                .startTime(yesterdayEndTime).build();
        List<MerchantPlaceBind> merchantPlaceBindList = merchantPlaceBindService.listBindRecord(merchantPlaceBindRequest);
        
        if (CollectionUtils.isNotEmpty(merchantPlaceBindList)) {
            for (MerchantPlaceBind merchantPlaceBind : merchantPlaceBindList) {
                Long bindTime = merchantPlaceBind.getBindTime();
                
                if (bindTime < yesterdayStartTime) {
                    merchantPlaceBind.setBindTime(yesterdayStartTime);
                }
                
                // 给绑定状态记录赋值解绑时间：yesterdayEndTime
                merchantPlaceBind.setUnBindTime(yesterdayEndTime);
                
                resultList.add(merchantPlaceBind);
            }
        }
        
        // 解绑状态记录
        MerchantPlaceConditionRequest merchantPlaceUnbindRequest = MerchantPlaceConditionRequest.builder().merchantId(merchantId).status(MerchantPlaceBindConstant.UN_BIND).build();
        List<MerchantPlaceBind> merchantPlaceUnbindList = merchantPlaceBindService.listUnbindRecord(merchantPlaceUnbindRequest);
        
        if (CollectionUtils.isNotEmpty(merchantPlaceUnbindList)) {
            
            for (MerchantPlaceBind merchantPlaceUnbind : merchantPlaceUnbindList) {
                Long bindTime = merchantPlaceUnbind.getBindTime();
                Long unBindTime = merchantPlaceUnbind.getUnBindTime();
                
                if (unBindTime < yesterdayStartTime) {
                    continue;
                }
                
                if (bindTime < yesterdayStartTime) {
                    merchantPlaceUnbind.setBindTime(yesterdayStartTime);
                }
                
                if (unBindTime > yesterdayEndTime) {
                    merchantPlaceUnbind.setUnBindTime(yesterdayEndTime);
                }
                
                resultList.add(merchantPlaceUnbind);
            }
        }
        
        return resultList;
    }
    
    private List<MerchantPlaceBind> getThisMonthMerchantPlaceBindList(Long merchantId, Long thisMonthStartTime, Long nowTime) {
        List<MerchantPlaceBind> resultList = new ArrayList<>();
        
        // 绑定状态记录
        MerchantPlaceConditionRequest merchantPlaceBindRequest = MerchantPlaceConditionRequest.builder().merchantId(merchantId).status(MerchantPlaceBindConstant.BIND).build();
        List<MerchantPlaceBind> merchantPlaceBindList = merchantPlaceBindService.listBindRecord(merchantPlaceBindRequest);
        
        if (CollectionUtils.isNotEmpty(merchantPlaceBindList)) {
            for (MerchantPlaceBind merchantPlaceBind : merchantPlaceBindList) {
                Long bindTime = merchantPlaceBind.getBindTime();
                
                if (bindTime < thisMonthStartTime) {
                    merchantPlaceBind.setBindTime(thisMonthStartTime);
                }
                
                // 给绑定状态记录赋值解绑时间：nowTime
                merchantPlaceBind.setUnBindTime(nowTime);
                
                resultList.add(merchantPlaceBind);
            }
        }
        
        // 解绑状态记录
        MerchantPlaceConditionRequest merchantPlaceUnbindRequest = MerchantPlaceConditionRequest.builder().merchantId(merchantId).status(MerchantPlaceBindConstant.UN_BIND).build();
        List<MerchantPlaceBind> merchantPlaceUnbindList = merchantPlaceBindService.listUnbindRecord(merchantPlaceUnbindRequest);
        
        if (CollectionUtils.isNotEmpty(merchantPlaceUnbindList)) {
            
            for (MerchantPlaceBind merchantPlaceUnbind : merchantPlaceUnbindList) {
                Long bindTime = merchantPlaceUnbind.getBindTime();
                Long unBindTime = merchantPlaceUnbind.getUnBindTime();
                
                if (unBindTime < thisMonthStartTime) {
                    continue;
                }
                
                if (bindTime < thisMonthStartTime) {
                    merchantPlaceUnbind.setBindTime(thisMonthStartTime);
                }
                
                resultList.add(merchantPlaceUnbind);
            }
        }
        
        return resultList;
    }
    
    private List<MerchantPlaceBind> getLastMonthMerchantPlaceBindList(Long merchantId, Long lastMonthStartTime, Long lastMonthEndTime) {
        List<MerchantPlaceBind> resultList = new ArrayList<>();
        
        // 绑定状态记录(bindTime<=lastMonthStartTime)
        MerchantPlaceConditionRequest merchantPlaceBindRequest = MerchantPlaceConditionRequest.builder().merchantId(merchantId).status(MerchantPlaceBindConstant.BIND)
                .startTime(lastMonthStartTime).build();
        List<MerchantPlaceBind> merchantPlaceBindList = merchantPlaceBindService.listBindRecord(merchantPlaceBindRequest);
        
        if (CollectionUtils.isNotEmpty(merchantPlaceBindList)) {
            for (MerchantPlaceBind merchantPlaceBind : merchantPlaceBindList) {
                Long bindTime = merchantPlaceBind.getBindTime();
                
                if (bindTime < lastMonthStartTime) {
                    merchantPlaceBind.setBindTime(lastMonthStartTime);
                }
                
                // 给绑定状态记录赋值解绑时间：endTime
                merchantPlaceBind.setUnBindTime(lastMonthEndTime);
                
                resultList.add(merchantPlaceBind);
            }
        }
        
        // 解绑状态记录
        MerchantPlaceConditionRequest merchantPlaceUnbindRequest = MerchantPlaceConditionRequest.builder().merchantId(merchantId).status(MerchantPlaceBindConstant.UN_BIND).build();
        List<MerchantPlaceBind> merchantPlaceUnbindList = merchantPlaceBindService.listUnbindRecord(merchantPlaceUnbindRequest);
        
        if (CollectionUtils.isNotEmpty(merchantPlaceUnbindList)) {
            for (MerchantPlaceBind merchantPlaceUnbind : merchantPlaceUnbindList) {
                Long bindTime = merchantPlaceUnbind.getBindTime();
                Long unBindTime = merchantPlaceUnbind.getUnBindTime();
                
                if (unBindTime < lastMonthStartTime) {
                    continue;
                }
                
                if (bindTime < lastMonthStartTime) {
                    merchantPlaceUnbind.setBindTime(lastMonthStartTime);
                }
                
                if (unBindTime > lastMonthEndTime) {
                    merchantPlaceUnbind.setUnBindTime(lastMonthEndTime);
                }
                
                resultList.add(merchantPlaceUnbind);
            }
        }
        
        return resultList;
    }
    
    private MerchantPowerPeriodVO getTotalPower(Integer tenantId, Long merchantId, List<Long> cabinetIds) {
        //两个月前的数据（来源于历史表，定时任务）
        MerchantPowerPeriodVO preTwoMonthPower = merchantCabinetPowerMonthRecordProService.sumMonthPower(cabinetIds, null);
        
        // 上月数据
        MerchantPowerPeriodVO lastMonthPower = getLastMonthPower(tenantId, merchantId, cabinetIds);
        
        // 本月数据
        MerchantPowerPeriodVO thisMonthPower = getThisMonthPower(tenantId, merchantId, cabinetIds);
        
        MerchantPowerPeriodVO powerVO = new MerchantPowerPeriodVO();
        if (Objects.nonNull(preTwoMonthPower)) {
            powerVO.setPower(Objects.isNull(preTwoMonthPower.getPower()) ? NumberConstant.ZERO_D : preTwoMonthPower.getPower());
            powerVO.setCharge(Objects.isNull(preTwoMonthPower.getCharge()) ? NumberConstant.ZERO_D : preTwoMonthPower.getCharge());
        }
        if (Objects.nonNull(lastMonthPower)) {
            powerVO.setPower(Objects.isNull(lastMonthPower.getPower()) ? NumberConstant.ZERO_D : lastMonthPower.getPower());
            powerVO.setCharge(Objects.isNull(lastMonthPower.getCharge()) ? NumberConstant.ZERO_D : lastMonthPower.getCharge());
        }
        if (Objects.nonNull(thisMonthPower)) {
            powerVO.setPower(Objects.isNull(thisMonthPower.getPower()) ? NumberConstant.ZERO_D : thisMonthPower.getPower());
            powerVO.setCharge(Objects.isNull(thisMonthPower.getCharge()) ? NumberConstant.ZERO_D : thisMonthPower.getCharge());
        }
        
        return powerVO;
    }
    
    @Slave
    @Override
    public MerchantProPowerLineVO lineData(MerchantCabinetPowerRequest request) {
        // 查询月份
        List<String> monthList = getMonthList(request.getStartTime(), request.getEndTime());
        if (CollectionUtils.isEmpty(monthList)) {
            log.warn("Merchant power for pro lineData, monthList is empty, uid={}, startTime={}, endTime={}", request.getUid(), request.getStartTime(), request.getEndTime());
            return null;
        }
        
        // 初始化
        MerchantProPowerLineVO vo = new MerchantProPowerLineVO();
        List<MerchantProPowerLineDataVO> powerList = new ArrayList<>();
        List<MerchantProPowerChargeLineDataVO> chargeList = new ArrayList<>();
        
        for (String monthDate : monthList) {
            MerchantProPowerLineDataVO powerData = new MerchantProPowerLineDataVO();
            powerData.setMonthDate(monthDate);
            powerData.setPower(NumberConstant.ZERO_D);
            powerList.add(powerData);
            
            MerchantProPowerChargeLineDataVO chargeData = new MerchantProPowerChargeLineDataVO();
            chargeData.setMonthDate(monthDate);
            chargeData.setCharge(NumberConstant.ZERO_D);
            chargeList.add(chargeData);
        }
        
        vo.setPowerList(powerList);
        vo.setChargeList(chargeList);
        
        Merchant merchant = merchantService.queryByUid(request.getUid());
        if (Objects.isNull(merchant)) {
            log.warn("Merchant power for pro lineData, merchant not exist, uid={}", request.getUid());
            return vo;
        }
        
        request.setMerchantId(merchant.getId());
        
        //获取要查询的柜机
        List<Long> cabinetIds = getStaticsCabinetIds(request);
        if (CollectionUtils.isEmpty(cabinetIds)) {
            log.warn("Merchant power for pro lineData, cabinetIds is empty, uid={}", request.getUid());
            return vo;
        }
        
        // 上个月的数据需要实时获取，历史记录只统计到上上个月
        String lastMonthDate = DateUtils.getMonthDate(1L);
        boolean hasLastMonth = false;
        MerchantPowerPeriodVO lastMonthPower = null;
        if (monthList.contains(lastMonthDate)) {
            // 1.实时统计上个月的数据
            lastMonthPower = getLastMonthPower(TenantContextHolder.getTenantId(), request.getMerchantId(), cabinetIds);
            
            hasLastMonth = true;
            monthList.remove(lastMonthDate);
        }
        
        List<MerchantProPowerLineDataVO> powerList1 = vo.getPowerList();
        List<MerchantProPowerChargeLineDataVO> chargeList1 = vo.getChargeList();
        
        // 2.统计2个月前的历史数据
        for (String monthDate : monthList) {
            if (!monthDate.matches(DateUtils.GREP_YEAR_MONTH)) {
                log.warn("Merchant power for pro lineData, monthDate not correct, monthDate={}", monthDate);
                break;
            }
            
            monthDate = monthDate + "-01";
            
            MerchantPowerPeriodVO merchantPowerPeriodVO = merchantCabinetPowerMonthRecordProService.sumMonthPower(cabinetIds, List.of(monthDate));
            
            // 电量
            MerchantProPowerLineDataVO power = new MerchantProPowerLineDataVO();
            power.setMonthDate(monthDate);
            power.setPower(Objects.isNull(merchantPowerPeriodVO) ? NumberConstant.ZERO_D : merchantPowerPeriodVO.getPower());
            powerList1.add(power);
            
            // 电费
            MerchantProPowerChargeLineDataVO charge = new MerchantProPowerChargeLineDataVO();
            charge.setMonthDate(monthDate);
            charge.setCharge(Objects.isNull(merchantPowerPeriodVO) ? NumberConstant.ZERO_D : merchantPowerPeriodVO.getCharge());
            chargeList1.add(charge);
        }
        
        if (hasLastMonth) {
            // 电量
            MerchantProPowerLineDataVO power = new MerchantProPowerLineDataVO();
            power.setMonthDate(lastMonthDate);
            power.setPower(Objects.isNull(lastMonthPower) ? NumberConstant.ZERO_D : lastMonthPower.getPower());
            powerList1.add(power);
            
            // 电费
            MerchantProPowerChargeLineDataVO charge = new MerchantProPowerChargeLineDataVO();
            charge.setMonthDate(lastMonthDate);
            charge.setCharge(Objects.isNull(lastMonthPower) ? NumberConstant.ZERO_D : lastMonthPower.getCharge());
            chargeList1.add(charge);
        }
        
        vo.setPowerList(powerList);
        vo.setChargeList(chargeList);
        
        return vo;
    }
    
    @Slave
    @Override
    public List<MerchantProCabinetPowerDetailVO> cabinetPowerDetail(MerchantCabinetPowerRequest request) {
        Merchant merchant = merchantService.queryByUid(request.getUid());
        if (Objects.isNull(merchant)) {
            log.warn("Merchant power for pro cabinetPowerDetail, merchant not exist, uid={}", request.getUid());
            return null;
        }
        
        Integer tenantId = TenantContextHolder.getTenantId();
        if (Objects.nonNull(tenantId) && !Objects.equals(tenantId, merchant.getTenantId())) {
            log.warn("Merchant power for pro error, tenant not exist, uid={}", request.getUid());
            return null;
        }
        
        request.setMerchantId(merchant.getId());
        
        // 查询的柜机
        Long cabinetId = request.getCabinetId();
        ElectricityCabinet electricityCabinet = electricityCabinetService.queryByIdFromCache(cabinetId.intValue());
        if (Objects.isNull(electricityCabinet)) {
            log.warn("Merchant power for pro cabinetPowerDetail, cabinet not exist, cabinetId={}", cabinetId);
            return null;
        }
        
        //查询的月份
        String monthDate = request.getMonthDate();
        if (Objects.nonNull(monthDate)) {
            if (!monthDate.matches(DateUtils.GREP_YEAR_MONTH)) {
                return Collections.emptyList();
            }
        }
        
        String thisMonthDate = DateUtils.getMonthDate(NumberConstant.ZERO_L);
        String lastMonthDate = DateUtils.getMonthDate(NumberConstant.ONE_L);
        
        // 近2个月数据实时查
        // 如果是本月
        if (Objects.equals(thisMonthDate, monthDate)) {
            // 本月第一天0点
            Long thisMonthStartTime = DateUtils.getBeforeMonthFirstDayTimestamp(NumberConstant.ZERO);
            long nowTime = System.currentTimeMillis();
            
            return getLiveMonthPowerForCabinetDetail(tenantId, merchant.getId(), cabinetId, monthDate, thisMonthStartTime, nowTime);
            
        } else if (Objects.equals(lastMonthDate, monthDate)) {
            // 如果是上月
            //上月第一天0点
            Long lastMonthStartTime = DateUtils.getBeforeMonthFirstDayTimestamp(NumberConstant.ONE);
            // 上月最后一天23:59:59
            long lastMonthEndTime = DateUtils.getBeforeMonthLastDayTimestamp(NumberConstant.ONE);
            
            return getLiveMonthPowerForCabinetDetail(tenantId, merchant.getId(), cabinetId, monthDate, lastMonthStartTime, lastMonthEndTime);
            
        } else {
            // 如果是2个月前,通过历史数据查询
            List<MerchantCabinetPowerMonthDetailPro> preTwoMonthPowerDetailList = merchantCabinetPowerMonthDetailProService.listByMonth(cabinetId, List.of(monthDate));
            
            if (CollectionUtils.isEmpty(preTwoMonthPowerDetailList)) {
                return Collections.emptyList();
            }
            
            return preTwoMonthPowerDetailList.parallelStream().map(detailPro -> {
                Long startTime = detailPro.getBeginTime();
                Long endTime = detailPro.getEndTime();
                Long eid = detailPro.getEid();
                Long placeId = detailPro.getPlaceId();
                Integer tenantId1 = detailPro.getTenantId();
                Integer cabinetMerchantBindStatus = detailPro.getCabinetMerchantBindStatus();
                
                return getCabinetPowerDetail(startTime, endTime, eid, placeId, tenantId1, monthDate, cabinetMerchantBindStatus);
                
            }).collect(Collectors.toList());
        }
    }
    
    private List<String> getMonthList(Long startTime, Long endTime) {
        if (Objects.isNull(startTime) || Objects.isNull(endTime)) {
            return Collections.emptyList();
        }
        List<String> list = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        
        calendar.setTimeInMillis(startTime);
        do {
            String month = DateUtil.format(calendar.getTime(), "yyyy-MM");
            list.add(month);
            calendar.add(Calendar.MONTH, 1);
        } while (calendar.getTimeInMillis() <= endTime);
        String endMonth = DateUtil.format(new Date(endTime), "yyyy-MM");
        if (!list.contains(endMonth)) {
            list.add(endMonth);
        }
        return list;
    }
    
    @Slave
    @Override
    public List<MerchantProCabinetPowerVO> cabinetPowerList(MerchantCabinetPowerRequest request) {
        Merchant merchant = merchantService.queryByUid(request.getUid());
        if (Objects.isNull(merchant)) {
            log.warn("Merchant power for pro cabinetPowerList, merchant not exist, uid={}", request.getUid());
            return null;
        }
        
        Integer tenantId = TenantContextHolder.getTenantId();
        if (Objects.nonNull(tenantId) && !Objects.equals(tenantId, merchant.getTenantId())) {
            log.warn("Merchant power for pro error, tenant not exist, uid={}", request.getUid());
            return null;
        }
        
        request.setMerchantId(merchant.getId());
        
        //获取要查询的柜机
        List<Long> cabinetIds = getStaticsCabinetIds(request);
        if (CollectionUtils.isEmpty(cabinetIds)) {
            log.warn("Merchant power for pro lineData, cabinetIds is empty, uid={}", request.getUid());
            return null;
        }
        
        List<MerchantProCabinetPowerVO> cabinetPowerList = new ArrayList<>();
        
        // 本年电量
        List<String> monthList = DateUtils.getMonthsUntilCurrent(NumberConstant.ZERO);
        if (CollectionUtils.isNotEmpty(monthList)) {
            // 所有柜机今日电量
            List<MerchantProLivePowerVO> todayPowerForCabinetList = getTodayPowerForCabinetList(tenantId, merchant.getId(), cabinetIds);
            // 所有柜机本月电量
            List<MerchantProLivePowerVO> thisMonthPowerList = getThisMonthPowerForCabinetList(tenantId, merchant.getId(), cabinetIds);
            
            // 所有柜机上月电量
            List<MerchantProLivePowerVO> lastMonthPowerList = new ArrayList<>();
            if (monthList.size() > NumberConstant.ONE) {
                lastMonthPowerList = getLastMonthPowerForCabinetList(tenantId, merchant.getId(), cabinetIds);
            }
            
            // 遍历柜机
            for (Long cabinetId : cabinetIds) {
                // 今日电量
                List<MerchantProLivePowerVO> todayPower = todayPowerForCabinetList.parallelStream().filter(e -> Objects.equals(e.getEid(), cabinetId)).collect(Collectors.toList());
                Double todayPowerSum = todayPower.stream().filter(Objects::nonNull).mapToDouble(MerchantProLivePowerVO::getPower).sum();
                Double todayChargeSum = todayPower.stream().filter(Objects::nonNull).mapToDouble(MerchantProLivePowerVO::getCharge).sum();
                
                // 本月电量
                List<MerchantProLivePowerVO> thisMonthPower = thisMonthPowerList.parallelStream().filter(e -> Objects.equals(e.getEid(), cabinetId)).collect(Collectors.toList());
                Double thisMonthPowerSum = thisMonthPower.stream().filter(Objects::nonNull).mapToDouble(MerchantProLivePowerVO::getPower).sum();
                Double thisMonthChargeSum = thisMonthPower.stream().filter(Objects::nonNull).mapToDouble(MerchantProLivePowerVO::getCharge).sum();
                
                // 上月电量
                List<MerchantProLivePowerVO> lastMonthPower = lastMonthPowerList.parallelStream().filter(e -> Objects.equals(e.getEid(), cabinetId)).collect(Collectors.toList());
                Double lastMonthPowerSum = lastMonthPower.stream().filter(Objects::nonNull).mapToDouble(MerchantProLivePowerVO::getPower).sum();
                Double lastMonthChargeSum = lastMonthPower.stream().filter(Objects::nonNull).mapToDouble(MerchantProLivePowerVO::getCharge).sum();
                
                // 本年电量
                double thisYearPower = thisMonthPowerSum + lastMonthPowerSum;
                double thisYearCharge = thisMonthChargeSum + lastMonthChargeSum;
                
                List<String> preTwoMonthList;
                if (monthList.size() > NumberConstant.TWO) {
                    List<String> subMonthList = monthList.subList(0, monthList.size() - NumberConstant.TWO);
                    preTwoMonthList = getPreTwoMonthList(subMonthList);
                    
                    MerchantPowerPeriodVO preTwoMonthPowerPeriod = merchantCabinetPowerMonthRecordProService.sumMonthPower(List.of(cabinetId), preTwoMonthList);
                    thisYearPower = thisYearPower + (Objects.isNull(preTwoMonthPowerPeriod) ? NumberConstant.ZERO_D : preTwoMonthPowerPeriod.getPower());
                    thisYearCharge = thisYearCharge + (Objects.isNull(preTwoMonthPowerPeriod) ? NumberConstant.ZERO_D : preTwoMonthPowerPeriod.getCharge());
                }
                
                //封装结果
                MerchantProCabinetPowerVO merchantProCabinetPowerVO = new MerchantProCabinetPowerVO();
                merchantProCabinetPowerVO.setTodayPower(todayPowerSum);
                merchantProCabinetPowerVO.setTodayCharge(todayChargeSum);
                merchantProCabinetPowerVO.setThisMonthPower(thisMonthPowerSum);
                merchantProCabinetPowerVO.setThisMonthCharge(thisMonthChargeSum);
                merchantProCabinetPowerVO.setThisYearPower(thisYearPower);
                merchantProCabinetPowerVO.setThisYearCharge(thisYearCharge);
                merchantProCabinetPowerVO.setTime(
                        Optional.ofNullable(electricityCabinetService.queryByIdFromCache(cabinetId.intValue()).getCreateTime()).orElse(NumberConstant.ZERO_L));
                
                cabinetPowerList.add(merchantProCabinetPowerVO);
            }
        }
        
        if (CollectionUtils.isEmpty(cabinetPowerList)) {
            return Collections.emptyList();
        }
        
        // cabinetPowerList 根据time进行倒叙排序
        cabinetPowerList.sort((o1, o2) -> o2.getTime().compareTo(o1.getTime()));
        
        return cabinetPowerList;
    }
    
    private List<String> getPreTwoMonthList(List<String> monthList) {
        List<String> preTwoMonthList = new ArrayList<>();
        for (int i = 1; i <= monthList.size(); i++) {
            preTwoMonthList.add(monthList.get(i - 1) + "-01");
        }
        return preTwoMonthList;
    }
    
    @Slave
    @Override
    public Integer isShowPowerPage(Long uid) {
        Merchant merchant = merchantService.queryByUid(uid);
        if (Objects.isNull(merchant)) {
            log.warn("Merchant power for pro isShowPowerPage, merchant not exist, uid={}", uid);
            return null;
        }
        List<MerchantPlaceBind> bindList = merchantPlaceBindService.listByMerchantId(merchant.getId(), null);
        
        if (CollectionUtils.isNotEmpty(bindList)) {
            return NumberConstant.ONE;
        }
        return NumberConstant.ZERO;
    }
    
    @Slave
    @Override
    public MerchantPlaceAndCabinetUserVO listPlaceAndCabinetByMerchantId(Long uid) {
        Merchant merchant = merchantService.queryByUid(uid);
        if (Objects.isNull(merchant)) {
            log.warn("Merchant power for pro listPlaceAndCabinetByMerchantId, merchant not exist, uid={}", uid);
            return null;
        }
        
        List<MerchantPlaceBind> bindList = merchantPlaceBindService.listByMerchantId(merchant.getId(), null);
        if (CollectionUtils.isEmpty(bindList)) {
            return null;
        }
        
        // 获取场地列表
        Set<Long> placeIdSet = bindList.parallelStream().map(MerchantPlaceBind::getPlaceId).collect(Collectors.toSet());
        List<MerchantPlaceSelectVO> placeList = placeIdSet.parallelStream().map(placeId -> {
            MerchantPlaceSelectVO merchantPlaceUserVO = new MerchantPlaceSelectVO();
            merchantPlaceUserVO.setPlaceId(placeId);
            merchantPlaceUserVO.setPlaceName(Optional.ofNullable(merchantPlaceService.queryByIdFromCache(placeId)).orElse(new MerchantPlace()).getName());
            
            return merchantPlaceUserVO;
        }).collect(Collectors.toList());
        
        List<MerchantPlaceCabinetVO> cabinetList = new ArrayList<>();
        List<MerchantPlaceCabinetBind> merchantPlaceCabinetBindList = merchantPlaceCabinetBindService.listByPlaceIds(placeIdSet);
        if (CollectionUtils.isNotEmpty(merchantPlaceCabinetBindList)) {
            Set<Long> cabinetIdSet = merchantPlaceCabinetBindList.parallelStream().map(MerchantPlaceCabinetBind::getCabinetId).collect(Collectors.toSet());
            cabinetIdSet.forEach(cabinetId -> {
                MerchantPlaceCabinetVO merchantPlaceCabinetVO = new MerchantPlaceCabinetVO();
                merchantPlaceCabinetVO.setCabinetId(cabinetId);
                merchantPlaceCabinetVO.setCabinetName(
                        Optional.ofNullable(electricityCabinetService.queryByIdFromCache(cabinetId.intValue())).orElse(new ElectricityCabinet()).getName());
                
                cabinetList.add(merchantPlaceCabinetVO);
            });
        }
        
        // 封装VO
        MerchantPlaceAndCabinetUserVO merchantPlaceAndCabinetUserVO = new MerchantPlaceAndCabinetUserVO();
        merchantPlaceAndCabinetUserVO.setPlaceList(CollectionUtils.isEmpty(placeList) ? Collections.emptyList() : placeList);
        merchantPlaceAndCabinetUserVO.setCabinetList(CollectionUtils.isEmpty(cabinetList) ? Collections.emptyList() : cabinetList);
        
        return merchantPlaceAndCabinetUserVO;
    }
    
    @Slave
    @Override
    public List<MerchantPlaceCabinetVO> listCabinetByPlaceId(Long uid, Long placeId) {
        Merchant merchant = merchantService.queryByUid(uid);
        if (Objects.isNull(merchant)) {
            log.warn("Merchant power for pro listCabinetByPlaceId, merchant not exist, uid={}", uid);
            return Collections.emptyList();
        }
        
        List<MerchantPlaceBind> bindList = merchantPlaceBindService.listByMerchantId(merchant.getId(), null);
        if (CollectionUtils.isEmpty(bindList)) {
            log.warn("Merchant power for pro listCabinetByPlaceId, bindList is empty, uid={}, placeId={}", uid, placeId);
            return Collections.emptyList();
        }
        
        // 判断所选场地是否存在
        Set<Long> placeIdSet = bindList.stream().map(MerchantPlaceBind::getPlaceId).collect(Collectors.toSet());
        if (CollectionUtils.isNotEmpty(placeIdSet) && !placeIdSet.contains(placeId)) {
            log.warn("Merchant power for pro listCabinetByPlaceId, place not exist, uid={}, placeId={}", uid, placeId);
            return Collections.emptyList();
        }
        
        List<MerchantPlaceCabinetVO> cabinetList = new ArrayList<>();
        List<MerchantPlaceCabinetBind> merchantPlaceCabinetBindList = merchantPlaceCabinetBindService.listByPlaceIds(Set.of(placeId));
        if (CollectionUtils.isNotEmpty(merchantPlaceCabinetBindList)) {
            Set<Long> cabinetIdSet = merchantPlaceCabinetBindList.stream().map(MerchantPlaceCabinetBind::getCabinetId).collect(Collectors.toSet());
            cabinetIdSet.forEach(cabinetId -> {
                MerchantPlaceCabinetVO merchantPlaceCabinetVO = new MerchantPlaceCabinetVO();
                merchantPlaceCabinetVO.setCabinetId(cabinetId);
                merchantPlaceCabinetVO.setCabinetName(
                        Optional.ofNullable(electricityCabinetService.queryByIdFromCache(cabinetId.intValue())).orElse(new ElectricityCabinet()).getName());
                
                cabinetList.add(merchantPlaceCabinetVO);
            });
        }
        
        if (CollectionUtils.isEmpty(cabinetList)) {
            return Collections.emptyList();
        }
        
        return cabinetList;
    }
    
    @Slave
    @Override
    public List<Long> getStaticsCabinetIds(MerchantCabinetPowerRequest request) {
        Long uid = request.getUid();
        Long placeId = request.getPlaceId();
        Long cabinetId = request.getCabinetId();
        
        // 设置key
        String key = CacheConstant.MERCHANT_PLACE_CABINET_SEARCH_LOCK + uid;
        if (Objects.nonNull(placeId)) {
            key = key + placeId;
            if (Objects.nonNull(cabinetId)) {
                key = key + cabinetId;
            }
        }
        
        // 先从缓存获取，如果未获取到再从数据库获取
        List<Long> cabinetIdList = null;
        String cabinetIdStr = redisService.get(key);
        if (StringUtils.isNotBlank(cabinetIdStr)) {
            return JsonUtil.fromJsonArray(cabinetIdStr, Long.class);
        }
        
        // 1.场地和柜机为null，查全量
        if (Objects.isNull(placeId) && Objects.isNull(cabinetId)) {
            MerchantPlaceAndCabinetUserVO merchantPlaceAndCabinetUserVO = this.listPlaceAndCabinetByMerchantId(uid);
            if (Objects.isNull(merchantPlaceAndCabinetUserVO) || CollectionUtils.isEmpty(merchantPlaceAndCabinetUserVO.getCabinetList())) {
                return Collections.emptyList();
            }
            
            List<MerchantPlaceCabinetVO> cabinetList = merchantPlaceAndCabinetUserVO.getCabinetList();
            cabinetIdList = cabinetList.stream().map(MerchantPlaceCabinetVO::getCabinetId).distinct().collect(Collectors.toList());
        }
        
        // 2.场地不为null，柜机为null
        if (Objects.nonNull(placeId) && Objects.isNull(cabinetId)) {
            List<MerchantPlaceCabinetVO> placeCabinetVOList = this.listCabinetByPlaceId(uid, placeId);
            cabinetIdList = placeCabinetVOList.stream().map(MerchantPlaceCabinetVO::getCabinetId).distinct().collect(Collectors.toList());
        }
        
        // 3. 场地不为null,柜机不为null
        if (Objects.nonNull(placeId) && Objects.nonNull(cabinetId)) {
            List<MerchantPlaceCabinetVO> placeCabinetVOList = this.listCabinetByPlaceId(uid, placeId);
            cabinetIdList = placeCabinetVOList.stream().map(MerchantPlaceCabinetVO::getCabinetId).distinct().collect(Collectors.toList());
            
            if (CollectionUtils.isNotEmpty(cabinetIdList) && cabinetIdList.contains(cabinetId)) {
                cabinetIdList = List.of(cabinetId);
            }
        }
        
        // 存入缓存
        redisService.saveWithString(key, cabinetIdList, 3L, TimeUnit.SECONDS);
        
        return cabinetIdList;
    }
    
}

@Slf4j
class CabinetPowerProRunnable implements Callable<MerchantProEidPowerListVO> {
    
    Long eid;
    
    ElePowerService elePowerService;
    
    List<MerchantPlaceCabinetBind> bindList;
    
    Integer tenantId;
    
    public CabinetPowerProRunnable(Long eid, ElePowerService elePowerService, List<MerchantPlaceCabinetBind> bindList, Integer tenantId) {
        this.eid = eid;
        this.elePowerService = elePowerService;
        this.bindList = bindList;
        this.tenantId = tenantId;
    }
    
    @Override
    public MerchantProEidPowerListVO call() throws Exception {
        // 过滤出该柜机的绑定记录
        List<MerchantPlaceCabinetBind> cabinetBindList = bindList.stream().filter(bind -> Objects.equals(bind.getCabinetId(), eid)).collect(Collectors.toList());
        log.info("Merchant CabinetPowerProRunnable 开始执行...eid={}, cabinetBindList={}", eid, cabinetBindList);
        
        if (CollectionUtils.isEmpty(cabinetBindList)) {
            log.warn("Merchant CabinetPowerProRunnable cabinetBindList is empty, eid={}, cabinetBindList={}", eid, cabinetBindList);
            return null;
        }
        
        List<MerchantProLivePowerVO> elePowerList = new ArrayList<>();
        // 遍历绑定时间段
        cabinetBindList.forEach(cabinetBind -> {
            Long bindTime = cabinetBind.getBindTime();
            Long unBindTime = cabinetBind.getUnBindTime();
            
            // 查询电量
            EleSumPowerVO eleSumPowerVO = elePowerService.listByCondition(bindTime, unBindTime, List.of(eid), tenantId);
            // 封装数据
            MerchantProLivePowerVO powerVO = new MerchantProLivePowerVO();
            powerVO.setEid(eid);
            powerVO.setPlaceId(cabinetBind.getPlaceId());
            powerVO.setStartTime(bindTime);
            powerVO.setEndTime(unBindTime);
            powerVO.setPower(Objects.isNull(eleSumPowerVO) ? NumberConstant.ZERO_D : eleSumPowerVO.getSumPower());
            powerVO.setCharge(Objects.isNull(eleSumPowerVO) ? NumberConstant.ZERO_D : eleSumPowerVO.getSumCharge());
            
            elePowerList.add(powerVO);
        });
        
        MerchantProEidPowerListVO vo = new MerchantProEidPowerListVO();
        vo.setEid(eid);
        vo.setPowerList(elePowerList);
        
        log.info("Merchant CabinetPowerProRunnable 执行完毕...eid={}, vo={}", eid, vo);
        
        return vo;
    }
    
}
