package com.xiliulou.electricity.service.impl.merchant;

import cn.hutool.core.date.DateUtil;
import com.xiliulou.core.thread.XllThreadPoolExecutorService;
import com.xiliulou.core.thread.XllThreadPoolExecutors;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.constant.merchant.MerchantPlaceBindConstant;
import com.xiliulou.electricity.constant.merchant.MerchantPlaceCabinetBindConstant;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.merchant.Merchant;
import com.xiliulou.electricity.entity.merchant.MerchantCabinetPowerMonthDetailProHistory;
import com.xiliulou.electricity.entity.merchant.MerchantPlace;
import com.xiliulou.electricity.entity.merchant.MerchantPlaceBind;
import com.xiliulou.electricity.entity.merchant.MerchantPlaceCabinetBind;
import com.xiliulou.electricity.request.merchant.MerchantCabinetPowerRequest;
import com.xiliulou.electricity.request.merchant.MerchantPlaceCabinetConditionRequest;
import com.xiliulou.electricity.request.merchant.MerchantPlaceConditionRequest;
import com.xiliulou.electricity.service.ElePowerService;
import com.xiliulou.electricity.service.ElectricityCabinetService;
import com.xiliulou.electricity.service.merchant.MerchantCabinetPowerMonthDetailProHistoryService;
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
import com.xiliulou.electricity.vo.merchant.MerchantProLivePowerVO;
import com.xiliulou.electricity.vo.merchant.MerchantProPowerChargeLineDataVO;
import com.xiliulou.electricity.vo.merchant.MerchantProPowerLineDataVO;
import com.xiliulou.electricity.vo.merchant.MerchantProPowerLineVO;
import com.xiliulou.electricity.vo.merchant.MerchantProPowerVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
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
    private MerchantCabinetPowerMonthDetailProHistoryService merchantCabinetPowerMonthDetailProHistoryService;
    
    @Resource
    private MerchantService merchantService;
    
    @Slave
    @Override
    public MerchantProPowerVO powerData(MerchantCabinetPowerRequest request) {
        log.info("Merchant powerData......");
        
        Merchant merchant = merchantService.queryByUid(request.getUid());
        if (Objects.isNull(merchant)) {
            log.warn("Merchant power error, merchant not exist, uid={}", request.getUid());
            return null;
        }
        
        Integer tenantId = TenantContextHolder.getTenantId();
        if (Objects.nonNull(tenantId) && !Objects.equals(tenantId, merchant.getTenantId())) {
            log.warn("Merchant power error, tenant not exist, uid={}", request.getUid());
            return null;
        }
        
        request.setMerchantId(merchant.getId());
        
        //获取要查询的柜机
        List<Long> cabinetIds = getStaticsCabinetIds(request);
        if (CollectionUtils.isEmpty(cabinetIds)) {
            log.warn("Merchant power powerData, cabinetIds is empty, uid={}", request.getUid());
            return null;
        }
        
        MerchantProPowerVO vo = new MerchantProPowerVO();
        
        // 1.今日电量
        CompletableFuture<Void> todayPowerFuture = CompletableFuture.runAsync(() -> {
            log.info("Merchant powerData getTodayPower......");
            MerchantPowerPeriodVO todayPower = getTodayPower(tenantId, merchant.getId(), cabinetIds);
            
            BigDecimal todayPowerData = getSafeBigDecimal(todayPower, MerchantPowerPeriodVO::getPower).setScale(2, RoundingMode.HALF_UP);
            BigDecimal todayChargeData = getSafeBigDecimal(todayPower, MerchantPowerPeriodVO::getCharge).setScale(2, RoundingMode.HALF_UP);
            
            vo.setTodayPower(todayPowerData);
            vo.setTodayCharge(todayChargeData);
            
            log.info("Merchant powerData getTodayPower......{}", todayPower);
        }, executorService).exceptionally(e -> {
            log.error("Query merchant today power data error! uid={}", request.getUid(), e);
            return null;
        });
        
        // 2.昨日电量
        CompletableFuture<Void> yesterdayPowerFuture = CompletableFuture.runAsync(() -> {
            log.info("Merchant powerData getYesterdayPower......");
            MerchantPowerPeriodVO yesterdayPower = getYesterdayPower(tenantId, merchant.getId(), cabinetIds);
            
            BigDecimal yesterdayPowerData = getSafeBigDecimal(yesterdayPower, MerchantPowerPeriodVO::getPower).setScale(2, RoundingMode.HALF_UP);
            BigDecimal yesterdayChargeData = getSafeBigDecimal(yesterdayPower, MerchantPowerPeriodVO::getCharge).setScale(2, RoundingMode.HALF_UP);
            
            vo.setYesterdayPower(yesterdayPowerData);
            vo.setYesterdayCharge(yesterdayChargeData);
            
            log.info("Merchant powerData getYesterdayPower......{}", yesterdayPower);
        }, executorService).exceptionally(e -> {
            log.error("Query merchant yesterday power data error! uid={}", request.getUid(), e);
            return null;
        });
        
        // 3.本月电量
        CompletableFuture<Void> thisMonthPowerFuture = CompletableFuture.runAsync(() -> {
            log.info("Merchant powerData getThisMonthPower......");
            
            MerchantPowerPeriodVO thisMonthPower = getThisMonthPower(tenantId, merchant.getId(), cabinetIds);
            
            BigDecimal thisMonthPowerData = getSafeBigDecimal(thisMonthPower, MerchantPowerPeriodVO::getPower).setScale(2, RoundingMode.HALF_UP);
            BigDecimal thisMonthChargeData = getSafeBigDecimal(thisMonthPower, MerchantPowerPeriodVO::getCharge).setScale(2, RoundingMode.HALF_UP);
            
            vo.setThisMonthPower(thisMonthPowerData);
            vo.setThisMonthCharge(thisMonthChargeData);
            
            log.info("Merchant powerData getThisMonthPower......{}", thisMonthPower);
        }, executorService).exceptionally(e -> {
            log.error("Query merchant this month power data error! uid={}", request.getUid(), e);
            return null;
        });
        
        // 4.上月电量
        CompletableFuture<Void> lastMonthPowerFuture = CompletableFuture.runAsync(() -> {
            log.info("Merchant powerData getLastMonthPower......");
            MerchantPowerPeriodVO lastMonthPower = getLastMonthPower(tenantId, merchant.getId(), cabinetIds);
            
            BigDecimal lastMonthPowerData = getSafeBigDecimal(lastMonthPower, MerchantPowerPeriodVO::getPower).setScale(2, RoundingMode.HALF_UP);
            BigDecimal lastMonthChargeData = getSafeBigDecimal(lastMonthPower, MerchantPowerPeriodVO::getCharge).setScale(2, RoundingMode.HALF_UP);
            
            vo.setLastMonthPower(lastMonthPowerData);
            vo.setLastMonthCharge(lastMonthChargeData);
            
            log.info("Merchant powerData getLastMonthPower......{}", lastMonthPower);
        }, executorService).exceptionally(e -> {
            log.error("Query merchant last month power data error! uid={}", request.getUid(), e);
            return null;
        });
        
        // 5.累计电量
        CompletableFuture<Void> totalPowerFuture = CompletableFuture.runAsync(() -> {
            log.info("Merchant powerData getTotalPower......");
            MerchantPowerPeriodVO totalPower = getTotalPower(tenantId, merchant.getId(), cabinetIds);
            
            vo.setTotalPower(totalPower.getPower().setScale(2, RoundingMode.HALF_UP));
            vo.setTotalCharge(totalPower.getCharge().setScale(2, RoundingMode.HALF_UP));
            
            log.info("Merchant powerData getTotalPower......{}", totalPower);
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
        log.info("Merchant getTodayPower merchantId={}, cabinetIds={}", merchantId, cabinetIds);
        
        // 今日0点
        Long todayStartTime = DateUtils.getTimeAgoStartTime(NumberConstant.ZERO);
        // 当前时间
        long nowTime = System.currentTimeMillis();
        
        // 获取商户场地绑定记录
        List<MerchantPlaceBind> merchantPlaceBindList = todayOrThisMonthMerchantPlaceBindList(merchantId, todayStartTime, nowTime);
        if (CollectionUtils.isEmpty(merchantPlaceBindList)) {
            log.warn("Merchant getTodayPower merchantPlaceBindList is empty! merchantId={}", merchantId);
            return null;
        }
        
        log.info("Merchant getTodayPower merchantId={}, merchantPlaceBindList={}", merchantId, merchantPlaceBindList);
        
        // 封装结果集
        List<MerchantProLivePowerVO> resultList = new ArrayList<>();
        
        // 按场地进行分组
        Map<Long, List<MerchantPlaceBind>> groupByPlaceIdMap = merchantPlaceBindList.stream().filter(Objects::nonNull)
                .collect(Collectors.groupingBy(MerchantPlaceBind::getPlaceId));
        
        for (Map.Entry<Long, List<MerchantPlaceBind>> entry : groupByPlaceIdMap.entrySet()) {
            Long placeId = entry.getKey();
            List<MerchantPlaceBind> placeBindList = entry.getValue();
            
            if (CollectionUtils.isEmpty(placeBindList)) {
                continue;
            }
            
            // 遍历场地
            for (MerchantPlaceBind placeBind : placeBindList) {
                Long bindTime = placeBind.getBindTime();
                Long unBindTime = placeBind.getUnBindTime();
                
                // 获取场地柜机绑定记录
                List<MerchantPlaceCabinetBind> cabinetBindList = getTodayCabinetBindList(placeId, bindTime, unBindTime);
                if (CollectionUtils.isEmpty(cabinetBindList)) {
                    log.warn("Merchant getTodayPower cabinetBindList is empty! placeId={}, bindTime={}, unBindTime={}", placeId, bindTime, unBindTime);
                    continue;
                }
                
                // 遍历柜机
                List<MerchantProLivePowerVO> periodPowerList = getPeriodPower(tenantId, placeId, cabinetIds, cabinetBindList);
                
                log.info("Merchant getTodayPower placeId={}, cabinetIds={}, periodPowerList={}", placeId, cabinetIds, periodPowerList);
                
                resultList.addAll(periodPowerList);
            }
        }
        
        log.info("Merchant getTodayPower merchantId={}, resultList={}", merchantId, resultList);
        
        // 封装数据
        MerchantPowerPeriodVO powerVO = new MerchantPowerPeriodVO();
        BigDecimal power = resultList.stream().filter(Objects::nonNull).map(MerchantProLivePowerVO::getPower).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal charge = resultList.stream().filter(Objects::nonNull).map(MerchantProLivePowerVO::getCharge).reduce(BigDecimal.ZERO, BigDecimal::add);
        
        powerVO.setPower(power.setScale(2, RoundingMode.HALF_UP));
        powerVO.setCharge(charge.setScale(2, RoundingMode.HALF_UP));
        
        log.info("Merchant getTodayPower merchantId={}, powerVO={}", merchantId, powerVO);
        
        return powerVO;
    }
    
    private MerchantPowerPeriodVO getYesterdayPower(Integer tenantId, Long merchantId, List<Long> cabinetIds) {
        log.info("Merchant getYesterdayPower merchantId={}, cabinetIds={}", merchantId, cabinetIds);
        
        // 昨日0点
        Long yesterdayStartTime = DateUtils.getTimeAgoStartTime(NumberConstant.ONE);
        // 昨日23:59:59
        long yesterdayEndTime = DateUtils.getTimeAgoEndTime(NumberConstant.ONE);
        
        // 获取商户场地绑定记录
        List<MerchantPlaceBind> merchantPlaceBindList = yesterdayOrLastMonthMerchantPlaceBindList(merchantId, yesterdayStartTime, yesterdayEndTime);
        if (CollectionUtils.isEmpty(merchantPlaceBindList)) {
            return null;
        }
        
        log.info("Merchant getYesterdayPower merchantId={}, merchantPlaceBindList={}", merchantId, merchantPlaceBindList);
        
        // 封装结果集
        List<MerchantProLivePowerVO> resultList = new ArrayList<>();
        
        // 按场地进行分组
        Map<Long, List<MerchantPlaceBind>> groupByPlaceIdMap = merchantPlaceBindList.stream().filter(Objects::nonNull)
                .collect(Collectors.groupingBy(MerchantPlaceBind::getPlaceId));
        
        for (Map.Entry<Long, List<MerchantPlaceBind>> entry : groupByPlaceIdMap.entrySet()) {
            Long placeId = entry.getKey();
            List<MerchantPlaceBind> placeBindList = entry.getValue();
            
            if (CollectionUtils.isEmpty(placeBindList)) {
                continue;
            }
            
            // 遍历场地
            for (MerchantPlaceBind placeBind : placeBindList) {
                Long bindTime = placeBind.getBindTime();
                Long unBindTime = placeBind.getUnBindTime();
                
                // 获取场地柜机绑定记录
                List<MerchantPlaceCabinetBind> cabinetBindList = getYesterdayCabinetBindList(placeId, bindTime, unBindTime);
                if (CollectionUtils.isEmpty(cabinetBindList)) {
                    continue;
                }
                
                // 遍历柜机
                List<MerchantProLivePowerVO> periodPowerList = getPeriodPower(tenantId, placeId, cabinetIds, cabinetBindList);
                
                log.info("Merchant getYesterdayPower placeId={}, cabinetIds={}, periodPowerList={}", placeId, cabinetIds, periodPowerList);
                
                resultList.addAll(periodPowerList);
            }
        }
        
        log.info("Merchant getYesterdayPower merchantId={}, resultList={}", merchantId, resultList);
        
        // 封装数据
        MerchantPowerPeriodVO powerVO = new MerchantPowerPeriodVO();
        BigDecimal power = resultList.stream().filter(Objects::nonNull).map(MerchantProLivePowerVO::getPower).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal charge = resultList.stream().filter(Objects::nonNull).map(MerchantProLivePowerVO::getCharge).reduce(BigDecimal.ZERO, BigDecimal::add);
        
        powerVO.setPower(power.setScale(2, RoundingMode.HALF_UP));
        powerVO.setCharge(charge.setScale(2, RoundingMode.HALF_UP));
        
        log.info("Merchant getYesterdayPower merchantId={}, powerVO={}", merchantId, powerVO);
        return powerVO;
    }
    
    private MerchantPowerPeriodVO getThisMonthPower(Integer tenantId, Long merchantId, List<Long> cabinetIds) {
        log.info("Merchant getThisMonthPower merchantId={}, cabinetIds={}", merchantId, cabinetIds);
        
        // 本月第一天0点
        Long thisMonthStartTime = DateUtils.getBeforeMonthFirstDayTimestamp(NumberConstant.ZERO);
        long nowTime = System.currentTimeMillis();
        
        // 获取商户场地绑定记录
        List<MerchantPlaceBind> merchantPlaceBindList = todayOrThisMonthMerchantPlaceBindList(merchantId, thisMonthStartTime, nowTime);
        if (CollectionUtils.isEmpty(merchantPlaceBindList)) {
            return null;
        }
        
        log.info("Merchant getThisMonthPower merchantId={}, merchantPlaceBindList={}", merchantId, merchantPlaceBindList);
        
        // 封装结果集
        List<MerchantProLivePowerVO> resultList = new ArrayList<>();
        
        // 按场地进行分组
        Map<Long, List<MerchantPlaceBind>> groupByPlaceIdMap = merchantPlaceBindList.stream().filter(Objects::nonNull)
                .collect(Collectors.groupingBy(MerchantPlaceBind::getPlaceId));
        
        for (Map.Entry<Long, List<MerchantPlaceBind>> entry : groupByPlaceIdMap.entrySet()) {
            Long placeId = entry.getKey();
            List<MerchantPlaceBind> placeBindList = entry.getValue();
            
            if (CollectionUtils.isEmpty(placeBindList)) {
                continue;
            }
            
            for (MerchantPlaceBind placeBind : placeBindList) {
                Long bindTime = placeBind.getBindTime();
                Long unBindTime = placeBind.getUnBindTime();
                
                List<MerchantPlaceCabinetBind> cabinetBindList = getThisMonthCabinetBindList(placeId, bindTime, unBindTime);
                if (CollectionUtils.isEmpty(cabinetBindList)) {
                    continue;
                }
                
                // 遍历柜机
                List<MerchantProLivePowerVO> periodPowerList = getPeriodPower(tenantId, placeId, cabinetIds, cabinetBindList);
                
                resultList.addAll(periodPowerList);
                
                log.info("Merchant getThisMonthPower placeId={}, cabinetIds={}, periodPowerList={}", placeId, cabinetIds, periodPowerList);
                
            }
        }
        
        log.info("Merchant getThisMonthPower merchantId={}, resultList={}", merchantId, resultList);
        
        // 封装数据
        MerchantPowerPeriodVO powerVO = new MerchantPowerPeriodVO();
        BigDecimal power = resultList.stream().filter(Objects::nonNull).map(MerchantProLivePowerVO::getPower).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal charge = resultList.stream().filter(Objects::nonNull).map(MerchantProLivePowerVO::getCharge).reduce(BigDecimal.ZERO, BigDecimal::add);
        
        powerVO.setPower(power.setScale(2, RoundingMode.HALF_UP));
        powerVO.setCharge(charge.setScale(2, RoundingMode.HALF_UP));
        
        log.info("Merchant getThisMonthPower merchantId={}, powerVO={}", merchantId, powerVO);
        
        return powerVO;
    }
    
    private MerchantPowerPeriodVO getLastMonthPower(Integer tenantId, Long merchantId, List<Long> cabinetIds) {
        log.info("Merchant getLastMonthPower merchantId={}, cabinetIds={}", merchantId, cabinetIds);
        
        //上月第一天0点
        Long lastMonthStartTime = DateUtils.getBeforeMonthFirstDayTimestamp(NumberConstant.ONE);
        // 上月最后一天23:59:59
        long lastMonthEndTime = DateUtils.getBeforeMonthLastDayTimestamp(NumberConstant.ONE);
        
        // 获取商户场地绑定记录
        List<MerchantPlaceBind> merchantPlaceBindList = yesterdayOrLastMonthMerchantPlaceBindList(merchantId, lastMonthStartTime, lastMonthEndTime);
        if (CollectionUtils.isEmpty(merchantPlaceBindList)) {
            return null;
        }
        
        log.info("Merchant getLastMonthPower merchantId={}, merchantPlaceBindList={}", merchantId, merchantPlaceBindList);
        
        // 封装结果集
        List<MerchantProLivePowerVO> resultList = new ArrayList<>();
        
        // 按场地进行分组
        Map<Long, List<MerchantPlaceBind>> groupByPlaceIdMap = merchantPlaceBindList.stream().filter(Objects::nonNull)
                .collect(Collectors.groupingBy(MerchantPlaceBind::getPlaceId));
        
        for (Map.Entry<Long, List<MerchantPlaceBind>> entry : groupByPlaceIdMap.entrySet()) {
            Long placeId = entry.getKey();
            List<MerchantPlaceBind> placeBindList = entry.getValue();
            
            if (CollectionUtils.isEmpty(placeBindList)) {
                continue;
            }
            
            // 遍历场地
            for (MerchantPlaceBind placeBind : placeBindList) {
                Long bindTime = placeBind.getBindTime();
                Long unBindTime = placeBind.getUnBindTime();
                
                // 获取场地柜机绑定记录
                List<MerchantPlaceCabinetBind> cabinetBindList = getLastMonthCabinetBindList(placeId, bindTime, unBindTime);
                if (CollectionUtils.isEmpty(cabinetBindList)) {
                    continue;
                }
                
                // 遍历柜机
                List<MerchantProLivePowerVO> periodPowerList = getPeriodPower(tenantId, placeId, cabinetIds, cabinetBindList);
                
                log.info("Merchant getLastMonthPower placeId={}, cabinetIds={}, periodPowerList={}", placeId, cabinetIds, periodPowerList);
                
                resultList.addAll(periodPowerList);
            }
        }
        
        log.info("Merchant getLastMonthPower merchantId={}, resultList={}", merchantId, resultList);
        
        // 封装数据
        MerchantPowerPeriodVO powerVO = new MerchantPowerPeriodVO();
        BigDecimal power = resultList.stream().filter(Objects::nonNull).map(MerchantProLivePowerVO::getPower).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal charge = resultList.stream().filter(Objects::nonNull).map(MerchantProLivePowerVO::getCharge).reduce(BigDecimal.ZERO, BigDecimal::add);
        
        powerVO.setPower(power.setScale(2, RoundingMode.HALF_UP));
        powerVO.setCharge(charge.setScale(2, RoundingMode.HALF_UP));
        
        log.info("Merchant getLastMonthPower merchantId={}, powerVO={}", merchantId, powerVO);
        
        return powerVO;
    }
    
    private List<MerchantProLivePowerVO> getPeriodPower(Integer tenantId, Long placeId, List<Long> cabinetIds, List<MerchantPlaceCabinetBind> placeCabinetBindList) {
        List<MerchantProLivePowerVO> resultList = new ArrayList<>();
        
        for (Long eid : cabinetIds) {
            // 过滤出该柜机的绑定记录
            List<MerchantPlaceCabinetBind> cabinetBindList = placeCabinetBindList.stream().filter(bind -> Objects.equals(bind.getCabinetId(), eid)).collect(Collectors.toList());
            log.info("Merchant power getPeriodPower eid={}, cabinetBindList={}", eid, cabinetBindList);
            
            if (CollectionUtils.isEmpty(cabinetBindList)) {
                log.warn("Merchant power getPeriodPower cabinetBindList is empty, eid={}, cabinetBindList={}", eid, cabinetBindList);
                continue;
            }
            
            // 遍历绑定时间段
            cabinetBindList.forEach(cabinetBind -> {
                Long cabinetBindTime = cabinetBind.getBindTime();
                Long cabinetUnbindTime = cabinetBind.getUnBindTime();
                
                // 查询电量
                EleSumPowerVO eleSumPowerVO = elePowerService.listByCondition(cabinetBindTime, cabinetUnbindTime, List.of(eid), tenantId);
                // 封装数据
                MerchantProLivePowerVO powerVO = new MerchantProLivePowerVO();
                powerVO.setEid(eid);
                powerVO.setPlaceId(placeId);
                powerVO.setStartTime(cabinetBindTime);
                powerVO.setEndTime(cabinetUnbindTime);
                
                powerVO.setPower(Objects.isNull(eleSumPowerVO) ? BigDecimal.ZERO : eleSumPowerVO.getSumPower());
                powerVO.setCharge(Objects.isNull(eleSumPowerVO) ? BigDecimal.ZERO : eleSumPowerVO.getSumCharge());
                
                resultList.add(powerVO);
            });
        }
        
        log.info("Merchant power getPeriodPower......cabinetIds={}, resultList={}", cabinetIds, resultList);
        
        return resultList;
    }
    
    private List<MerchantProCabinetPowerDetailVO> getPeriodPowerForDetail(Integer tenantId, Long placeId, Long eid, List<MerchantPlaceCabinetBind> placeCabinetBindList,
            String monthDate, Boolean isPlaceBind, Long endTime) {
        
        return placeCabinetBindList.stream().map(cabinetBind -> {
            Long bindTime = cabinetBind.getBindTime();
            Long unbindTime = cabinetBind.getUnBindTime();
            Long createTime = cabinetBind.getCreateTime();
            
            //绑定：绑定状态或者解绑状态跨月末
            boolean isCabinetBind = Objects.equals(cabinetBind.getStatus(), MerchantPlaceCabinetBindConstant.STATUS_BIND) || (
                    Objects.equals(cabinetBind.getStatus(), MerchantPlaceCabinetBindConstant.STATUS_UNBIND) && Objects.equals(unbindTime, endTime));
            
            Integer status = isPlaceBind && isCabinetBind ? MerchantPlaceBindConstant.BIND : MerchantPlaceBindConstant.UN_BIND;
            
            return getCabinetPowerDetail(bindTime, unbindTime, eid, placeId, tenantId, monthDate, status, createTime);
        }).collect(Collectors.toList());
        
    }
    
    
    private List<MerchantProLivePowerVO> getTodayPowerForCabinetList(Integer tenantId, Long merchantId, List<Long> cabinetIds) {
        log.info("Merchant getTodayPowerForCabinetList......");
        
        // 今日0点
        Long todayStartTime = DateUtils.getTimeAgoStartTime(NumberConstant.ZERO);
        // 当前时间
        long nowTime = System.currentTimeMillis();
        
        // 获取商户场地绑定记录
        List<MerchantPlaceBind> merchantPlaceBindList = todayOrThisMonthMerchantPlaceBindList(merchantId, todayStartTime, nowTime);
        if (CollectionUtils.isEmpty(merchantPlaceBindList)) {
            return Collections.emptyList();
        }
        
        log.info("Merchant getTodayPowerForCabinetList merchantId={}, merchantPlaceBindList={}", merchantId, merchantPlaceBindList);
        
        // 封装结果集
        List<MerchantProLivePowerVO> resultList = new ArrayList<>();
        
        // 按场地进行分组
        Map<Long, List<MerchantPlaceBind>> groupByPlaceIdMap = merchantPlaceBindList.stream().filter(Objects::nonNull)
                .collect(Collectors.groupingBy(MerchantPlaceBind::getPlaceId));
        
        for (Map.Entry<Long, List<MerchantPlaceBind>> entry : groupByPlaceIdMap.entrySet()) {
            Long placeId = entry.getKey();
            List<MerchantPlaceBind> placeBindList = entry.getValue();
            
            if (CollectionUtils.isEmpty(placeBindList)) {
                continue;
            }
            
            for (MerchantPlaceBind placeBind : placeBindList) {
                Long bindTime = placeBind.getBindTime();
                Long unBindTime = placeBind.getUnBindTime();
                
                // 获取场地柜机绑定记录
                List<MerchantPlaceCabinetBind> cabinetBindList = getTodayCabinetBindList(placeId, bindTime, unBindTime);
                if (CollectionUtils.isEmpty(cabinetBindList)) {
                    continue;
                }
                
                // 遍历柜机
                List<MerchantProLivePowerVO> periodPowerList = getPeriodPower(tenantId, placeId, cabinetIds, cabinetBindList);
                
                log.info("Merchant getTodayPowerForCabinetList placeId={}, cabinetIds={}, periodPowerList={}", placeId, cabinetIds, periodPowerList);
                
                resultList.addAll(periodPowerList);
            }
            
        }
        
        log.info("Merchant getTodayPowerForCabinetList merchantId={}, resultList={}", merchantId, resultList);
        
        // resultList 根据eid进行分组
        Map<Long, List<MerchantProLivePowerVO>> groupByEidMap = resultList.stream().filter(Objects::nonNull).collect(Collectors.groupingBy(MerchantProLivePowerVO::getEid));
        if (MapUtils.isEmpty(groupByEidMap)) {
            return Collections.emptyList();
        }
        
        List<MerchantProLivePowerVO> voList = new ArrayList<>();
        // 遍历groupByEidMap
        groupByEidMap.forEach((k, v) -> {
            BigDecimal sumPower = v.stream().filter(Objects::nonNull).map(MerchantProLivePowerVO::getPower).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal sumCharge = v.stream().filter(Objects::nonNull).map(MerchantProLivePowerVO::getCharge).reduce(BigDecimal.ZERO, BigDecimal::add);
            
            MerchantProLivePowerVO powerVO = new MerchantProLivePowerVO();
            powerVO.setEid(k);
            powerVO.setPower(sumPower);
            powerVO.setCharge(sumCharge);
            
            voList.add(powerVO);
        });
        
        if (CollectionUtils.isEmpty(voList)) {
            return Collections.emptyList();
        }
        
        log.info("Merchant getTodayPowerForCabinetList merchantId={}, voList={}", merchantId, voList);
        
        return voList;
    }
    
    /**
     * 1.时间段根据bindTime从小到大排序 2.去时间段子集
     */
    private List<MerchantPlaceCabinetBind> removeSubset(List<MerchantPlaceCabinetBind> cabinetBindList) {
        // 排序
        cabinetBindList.sort(Comparator.comparing(bind -> bind.getBindTime() == null ? NumberConstant.ZERO_L : bind.getBindTime()));
        
        List<MerchantPlaceCabinetBind> resultList = new ArrayList<>();
        
        for (MerchantPlaceCabinetBind current : cabinetBindList) {
            boolean isSubSet = false;
            
            // 检查结果集中是否有元素包含了当前元素的时间段
            for (MerchantPlaceCabinetBind existing : resultList) {
                if (existing.getBindTime() <= current.getBindTime() && current.getUnBindTime() <= existing.getUnBindTime()) {
                    isSubSet = true;
                    break;
                }
            }
            
            // 如果当前元素不是任何已有时间段的子集，则添加到结果集中
            if (!isSubSet) {
                resultList.add(current);
            }
        }
        
        return resultList;
    }
    
    private List<MerchantProCabinetPowerDetailVO> getThisMonthPowerForCabinetDetail(Integer tenantId, Long merchantId, Long cabinetId, String monthDate, Long startTime,
            Long endTime) {
        log.info("Merchant getThisMonthPowerForCabinetDetail merchantId={}, monthDate={}, cabinetId={}", merchantId, monthDate, cabinetId);
        
        // 获取商户场地绑定记录
        List<MerchantPlaceBind> merchantPlaceBindList = todayOrThisMonthMerchantPlaceBindList(merchantId, startTime, endTime);
        if (CollectionUtils.isEmpty(merchantPlaceBindList)) {
            return Collections.emptyList();
        }
        
        log.info("Merchant getThisMonthPowerForCabinetDetail merchantId={}, cabinetId={}, merchantPlaceBindList={}", merchantId, cabinetId, merchantPlaceBindList);
        
        // 结果集
        List<MerchantProCabinetPowerDetailVO> resultList = new ArrayList<>();
        
        // 根据场地进行分组
        Map<Long, List<MerchantPlaceBind>> placeCabinetBindMap = merchantPlaceBindList.stream().collect(Collectors.groupingBy(MerchantPlaceBind::getPlaceId));
        
        for (Map.Entry<Long, List<MerchantPlaceBind>> entry : placeCabinetBindMap.entrySet()) {
            Long placeId = entry.getKey();
            List<MerchantPlaceBind> placeBindList = entry.getValue();
            
            if (CollectionUtils.isEmpty(placeBindList)) {
                continue;
            }
            
            log.info("Merchant getThisMonthPowerForCabinetDetail merchantId={}, placeId={}, placeBindList={}", merchantId, placeId, placeBindList);
            
            List<MerchantProCabinetPowerDetailVO> placeResultList = new ArrayList<>();
            
            // 遍历场地
            for (MerchantPlaceBind placeBind : placeBindList) {
                Long bindTime = placeBind.getBindTime();
                Long unBindTime = placeBind.getUnBindTime();
                
                //绑定：绑定状态或者解绑状态跨月末
                boolean isPlaceBind = Objects.equals(placeBind.getType(), MerchantPlaceBindConstant.BIND);
                
                // 获取场地柜机绑定记录
                List<MerchantPlaceCabinetBind> placeCabinetBindList = getThisMonthCabinetBindListForCabinetDetail(placeId, bindTime, unBindTime, cabinetId);
                
                log.info("Merchant getThisMonthPowerForCabinetDetail merchantId={}, placeId={}, placeCabinetBindList={}", merchantId, placeId, placeCabinetBindList);
                
                if (CollectionUtils.isEmpty(placeCabinetBindList)) {
                    continue;
                }
                
                // 遍历柜机
                List<MerchantProCabinetPowerDetailVO> periodPowerList = getPeriodPowerForDetail(tenantId, placeId, cabinetId, placeCabinetBindList, monthDate, isPlaceBind,
                        endTime);
                
                placeResultList.addAll(periodPowerList);
            }
            
            log.info("Merchant getThisMonthPowerForCabinetDetail merchantId={}, placeResultList={}", merchantId, placeResultList);
            
            // 合并连续时间段的记录（前一个时间段的endTime和后一个时间段的startTime是同一天）
            List<MerchantProCabinetPowerDetailVO> afterMergeDetailResultList = mergeSerialTimeDetail(placeResultList, null);
            if (CollectionUtils.isNotEmpty(afterMergeDetailResultList)) {
                resultList.addAll(afterMergeDetailResultList);
            }
            
            log.info("Merchant getThisMonthPowerForCabinetDetail merchantId={}, resultList={}", merchantId, resultList);
        }
        
        return resultList;
    }
    
    private List<MerchantProCabinetPowerDetailVO> getLastMonthPowerForCabinetDetail(Integer tenantId, Long merchantId, Long cabinetId, String monthDate, Long startTime,
            Long endTime, Long thisMonthStartTime) {
        log.info("Merchant getLastMonthPowerForCabinetDetail merchantId={}, monthDate={}, cabinetId={}", merchantId, monthDate, cabinetId);
        
        // 获取商户场地绑定记录
        List<MerchantPlaceBind> merchantPlaceBindList = yesterdayOrLastMonthMerchantPlaceBindList(merchantId, startTime, endTime);
        if (CollectionUtils.isEmpty(merchantPlaceBindList)) {
            return Collections.emptyList();
        }
        
        log.info("Merchant getLastMonthPowerForCabinetDetail merchantId={}, cabinetId={}, merchantPlaceBindList={}", merchantId, cabinetId, merchantPlaceBindList);
        
        // 结果集
        List<MerchantProCabinetPowerDetailVO> resultList = new ArrayList<>();
        
        // 根据场地进行分组
        Map<Long, List<MerchantPlaceBind>> placeCabinetBindMap = merchantPlaceBindList.stream().collect(Collectors.groupingBy(MerchantPlaceBind::getPlaceId));
        
        for (Map.Entry<Long, List<MerchantPlaceBind>> entry : placeCabinetBindMap.entrySet()) {
            Long placeId = entry.getKey();
            List<MerchantPlaceBind> placeBindList = entry.getValue();
            
            if (CollectionUtils.isEmpty(placeBindList)) {
                continue;
            }
            
            log.info("Merchant getLastMonthPowerForCabinetDetail merchantId={}, placeId={}, placeBindList={}", merchantId, placeId, placeBindList);
            
            List<MerchantProCabinetPowerDetailVO> placeResultList = new ArrayList<>();
            
            // 遍历场地
            for (MerchantPlaceBind placeBind : placeBindList) {
                Long bindTime = placeBind.getBindTime();
                Long unBindTime = placeBind.getUnBindTime();
                
                //绑定：绑定状态或者解绑状态跨月末
                boolean isPlaceBind = Objects.equals(placeBind.getType(), MerchantPlaceBindConstant.BIND) || (Objects.equals(placeBind.getType(), MerchantPlaceBindConstant.UN_BIND)
                        && (Objects.equals(unBindTime, endTime)));
                
                // 获取场地柜机绑定记录
                List<MerchantPlaceCabinetBind> placeCabinetBindList = getLastMonthCabinetBindListForCabinetDetail(placeId, bindTime, unBindTime, cabinetId, thisMonthStartTime);
                if (CollectionUtils.isEmpty(placeCabinetBindList)) {
                    continue;
                }
                
                // 遍历柜机
                List<MerchantProCabinetPowerDetailVO> periodPowerList = getPeriodPowerForDetail(tenantId, placeId, cabinetId, placeCabinetBindList, monthDate, isPlaceBind,
                        endTime);
                
                placeResultList.addAll(periodPowerList);
            }
            
            log.info("Merchant getLastMonthPowerForCabinetDetail merchantId={}, placeResultList={}", merchantId, placeResultList);
            
            // 合并连续时间段的记录（前一个时间段的endTime和后一个时间段的startTime是同一天）
            List<MerchantProCabinetPowerDetailVO> afterMergeDetailResultList = mergeSerialTimeDetail(placeResultList, thisMonthStartTime);
            
            if (CollectionUtils.isNotEmpty(afterMergeDetailResultList)) {
                resultList.addAll(afterMergeDetailResultList);
            }
            
            log.info("Merchant getLastMonthPowerForCabinetDetail merchantId={}, resultList={}", merchantId, resultList);
        }
        
        return resultList;
    }
    
    private MerchantProCabinetPowerDetailVO getCabinetPowerDetail(Long startTime, Long endTime, Long eid, Long placeId, Integer tenantId, String monthDate,
            Integer merchantCabinetBindStatus, Long createTime) {
        
        log.info("Merchant getCabinetPowerDetail eid={}, startTime={}, endTime={}", eid, startTime, endTime);
        
        // 查询电量
        EleSumPowerVO eleSumPowerVO = elePowerService.listByCondition(startTime, endTime, List.of(eid), tenantId);
        
        ElectricityCabinet electricityCabinet = electricityCabinetService.queryByIdFromCache(eid.intValue());
        
        MerchantProCabinetPowerDetailVO detailVO = MerchantProCabinetPowerDetailVO.builder().monthDate(monthDate).cabinetId(eid)
                .cabinetName(Optional.ofNullable(electricityCabinet).orElse(new ElectricityCabinet()).getName())
                .sn(Optional.ofNullable(electricityCabinet).orElse(new ElectricityCabinet()).getSn())
                .power(Objects.isNull(eleSumPowerVO) ? BigDecimal.ZERO : eleSumPowerVO.getSumPower())
                .charge(Objects.isNull(eleSumPowerVO) ? BigDecimal.ZERO : eleSumPowerVO.getSumCharge()).startTime(startTime).placeId(placeId)
                .placeName(Optional.ofNullable(merchantPlaceService.queryByIdFromCache(placeId)).orElse(new MerchantPlace()).getName()).bindStatus(merchantCabinetBindStatus)
                .createTime(createTime).build();
        
        if (Objects.equals(merchantCabinetBindStatus, MerchantPlaceBindConstant.UN_BIND)) {
            detailVO.setEndTime(endTime);
        }
        
        return detailVO;
    }
    
    /**
     * 合并连续时间段的记录
     */
    private static List<MerchantProCabinetPowerDetailVO> mergeSerialTimeDetail(List<MerchantProCabinetPowerDetailVO> detailList, Long thisMonthStartTime) {
        List<MerchantProCabinetPowerDetailVO> resultList = new ArrayList<>();
        
        // 去重
        detailList = detailList.stream().distinct().collect(Collectors.toList());
        
        // 统计上月时，本月拉取到上月的时间段不参与合并
        Map<Long, List<MerchantProCabinetPowerDetailVO>> fetchBindMap = null;
        if (Objects.nonNull(thisMonthStartTime)) {
            // 本月拉取到上月的时间段不参与合并
            List<MerchantProCabinetPowerDetailVO> fetchBindList = detailList.stream().filter(bind -> bind.getCreateTime() >= thisMonthStartTime).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(fetchBindList)) {
                // 按场地进行分组
                fetchBindMap = fetchBindList.stream().collect(Collectors.groupingBy(MerchantProCabinetPowerDetailVO::getPlaceId));
                detailList.removeAll(fetchBindList);
            }
        }
        
        // 按场地分组
        Map<Long, List<MerchantProCabinetPowerDetailVO>> placeCabinetBindMap = detailList.stream().collect(Collectors.groupingBy(MerchantProCabinetPowerDetailVO::getPlaceId));
        
        for (Map.Entry<Long, List<MerchantProCabinetPowerDetailVO>> entry : placeCabinetBindMap.entrySet()) {
            List<MerchantProCabinetPowerDetailVO> placeDetailList = entry.getValue();
            Long placeId = entry.getKey();
            
            if (CollectionUtils.isEmpty(placeDetailList)) {
                continue;
            }
            
            // 按柜机分组
            Map<Long, List<MerchantProCabinetPowerDetailVO>> cabinetBindMap = placeDetailList.stream()
                    .collect(Collectors.groupingBy(MerchantProCabinetPowerDetailVO::getCabinetId));
            
            for (Map.Entry<Long, List<MerchantProCabinetPowerDetailVO>> cabinetEntry : cabinetBindMap.entrySet()) {
                List<MerchantProCabinetPowerDetailVO> cabinetDetailList = cabinetEntry.getValue();
                
                if (CollectionUtils.isEmpty(cabinetDetailList)) {
                    continue;
                }
                
                // 按状态分组
                Map<Integer, List<MerchantProCabinetPowerDetailVO>> groupMap = cabinetDetailList.stream()
                        .collect(Collectors.groupingBy(MerchantProCabinetPowerDetailVO::getBindStatus));
                
                for (Map.Entry<Integer, List<MerchantProCabinetPowerDetailVO>> statusEntry : groupMap.entrySet()) {
                    List<MerchantProCabinetPowerDetailVO> list = statusEntry.getValue();
                    
                    if (CollectionUtils.isEmpty(list)) {
                        continue;
                    }
                    
                    // 排序
                    list.sort(Comparator.comparing(detail -> detail.getEndTime() == null ? NumberConstant.ZERO_L : detail.getEndTime()));
                    
                    if (list.size() > NumberConstant.ONE) {
                        // 合并时间段
                        for (int i = 0; i < list.size() - 1; i++) {
                            MerchantProCabinetPowerDetailVO current = list.get(i);
                            MerchantProCabinetPowerDetailVO next = list.get(i + 1);
                            
                            if (Objects.equals(DateUtils.getTimeByTimeStamp(current.getEndTime()), DateUtils.getTimeByTimeStamp(next.getStartTime()))) {
                                current.setEndTime(next.getEndTime());
                                if (current.getStartTime() > next.getStartTime()) {
                                    current.setEndTime(next.getStartTime());
                                }
                                
                                BigDecimal currentPower = Objects.isNull(current.getPower()) ? BigDecimal.ZERO : current.getPower();
                                BigDecimal currentCharge = Objects.isNull(current.getCharge()) ? BigDecimal.ZERO : current.getCharge();
                                BigDecimal nextPower = Objects.isNull(next.getPower()) ? BigDecimal.ZERO : next.getPower();
                                BigDecimal nextCharge = Objects.isNull(next.getCharge()) ? BigDecimal.ZERO : next.getCharge();
                                
                                current.setPower(currentPower.add(nextPower));
                                current.setCharge(currentCharge.add(nextCharge));
                                
                                list.remove(next);
                            }
                        }
                    }
                    
                    resultList.addAll(list);
                }
            }
            
            // 将拉取的时间段数据添加到结果中
            if (MapUtils.isNotEmpty(fetchBindMap)) {
                fetchBindMap.forEach((key, bindList) -> {
                    if (Objects.equals(placeId, key)) {
                        resultList.addAll(bindList);
                    }
                });
            }
        }
        
        //绑定状态没有结束时间
        return resultList.stream().peek(detail -> {
            if (Objects.equals(MerchantPlaceBindConstant.BIND, detail.getBindStatus())) {
                detail.setEndTime(null);
            }
        }).collect(Collectors.toList());
    }
    
    private List<MerchantProLivePowerVO> getThisMonthPowerForCabinetList(Integer tenantId, Long merchantId, List<Long> cabinetIds) {
        log.info("Merchant getThisMonthPowerForCabinetList merchantId={}, cabinetIds={}", merchantId, cabinetIds);
        
        // 本月第一天0点
        Long thisMonthStartTime = DateUtils.getBeforeMonthFirstDayTimestamp(NumberConstant.ZERO);
        long nowTime = System.currentTimeMillis();
        
        // 获取商户场地绑定记录
        List<MerchantPlaceBind> merchantPlaceBindList = todayOrThisMonthMerchantPlaceBindList(merchantId, thisMonthStartTime, nowTime);
        if (CollectionUtils.isEmpty(merchantPlaceBindList)) {
            return Collections.emptyList();
        }
        
        // 封装结果集
        List<MerchantProLivePowerVO> resultList = new ArrayList<>();
        
        // 按场地进行分组
        Map<Long, List<MerchantPlaceBind>> groupByPlaceIdMap = merchantPlaceBindList.stream().filter(Objects::nonNull)
                .collect(Collectors.groupingBy(MerchantPlaceBind::getPlaceId));
        
        for (Map.Entry<Long, List<MerchantPlaceBind>> entry : groupByPlaceIdMap.entrySet()) {
            Long placeId = entry.getKey();
            List<MerchantPlaceBind> placeBindList = entry.getValue();
            
            if (CollectionUtils.isEmpty(placeBindList)) {
                continue;
            }
            
            // 遍历场地
            for (MerchantPlaceBind placeBind : placeBindList) {
                Long bindTime = placeBind.getBindTime();
                Long unBindTime = placeBind.getUnBindTime();
                
                // 获取场地柜机绑定记录
                List<MerchantPlaceCabinetBind> cabinetBindList = getThisMonthCabinetBindList(placeId, bindTime, unBindTime);
                
                if (CollectionUtils.isEmpty(cabinetBindList)) {
                    continue;
                }
                
                // 遍历柜机
                List<MerchantProLivePowerVO> periodPowerList = getPeriodPower(tenantId, placeId, cabinetIds, cabinetBindList);
                
                log.info("Merchant getThisMonthPowerForCabinetList placeId={}, cabinetIds={}, periodPowerList={}", placeId, cabinetIds, periodPowerList);
                
                resultList.addAll(periodPowerList);
            }
            
        }
        
        log.info("Merchant getThisMonthPowerForCabinetList merchantId={}, resultList={}", merchantId, resultList);
        
        // resultList 根据eid进行分组
        Map<Long, List<MerchantProLivePowerVO>> groupByEidMap = resultList.stream().filter(Objects::nonNull).collect(Collectors.groupingBy(MerchantProLivePowerVO::getEid));
        if (MapUtils.isEmpty(groupByEidMap)) {
            return Collections.emptyList();
        }
        
        List<MerchantProLivePowerVO> voList = new ArrayList<>();
        // 遍历groupByEidMap
        groupByEidMap.forEach((k, v) -> {
            BigDecimal sumPower = v.stream().filter(Objects::nonNull).map(MerchantProLivePowerVO::getPower).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal sumCharge = v.stream().filter(Objects::nonNull).map(MerchantProLivePowerVO::getCharge).reduce(BigDecimal.ZERO, BigDecimal::add);
            
            MerchantProLivePowerVO powerVO = new MerchantProLivePowerVO();
            powerVO.setEid(k);
            powerVO.setPower(sumPower);
            powerVO.setCharge(sumCharge);
            
            voList.add(powerVO);
        });
        
        if (CollectionUtils.isEmpty(voList)) {
            return Collections.emptyList();
        }
        
        log.info("Merchant getThisMonthPowerForCabinetList merchantId={}, voList={}", merchantId, voList);
        
        return voList;
    }
    
    private List<MerchantProLivePowerVO> getLastMonthPowerForCabinetList(Integer tenantId, Long merchantId, List<Long> cabinetIds) {
        log.info("Merchant getLastMonthPowerForCabinetList merchantId={}, cabinetIds={}", merchantId, cabinetIds);
        
        //上月第一天0点
        Long lastMonthStartTime = DateUtils.getBeforeMonthFirstDayTimestamp(NumberConstant.ONE);
        // 上月最后一天23:59:59
        long lastMonthEndTime = DateUtils.getBeforeMonthLastDayTimestamp(NumberConstant.ONE);
        
        // 获取商户场地绑定记录
        List<MerchantPlaceBind> merchantPlaceBindList = yesterdayOrLastMonthMerchantPlaceBindList(merchantId, lastMonthStartTime, lastMonthEndTime);
        if (CollectionUtils.isEmpty(merchantPlaceBindList)) {
            return Collections.emptyList();
        }
        
        log.info("Merchant getLastMonthPowerForCabinetList merchantId={}, merchantPlaceBindList={}", merchantId, merchantPlaceBindList);
        
        // 封装结果集
        List<MerchantProLivePowerVO> resultList = new ArrayList<>();
        
        // 按场地进行分组
        Map<Long, List<MerchantPlaceBind>> groupByPlaceIdMap = merchantPlaceBindList.stream().filter(Objects::nonNull)
                .collect(Collectors.groupingBy(MerchantPlaceBind::getPlaceId));
        
        for (Map.Entry<Long, List<MerchantPlaceBind>> entry : groupByPlaceIdMap.entrySet()) {
            Long placeId = entry.getKey();
            List<MerchantPlaceBind> placeBindList = entry.getValue();
            
            if (CollectionUtils.isEmpty(placeBindList)) {
                continue;
            }
            
            // 遍历场地
            for (MerchantPlaceBind placeBind : placeBindList) {
                Long bindTime = placeBind.getBindTime();
                Long unBindTime = placeBind.getUnBindTime();
                
                // 获取场地柜机绑定记录
                List<MerchantPlaceCabinetBind> cabinetBindList = getLastMonthCabinetBindList(placeId, bindTime, unBindTime);
                if (CollectionUtils.isEmpty(cabinetBindList)) {
                    continue;
                }
                
                // 遍历柜机
                List<MerchantProLivePowerVO> periodPowerList = getPeriodPower(tenantId, placeId, cabinetIds, cabinetBindList);
                
                log.info("Merchant getLastMonthPowerForCabinetList placeId={}, cabinetIds={}, periodPowerList={}", placeId, cabinetIds, periodPowerList);
                
                resultList.addAll(periodPowerList);
                
            }
        }
        
        log.info("Merchant getLastMonthPowerForCabinetList merchantId={}, resultList={}", merchantId, resultList);
        
        // resultList 根据eid进行分组
        Map<Long, List<MerchantProLivePowerVO>> groupByEidMap = resultList.stream().filter(Objects::nonNull).collect(Collectors.groupingBy(MerchantProLivePowerVO::getEid));
        if (MapUtils.isEmpty(groupByEidMap)) {
            return Collections.emptyList();
        }
        
        List<MerchantProLivePowerVO> voList = new ArrayList<>();
        // 遍历groupByEidMap
        groupByEidMap.forEach((k, v) -> {
            BigDecimal sumPower = v.stream().filter(Objects::nonNull).map(MerchantProLivePowerVO::getPower).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal sumCharge = v.stream().filter(Objects::nonNull).map(MerchantProLivePowerVO::getCharge).reduce(BigDecimal.ZERO, BigDecimal::add);
            
            MerchantProLivePowerVO powerVO = new MerchantProLivePowerVO();
            powerVO.setEid(k);
            powerVO.setPower(sumPower);
            powerVO.setCharge(sumCharge);
            
            voList.add(powerVO);
        });
        
        if (CollectionUtils.isEmpty(voList)) {
            return Collections.emptyList();
        }
        
        log.info("Merchant getLastMonthPowerForCabinetList merchantId={}, voList={}", merchantId, voList);
        
        return voList;
    }
    
    private List<MerchantPlaceCabinetBind> getTodayCabinetBindList(Long placeId, Long merchantPlaceBindTime, Long merchantPlaceUnbindTime) {
        List<MerchantPlaceCabinetBind> stepOneList = new ArrayList<>();
        
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
                
                stepOneList.add(placeCabinetBind);
            }
        }
        
        // 解绑状态记录
        MerchantPlaceCabinetConditionRequest placeCabinetUnbindRequest = MerchantPlaceCabinetConditionRequest.builder().placeId(placeId)
                .status(MerchantPlaceCabinetBindConstant.STATUS_UNBIND).startTime(merchantPlaceBindTime).build();
        List<MerchantPlaceCabinetBind> cabinetUnbindList = merchantPlaceCabinetBindService.listUnbindRecord(placeCabinetUnbindRequest);
        
        if (CollectionUtils.isEmpty(cabinetUnbindList)) {
            return stepOneList;
        }
        
        // 先掐头去尾
        for (MerchantPlaceCabinetBind placeCabinetBind : cabinetUnbindList) {
            Long bindTime = placeCabinetBind.getBindTime();
            
            if (bindTime < merchantPlaceBindTime) {
                placeCabinetBind.setBindTime(merchantPlaceBindTime);
            }
            
            stepOneList.add(placeCabinetBind);
        }
        
        List<MerchantPlaceCabinetBind> resultList = new ArrayList<>();
        
        // 再按柜机进行分组，然后去除子集
        Map<Long, List<MerchantPlaceCabinetBind>> groupByCabinetIdMap = stepOneList.stream().filter(Objects::nonNull)
                .collect(Collectors.groupingBy(MerchantPlaceCabinetBind::getCabinetId));
        
        for (Map.Entry<Long, List<MerchantPlaceCabinetBind>> entry : groupByCabinetIdMap.entrySet()) {
            
            List<MerchantPlaceCabinetBind> bindList = entry.getValue();
            
            List<MerchantPlaceCabinetBind> afterRemoveSubsetList = removeSubset(bindList);
            
            resultList.addAll(afterRemoveSubsetList);
        }
        
        return resultList;
    }
    
    private List<MerchantPlaceCabinetBind> getYesterdayCabinetBindList(Long placeId, Long merchantPlaceBindTime, Long merchantPlaceUnbindTime) {
        List<MerchantPlaceCabinetBind> stepOneList = new ArrayList<>();
        
        // 绑定状态记录
        MerchantPlaceCabinetConditionRequest placeCabinetBindRequest = MerchantPlaceCabinetConditionRequest.builder().placeId(placeId)
                .status(MerchantPlaceCabinetBindConstant.STATUS_BIND).endTime(merchantPlaceUnbindTime).build();
        List<MerchantPlaceCabinetBind> cabinetBindList = merchantPlaceCabinetBindService.listBindRecord(placeCabinetBindRequest);
        
        if (CollectionUtils.isNotEmpty(cabinetBindList)) {
            for (MerchantPlaceCabinetBind placeCabinetBind : cabinetBindList) {
                Long bindTime = placeCabinetBind.getBindTime();
                
                if (bindTime < merchantPlaceBindTime) {
                    placeCabinetBind.setBindTime(merchantPlaceBindTime);
                }
                
                // 给绑定状态记录赋值解绑时间：merchantPlaceUnbindTime
                placeCabinetBind.setUnBindTime(merchantPlaceUnbindTime);
                
                stepOneList.add(placeCabinetBind);
            }
        }
        
        // 解绑状态记录
        MerchantPlaceCabinetConditionRequest placeCabinetUnbindRequest = MerchantPlaceCabinetConditionRequest.builder().placeId(placeId)
                .status(MerchantPlaceCabinetBindConstant.STATUS_UNBIND).startTime(merchantPlaceBindTime).endTime(merchantPlaceUnbindTime).build();
        List<MerchantPlaceCabinetBind> cabinetUnbindList = merchantPlaceCabinetBindService.listUnbindRecord(placeCabinetUnbindRequest);
        
        if (CollectionUtils.isEmpty(cabinetUnbindList)) {
            return stepOneList;
        }
        
        // 先掐头去尾
        for (MerchantPlaceCabinetBind placeCabinetBind : cabinetUnbindList) {
            Long bindTime = placeCabinetBind.getBindTime();
            Long unBindTime = placeCabinetBind.getUnBindTime();
            
            if (bindTime < merchantPlaceBindTime) {
                placeCabinetBind.setBindTime(merchantPlaceBindTime);
            }
            
            if (unBindTime > merchantPlaceUnbindTime) {
                placeCabinetBind.setUnBindTime(merchantPlaceUnbindTime);
            }
            
            stepOneList.add(placeCabinetBind);
        }
        
        List<MerchantPlaceCabinetBind> resultList = new ArrayList<>();
        
        // 再按柜机进行分组，然后去除子集
        Map<Long, List<MerchantPlaceCabinetBind>> groupByCabinetIdMap = stepOneList.stream().filter(Objects::nonNull)
                .collect(Collectors.groupingBy(MerchantPlaceCabinetBind::getCabinetId));
        
        for (Map.Entry<Long, List<MerchantPlaceCabinetBind>> entry : groupByCabinetIdMap.entrySet()) {
            
            List<MerchantPlaceCabinetBind> bindList = entry.getValue();
            
            List<MerchantPlaceCabinetBind> afterRemoveSubsetList = removeSubset(bindList);
            
            resultList.addAll(afterRemoveSubsetList);
        }
        
        return resultList;
    }
    
    private List<MerchantPlaceCabinetBind> getThisMonthCabinetBindList(Long placeId, Long merchantPlaceBindTime, Long merchantPlaceUnbindTime) {
        List<MerchantPlaceCabinetBind> stepOneList = new ArrayList<>();
        
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
                
                stepOneList.add(placeCabinetBind);
            }
        }
        
        // 解绑状态记录
        MerchantPlaceCabinetConditionRequest placeCabinetUnbindRequest = MerchantPlaceCabinetConditionRequest.builder().placeId(placeId).cabinetIds(null)
                .status(MerchantPlaceCabinetBindConstant.STATUS_UNBIND).startTime(merchantPlaceBindTime).build();
        
        List<MerchantPlaceCabinetBind> cabinetUnbindList = merchantPlaceCabinetBindService.listUnbindRecord(placeCabinetUnbindRequest);
        
        if (CollectionUtils.isEmpty(cabinetUnbindList)) {
            return stepOneList;
        }
        
        // 先掐头去尾
        for (MerchantPlaceCabinetBind placeCabinetBind : cabinetUnbindList) {
            Long bindTime = placeCabinetBind.getBindTime();
            
            if (bindTime < merchantPlaceBindTime) {
                placeCabinetBind.setBindTime(merchantPlaceBindTime);
            }
            
            stepOneList.add(placeCabinetBind);
        }
        
        List<MerchantPlaceCabinetBind> resultList = new ArrayList<>();
        
        // 再按柜机进行分组，然后去除子集
        Map<Long, List<MerchantPlaceCabinetBind>> groupByCabinetIdMap = stepOneList.stream().filter(Objects::nonNull)
                .collect(Collectors.groupingBy(MerchantPlaceCabinetBind::getCabinetId));
        
        for (Map.Entry<Long, List<MerchantPlaceCabinetBind>> entry : groupByCabinetIdMap.entrySet()) {
            
            List<MerchantPlaceCabinetBind> bindList = entry.getValue();
            
            List<MerchantPlaceCabinetBind> afterRemoveSubsetList = removeSubset(bindList);
            
            resultList.addAll(afterRemoveSubsetList);
        }
        
        return resultList;
    }
    
    private List<MerchantPlaceCabinetBind> getThisMonthCabinetBindListForCabinetDetail(Long placeId, Long placeBindTime, Long placeUnbindTime, Long cabinetId) {
        List<MerchantPlaceCabinetBind> stepOneList = new ArrayList<>();
        
        // 绑定状态记录
        MerchantPlaceCabinetConditionRequest placeCabinetBindRequest = MerchantPlaceCabinetConditionRequest.builder().placeId(placeId).cabinetIds(Set.of(cabinetId))
                .status(MerchantPlaceCabinetBindConstant.STATUS_BIND).endTime(placeUnbindTime).build();
        List<MerchantPlaceCabinetBind> cabinetBindList = merchantPlaceCabinetBindService.listBindRecord(placeCabinetBindRequest);
        
        if (CollectionUtils.isNotEmpty(cabinetBindList)) {
            for (MerchantPlaceCabinetBind placeCabinetBind : cabinetBindList) {
                Long bindTime = placeCabinetBind.getBindTime();
                
                if (bindTime < placeBindTime) {
                    placeCabinetBind.setBindTime(placeBindTime);
                }
                
                // 给绑定状态记录赋值解绑时间：placeUnbindTime
                placeCabinetBind.setUnBindTime(placeUnbindTime);
                
                stepOneList.add(placeCabinetBind);
            }
        }
        
        // 解绑状态记录
        MerchantPlaceCabinetConditionRequest placeCabinetUnbindRequest = MerchantPlaceCabinetConditionRequest.builder().placeId(placeId).cabinetIds(Set.of(cabinetId))
                .status(MerchantPlaceCabinetBindConstant.STATUS_UNBIND).startTime(placeBindTime).endTime(placeUnbindTime).build();
        List<MerchantPlaceCabinetBind> cabinetUnbindList = merchantPlaceCabinetBindService.listUnbindRecord(placeCabinetUnbindRequest);
        
        if (CollectionUtils.isEmpty(cabinetUnbindList)) {
            return stepOneList;
        }
        
        // 先掐头去尾
        for (MerchantPlaceCabinetBind placeCabinetBind : cabinetUnbindList) {
            Long bindTime = placeCabinetBind.getBindTime();
            
            if (bindTime < placeBindTime) {
                placeCabinetBind.setBindTime(placeBindTime);
            }
            
            stepOneList.add(placeCabinetBind);
        }
        
        List<MerchantPlaceCabinetBind> resultList = new ArrayList<>();
        
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
        
        return resultList;
    }
    
    private List<MerchantPlaceCabinetBind> getLastMonthCabinetBindListForCabinetDetail(Long placeId, Long placeBindTime, Long placeUnbindTime, Long cabinetId,
            Long thisMonthStartTime) {
        List<MerchantPlaceCabinetBind> stepOneList = new ArrayList<>();
        
        // 绑定状态记录
        MerchantPlaceCabinetConditionRequest placeCabinetBindRequest = MerchantPlaceCabinetConditionRequest.builder().placeId(placeId).cabinetIds(Set.of(cabinetId))
                .status(MerchantPlaceCabinetBindConstant.STATUS_BIND).endTime(placeUnbindTime).build();
        List<MerchantPlaceCabinetBind> cabinetBindList = merchantPlaceCabinetBindService.listBindRecord(placeCabinetBindRequest);
        
        if (CollectionUtils.isNotEmpty(cabinetBindList)) {
            for (MerchantPlaceCabinetBind placeCabinetBind : cabinetBindList) {
                Long bindTime = placeCabinetBind.getBindTime();
                
                if (bindTime < placeBindTime) {
                    placeCabinetBind.setBindTime(placeBindTime);
                }
                
                // 给绑定状态记录赋值解绑时间：placeUnbindTime
                placeCabinetBind.setUnBindTime(placeUnbindTime);
                
                stepOneList.add(placeCabinetBind);
            }
        }
        
        // 解绑状态记录
        MerchantPlaceCabinetConditionRequest placeCabinetUnbindRequest = MerchantPlaceCabinetConditionRequest.builder().placeId(placeId).cabinetIds(Set.of(cabinetId))
                .status(MerchantPlaceCabinetBindConstant.STATUS_UNBIND).startTime(placeBindTime).endTime(placeUnbindTime).build();
        List<MerchantPlaceCabinetBind> cabinetUnbindList = merchantPlaceCabinetBindService.listUnbindRecord(placeCabinetUnbindRequest);
        
        if (CollectionUtils.isEmpty(cabinetUnbindList)) {
            return stepOneList;
        }
        
        // 先掐头去尾
        for (MerchantPlaceCabinetBind placeCabinetBind : cabinetUnbindList) {
            Long bindTime = placeCabinetBind.getBindTime();
            
            if (bindTime < placeBindTime) {
                placeCabinetBind.setBindTime(placeBindTime);
            }
            
            stepOneList.add(placeCabinetBind);
        }
        
        List<MerchantPlaceCabinetBind> resultList = new ArrayList<>();
        
        // 本月拉取到上月的时间段不参与合并
        List<MerchantPlaceCabinetBind> fetchBindList = stepOneList.stream().filter(bind -> bind.getCreateTime() >= thisMonthStartTime).collect(Collectors.toList());
        
        stepOneList.removeAll(fetchBindList);
        
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
        
        resultList.addAll(fetchBindList);
        
        return resultList;
    }
    
    private List<MerchantPlaceCabinetBind> getLastMonthCabinetBindList(Long placeId, Long merchantPlaceBindTime, Long merchantPlaceUnbindTime) {
        List<MerchantPlaceCabinetBind> stepOneList = new ArrayList<>();
        
        // 绑定状态记录((bindTime<=merchantPlaceUnbindTime))
        MerchantPlaceCabinetConditionRequest placeCabinetBindRequest = MerchantPlaceCabinetConditionRequest.builder().placeId(placeId)
                .status(MerchantPlaceCabinetBindConstant.STATUS_BIND).endTime(merchantPlaceUnbindTime).build();
        List<MerchantPlaceCabinetBind> cabinetBindList = merchantPlaceCabinetBindService.listBindRecord(placeCabinetBindRequest);
        
        if (CollectionUtils.isNotEmpty(cabinetBindList)) {
            for (MerchantPlaceCabinetBind placeCabinetBind : cabinetBindList) {
                Long bindTime = placeCabinetBind.getBindTime();
                
                if (bindTime < merchantPlaceBindTime) {
                    placeCabinetBind.setBindTime(merchantPlaceBindTime);
                }
                
                // 给绑定状态记录赋值解绑时间：endTime
                placeCabinetBind.setUnBindTime(merchantPlaceUnbindTime);
                
                stepOneList.add(placeCabinetBind);
            }
        }
        
        // 解绑状态记录
        MerchantPlaceCabinetConditionRequest placeCabinetUnbindRequest = MerchantPlaceCabinetConditionRequest.builder().placeId(placeId)
                .status(MerchantPlaceCabinetBindConstant.STATUS_UNBIND).startTime(merchantPlaceBindTime).endTime(merchantPlaceUnbindTime).build();
        List<MerchantPlaceCabinetBind> cabinetUnbindList = merchantPlaceCabinetBindService.listUnbindRecord(placeCabinetUnbindRequest);
        
        if (CollectionUtils.isEmpty(cabinetUnbindList)) {
            return stepOneList;
        }
        
        // 先掐头去尾
        for (MerchantPlaceCabinetBind placeCabinetBind : cabinetUnbindList) {
            Long bindTime = placeCabinetBind.getBindTime();
            Long unBindTime = placeCabinetBind.getUnBindTime();
            
            if (bindTime < merchantPlaceBindTime) {
                placeCabinetBind.setBindTime(merchantPlaceBindTime);
            }
            
            if (unBindTime > merchantPlaceUnbindTime) {
                placeCabinetBind.setUnBindTime(merchantPlaceUnbindTime);
            }
            
            stepOneList.add(placeCabinetBind);
        }
        
        List<MerchantPlaceCabinetBind> resultList = new ArrayList<>();
        
        // 再按柜机进行分组，然后去除子集
        Map<Long, List<MerchantPlaceCabinetBind>> groupByCabinetIdMap = stepOneList.stream().filter(Objects::nonNull)
                .collect(Collectors.groupingBy(MerchantPlaceCabinetBind::getCabinetId));
        
        for (Map.Entry<Long, List<MerchantPlaceCabinetBind>> entry : groupByCabinetIdMap.entrySet()) {
            
            List<MerchantPlaceCabinetBind> bindList = entry.getValue();
            
            List<MerchantPlaceCabinetBind> afterRemoveSubsetList = removeSubset(bindList);
            
            resultList.addAll(afterRemoveSubsetList);
        }
        
        return resultList;
    }
    
    private List<MerchantPlaceBind> todayOrThisMonthMerchantPlaceBindList(Long merchantId, Long startTime, Long nowTime) {
        List<MerchantPlaceBind> resultList = new ArrayList<>();
        
        // 绑定状态记录
        MerchantPlaceConditionRequest merchantPlaceBindRequest = MerchantPlaceConditionRequest.builder().merchantId(merchantId).status(MerchantPlaceBindConstant.BIND).build();
        List<MerchantPlaceBind> merchantPlaceBindList = merchantPlaceBindService.listBindRecord(merchantPlaceBindRequest);
        
        if (CollectionUtils.isNotEmpty(merchantPlaceBindList)) {
            for (MerchantPlaceBind merchantPlaceBind : merchantPlaceBindList) {
                Long bindTime = merchantPlaceBind.getBindTime();
                
                if (bindTime < startTime) {
                    merchantPlaceBind.setBindTime(startTime);
                }
                
                // 给绑定状态记录赋值解绑时间：nowTime
                merchantPlaceBind.setUnBindTime(nowTime);
                
                resultList.add(merchantPlaceBind);
            }
        }
        
        log.info("Merchant todayOrThisMonthMerchantPlaceBindList for bind, startTime={}, resultList={}", startTime, resultList);
        
        // 解绑状态记录
        MerchantPlaceConditionRequest merchantPlaceUnbindRequest = MerchantPlaceConditionRequest.builder().merchantId(merchantId).status(MerchantPlaceBindConstant.UN_BIND)
                .startTime(startTime).build();
        List<MerchantPlaceBind> merchantPlaceUnbindList = merchantPlaceBindService.listUnbindRecord(merchantPlaceUnbindRequest);
        
        if (CollectionUtils.isEmpty(merchantPlaceUnbindList)) {
            return resultList;
        }
        
        log.info("Merchant todayOrThisMonthMerchantPlaceBindList for unbind, startTime={}, merchantPlaceUnbindList={}", startTime, merchantPlaceUnbindList);
        
        for (MerchantPlaceBind merchantPlaceUnbind : merchantPlaceUnbindList) {
            Long bindTime = merchantPlaceUnbind.getBindTime();
            
            if (bindTime < startTime) {
                merchantPlaceUnbind.setBindTime(startTime);
            }
            
            resultList.add(merchantPlaceUnbind);
        }
        
        log.info("Merchant todayOrThisMonthMerchantPlaceBindList, startTime={}, resultList={}", startTime, resultList);
        
        return resultList;
    }
    
    private List<MerchantPlaceBind> yesterdayOrLastMonthMerchantPlaceBindList(Long merchantId, Long lastMonthStartTime, Long lastMonthEndTime) {
        List<MerchantPlaceBind> resultList = new ArrayList<>();
        
        // 绑定状态记录
        MerchantPlaceConditionRequest merchantPlaceBindRequest = MerchantPlaceConditionRequest.builder().merchantId(merchantId).status(MerchantPlaceBindConstant.BIND)
                .endTime(lastMonthEndTime).build();
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
        MerchantPlaceConditionRequest merchantPlaceUnbindRequest = MerchantPlaceConditionRequest.builder().merchantId(merchantId).status(MerchantPlaceBindConstant.UN_BIND)
                .startTime(lastMonthStartTime).endTime(lastMonthEndTime).build();
        List<MerchantPlaceBind> merchantPlaceUnbindList = merchantPlaceBindService.listUnbindRecord(merchantPlaceUnbindRequest);
        
        if (CollectionUtils.isNotEmpty(merchantPlaceUnbindList)) {
            for (MerchantPlaceBind merchantPlaceUnbind : merchantPlaceUnbindList) {
                Long bindTime = merchantPlaceUnbind.getBindTime();
                Long unBindTime = merchantPlaceUnbind.getUnBindTime();
                
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
        log.info("Merchant powerData getTotalPower merchantId={}, cabinetIds={}", merchantId, cabinetIds);
        
        //两个月前的数据（来源于历史表，定时任务）
        MerchantPowerPeriodVO preTwoMonthPower = merchantCabinetPowerMonthRecordProService.sumMonthPower(cabinetIds, null, merchantId);
        
        log.info("Merchant powerData getTotalPower merchantId={}, cabinetIds={}, preTwoMonthPower={}", merchantId, cabinetIds, preTwoMonthPower);
        
        // 上月数据
        MerchantPowerPeriodVO lastMonthPower = getLastMonthPower(tenantId, merchantId, cabinetIds);
        
        log.info("Merchant powerData getTotalPower merchantId={}, cabinetIds={}, lastMonthPower={}", merchantId, cabinetIds, lastMonthPower);
        
        // 本月数据
        MerchantPowerPeriodVO thisMonthPower = getThisMonthPower(tenantId, merchantId, cabinetIds);
        
        log.info("Merchant powerData getTotalPower merchantId={}, cabinetIds={}, thisMonthPower={}", merchantId, cabinetIds, thisMonthPower);
        
        MerchantPowerPeriodVO powerVO = new MerchantPowerPeriodVO();
        
        BigDecimal preTwoMonthPowerData = getSafeBigDecimal(preTwoMonthPower, MerchantPowerPeriodVO::getPower);
        BigDecimal preTwoMonthChargeData = getSafeBigDecimal(preTwoMonthPower, MerchantPowerPeriodVO::getCharge);
        BigDecimal lastMonthPowerData = getSafeBigDecimal(lastMonthPower, MerchantPowerPeriodVO::getPower);
        BigDecimal lastMonthChargeData = getSafeBigDecimal(lastMonthPower, MerchantPowerPeriodVO::getCharge);
        BigDecimal thisMonthPowerData = getSafeBigDecimal(thisMonthPower, MerchantPowerPeriodVO::getPower);
        BigDecimal thisMonthChargeData = getSafeBigDecimal(thisMonthPower, MerchantPowerPeriodVO::getCharge);
        
        powerVO.setPower(preTwoMonthPowerData.add(lastMonthPowerData).add(thisMonthPowerData));
        powerVO.setCharge(preTwoMonthChargeData.add(lastMonthChargeData).add(thisMonthChargeData));
        
        log.info("Merchant powerData getTotalPower merchantId={}, powerVO={}", merchantId, powerVO);
        
        return powerVO;
    }
    
    @Slave
    @Override
    public MerchantProPowerLineVO lineData(MerchantCabinetPowerRequest request) {
        log.info("Merchant powerData lineData......");
        
        // 查询月份
        List<String> monthList = getMonthList(request.getStartTime(), request.getEndTime());
        if (CollectionUtils.isEmpty(monthList)) {
            log.warn("Merchant power  lineData, monthList is empty, uid={}, startTime={}, endTime={}", request.getUid(), request.getStartTime(), request.getEndTime());
            return null;
        }
        
        // 初始化
        MerchantProPowerLineVO vo = initLineData(monthList);
        
        Merchant merchant = merchantService.queryByUid(request.getUid());
        if (Objects.isNull(merchant)) {
            log.warn("Merchant power lineData, merchant not exist, uid={}", request.getUid());
            return vo;
        }
        
        request.setMerchantId(merchant.getId());
        
        //获取要查询的柜机
        List<Long> cabinetIds = getStaticsCabinetIds(request);
        if (CollectionUtils.isEmpty(cabinetIds)) {
            log.warn("Merchant power lineData, cabinetIds is empty, uid={}", request.getUid());
            return vo;
        }
        
        String lastMonthDate = DateUtils.getMonthDate(1L);
        // 移除上个月
        monthList.remove(lastMonthDate);
        
        // 1.统计2个月前的历史数据
        List<MerchantProPowerLineDataVO> powerList = new ArrayList<>();
        List<MerchantProPowerChargeLineDataVO> chargeList = new ArrayList<>();
        
        for (String monthDate : monthList) {
            if (!monthDate.matches(DateUtils.GREP_YEAR_MONTH)) {
                log.warn("Merchant power lineData, monthDate not correct, monthDate={}", monthDate);
                break;
            }
            
            String monthDateDB = monthDate + "-01";
            
            MerchantPowerPeriodVO merchantPowerPeriodVO = merchantCabinetPowerMonthRecordProService.sumMonthPower(cabinetIds, List.of(monthDateDB), merchant.getId());
            
            BigDecimal monthPowerData = getSafeBigDecimal(merchantPowerPeriodVO, MerchantPowerPeriodVO::getPower).setScale(2, RoundingMode.HALF_UP);
            BigDecimal monthChargeData = getSafeBigDecimal(merchantPowerPeriodVO, MerchantPowerPeriodVO::getCharge).setScale(2, RoundingMode.HALF_UP);
            
            // 电量
            MerchantProPowerLineDataVO power = new MerchantProPowerLineDataVO();
            power.setMonthDate(monthDate);
            power.setPower(monthPowerData);
            
            powerList.add(power);
            
            // 电费
            MerchantProPowerChargeLineDataVO charge = new MerchantProPowerChargeLineDataVO();
            charge.setMonthDate(monthDate);
            charge.setCharge(monthChargeData);
            
            chargeList.add(charge);
        }
        
        // 2.实时获取上个月的数据
        MerchantPowerPeriodVO lastMonthPower = getLastMonthPower(TenantContextHolder.getTenantId(), request.getMerchantId(), cabinetIds);
        
        BigDecimal lastMonthPowerData = getSafeBigDecimal(lastMonthPower, MerchantPowerPeriodVO::getPower).setScale(2, RoundingMode.HALF_UP);
        BigDecimal lastMonthChargeData = getSafeBigDecimal(lastMonthPower, MerchantPowerPeriodVO::getCharge).setScale(2, RoundingMode.HALF_UP);
        
        // 电量
        MerchantProPowerLineDataVO power = new MerchantProPowerLineDataVO();
        power.setMonthDate(lastMonthDate);
        power.setPower(lastMonthPowerData);
        powerList.add(power);
        
        // 电费
        MerchantProPowerChargeLineDataVO charge = new MerchantProPowerChargeLineDataVO();
        charge.setMonthDate(lastMonthDate);
        charge.setCharge(lastMonthChargeData);
        chargeList.add(charge);
        
        vo.setPowerList(powerList);
        vo.setChargeList(chargeList);
        
        log.info("Merchant powerData lineData......vo={}", vo);
        return vo;
    }
    
    private MerchantProPowerLineVO initLineData(List<String> monthList) {
        MerchantProPowerLineVO vo = new MerchantProPowerLineVO();
        List<MerchantProPowerLineDataVO> powerList = new ArrayList<>();
        List<MerchantProPowerChargeLineDataVO> chargeList = new ArrayList<>();
        
        for (String monthDate : monthList) {
            MerchantProPowerLineDataVO powerData = new MerchantProPowerLineDataVO();
            powerData.setMonthDate(monthDate);
            powerData.setPower(BigDecimal.ZERO);
            powerList.add(powerData);
            
            MerchantProPowerChargeLineDataVO chargeData = new MerchantProPowerChargeLineDataVO();
            chargeData.setMonthDate(monthDate);
            chargeData.setCharge(BigDecimal.ZERO);
            chargeList.add(chargeData);
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
            log.warn("Merchant power cabinetPowerDetail, merchant not exist, uid={}", request.getUid());
            return Collections.emptyList();
        }
        
        Integer tenantId = TenantContextHolder.getTenantId();
        if (Objects.nonNull(tenantId) && !Objects.equals(tenantId, merchant.getTenantId())) {
            log.warn("Merchant power error, tenant not exist, uid={}", request.getUid());
            return Collections.emptyList();
        }
        
        request.setMerchantId(merchant.getId());
        
        // 查询的柜机
        Long cabinetId = request.getCabinetId();
        ElectricityCabinet electricityCabinet = electricityCabinetService.queryByIdFromCache(cabinetId.intValue());
        if (Objects.isNull(electricityCabinet)) {
            log.warn("Merchant power cabinetPowerDetail, cabinet not exist, cabinetId={}", cabinetId);
            return Collections.emptyList();
        }
        
        //查询的月份
        String monthDate = request.getMonthDate();
        if (!monthDate.matches(DateUtils.GREP_YEAR_MONTH)) {
            return Collections.emptyList();
        }
        
        String thisMonthDate = DateUtils.getMonthDate(NumberConstant.ZERO_L);
        String lastMonthDate = DateUtils.getMonthDate(NumberConstant.ONE_L);
        
        // 本月第一天0点
        Long thisMonthStartTime = DateUtils.getBeforeMonthFirstDayTimestamp(NumberConstant.ZERO);
    
        List<MerchantProCabinetPowerDetailVO> resultList = new ArrayList<>();
        
        // 如果是本月
        if (Objects.equals(thisMonthDate, monthDate)) {
            long nowTime = System.currentTimeMillis();
    
            resultList = getThisMonthPowerForCabinetDetail(tenantId, merchant.getId(), cabinetId, monthDate, thisMonthStartTime, nowTime);
            
        } else if (Objects.equals(lastMonthDate, monthDate)) {
            // 如果是上月
            //上月第一天0点
            Long lastMonthStartTime = DateUtils.getBeforeMonthFirstDayTimestamp(NumberConstant.ONE);
            // 上月最后一天23:59:59
            long lastMonthEndTime = DateUtils.getBeforeMonthLastDayTimestamp(NumberConstant.ONE);
    
            resultList = getLastMonthPowerForCabinetDetail(tenantId, merchant.getId(), cabinetId, monthDate, lastMonthStartTime, lastMonthEndTime, thisMonthStartTime);
            
        } else {
            // 如果是2个月前,通过历史数据查询
            List<MerchantCabinetPowerMonthDetailProHistory> preTwoMonthPowerDetailList = merchantCabinetPowerMonthDetailProHistoryService.listByMonth(cabinetId,
                    List.of(monthDate + "-01"), merchant.getId());
            
            if (CollectionUtils.isEmpty(preTwoMonthPowerDetailList)) {
                return Collections.emptyList();
            }
    
            resultList = preTwoMonthPowerDetailList.parallelStream().map(detailPro -> {
                Long placeId = detailPro.getPlaceId();
                Integer bindStatus = detailPro.getCabinetMerchantBindStatus();
                
                MerchantProCabinetPowerDetailVO detailVO = MerchantProCabinetPowerDetailVO.builder().monthDate(monthDate).cabinetId(detailPro.getEid())
                        .cabinetName(electricityCabinet.getName()).sn(electricityCabinet.getSn()).power(detailPro.getSumPower()).charge(detailPro.getSumCharge())
                        .startTime(detailPro.getBeginTime()).placeId(placeId)
                        .placeName(Optional.ofNullable(merchantPlaceService.queryByIdFromCache(placeId)).orElse(new MerchantPlace()).getName()).bindStatus(bindStatus).build();
                
                if (Objects.equals(bindStatus, MerchantPlaceBindConstant.UN_BIND)) {
                    detailVO.setEndTime(detailPro.getEndTime());
                }
                
                return detailVO;
                
            }).collect(Collectors.toList());
        }
        
        if (CollectionUtils.isEmpty(resultList)) {
            return Collections.emptyList();
        }
        
        // resultList 按placeId进行分组，组内元素排序规则为：bindStatus=0的放在最前面，其它的按照startTime倒叙
        return resultList.stream().collect(Collectors.groupingBy(MerchantProCabinetPowerDetailVO::getPlaceId, Collectors.collectingAndThen(Collectors.toList(), list -> {
            list.sort(Comparator.comparing((MerchantProCabinetPowerDetailVO r) -> Objects.equals(r.getBindStatus(), MerchantPlaceBindConstant.BIND) ? 0 : 1)
                    .thenComparing(MerchantProCabinetPowerDetailVO::getStartTime, Comparator.reverseOrder()));
            return list;
        }))).values().stream().flatMap(Collection::stream).collect(Collectors.toList());
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
        log.info("Merchant powerData cabinetPowerList......");
        
        Merchant merchant = merchantService.queryByUid(request.getUid());
        if (Objects.isNull(merchant)) {
            log.warn("Merchant power cabinetPowerList merchant not exist, uid={}", request.getUid());
            return null;
        }
        
        Integer tenantId = TenantContextHolder.getTenantId();
        if (Objects.nonNull(tenantId) && !Objects.equals(tenantId, merchant.getTenantId())) {
            log.warn("Merchant power cabinetPowerList tenant not exist, uid={}", request.getUid());
            return null;
        }
        
        request.setMerchantId(merchant.getId());
        
        //获取要查询的柜机
        List<Long> cabinetIds = getStaticsCabinetIds(request);
        if (CollectionUtils.isEmpty(cabinetIds)) {
            log.warn("Merchant power cabinetPowerList cabinetIds is empty, uid={}", request.getUid());
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
                BigDecimal todayPowerSum = todayPower.stream().filter(Objects::nonNull).map(MerchantProLivePowerVO::getPower).reduce(BigDecimal.ZERO, BigDecimal::add);
                todayPowerSum = todayPowerSum.setScale(2, RoundingMode.HALF_UP);
                
                BigDecimal todayChargeSum = todayPower.stream().filter(Objects::nonNull).map(MerchantProLivePowerVO::getCharge).reduce(BigDecimal.ZERO, BigDecimal::add);
                todayChargeSum = todayChargeSum.setScale(2, RoundingMode.HALF_UP);
                
                // 本月电量
                List<MerchantProLivePowerVO> thisMonthPower = thisMonthPowerList.parallelStream().filter(e -> Objects.equals(e.getEid(), cabinetId)).collect(Collectors.toList());
                BigDecimal thisMonthPowerSum = thisMonthPower.stream().filter(Objects::nonNull).map(MerchantProLivePowerVO::getPower).reduce(BigDecimal.ZERO, BigDecimal::add);
                thisMonthPowerSum = thisMonthPowerSum.setScale(2, RoundingMode.HALF_UP);
                
                BigDecimal thisMonthChargeSum = thisMonthPower.stream().filter(Objects::nonNull).map(MerchantProLivePowerVO::getCharge).reduce(BigDecimal.ZERO, BigDecimal::add);
                thisMonthChargeSum = thisMonthChargeSum.setScale(2, RoundingMode.HALF_UP);
                
                // 上月电量
                List<MerchantProLivePowerVO> lastMonthPower = lastMonthPowerList.parallelStream().filter(e -> Objects.equals(e.getEid(), cabinetId)).collect(Collectors.toList());
                BigDecimal lastMonthPowerSum = lastMonthPower.stream().filter(Objects::nonNull).map(MerchantProLivePowerVO::getPower).reduce(BigDecimal.ZERO, BigDecimal::add);
                
                BigDecimal lastMonthChargeSum = lastMonthPower.stream().filter(Objects::nonNull).map(MerchantProLivePowerVO::getCharge).reduce(BigDecimal.ZERO, BigDecimal::add);
                lastMonthChargeSum = lastMonthChargeSum.setScale(2, RoundingMode.HALF_UP);
                
                // 本年电量
                BigDecimal thisYearPower = thisMonthPowerSum.add(lastMonthPowerSum);
                BigDecimal thisYearCharge = thisMonthChargeSum.add(lastMonthChargeSum);
                
                List<String> preTwoMonthList;
                if (monthList.size() > NumberConstant.TWO) {
                    List<String> subMonthList = monthList.subList(0, monthList.size() - NumberConstant.TWO);
                    preTwoMonthList = getPreTwoMonthList(subMonthList);
                    
                    MerchantPowerPeriodVO preTwoMonthPowerPeriod = merchantCabinetPowerMonthRecordProService.sumMonthPower(List.of(cabinetId), preTwoMonthList, merchant.getId());
                    if (Objects.nonNull(preTwoMonthPowerPeriod)) {
                        if (Objects.nonNull(preTwoMonthPowerPeriod.getPower())) {
                            BigDecimal preTwoMonthPower = preTwoMonthPowerPeriod.getPower().setScale(2, RoundingMode.HALF_UP);
                            thisYearPower = thisYearPower.add(preTwoMonthPower);
                        }
                        
                        if (Objects.nonNull(preTwoMonthPowerPeriod.getCharge())) {
                            BigDecimal preTwoMonthCharge = preTwoMonthPowerPeriod.getCharge().setScale(2, RoundingMode.HALF_UP);
                            thisYearCharge = thisYearCharge.add(preTwoMonthCharge);
                        }
                    }
                }
                
                ElectricityCabinet electricityCabinet = electricityCabinetService.queryByIdFromCache(cabinetId.intValue());
                
                //封装结果
                MerchantProCabinetPowerVO merchantProCabinetPowerVO = new MerchantProCabinetPowerVO();
                merchantProCabinetPowerVO.setCabinetId(cabinetId);
                merchantProCabinetPowerVO.setCabinetName(Objects.isNull(electricityCabinet) ? "" : electricityCabinet.getName());
                merchantProCabinetPowerVO.setTodayPower(todayPowerSum);
                merchantProCabinetPowerVO.setTodayCharge(todayChargeSum);
                merchantProCabinetPowerVO.setThisMonthPower(thisMonthPowerSum);
                merchantProCabinetPowerVO.setThisMonthCharge(thisMonthChargeSum);
                merchantProCabinetPowerVO.setThisYearPower(thisYearPower);
                merchantProCabinetPowerVO.setThisYearCharge(thisYearCharge);
                merchantProCabinetPowerVO.setTime(Objects.isNull(electricityCabinet) ? NumberConstant.ZERO : electricityCabinet.getCreateTime());
                
                cabinetPowerList.add(merchantProCabinetPowerVO);
                
                log.info("Merchant power cabinetPowerList electricityCabinet={}, merchantProCabinetPowerVO={}", electricityCabinet, merchantProCabinetPowerVO);
            }
        }
        
        if (CollectionUtils.isEmpty(cabinetPowerList)) {
            return Collections.emptyList();
        }
        
        // cabinetPowerList 根据time进行倒叙排序
        cabinetPowerList.sort((o1, o2) -> o2.getTime().compareTo(o1.getTime()));
        
        log.info("Merchant power cabinetPowerList merchantId={}, cabinetPowerList={}", merchant.getId(), cabinetPowerList);
        
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
            log.warn("Merchant power isShowPowerPage, merchant not exist, uid={}", uid);
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
            log.warn("Merchant power listPlaceAndCabinetByMerchantId, merchant not exist, uid={}", uid);
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
            log.warn("Merchant power listCabinetByPlaceId, merchant not exist, uid={}", uid);
            return Collections.emptyList();
        }
        
        List<MerchantPlaceBind> bindList = merchantPlaceBindService.listByMerchantId(merchant.getId(), null);
        if (CollectionUtils.isEmpty(bindList)) {
            log.warn("Merchant power listCabinetByPlaceId, bindList is empty, uid={}, placeId={}", uid, placeId);
            return Collections.emptyList();
        }
        
        // 判断所选场地是否存在
        Set<Long> placeIdSet = bindList.stream().map(MerchantPlaceBind::getPlaceId).collect(Collectors.toSet());
        if (CollectionUtils.isNotEmpty(placeIdSet) && !placeIdSet.contains(placeId)) {
            log.warn("Merchant power listCabinetByPlaceId, place not exist, uid={}, placeId={}", uid, placeId);
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
        
        List<Long> cabinetIdList = null;
        
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
        
        // 4. 场地为null, 柜机不为null
        if (Objects.isNull(placeId) && Objects.nonNull(cabinetId)) {
            cabinetIdList = List.of(cabinetId);
        }
        
        return cabinetIdList;
    }
    
    private BigDecimal getSafeBigDecimal(MerchantPowerPeriodVO powerData, Function<MerchantPowerPeriodVO, BigDecimal> getter) {
        return Optional.ofNullable(powerData).map(getter).orElse(BigDecimal.ZERO);
    }
    
}
