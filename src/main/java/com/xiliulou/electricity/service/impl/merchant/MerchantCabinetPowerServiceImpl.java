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
import com.xiliulou.electricity.utils.DateUtils;
import com.xiliulou.electricity.vo.EleSumPowerVO;
import com.xiliulou.electricity.vo.merchant.MerchantPlaceAndCabinetUserVO;
import com.xiliulou.electricity.vo.merchant.MerchantPlaceCabinetVO;
import com.xiliulou.electricity.vo.merchant.MerchantPlaceSelectVO;
import com.xiliulou.electricity.vo.merchant.MerchantPowerPeriodVO;
import com.xiliulou.electricity.vo.merchant.MerchantProCabinetPowerDetailVO;
import com.xiliulou.electricity.vo.merchant.MerchantProCabinetPowerVO;
import com.xiliulou.electricity.vo.merchant.MerchantProLivePowerVO;
import com.xiliulou.electricity.vo.merchant.MerchantProPowerChargeLineDataVO;
import com.xiliulou.electricity.vo.merchant.MerchantProPowerDetailVO;
import com.xiliulou.electricity.vo.merchant.MerchantProPowerLineDataVO;
import com.xiliulou.electricity.vo.merchant.MerchantProPowerLineVO;
import com.xiliulou.electricity.vo.merchant.MerchantProPowerVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
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
    private RedisService redisService;
    
    @Resource
    private MerchantCabinetPowerMonthRecordProService merchantCabinetPowerMonthRecordProService;
    
    @Resource
    private MerchantCabinetPowerMonthDetailProService merchantCabinetPowerMonthDetailProService;
    
    @Resource
    private MerchantService merchantService;
    
    @Slave
    @Override
    public MerchantProPowerVO powerData(MerchantCabinetPowerRequest request) {
        Merchant merchant = merchantService.queryByUid(request.getUid());
        if (Objects.isNull(merchant)) {
            log.warn("Merchant power for pro todayPower, merchant not exist, uid={}", request.getUid());
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
            MerchantPowerPeriodVO todayPower = getTodayPower(cabinetIds);
            vo.setTodayPower(todayPower.getPower());
            vo.setTodayCharge(todayPower.getCharge());
            
        }, executorService).exceptionally(e -> {
            log.error("Query merchant today power data error! uid={}", request.getUid(), e);
            return null;
        });
        
        // 2.昨日电量
        CompletableFuture<Void> yesterdayPowerFuture = CompletableFuture.runAsync(() -> {
            MerchantPowerPeriodVO yesterdayPower = getYesterdayPower(cabinetIds);
            vo.setYesterdayPower(yesterdayPower.getPower());
            vo.setYesterdayCharge(yesterdayPower.getCharge());
            
        }, executorService).exceptionally(e -> {
            log.error("Query merchant yesterday power data error! uid={}", request.getUid(), e);
            return null;
        });
        
        // 3.本月电量
        CompletableFuture<Void> thisMonthPowerFuture = CompletableFuture.runAsync(() -> {
            MerchantPowerPeriodVO thisMonthPower = getThisMonthPower(merchant.getTenantId(), merchant.getId(), cabinetIds);
            vo.setThisMonthPower(thisMonthPower.getPower());
            vo.setThisMonthCharge(thisMonthPower.getCharge());
            
        }, executorService).exceptionally(e -> {
            log.error("Query merchant this month power data error! uid={}", request.getUid(), e);
            return null;
        });
        
        // 4.上月电量
        CompletableFuture<Void> lastMonthPowerFuture = CompletableFuture.runAsync(() -> {
            MerchantPowerPeriodVO lastMonthPower = getLastMonthPower(merchant.getTenantId(), merchant.getId(), cabinetIds);
            
            vo.setLastMonthPower(Objects.isNull(lastMonthPower) ? NumberConstant.ZERO_D : lastMonthPower.getPower());
            vo.setLastMonthCharge(Objects.isNull(lastMonthPower) ? NumberConstant.ZERO_D : lastMonthPower.getCharge());
            
        }, executorService).exceptionally(e -> {
            log.error("Query merchant last month power data error! uid={}", request.getUid(), e);
            return null;
        });
        
        // 5.累计电量
        CompletableFuture<Void> totalPowerFuture = CompletableFuture.runAsync(() -> {
            MerchantPowerPeriodVO totalPower = getTotalPower(cabinetIds);
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
    
    private MerchantPowerPeriodVO getTodayPower(List<Long> cabinetIds) {
        // 今日0点
        Long todayStartTime = DateUtils.getTimeAgoStartTime(NumberConstant.ZERO);
        // 当前时间
        long todayEndTime = System.currentTimeMillis();
        
        // 执行查询
        MerchantProLivePowerVO livePowerVO = getLivePowerData(cabinetIds, todayStartTime, todayEndTime, "today");
        
        MerchantPowerPeriodVO todayVO = new MerchantPowerPeriodVO();
        todayVO.setPower(Objects.isNull(livePowerVO) ? NumberConstant.ZERO_D : livePowerVO.getPower());
        todayVO.setCharge(Objects.isNull(livePowerVO) ? NumberConstant.ZERO_D : livePowerVO.getCharge());
        
        return todayVO;
    }
    
    private MerchantPowerPeriodVO getYesterdayPower(List<Long> cabinetIds) {
        // 昨日0点
        Long yesterdayStartTime = DateUtils.getTimeAgoStartTime(NumberConstant.ONE);
        // 昨日23:59:59
        long yesterdayEndTime = DateUtils.getTimeAgoEndTime(NumberConstant.ONE);
        
        // 执行查询
        MerchantProLivePowerVO livePowerVO = getLivePowerData(cabinetIds, yesterdayStartTime, yesterdayEndTime, "yesterday");
        
        MerchantPowerPeriodVO yesterdayVO = new MerchantPowerPeriodVO();
        yesterdayVO.setPower(Objects.isNull(livePowerVO) ? NumberConstant.ZERO_D : livePowerVO.getPower());
        yesterdayVO.setCharge(Objects.isNull(livePowerVO) ? NumberConstant.ZERO_D : livePowerVO.getCharge());
        
        return yesterdayVO;
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
        List<MerchantPowerPeriodVO> resultList = new ArrayList<>();
        
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
                List<Future<MerchantPowerPeriodVO>> futureList = executorService.invokeAll(collect);
                if (CollectionUtils.isNotEmpty(futureList)) {
                    for (Future<MerchantPowerPeriodVO> future : futureList) {
                        MerchantPowerPeriodVO result = future.get();
                        resultList.add(result);
                    }
                }
            } catch (InterruptedException | ExecutionException e) {
                log.error("Merchant get lastMonth power for pro Exception occur!", e);
            }
        }
        
        // 执行查询
        MerchantProLivePowerVO livePowerVO = getLivePowerData(cabinetIds, thisMonthStartTime, System.currentTimeMillis(), "thisMonth");
        
        MerchantPowerPeriodVO thisMonthVO = new MerchantPowerPeriodVO();
        thisMonthVO.setPower(Objects.isNull(livePowerVO) ? NumberConstant.ZERO_D : livePowerVO.getPower());
        thisMonthVO.setCharge(Objects.isNull(livePowerVO) ? NumberConstant.ZERO_D : livePowerVO.getCharge());
        
        return thisMonthVO;
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
        List<MerchantPowerPeriodVO> resultList = new ArrayList<>();
        
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
                List<Future<MerchantPowerPeriodVO>> futureList = executorService.invokeAll(collect);
                if (CollectionUtils.isNotEmpty(futureList)) {
                    for (Future<MerchantPowerPeriodVO> future : futureList) {
                        MerchantPowerPeriodVO result = future.get();
                        resultList.add(result);
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
        powerVO.setPower(resultList.stream().mapToDouble(MerchantPowerPeriodVO::getPower).sum());
        powerVO.setCharge(resultList.stream().mapToDouble(MerchantPowerPeriodVO::getCharge).sum());
        
        return powerVO;
    }
    
    private List<MerchantPlaceCabinetBind> getThisMonthPlaceCabinetBindList(Long placeId, Long merchantPlaceBindTime, Long merchantPlaceUnbindTime) {
        List<MerchantPlaceCabinetBind> allPlaceCabinetBindList = new ArrayList<>();
        List<MerchantPlaceCabinetBind> beforeHandleAllPlaceCabinetBindList = new ArrayList<>();
        
        // 绑定状态记录((bindTime<=merchantPlaceUnbindTime))
        MerchantPlaceCabinetConditionRequest placeCabinetBindRequest = MerchantPlaceCabinetConditionRequest.builder().placeId(placeId)
                .status(MerchantPlaceCabinetBindConstant.STATUS_BIND).startTime(merchantPlaceUnbindTime).build();
        List<MerchantPlaceCabinetBind> cabinetBindList = merchantPlaceCabinetBindService.listBindRecord(placeCabinetBindRequest);
        
        if (CollectionUtils.isNotEmpty(cabinetBindList)) {
            MerchantPlaceCabinetBind placeCabinetBind = cabinetBindList.get(NumberConstant.ZERO);
            Long bindTime = placeCabinetBind.getBindTime();
            
            if (bindTime < merchantPlaceBindTime) {
                placeCabinetBind.setBindTime(merchantPlaceBindTime);
            }
            
            // 给绑定状态记录赋值解绑时间：endTime
            placeCabinetBind.setUnBindTime(merchantPlaceUnbindTime);
            
            beforeHandleAllPlaceCabinetBindList.add(placeCabinetBind);
        }
        
        // 解绑状态记录
        MerchantPlaceCabinetConditionRequest placeCabinetUnbindRequest = MerchantPlaceCabinetConditionRequest.builder().placeId(placeId)
                .status(MerchantPlaceCabinetBindConstant.STATUS_UNBIND).build();
        List<MerchantPlaceCabinetBind> cabinetUnbindList = merchantPlaceCabinetBindService.listUnbindRecord(placeCabinetUnbindRequest);
        
        if (CollectionUtils.isNotEmpty(cabinetUnbindList)) {
            beforeHandleAllPlaceCabinetBindList.addAll(cabinetUnbindList);
            
            // 先掐头去尾
            List<MerchantPlaceCabinetBind> stepOneList = new ArrayList<>();
            beforeHandleAllPlaceCabinetBindList.forEach(placeCabinetBind -> {
                Long bindTime = placeCabinetBind.getBindTime();
                Long unBindTime = placeCabinetBind.getUnBindTime();
                
                if (bindTime < merchantPlaceBindTime) {
                    placeCabinetBind.setBindTime(merchantPlaceBindTime);
                }
                
                if (unBindTime > merchantPlaceUnbindTime) {
                    placeCabinetBind.setUnBindTime(merchantPlaceUnbindTime);
                }
                
                stepOneList.add(placeCabinetBind);
            });
            
            // 再去除时间段子集
            for (MerchantPlaceCabinetBind current : cabinetUnbindList) {
                boolean isSubset = false;
                
                for (MerchantPlaceCabinetBind previous : stepOneList) {
                    if (current.getBindTime() >= (previous.getBindTime()) && current.getUnBindTime() <= (previous.getUnBindTime())) {
                        isSubset = true;
                        break;
                    }
                }
                
                if (!isSubset) {
                    allPlaceCabinetBindList.add(current);
                }
            }
        }
        
        return allPlaceCabinetBindList;
    }
    
    private List<MerchantPlaceCabinetBind> getLastMonthPlaceCabinetBindList(Long placeId, Long merchantPlaceBindTime, Long merchantPlaceUnbindTime) {
        List<MerchantPlaceCabinetBind> allPlaceCabinetBindList = new ArrayList<>();
        List<MerchantPlaceCabinetBind> beforeHandleAllPlaceCabinetBindList = new ArrayList<>();
        
        // 绑定状态记录((bindTime<=merchantPlaceUnbindTime))
        MerchantPlaceCabinetConditionRequest placeCabinetBindRequest = MerchantPlaceCabinetConditionRequest.builder().placeId(placeId)
                .status(MerchantPlaceCabinetBindConstant.STATUS_BIND).startTime(merchantPlaceUnbindTime).build();
        List<MerchantPlaceCabinetBind> cabinetBindList = merchantPlaceCabinetBindService.listBindRecord(placeCabinetBindRequest);
        
        if (CollectionUtils.isNotEmpty(cabinetBindList)) {
            MerchantPlaceCabinetBind placeCabinetBind = cabinetBindList.get(NumberConstant.ZERO);
            Long bindTime = placeCabinetBind.getBindTime();
            
            if (bindTime < merchantPlaceBindTime) {
                placeCabinetBind.setBindTime(merchantPlaceBindTime);
            }
            
            // 给绑定状态记录赋值解绑时间：endTime
            placeCabinetBind.setUnBindTime(merchantPlaceUnbindTime);
            
            beforeHandleAllPlaceCabinetBindList.add(placeCabinetBind);
        }
        
        // 解绑状态记录
        MerchantPlaceCabinetConditionRequest placeCabinetUnbindRequest = MerchantPlaceCabinetConditionRequest.builder().placeId(placeId)
                .status(MerchantPlaceCabinetBindConstant.STATUS_UNBIND).build();
        List<MerchantPlaceCabinetBind> cabinetUnbindList = merchantPlaceCabinetBindService.listUnbindRecord(placeCabinetUnbindRequest);
        
        if (CollectionUtils.isNotEmpty(cabinetUnbindList)) {
            beforeHandleAllPlaceCabinetBindList.addAll(cabinetUnbindList);
            
            // 先掐头去尾
            List<MerchantPlaceCabinetBind> stepOneList = new ArrayList<>();
            beforeHandleAllPlaceCabinetBindList.forEach(placeCabinetBind -> {
                Long bindTime = placeCabinetBind.getBindTime();
                Long unBindTime = placeCabinetBind.getUnBindTime();
                
                if (bindTime < merchantPlaceBindTime) {
                    placeCabinetBind.setBindTime(merchantPlaceBindTime);
                }
                
                if (unBindTime > merchantPlaceUnbindTime) {
                    placeCabinetBind.setUnBindTime(merchantPlaceUnbindTime);
                }
                
                stepOneList.add(placeCabinetBind);
            });
            
            // 再去除时间段子集
            for (MerchantPlaceCabinetBind current : cabinetUnbindList) {
                boolean isSubset = false;
                
                for (MerchantPlaceCabinetBind previous : stepOneList) {
                    if (current.getBindTime() >= (previous.getBindTime()) && current.getUnBindTime() <= (previous.getUnBindTime())) {
                        isSubset = true;
                        break;
                    }
                }
                
                if (!isSubset) {
                    allPlaceCabinetBindList.add(current);
                }
            }
        }
        
        return allPlaceCabinetBindList;
    }
    
    private List<MerchantPlaceBind> getThisMonthMerchantPlaceBindList(Long merchantId, Long thisMonthStartTime, Long nowTime) {
        List<MerchantPlaceBind> allMerchantPlaceBindList = new ArrayList<>();
        
        // 绑定状态记录
        MerchantPlaceConditionRequest merchantPlaceBindRequest = MerchantPlaceConditionRequest.builder().merchantId(merchantId).status(MerchantPlaceBindConstant.BIND).build();
        List<MerchantPlaceBind> merchantPlaceBindList = merchantPlaceBindService.listBindRecord(merchantPlaceBindRequest);
        
        if (CollectionUtils.isNotEmpty(merchantPlaceBindList)) {
            MerchantPlaceBind merchantPlaceBind = merchantPlaceBindList.get(NumberConstant.ZERO);
            Long bindTime = merchantPlaceBind.getBindTime();
            
            if (bindTime < thisMonthStartTime) {
                merchantPlaceBind.setBindTime(thisMonthStartTime);
            }
            
            // 给绑定状态记录赋值解绑时间：nowTime
            merchantPlaceBind.setUnBindTime(nowTime);
            
            allMerchantPlaceBindList.add(merchantPlaceBind);
        }
        
        // 解绑状态记录
        MerchantPlaceConditionRequest merchantPlaceUnbindRequest = MerchantPlaceConditionRequest.builder().merchantId(merchantId).status(MerchantPlaceBindConstant.UN_BIND).build();
        List<MerchantPlaceBind> merchantPlaceUnbindList = merchantPlaceBindService.listUnbindRecord(merchantPlaceUnbindRequest);
        
        if (CollectionUtils.isNotEmpty(merchantPlaceUnbindList)) {
            merchantPlaceUnbindList.forEach(merchantPlaceUnbind -> {
                Long bindTime = merchantPlaceUnbind.getBindTime();
                
                if (bindTime < thisMonthStartTime) {
                    merchantPlaceUnbind.setBindTime(thisMonthStartTime);
                }
                
                allMerchantPlaceBindList.add(merchantPlaceUnbind);
            });
        }
        
        return allMerchantPlaceBindList;
    }
    
    private List<MerchantPlaceBind> getLastMonthMerchantPlaceBindList(Long merchantId, Long lastMonthStartTime, Long lastMonthEndTime) {
        List<MerchantPlaceBind> allMerchantPlaceBindList = new ArrayList<>();
        
        // 绑定状态记录(bindTime<=lastMonthStartTime)
        MerchantPlaceConditionRequest merchantPlaceBindRequest = MerchantPlaceConditionRequest.builder().merchantId(merchantId).status(MerchantPlaceBindConstant.BIND)
                .startTime(lastMonthStartTime).build();
        List<MerchantPlaceBind> merchantPlaceBindList = merchantPlaceBindService.listBindRecord(merchantPlaceBindRequest);
        
        if (CollectionUtils.isNotEmpty(merchantPlaceBindList)) {
            MerchantPlaceBind merchantPlaceBind = merchantPlaceBindList.get(NumberConstant.ZERO);
            Long bindTime = merchantPlaceBind.getBindTime();
            
            if (bindTime < lastMonthStartTime) {
                merchantPlaceBind.setBindTime(lastMonthStartTime);
            }
            
            // 给绑定状态记录赋值解绑时间：endTime
            merchantPlaceBind.setUnBindTime(lastMonthEndTime);
            
            allMerchantPlaceBindList.add(merchantPlaceBind);
        }
        
        // 解绑状态记录
        MerchantPlaceConditionRequest merchantPlaceUnbindRequest = MerchantPlaceConditionRequest.builder().merchantId(merchantId).status(MerchantPlaceBindConstant.UN_BIND).build();
        List<MerchantPlaceBind> merchantPlaceUnbindList = merchantPlaceBindService.listUnbindRecord(merchantPlaceUnbindRequest);
        
        if (CollectionUtils.isNotEmpty(merchantPlaceUnbindList)) {
            merchantPlaceUnbindList.forEach(merchantPlaceUnbind -> {
                Long bindTime = merchantPlaceUnbind.getBindTime();
                Long unBindTime = merchantPlaceUnbind.getUnBindTime();
                
                if (bindTime < lastMonthStartTime) {
                    merchantPlaceUnbind.setBindTime(lastMonthStartTime);
                }
                
                if (unBindTime > lastMonthEndTime) {
                    merchantPlaceUnbind.setUnBindTime(lastMonthEndTime);
                }
                
                allMerchantPlaceBindList.add(merchantPlaceUnbind);
            });
        }
        
        return allMerchantPlaceBindList;
    }
    
    private MerchantPowerPeriodVO getTotalPower(List<Long> cabinetIds) {
        //两个月前的数据（来源于历史表，定时任务）
        MerchantPowerPeriodVO preTwoMonthPowerVO = merchantCabinetPowerMonthRecordProService.sumMonthPower(cabinetIds, null);
        
        //上月第一天0点
        Long lastMonthStartTime = DateUtils.getBeforeMonthFirstDayTimestamp(NumberConstant.ONE);
        // 上月到当前的实时数据
        MerchantProLivePowerVO recentTwoMonthPowerVO = getLivePowerData(cabinetIds, lastMonthStartTime, System.currentTimeMillis(), "totalPower-recentlyTwoMonth");
        
        MerchantPowerPeriodVO totalPowerVO = new MerchantPowerPeriodVO();
        totalPowerVO.setPower(NumberConstant.ZERO_D);
        totalPowerVO.setCharge(NumberConstant.ZERO_D);
        
        if (Objects.nonNull(preTwoMonthPowerVO)) {
            totalPowerVO.setPower(totalPowerVO.getPower() + preTwoMonthPowerVO.getPower());
            totalPowerVO.setCharge(totalPowerVO.getCharge() + preTwoMonthPowerVO.getCharge());
        }
        if (Objects.nonNull(recentTwoMonthPowerVO)) {
            totalPowerVO.setPower(totalPowerVO.getPower() + recentTwoMonthPowerVO.getPower());
            totalPowerVO.setCharge(totalPowerVO.getCharge() + recentTwoMonthPowerVO.getCharge());
        }
        
        return totalPowerVO;
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
        monthList.remove(lastMonthDate);
        
        // 1.实时统计上个月的数据 todo
        //MerchantPowerPeriodVO lastMonthPower = this.getLastMonthPower(cabinetIds);
        MerchantPowerPeriodVO lastMonthPower = null;
        //电量
        MerchantProPowerLineDataVO lastMonthPowerVO = new MerchantProPowerLineDataVO();
        lastMonthPowerVO.setMonthDate(lastMonthDate);
        lastMonthPowerVO.setPower(Objects.isNull(lastMonthPower) ? NumberConstant.ZERO_D : lastMonthPower.getPower());
        powerList.add(lastMonthPowerVO);
        
        MerchantProPowerChargeLineDataVO lastMonthChargeVO = new MerchantProPowerChargeLineDataVO();
        lastMonthChargeVO.setMonthDate(lastMonthDate);
        lastMonthChargeVO.setCharge(Objects.isNull(lastMonthPower) ? NumberConstant.ZERO_D : lastMonthPower.getCharge());
        chargeList.add(lastMonthChargeVO);
        
        // 2.统计2个月前的历史数据
        for (String monthDate : monthList) {
            if (!monthDate.matches(DateUtils.GREP_YEAR_MONTH)) {
                log.warn("Merchant power for pro lineData, monthDate not correct, monthDate={}", monthDate);
                break;
            }
            
            monthDate = monthDate + "-01";
            
            MerchantPowerPeriodVO merchantPowerPeriodVO = merchantCabinetPowerMonthRecordProService.sumMonthPower(cabinetIds, List.of(monthDate));
            
            //电量
            MerchantProPowerLineDataVO power = new MerchantProPowerLineDataVO();
            power.setMonthDate(monthDate);
            power.setPower(Optional.ofNullable(merchantPowerPeriodVO.getCharge()).orElse(NumberConstant.ZERO_D));
            powerList.add(power);
            
            MerchantProPowerChargeLineDataVO charge = new MerchantProPowerChargeLineDataVO();
            charge.setMonthDate(monthDate);
            charge.setCharge(Optional.ofNullable(merchantPowerPeriodVO.getCharge()).orElse(NumberConstant.ZERO_D));
            chargeList.add(charge);
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
        
        request.setMerchantId(merchant.getId());
        
        //获取要查询的柜机
        List<Long> cabinetIds = getStaticsCabinetIds(request);
        if (CollectionUtils.isEmpty(cabinetIds)) {
            return Collections.emptyList();
        }
        
        String monthDate = request.getMonthDate();
        if (Objects.nonNull(monthDate)) {
            if (!monthDate.matches(DateUtils.GREP_YEAR_MONTH)) {
                return Collections.emptyList();
            }
        }
        
        Long cabinetId = request.getCabinetId();
        ElectricityCabinet electricityCabinet = electricityCabinetService.queryByIdFromCache(Math.toIntExact(cabinetId));
        if (Objects.isNull(electricityCabinet)) {
            log.warn("Merchant power for pro cabinetPowerDetail, cabinet not exist, cabinetId={}", cabinetId);
            return null;
        }
        
        String thisMonthDate = DateUtils.getMonthDate(NumberConstant.ZERO_L);
        String lastMonthDate = DateUtils.getMonthDate(NumberConstant.ONE_L);
        
        List<MerchantProCabinetPowerDetailVO> rspList;
        
        // 近2个月数据实时查
        // 如果是本月
        if (Objects.equals(thisMonthDate, monthDate)) {
            // 本月第一天0点
            Long thisMonthStartTime = DateUtils.getBeforeMonthFirstDayTimestamp(NumberConstant.ZERO);
            
            MerchantProLivePowerVO thisMonthPower = getLivePowerData(List.of(cabinetId), thisMonthStartTime, System.currentTimeMillis(), "cabinetPowerDetail-thisMonth");
            
            rspList = this.assembleLiveDetailPower(electricityCabinet, thisMonthPower, monthDate);
        } else if (Objects.equals(lastMonthDate, monthDate)) {
            // 如果是上月
            
            //上月第一天0点
            Long lastMonthStartTime = DateUtils.getBeforeMonthFirstDayTimestamp(NumberConstant.ONE);
            // 上月最后一天23:59:59
            long lastMonthEndTime = DateUtils.getBeforeMonthLastDayTimestamp(NumberConstant.ONE);
            
            MerchantProLivePowerVO lastMonthPower = getLivePowerData(List.of(cabinetId), lastMonthStartTime, lastMonthEndTime, "cabinetPowerDetail-lastMonth");
            
            rspList = this.assembleLiveDetailPower(electricityCabinet, lastMonthPower, monthDate);
        } else {
            // 如果是2个月前,通过历史数据查询
            List<MerchantCabinetPowerMonthDetailPro> historyDetailList = merchantCabinetPowerMonthDetailProService.listByMonth(cabinetId, List.of(monthDate));
            rspList = assembleHistoryDetailPower(electricityCabinet, historyDetailList, monthDate + "-01");
        }
        
        if (CollectionUtils.isEmpty(rspList)) {
            return Collections.emptyList();
        }
        
        return rspList;
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
        
        request.setMerchantId(merchant.getId());
        
        //获取要查询的柜机
        List<Long> cabinetIds = getStaticsCabinetIds(request);
        if (CollectionUtils.isEmpty(cabinetIds)) {
            log.warn("Merchant power for pro lineData, cabinetIds is empty, uid={}", request.getUid());
            return null;
        }
        
        List<MerchantProCabinetPowerVO> cabinetPowerList = new ArrayList<>();
        
        // 遍历柜机
        cabinetIds.forEach(cabinetId -> {
            // 今日0点
            Long todayStartTime = DateUtils.getTimeAgoStartTime(NumberConstant.ZERO);
            // 当前时间
            long todayEndTime = System.currentTimeMillis();
            // 今日电量/电费
            MerchantProLivePowerVO todayPower = getLivePowerData(List.of(cabinetId), todayStartTime, todayEndTime, "cabinetPowerList-today");
            
            // 本月第一天0点
            Long thisMonthStartTime = DateUtils.getBeforeMonthFirstDayTimestamp(NumberConstant.ZERO);
            // 本月电量/电费
            MerchantProLivePowerVO thisMonthPower = getLivePowerData(List.of(cabinetId), thisMonthStartTime, System.currentTimeMillis(), "cabinetPowerList-thisMonth");
            
            //上月第一天0点
            Long lastMonthStartTime = DateUtils.getBeforeMonthFirstDayTimestamp(NumberConstant.ONE);
            // 上月最后一天23:59:59
            long lastMonthEndTime = DateUtils.getBeforeMonthLastDayTimestamp(NumberConstant.ONE);
            MerchantProLivePowerVO lastMonthPower = getLivePowerData(List.of(cabinetId), lastMonthStartTime, lastMonthEndTime, "cabinetPowerList-lastMonth");
            
            // 获取历史数据查询的月份，比如：当前月份2024-02，要查的月份为2023-12、2023-11、2023-10、2023-09、2023-08、2023-07、2023-06、2023-05、2023-04、2023-03
            List<String> monthList = new ArrayList<>(10);
            for (int i = NumberConstant.TWO; i <= NumberConstant.TWELVE; i++) {
                monthList.add(DateUtils.getMonthDate((long) i) + "-01");
            }
            MerchantPowerPeriodVO merchantPowerPeriodVO = merchantCabinetPowerMonthRecordProService.sumMonthPower(List.of(cabinetId), monthList);
            Long historyLatestReportTime = merchantCabinetPowerMonthDetailProService.queryLatestReportTime(cabinetId, monthList);
            
            // 最新上报时间
            Long latestTime = NumberConstant.ZERO_L;
            // 本年电量、本年电费
            Double thisYearPower = NumberConstant.ZERO_D;
            Double thisYearCharge = NumberConstant.ZERO_D;
            if (Objects.nonNull(merchantPowerPeriodVO)) {
                thisYearPower += merchantPowerPeriodVO.getPower();
                thisYearCharge += merchantPowerPeriodVO.getCharge();
                latestTime = historyLatestReportTime;
            }
            if (Objects.nonNull(lastMonthPower)) {
                thisYearPower += lastMonthPower.getPower();
                thisYearCharge += lastMonthPower.getCharge();
                latestTime = lastMonthPower.getLatestTime();
            }
            if (Objects.nonNull(thisMonthPower)) {
                thisYearPower += thisMonthPower.getPower();
                thisYearCharge += thisMonthPower.getCharge();
                latestTime = thisMonthPower.getLatestTime();
            }
            
            if (Objects.nonNull(todayPower)) {
                latestTime = todayPower.getLatestTime();
            }
            
            //封装结果
            MerchantProCabinetPowerVO merchantProCabinetPowerVO = new MerchantProCabinetPowerVO();
            merchantProCabinetPowerVO.setTodayPower(Objects.isNull(todayPower) ? NumberConstant.ZERO_D : todayPower.getPower());
            merchantProCabinetPowerVO.setTodayCharge(Objects.isNull(todayPower) ? NumberConstant.ZERO_D : todayPower.getCharge());
            merchantProCabinetPowerVO.setThisMonthPower(Objects.isNull(thisMonthPower) ? NumberConstant.ZERO_D : thisMonthPower.getPower());
            merchantProCabinetPowerVO.setThisMonthCharge(Objects.isNull(thisMonthPower) ? NumberConstant.ZERO_D : thisMonthPower.getCharge());
            merchantProCabinetPowerVO.setThisYearPower(thisYearPower);
            merchantProCabinetPowerVO.setThisYearCharge(thisYearCharge);
            //merchantProCabinetPowerVO.setLatestTime(latestTime);
            merchantProCabinetPowerVO.setTime(
                    Optional.ofNullable(electricityCabinetService.queryByIdFromCache(cabinetId.intValue()).getCreateTime()).orElse(NumberConstant.ZERO_L));
            
            cabinetPowerList.add(merchantProCabinetPowerVO);
        });
        
        if (CollectionUtils.isEmpty(cabinetPowerList)) {
            return Collections.emptyList();
        }
        
        // cabinetPowerList 根据time进行倒叙排序
        cabinetPowerList.sort((o1, o2) -> o2.getTime().compareTo(o1.getTime()));
        
        return cabinetPowerList;
    }
    
    private List<MerchantProCabinetPowerDetailVO> assembleHistoryDetailPower(ElectricityCabinet cabinet, List<MerchantCabinetPowerMonthDetailPro> historyDetailList,
            String monthDate) {
        if (CollectionUtils.isEmpty(historyDetailList)) {
            return Collections.emptyList();
        }
        
        return historyDetailList.stream().map(detail -> {
            MerchantProCabinetPowerDetailVO vo = MerchantProCabinetPowerDetailVO.builder().monthDate(monthDate).cabinetId(detail.getEid()).sn(cabinet.getSn())
                    .cabinetName(cabinet.getName()).power(detail.getSumPower().doubleValue()).charge(detail.getSumCharge().doubleValue()).startTime(detail.getBeginTime())
                    .placeId(detail.getPlaceId()).placeName(Optional.ofNullable(merchantPlaceService.queryByIdFromCache(detail.getPlaceId())).orElse(new MerchantPlace()).getName())
                    .bindStatus(detail.getCabinetMerchantBindStatus()).build();
            
            // 解绑状态设置解绑时间,绑定状态没有解绑时间
            if (Objects.equals(detail.getCabinetMerchantBindStatus(), MerchantPlaceCabinetBindConstant.STATUS_UNBIND)) {
                vo.setEndTime(detail.getEndTime());
            }
            
            return vo;
        }).collect(Collectors.toList());
        
    }
    
    private List<MerchantProCabinetPowerDetailVO> assembleLiveDetailPower(ElectricityCabinet cabinet, MerchantProLivePowerVO monthPower, String monthDate) {
        if (Objects.isNull(monthPower)) {
            return Collections.emptyList();
        }
        
        List<MerchantProPowerDetailVO> detailVOList = monthPower.getDetailVOList();
        if (CollectionUtils.isEmpty(detailVOList)) {
            return Collections.emptyList();
        }
        
        return detailVOList.stream()
                .map(detail -> MerchantProCabinetPowerDetailVO.builder().monthDate(monthDate).cabinetId(detail.getEid()).sn(cabinet.getSn()).cabinetName(cabinet.getName())
                        .power(detail.getPower()).charge(detail.getCharge()).startTime(detail.getStartTime()).endTime(detail.getEndTime()).placeId(detail.getPlaceId())
                        .placeName(Optional.ofNullable(merchantPlaceService.queryByIdFromCache(detail.getPlaceId())).orElse(new MerchantPlace()).getName())
                        .bindStatus(detail.getCabinetMerchantBindStatus()).build()).collect(Collectors.toList());
    }
    
    /**
     * 获取实时电量/电费处理逻辑
     */
    private MerchantProLivePowerVO getLivePowerData(List<Long> cabinetIds, Long startTime, Long endTime, String date) {
        // 1.场地柜机绑定记录（绑定状态）：bindTime<=endTime
        MerchantPlaceCabinetConditionRequest cabinetBindRequest = new MerchantPlaceCabinetConditionRequest();
        cabinetBindRequest.setCabinetIds(new HashSet<>(cabinetIds));
        cabinetBindRequest.setEndTime(endTime);
        cabinetBindRequest.setStatus(MerchantPlaceCabinetBindConstant.STATUS_BIND);
        // 绑定状态记录
        List<MerchantPlaceCabinetBind> cabinetBindList = merchantPlaceCabinetBindService.listBindRecord(cabinetBindRequest);
        
        // 2.解绑的场地柜机绑定记录（解绑状态）：unbindTime>=startTime或bindTime<=endTime
        MerchantPlaceCabinetConditionRequest cabinetUnbindRequest = new MerchantPlaceCabinetConditionRequest();
        cabinetUnbindRequest.setStartTime(startTime);
        cabinetUnbindRequest.setEndTime(endTime);
        cabinetUnbindRequest.setStatus(MerchantPlaceCabinetBindConstant.STATUS_UNBIND);
        cabinetUnbindRequest.setCabinetIds(new HashSet<>(cabinetIds));
        // 解绑状态记录
        List<MerchantPlaceCabinetBind> cabinetUnbindList = merchantPlaceCabinetBindService.listUnbindRecord(cabinetUnbindRequest);
        
        if (CollectionUtils.isEmpty(cabinetBindList) && CollectionUtils.isEmpty(cabinetUnbindList)) {
            return null;
        }
        
        // 3.商户场地绑定记录（绑定状态）：bindTime<=endTime
        MerchantPlaceConditionRequest placeBindRequest = new MerchantPlaceConditionRequest();
        placeBindRequest.setMerchantId(placeBindRequest.getMerchantId());
        placeBindRequest.setStatus(MerchantPlaceBindConstant.BIND);
        List<MerchantPlaceBind> placeBindList = merchantPlaceBindService.listBindRecord(placeBindRequest);
        
        // 4.解绑的商户场地绑定记录（解绑状态）：unbindTime>=startTime或bindTime<=endTime
        MerchantPlaceConditionRequest placeUnbindRequest = new MerchantPlaceConditionRequest();
        placeUnbindRequest.setMerchantId(placeUnbindRequest.getMerchantId());
        placeUnbindRequest.setStartTime(startTime);
        placeUnbindRequest.setEndTime(endTime);
        placeUnbindRequest.setStatus(MerchantPlaceBindConstant.UN_BIND);
        List<MerchantPlaceBind> placeUnbindList = merchantPlaceBindService.listBindRecord(placeBindRequest);
        
        if (CollectionUtils.isEmpty(placeBindList) && CollectionUtils.isEmpty(placeUnbindList)) {
            return null;
        }
        List<MerchantPlaceBind> placeMergeBindList = new ArrayList<>();
        placeMergeBindList.addAll(placeBindList);
        placeMergeBindList.addAll(placeUnbindList);
        
        // 5.根据柜机进行统计
        List<MerchantProPowerDetailVO> powerVOList = new ArrayList<>();
        //        List<CabinetPowerProRunnable> collect = cabinetIds.parallelStream()
        //                .map(eid -> new CabinetPowerProRunnable(eid, electricityCabinetService, merchantPlaceBindService, elePowerService, startTime, endTime, cabinetBindList,
        //                        cabinetUnbindList, placeMergeBindList)).collect(Collectors.toList());
        //        try {
        //            List<Future<List<MerchantProPowerDetailVO>>> futureList = executorService.invokeAll(collect);
        //            if (CollectionUtils.isNotEmpty(futureList)) {
        //                for (Future<List<MerchantProPowerDetailVO>> future : futureList) {
        //                    List<MerchantProPowerDetailVO> result = future.get();
        //                    if (CollectionUtils.isNotEmpty(result)) {
        //                        powerVOList.addAll(result);
        //                    }
        //                }
        //            }
        //        } catch (InterruptedException | ExecutionException e) {
        //            log.error("merchant " + date + " power for pro Exception occur! ", e);
        //        }
        
        MerchantProLivePowerVO vo = null;
        if (CollectionUtils.isNotEmpty(powerVOList)) {
            // 电量
            double sumPower = powerVOList.parallelStream().mapToDouble(MerchantProPowerDetailVO::getPower).sum();
            double sumCharge = powerVOList.parallelStream().mapToDouble(MerchantProPowerDetailVO::getCharge).sum();
            
            vo = new MerchantProLivePowerVO();
            vo.setPower(sumPower);
            vo.setCharge(sumCharge);
            vo.setLatestTime(powerVOList.stream().mapToLong(MerchantProPowerDetailVO::getLatestTime).max().orElse(NumberConstant.ZERO));
            vo.setDetailVOList(powerVOList);
        }
        
        return vo;
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
        Long merchantId = request.getMerchantId();
        if (Objects.isNull(merchantId)) {
            return Collections.emptyList();
        }
        
        Long placeId = request.getPlaceId();
        Long cabinetId = request.getCabinetId();
        
        // 设置key
        String key = CacheConstant.MERCHANT_PLACE_CABINET_SEARCH_LOCK + merchantId;
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
            MerchantPlaceAndCabinetUserVO merchantPlaceAndCabinetUserVO = this.listPlaceAndCabinetByMerchantId(merchantId);
            if (Objects.isNull(merchantPlaceAndCabinetUserVO) || CollectionUtils.isEmpty(merchantPlaceAndCabinetUserVO.getCabinetList())) {
                return Collections.emptyList();
            }
            
            List<MerchantPlaceCabinetVO> cabinetList = merchantPlaceAndCabinetUserVO.getCabinetList();
            cabinetIdList = cabinetList.stream().map(MerchantPlaceCabinetVO::getCabinetId).distinct().collect(Collectors.toList());
        }
        
        // 2.场地不为null，柜机为null
        if (Objects.nonNull(placeId) && Objects.isNull(cabinetId)) {
            List<MerchantPlaceCabinetVO> placeCabinetVOList = this.listCabinetByPlaceId(merchantId, placeId);
            cabinetIdList = placeCabinetVOList.stream().map(MerchantPlaceCabinetVO::getCabinetId).distinct().collect(Collectors.toList());
        }
        
        // 3. 场地不为null,柜机不为null
        if (Objects.nonNull(placeId) && Objects.nonNull(cabinetId)) {
            List<MerchantPlaceCabinetVO> placeCabinetVOList = this.listCabinetByPlaceId(merchantId, merchantId);
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
class CabinetPowerProRunnable implements Callable<MerchantPowerPeriodVO> {
    
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
    public MerchantPowerPeriodVO call() throws Exception {
        // 过滤出该柜机的绑定记录
        List<MerchantPlaceCabinetBind> cabinetBindList = bindList.stream().filter(bind -> Objects.equals(bind.getCabinetId(), eid)).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(cabinetBindList)) {
            return null;
        }
        
        List<EleSumPowerVO> elePowerList = new ArrayList<>();
        
        // 遍历绑定时间段
        cabinetBindList.forEach(cabinetBind -> {
            Long bindTime = cabinetBind.getBindTime();
            Long unBindTime = cabinetBind.getUnBindTime();
            
            // 查询电量
            EleSumPowerVO eleSumPowerVO = elePowerService.listByCondition(bindTime, unBindTime, List.of(eid), tenantId);
            if (Objects.nonNull(eleSumPowerVO)) {
                elePowerList.add(eleSumPowerVO);
            }
        });
        
        if (CollectionUtils.isEmpty(elePowerList)) {
            return null;
        }
        
        // 封装数据
        MerchantPowerPeriodVO powerVO = new MerchantPowerPeriodVO();
        powerVO.setPower(elePowerList.stream().mapToDouble(EleSumPowerVO::getSumPower).sum());
        powerVO.setCharge(elePowerList.stream().mapToDouble(EleSumPowerVO::getSumCharge).sum());
        
        return powerVO;
    }
    
    
    /**
     * 查询并处理数据
     */
    private MerchantProPowerDetailVO handlePowerDetail(Long startTime, Long endTime, Long eid, Long placeId, Long merchantId, Integer cabinetMerchantBindStatus) {
        //查询柜机
        //        ElectricityCabinet cabinet = electricityCabinetService.queryByIdFromCache(eid.intValue());
        //
        //        // 查询电量
        //        EleSumPowerVO eleSumPowerVO = elePowerService.listByCondition(startTime, endTime, List.of(eid), cabinet.getTenantId());
        //
        //        // 查询最新一条记录的reportTime，用于排序
        //        Long latestTime = elePowerService.queryLatestReportTime(startTime, endTime, List.of(eid), cabinet.getTenantId());
        //
        //        return MerchantProPowerDetailVO.builder().eid(eid).power(eleSumPowerVO.getSumPower()).charge(eleSumPowerVO.getSumCharge()).latestTime(latestTime).startTime(startTime)
        //                .endTime(endTime).placeId(placeId).merchantId(merchantId).cabinetMerchantBindStatus(cabinetMerchantBindStatus).build();
        return null;
    }
}
