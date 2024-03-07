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
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
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
            log.info("执行今日电量开始......");
            MerchantPowerPeriodVO todayPower = getTodayPower(tenantId, merchant.getId(), cabinetIds);
            
            Double power = NumberConstant.ZERO_D;
            Double charge = NumberConstant.ZERO_D;
            if (Objects.nonNull(todayPower)) {
                if (Objects.nonNull(todayPower.getPower())) {
                    power = BigDecimal.valueOf(todayPower.getPower()).setScale(2, RoundingMode.HALF_UP).doubleValue();
                }
                if (Objects.nonNull(todayPower.getCharge())) {
                    charge = BigDecimal.valueOf(todayPower.getCharge()).setScale(2, RoundingMode.HALF_UP).doubleValue();
                }
            }
            
            vo.setTodayPower(power);
            vo.setTodayCharge(charge);
            
            log.info("执行今日电量结束......{}", todayPower);
        }, executorService).exceptionally(e -> {
            log.error("Query merchant today power data error! uid={}", request.getUid(), e);
            return null;
        });
        
        // 2.昨日电量
        CompletableFuture<Void> yesterdayPowerFuture = CompletableFuture.runAsync(() -> {
            log.info("执行昨日电量开始......");
            MerchantPowerPeriodVO yesterdayPower = getYesterdayPower(tenantId, merchant.getId(), cabinetIds);
            
            Double power = NumberConstant.ZERO_D;
            Double charge = NumberConstant.ZERO_D;
            if (Objects.nonNull(yesterdayPower)) {
                if (Objects.nonNull(yesterdayPower.getPower())) {
                    power = BigDecimal.valueOf(yesterdayPower.getPower()).setScale(2, RoundingMode.HALF_UP).doubleValue();
                }
                if (Objects.nonNull(yesterdayPower.getCharge())) {
                    charge = BigDecimal.valueOf(yesterdayPower.getCharge()).setScale(2, RoundingMode.HALF_UP).doubleValue();
                }
            }
            
            vo.setYesterdayPower(power);
            vo.setYesterdayCharge(charge);
            
            log.info("执行昨日电量结束......{}", yesterdayPower);
        }, executorService).exceptionally(e -> {
            log.error("Query merchant yesterday power data error! uid={}", request.getUid(), e);
            return null;
        });
        
        // 3.本月电量
        CompletableFuture<Void> thisMonthPowerFuture = CompletableFuture.runAsync(() -> {
            log.info("执行本月电量开始......");
            
            MerchantPowerPeriodVO thisMonthPower = getThisMonthPower(tenantId, merchant.getId(), cabinetIds);
            
            Double power = NumberConstant.ZERO_D;
            Double charge = NumberConstant.ZERO_D;
            if (Objects.nonNull(thisMonthPower)) {
                if (Objects.nonNull(thisMonthPower.getPower())) {
                    power = BigDecimal.valueOf(thisMonthPower.getPower()).setScale(2, RoundingMode.HALF_UP).doubleValue();
                }
                if (Objects.nonNull(thisMonthPower.getCharge())) {
                    charge = BigDecimal.valueOf(thisMonthPower.getCharge()).setScale(2, RoundingMode.HALF_UP).doubleValue();
                }
            }
            
            vo.setThisMonthPower(power);
            vo.setThisMonthCharge(charge);
            
            log.info("执行本月电量结束......{}", thisMonthPower);
        }, executorService).exceptionally(e -> {
            log.error("Query merchant this month power data error! uid={}", request.getUid(), e);
            return null;
        });
        
        // 4.上月电量
        CompletableFuture<Void> lastMonthPowerFuture = CompletableFuture.runAsync(() -> {
            log.info("执行上月电量开始......");
            MerchantPowerPeriodVO lastMonthPower = getLastMonthPower(tenantId, merchant.getId(), cabinetIds);
            
            Double power = NumberConstant.ZERO_D;
            Double charge = NumberConstant.ZERO_D;
            if (Objects.nonNull(lastMonthPower)) {
                if (Objects.nonNull(lastMonthPower.getPower())) {
                    power = BigDecimal.valueOf(lastMonthPower.getPower()).setScale(2, RoundingMode.HALF_UP).doubleValue();
                }
                if (Objects.nonNull(lastMonthPower.getCharge())) {
                    charge = BigDecimal.valueOf(lastMonthPower.getCharge()).setScale(2, RoundingMode.HALF_UP).doubleValue();
                }
            }
            
            vo.setLastMonthPower(power);
            vo.setLastMonthCharge(charge);
            
            log.info("执行上月电量结束......{}", lastMonthPower);
        }, executorService).exceptionally(e -> {
            log.error("Query merchant last month power data error! uid={}", request.getUid(), e);
            return null;
        });
        
        // 5.累计电量
        CompletableFuture<Void> totalPowerFuture = CompletableFuture.runAsync(() -> {
            log.info("执行累计电量开始......");
            MerchantPowerPeriodVO totalPower = getTotalPower(tenantId, merchant.getId(), cabinetIds);
            
            Double power = NumberConstant.ZERO_D;
            Double charge = NumberConstant.ZERO_D;
            if (Objects.nonNull(totalPower.getPower())) {
                power = BigDecimal.valueOf(totalPower.getPower()).setScale(2, RoundingMode.HALF_UP).doubleValue();
            }
            if (Objects.nonNull(totalPower.getCharge())) {
                charge = BigDecimal.valueOf(totalPower.getCharge()).setScale(2, RoundingMode.HALF_UP).doubleValue();
            }
            
            vo.setTotalPower(power);
            vo.setTotalCharge(charge);
            
            log.info("执行累计电量结束......{}", totalPower);
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
        log.info("开始执行今日,merchantId={}, cabinetIds={}", merchantId, cabinetIds);
        
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
        
        log.info("执行今日===商户场地绑定记录===>,merchantId={}, merchantPlaceBindList={}", merchantId, merchantPlaceBindList);
        
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
                log.warn("Merchant getTodayPower placeCabinetBindList is empty! placeId={}, bindTime={}, unBindTime={}", placeId, bindTime, unBindTime);
                continue;
            }
            
            log.info("执行今日===场地柜机绑定记录===>,placeId={}, placeCabinetBindList={}", placeId, placeCabinetBindList);
            
            // 遍历柜机
            List<MerchantProLivePowerVO> periodPowerList = getPeriodPower(tenantId, placeId, cabinetIds, placeCabinetBindList);
            
            log.info("执行今日===遍历柜机===>，placeId={}, cabinetIds={}, periodPowerList={}", placeId, cabinetIds, periodPowerList);
            
            resultList.addAll(periodPowerList);
        }
        
        log.info("执行今日===resultList===>，merchantId={}, resultList={}", merchantId, resultList);
        
        // 封装数据
        MerchantPowerPeriodVO powerVO = new MerchantPowerPeriodVO();
        powerVO.setPower(resultList.stream().filter(Objects::nonNull).mapToDouble(MerchantProLivePowerVO::getPower).sum());
        powerVO.setCharge(resultList.stream().filter(Objects::nonNull).mapToDouble(MerchantProLivePowerVO::getCharge).sum());
        
        log.info("执行今日结束===>，merchantId={}, powerVO={}", merchantId, powerVO);
        
        return powerVO;
    }
    
    private List<MerchantProLivePowerVO> getPeriodPower(Integer tenantId, Long placeId, List<Long> cabinetIds, List<MerchantPlaceCabinetBind> placeCabinetBindList) {
        List<MerchantProLivePowerVO> resultList = new ArrayList<>();
        
        for (Long eid : cabinetIds) {
            // 过滤出该柜机的绑定记录
            List<MerchantPlaceCabinetBind> cabinetBindList = placeCabinetBindList.stream().filter(bind -> Objects.equals(bind.getCabinetId(), eid)).collect(Collectors.toList());
            log.info("Merchant 执行遍历柜机开始......eid={}, cabinetBindList={}", eid, cabinetBindList);
            
            if (CollectionUtils.isEmpty(cabinetBindList)) {
                log.warn("Merchant Power for pro cabinetBindList is empty, eid={}, cabinetBindList={}", eid, cabinetBindList);
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
                powerVO.setPower(Objects.isNull(eleSumPowerVO) ? NumberConstant.ZERO_D : eleSumPowerVO.getSumPower());
                powerVO.setCharge(Objects.isNull(eleSumPowerVO) ? NumberConstant.ZERO_D : eleSumPowerVO.getSumCharge());
                
                resultList.add(powerVO);
            });
        }
        
        log.info("Merchant 执行遍历柜机结束......cabinetIds={}, resultList={}", cabinetIds, resultList);
        
        return resultList;
    }
    
    private List<MerchantProCabinetPowerDetailVO> getPeriodPowerForDetail(Integer tenantId, Long placeId, Long eid, List<MerchantPlaceCabinetBind> placeCabinetBindList,
            String monthDate, Integer bindStatus) {
        
        return placeCabinetBindList.stream().map(cabinetBind -> {
            Long startTime = cabinetBind.getBindTime();
            Long endTime = cabinetBind.getUnBindTime();
            
            return getCabinetPowerDetail(startTime, endTime, eid, placeId, tenantId, monthDate, bindStatus);
        }).collect(Collectors.toList());
        
    }
    
    
    private List<MerchantProLivePowerVO> getTodayPowerForCabinetList(Integer tenantId, Long merchantId, List<Long> cabinetIds) {
        // 今日0点
        Long todayStartTime = DateUtils.getTimeAgoStartTime(NumberConstant.ZERO);
        // 当前时间
        long nowTime = System.currentTimeMillis();
        
        // 获取商户场地绑定记录
        List<MerchantPlaceBind> merchantPlaceBindList = todayOrThisMonthMerchantPlaceBindList(merchantId, todayStartTime, nowTime);
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
            List<MerchantProLivePowerVO> periodPowerList = getPeriodPower(tenantId, placeId, cabinetIds, placeCabinetBindList);
            
            log.info("执行getTodayPower...遍历柜机，placeId={}, cabinetIds={}, periodPowerList={}", placeId, cabinetIds, periodPowerList);
            
            resultList.addAll(periodPowerList);
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
        log.info("开始执行昨日,merchantId={}, cabinetIds={}", merchantId, cabinetIds);
        
        // 昨日0点
        Long yesterdayStartTime = DateUtils.getTimeAgoStartTime(NumberConstant.ONE);
        // 昨日23:59:59
        long yesterdayEndTime = DateUtils.getTimeAgoEndTime(NumberConstant.ONE);
        
        // 获取商户场地绑定记录
        List<MerchantPlaceBind> merchantPlaceBindList = yesterdayOrLastMonthMerchantPlaceBindList(merchantId, yesterdayStartTime, yesterdayEndTime);
        if (CollectionUtils.isEmpty(merchantPlaceBindList)) {
            return null;
        }
        
        log.info("执行昨日===商户场地绑定记录===>,merchantId={}, merchantPlaceBindList={}", merchantId, merchantPlaceBindList);
        
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
            
            log.info("执行昨日===场地柜机绑定记录===>,placeId={}, placeCabinetBindList={}", placeId, placeCabinetBindList);
            
            // 遍历柜机
            List<MerchantProLivePowerVO> periodPowerList = getPeriodPower(tenantId, placeId, cabinetIds, placeCabinetBindList);
            
            log.info("执行昨日===遍历柜机===>，placeId={}, cabinetIds={}, periodPowerList={}", placeId, cabinetIds, periodPowerList);
            
            resultList.addAll(periodPowerList);
        }
        
        log.info("执行昨日===resultList===>，merchantId={}, resultList={}", merchantId, resultList);
        
        // 封装数据
        MerchantPowerPeriodVO powerVO = new MerchantPowerPeriodVO();
        powerVO.setPower(resultList.stream().filter(Objects::nonNull).mapToDouble(MerchantProLivePowerVO::getPower).sum());
        powerVO.setCharge(resultList.stream().filter(Objects::nonNull).mapToDouble(MerchantProLivePowerVO::getCharge).sum());
        
        log.info("执行昨日结束===>，merchantId={}, powerVO={}", merchantId, powerVO);
        return powerVO;
    }
    
    private MerchantPowerPeriodVO getThisMonthPower(Integer tenantId, Long merchantId, List<Long> cabinetIds) {
        log.info("开始执行本月,merchantId={}, cabinetIds={}", merchantId, cabinetIds);
        
        // 本月第一天0点
        Long thisMonthStartTime = DateUtils.getBeforeMonthFirstDayTimestamp(NumberConstant.ZERO);
        long nowTime = System.currentTimeMillis();
        
        // 获取商户场地绑定记录
        List<MerchantPlaceBind> merchantPlaceBindList = todayOrThisMonthMerchantPlaceBindList(merchantId, thisMonthStartTime, nowTime);
        if (CollectionUtils.isEmpty(merchantPlaceBindList)) {
            return null;
        }
        
        log.info("执行本月===商户场地绑定记录===>,merchantId={}, merchantPlaceBindList={}", merchantId, merchantPlaceBindList);
        
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
            
            // 场地柜机绑定记录
            List<MerchantPlaceCabinetBind> allCabinetBindListByPlace = new ArrayList<>();
            
            for (MerchantPlaceBind placeBind : placeBindList) {
                Long bindTime = placeBind.getBindTime();
                Long unBindTime = placeBind.getUnBindTime();
                
                List<MerchantPlaceCabinetBind> cabinetBindList = getThisMonthPlaceCabinetBindList(placeId, bindTime, unBindTime, null);
                if (CollectionUtils.isEmpty(cabinetBindList)) {
                    continue;
                }
                
                allCabinetBindListByPlace.addAll(cabinetBindList);
            }
            
            // allCabinetBindListByPlace去除子集
            List<MerchantPlaceCabinetBind> cabinetBindListAfterRemoveSubset = removeSubSet(allCabinetBindListByPlace);
            
            // 遍历柜机
            List<MerchantProLivePowerVO> periodPowerList = getPeriodPower(tenantId, placeId, cabinetIds, cabinetBindListAfterRemoveSubset);
            
            resultList.addAll(periodPowerList);
            
            log.info("执行本月===遍历柜机===>，placeId={}, cabinetIds={}, periodPowerList={}", placeId, cabinetIds, periodPowerList);
            
        }
        
        log.info("执行本月===resultList===>，merchantId={}, resultList={}", merchantId, resultList);
        
        // 封装数据
        MerchantPowerPeriodVO powerVO = new MerchantPowerPeriodVO();
        powerVO.setPower(resultList.stream().filter(Objects::nonNull).mapToDouble(MerchantProLivePowerVO::getPower).sum());
        powerVO.setCharge(resultList.stream().filter(Objects::nonNull).mapToDouble(MerchantProLivePowerVO::getCharge).sum());
        
        log.info("执行本月结束===>，merchantId={}, powerVO={}", merchantId, powerVO);
        
        return powerVO;
    }
    
    private List<MerchantPlaceCabinetBind> removeSubSet(List<MerchantPlaceCabinetBind> cabinetBindList) {
        List<MerchantPlaceCabinetBind> resultList = new ArrayList<>();
        
        for (MerchantPlaceCabinetBind current : cabinetBindList) {
            // 标记是否找到包含当前元素的时间段
            boolean contained = false;
            
            // 遍历结果集中的每个元素，查找与当前元素有时间交集的情况
            for (Iterator<MerchantPlaceCabinetBind> iterator = resultList.iterator(); iterator.hasNext(); ) {
                MerchantPlaceCabinetBind previous = iterator.next();
                
                // 如果当前元素被之前元素的时间段完全包含，则移除之前的元素，并标记找到包含关系
                if (previous.getBindTime() <= current.getBindTime() && current.getUnBindTime() <= previous.getUnBindTime()) {
                    contained = true;
                    iterator.remove();
                    break;
                }
                
                // 如果当前元素包含之前元素的时间段，则移除之前的元素
                if (current.getBindTime() <= previous.getBindTime() && current.getUnBindTime() >= previous.getUnBindTime()) {
                    iterator.remove();
                }
            }
            
            // 如果当前元素没有被任何已存在的时间段包含，则将其添加到结果集中
            if (!contained) {
                resultList.add(current);
            }
        }
        
        return resultList;
    }
    
    private List<MerchantProCabinetPowerDetailVO> removeSubSetForDetail(List<MerchantProCabinetPowerDetailVO> cabinetBindList) {
        List<MerchantProCabinetPowerDetailVO> resultList = new ArrayList<>();
        
        for (MerchantProCabinetPowerDetailVO current : cabinetBindList) {
            // 标记是否找到包含当前元素的时间段
            boolean contained = false;
            
            // 遍历结果集中的每个元素，查找与当前元素有时间交集的情况
            for (Iterator<MerchantProCabinetPowerDetailVO> iterator = resultList.iterator(); iterator.hasNext(); ) {
                MerchantProCabinetPowerDetailVO previous = iterator.next();
                
                // 如果当前元素被之前元素的时间段完全包含，则移除之前的元素，并标记找到包含关系
                if (previous.getStartTime() <= current.getStartTime() && current.getEndTime() <= previous.getEndTime()) {
                    contained = true;
                    iterator.remove();
                    break;
                }
                
                // 如果当前元素包含之前元素的时间段，则移除之前的元素
                if (current.getStartTime() <= previous.getStartTime() && current.getEndTime() >= previous.getEndTime()) {
                    iterator.remove();
                }
            }
            
            // 如果当前元素没有被任何已存在的时间段包含，则将其添加到结果集中
            if (!contained) {
                resultList.add(current);
            }
        }
        
        return resultList;
    }
    
    private List<MerchantProCabinetPowerDetailVO> getLiveMonthPowerForCabinetDetail(Integer tenantId, Long merchantId, Long cabinetId, String monthDate, Long startTime,
            Long endTime) {
        // 获取商户场地绑定记录
        List<MerchantPlaceBind> merchantPlaceBindList = todayOrThisMonthMerchantPlaceBindList(merchantId, startTime, endTime);
        if (CollectionUtils.isEmpty(merchantPlaceBindList)) {
            return Collections.emptyList();
        }
        
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
            
            List<MerchantProCabinetPowerDetailVO> merchantDetailList = new ArrayList<>();
            
            for (MerchantPlaceBind placeBind : placeBindList) {
                Integer status = placeBind.getType();
                Long bindTime = placeBind.getBindTime();
                Long unBindTime = placeBind.getUnBindTime();
                
                // 获取场地柜机绑定记录
                List<MerchantPlaceCabinetBind> placeCabinetBindList = getThisMonthPlaceCabinetBindList(placeId, bindTime, unBindTime, Set.of(cabinetId));
                if (CollectionUtils.isEmpty(placeCabinetBindList)) {
                    continue;
                }
                
                // 遍历柜机
                List<MerchantProCabinetPowerDetailVO> periodPowerList = getPeriodPowerForDetail(tenantId, placeId, cabinetId, placeCabinetBindList, monthDate, status);
                
                merchantDetailList.addAll(periodPowerList);
            }
            
            //去除子集，合并连续时间段
            List<MerchantProCabinetPowerDetailVO> removeSubSetDetailList = removeSubSetForDetail(merchantDetailList);
            
            // 合并连续时间段的记录（前一个时间段的endTime和后一个时间段的startTime是同一天）
            List<MerchantProCabinetPowerDetailVO> afterMergeDetailResultList = mergeSerialTimeDetail(removeSubSetDetailList);
            
            if (CollectionUtils.isNotEmpty(afterMergeDetailResultList)) {
                resultList.addAll(afterMergeDetailResultList);
            }
        }
        
        return resultList;
    }
    
    private MerchantProCabinetPowerDetailVO getCabinetPowerDetail(Long startTime, Long endTime, Long eid, Long placeId, Integer tenantId, String monthDate,
            Integer merchantCabinetBindStatus) {
        // 查询电量
        EleSumPowerVO eleSumPowerVO = elePowerService.listByCondition(startTime, endTime, List.of(eid), tenantId);
        
        ElectricityCabinet electricityCabinet = electricityCabinetService.queryByIdFromCache(eid.intValue());
        
        return MerchantProCabinetPowerDetailVO.builder().monthDate(monthDate).cabinetName(Optional.ofNullable(electricityCabinet).orElse(new ElectricityCabinet()).getName())
                .sn(Optional.ofNullable(electricityCabinet).orElse(new ElectricityCabinet()).getSn())
                .power(Objects.isNull(eleSumPowerVO) ? NumberConstant.ZERO_D : eleSumPowerVO.getSumPower())
                .charge(Objects.isNull(eleSumPowerVO) ? NumberConstant.ZERO_D : eleSumPowerVO.getSumCharge()).startTime(startTime).endTime(endTime).placeId(placeId)
                .placeName(Optional.ofNullable(merchantPlaceService.queryByIdFromCache(placeId)).orElse(new MerchantPlace()).getName()).bindStatus(merchantCabinetBindStatus)
                .build();
    }
    
    /**
     * 合并连续时间段的记录
     */
    private List<MerchantProCabinetPowerDetailVO> mergeSerialTimeDetail(List<MerchantProCabinetPowerDetailVO> detailList) {
        //去重
        detailList = detailList.stream().distinct().collect(Collectors.toList());
        // 排序
        detailList.sort(Comparator.comparing(MerchantProCabinetPowerDetailVO::getEndTime));
        
        // 合并时间段
        
        List<MerchantProCabinetPowerDetailVO> resultList = new ArrayList<>();
        for (int i = 0; i < detailList.size() - 1; i++) {
            MerchantProCabinetPowerDetailVO current = detailList.get(i);
            MerchantProCabinetPowerDetailVO next = detailList.get(i + 1);
            
            if (Objects.equals(DateUtils.getTimeByTimeStamp(current.getEndTime()), DateUtils.getTimeByTimeStamp(next.getStartTime()))) {
                current.setEndTime(next.getEndTime());
                
                double currentPower = Objects.isNull(current.getPower()) ? NumberConstant.ZERO_D : current.getPower();
                double currentCharge = Objects.isNull(current.getCharge()) ? NumberConstant.ZERO : current.getCharge();
                double nextPower = Objects.isNull(next.getPower()) ? NumberConstant.ZERO : next.getPower();
                double nextCharge = Objects.isNull(next.getCharge()) ? NumberConstant.ZERO : next.getCharge();
                
                current.setPower(currentPower + nextPower);
                current.setCharge(currentCharge + nextCharge);
                
                detailList.remove(next);
            }
        }
        
        return detailList;
        
    }
    
    private List<MerchantProLivePowerVO> getThisMonthPowerForCabinetList(Integer tenantId, Long merchantId, List<Long> cabinetIds) {
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
        
        // 遍历场地
        for (MerchantPlaceBind merchantPlaceBind : merchantPlaceBindList) {
            Long placeId = merchantPlaceBind.getPlaceId();
            Long bindTime = merchantPlaceBind.getBindTime();
            Long unBindTime = merchantPlaceBind.getUnBindTime();
            
            // 获取场地柜机绑定记录
            List<MerchantPlaceCabinetBind> placeCabinetBindList = getThisMonthPlaceCabinetBindList(placeId, bindTime, unBindTime, null);
            if (CollectionUtils.isEmpty(placeCabinetBindList)) {
                continue;
            }
            
            // 遍历柜机
            List<MerchantProLivePowerVO> periodPowerList = getPeriodPower(tenantId, placeId, cabinetIds, placeCabinetBindList);
            
            log.info("执行getTodayPower...遍历柜机，placeId={}, cabinetIds={}, periodPowerList={}", placeId, cabinetIds, periodPowerList);
            
            resultList.addAll(periodPowerList);
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
        log.info("开始执行上月,merchantId={}, cabinetIds={}", merchantId, cabinetIds);
        
        //上月第一天0点
        Long lastMonthStartTime = DateUtils.getBeforeMonthFirstDayTimestamp(NumberConstant.ONE);
        // 上月最后一天23:59:59
        long lastMonthEndTime = DateUtils.getBeforeMonthLastDayTimestamp(NumberConstant.ONE);
        
        // 获取商户场地绑定记录
        List<MerchantPlaceBind> merchantPlaceBindList = yesterdayOrLastMonthMerchantPlaceBindList(merchantId, lastMonthStartTime, lastMonthEndTime);
        if (CollectionUtils.isEmpty(merchantPlaceBindList)) {
            return null;
        }
        
        log.info("执行上月===商户场地绑定记录===>,merchantId={}, merchantPlaceBindList={}", merchantId, merchantPlaceBindList);
        
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
            
            log.info("执行上月===场地柜机绑定记录===>,placeId={}, placeCabinetBindList={}", placeId, placeCabinetBindList);
            
            // 遍历柜机
            List<MerchantProLivePowerVO> periodPowerList = getPeriodPower(tenantId, placeId, cabinetIds, placeCabinetBindList);
            
            log.info("执行上月===遍历柜机===>，placeId={}, cabinetIds={}, periodPowerList={}", placeId, cabinetIds, periodPowerList);
            
            resultList.addAll(periodPowerList);
        }
        
        log.info("执行上月===resultList===>，merchantId={}, resultList={}", merchantId, resultList);
        
        // 封装数据
        MerchantPowerPeriodVO powerVO = new MerchantPowerPeriodVO();
        powerVO.setPower(resultList.stream().filter(Objects::nonNull).mapToDouble(MerchantProLivePowerVO::getPower).sum());
        powerVO.setCharge(resultList.stream().filter(Objects::nonNull).mapToDouble(MerchantProLivePowerVO::getCharge).sum());
        
        log.info("执行上月结束===>，merchantId={}, powerVO={}", merchantId, powerVO);
        
        return powerVO;
    }
    
    private List<MerchantProLivePowerVO> getLastMonthPowerForCabinetList(Integer tenantId, Long merchantId, List<Long> cabinetIds) {
        //上月第一天0点
        Long lastMonthStartTime = DateUtils.getBeforeMonthFirstDayTimestamp(NumberConstant.ONE);
        // 上月最后一天23:59:59
        long lastMonthEndTime = DateUtils.getBeforeMonthLastDayTimestamp(NumberConstant.ONE);
        
        // 获取商户场地绑定记录
        List<MerchantPlaceBind> merchantPlaceBindList = yesterdayOrLastMonthMerchantPlaceBindList(merchantId, lastMonthStartTime, lastMonthEndTime);
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
            List<MerchantProLivePowerVO> periodPowerList = getPeriodPower(tenantId, placeId, cabinetIds, placeCabinetBindList);
            
            log.info("执行getTodayPower...遍历柜机，placeId={}, cabinetIds={}, periodPowerList={}", placeId, cabinetIds, periodPowerList);
            
            resultList.addAll(periodPowerList);
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
                .status(MerchantPlaceCabinetBindConstant.STATUS_UNBIND).startTime(merchantPlaceBindTime).build();
        List<MerchantPlaceCabinetBind> cabinetUnbindList = merchantPlaceCabinetBindService.listUnbindRecord(placeCabinetUnbindRequest);
        
        if (CollectionUtils.isEmpty(cabinetUnbindList)) {
            return allPlaceCabinetBindList;
        }
        
        allPlaceCabinetBindList.addAll(cabinetUnbindList);
        
        // 先掐头去尾
        List<MerchantPlaceCabinetBind> stepOneList = new ArrayList<>();
        for (MerchantPlaceCabinetBind placeCabinetBind : allPlaceCabinetBindList) {
            Long bindTime = placeCabinetBind.getBindTime();
            
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
        
        return resultList;
    }
    
    private List<MerchantPlaceCabinetBind> getYesterdayPlaceCabinetBindList(Long placeId, Long merchantPlaceBindTime, Long merchantPlaceUnbindTime) {
        List<MerchantPlaceCabinetBind> resultList = new ArrayList<>();
        List<MerchantPlaceCabinetBind> allPlaceCabinetBindList = new ArrayList<>();
        
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
                
                allPlaceCabinetBindList.add(placeCabinetBind);
            }
        }
        
        // 解绑状态记录
        MerchantPlaceCabinetConditionRequest placeCabinetUnbindRequest = MerchantPlaceCabinetConditionRequest.builder().placeId(placeId)
                .status(MerchantPlaceCabinetBindConstant.STATUS_UNBIND).startTime(merchantPlaceBindTime).endTime(merchantPlaceUnbindTime).build();
        List<MerchantPlaceCabinetBind> cabinetUnbindList = merchantPlaceCabinetBindService.listUnbindRecord(placeCabinetUnbindRequest);
        
        if (CollectionUtils.isEmpty(cabinetUnbindList)) {
            return allPlaceCabinetBindList;
        }
        
        allPlaceCabinetBindList.addAll(cabinetUnbindList);
        
        // 先掐头去尾
        List<MerchantPlaceCabinetBind> stepOneList = new ArrayList<>();
        for (MerchantPlaceCabinetBind placeCabinetBind : allPlaceCabinetBindList) {
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
    
    private List<MerchantPlaceCabinetBind> getThisMonthPlaceCabinetBindList(Long placeId, Long merchantPlaceBindTime, Long merchantPlaceUnbindTime, Set<Long> cabinetIds) {
        List<MerchantPlaceCabinetBind> resultList = new ArrayList<>();
        List<MerchantPlaceCabinetBind> allPlaceCabinetBindList = new ArrayList<>();
        
        // 绑定状态记录
        MerchantPlaceCabinetConditionRequest placeCabinetBindRequest = MerchantPlaceCabinetConditionRequest.builder().placeId(placeId).cabinetIds(cabinetIds)
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
        MerchantPlaceCabinetConditionRequest placeCabinetUnbindRequest = MerchantPlaceCabinetConditionRequest.builder().placeId(placeId).cabinetIds(cabinetIds)
                .status(MerchantPlaceCabinetBindConstant.STATUS_UNBIND).startTime(merchantPlaceBindTime).build();
        
        List<MerchantPlaceCabinetBind> cabinetUnbindList = merchantPlaceCabinetBindService.listUnbindRecord(placeCabinetUnbindRequest);
        
        if (CollectionUtils.isEmpty(cabinetUnbindList)) {
            return allPlaceCabinetBindList;
        }
        
        allPlaceCabinetBindList.addAll(cabinetUnbindList);
        
        // 先掐头去尾
        List<MerchantPlaceCabinetBind> stepOneList = new ArrayList<>();
        for (MerchantPlaceCabinetBind placeCabinetBind : allPlaceCabinetBindList) {
            Long bindTime = placeCabinetBind.getBindTime();
            
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
        
        return resultList;
    }
    
    private List<MerchantPlaceCabinetBind> getThisMonthPlaceCabinetBindListForCabinetDetail(Long cabinetId, Long thisMonthStartTime, Long nowTime) {
        List<MerchantPlaceCabinetBind> resultList = new ArrayList<>();
        List<MerchantPlaceCabinetBind> allPlaceCabinetBindList = new ArrayList<>();
        
        // 绑定状态记录
        MerchantPlaceCabinetConditionRequest placeCabinetBindRequest = MerchantPlaceCabinetConditionRequest.builder().cabinetIds(Set.of(cabinetId))
                .status(MerchantPlaceCabinetBindConstant.STATUS_BIND).build();
        List<MerchantPlaceCabinetBind> cabinetBindList = merchantPlaceCabinetBindService.listBindRecord(placeCabinetBindRequest);
        
        if (CollectionUtils.isNotEmpty(cabinetBindList)) {
            for (MerchantPlaceCabinetBind placeCabinetBind : cabinetBindList) {
                Long bindTime = placeCabinetBind.getBindTime();
                
                if (bindTime < thisMonthStartTime) {
                    placeCabinetBind.setBindTime(thisMonthStartTime);
                }
                
                // 给绑定状态记录赋值解绑时间：nowTime
                placeCabinetBind.setUnBindTime(nowTime);
                
                allPlaceCabinetBindList.add(placeCabinetBind);
            }
        }
        
        // 解绑状态记录
        MerchantPlaceCabinetConditionRequest placeCabinetUnbindRequest = MerchantPlaceCabinetConditionRequest.builder().cabinetIds(Set.of(cabinetId))
                .status(MerchantPlaceCabinetBindConstant.STATUS_UNBIND).startTime(thisMonthStartTime).build();
        List<MerchantPlaceCabinetBind> cabinetUnbindList = merchantPlaceCabinetBindService.listUnbindRecord(placeCabinetUnbindRequest);
        
        if (CollectionUtils.isEmpty(cabinetUnbindList)) {
            return allPlaceCabinetBindList;
        }
        
        allPlaceCabinetBindList.addAll(cabinetUnbindList);
        
        // 先掐头去尾
        List<MerchantPlaceCabinetBind> stepOneList = new ArrayList<>();
        for (MerchantPlaceCabinetBind placeCabinetBind : allPlaceCabinetBindList) {
            Long bindTime = placeCabinetBind.getBindTime();
            
            if (bindTime < thisMonthStartTime) {
                placeCabinetBind.setBindTime(thisMonthStartTime);
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
        
        return resultList;
    }
    
    private List<MerchantPlaceCabinetBind> getLastMonthPlaceCabinetBindList(Long placeId, Long merchantPlaceBindTime, Long merchantPlaceUnbindTime) {
        List<MerchantPlaceCabinetBind> resultList = new ArrayList<>();
        List<MerchantPlaceCabinetBind> allPlaceCabinetBindList = new ArrayList<>();
        
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
                
                allPlaceCabinetBindList.add(placeCabinetBind);
            }
        }
        
        // 解绑状态记录
        MerchantPlaceCabinetConditionRequest placeCabinetUnbindRequest = MerchantPlaceCabinetConditionRequest.builder().placeId(placeId)
                .status(MerchantPlaceCabinetBindConstant.STATUS_UNBIND).startTime(merchantPlaceBindTime).endTime(merchantPlaceUnbindTime).build();
        List<MerchantPlaceCabinetBind> cabinetUnbindList = merchantPlaceCabinetBindService.listUnbindRecord(placeCabinetUnbindRequest);
        
        if (CollectionUtils.isEmpty(cabinetUnbindList)) {
            return allPlaceCabinetBindList;
        }
        
        allPlaceCabinetBindList.addAll(cabinetUnbindList);
        
        // 先掐头去尾
        List<MerchantPlaceCabinetBind> stepOneList = new ArrayList<>();
        for (MerchantPlaceCabinetBind placeCabinetBind : allPlaceCabinetBindList) {
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
        
        // 解绑状态记录
        MerchantPlaceConditionRequest merchantPlaceUnbindRequest = MerchantPlaceConditionRequest.builder().merchantId(merchantId).status(MerchantPlaceBindConstant.UN_BIND)
                .startTime(startTime).build();
        List<MerchantPlaceBind> merchantPlaceUnbindList = merchantPlaceBindService.listUnbindRecord(merchantPlaceUnbindRequest);
        
        if (CollectionUtils.isEmpty(merchantPlaceUnbindList)) {
            return resultList;
        }
        
        for (MerchantPlaceBind merchantPlaceUnbind : merchantPlaceUnbindList) {
            Long bindTime = merchantPlaceUnbind.getBindTime();
            
            if (bindTime < startTime) {
                merchantPlaceUnbind.setBindTime(startTime);
            }
            
            resultList.add(merchantPlaceUnbind);
        }
        
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
        log.info("开始执行累计,merchantId={}, cabinetIds={}", merchantId, cabinetIds);
        
        //两个月前的数据（来源于历史表，定时任务）
        MerchantPowerPeriodVO preTwoMonthPower = merchantCabinetPowerMonthRecordProService.sumMonthPower(cabinetIds, null);
        
        log.info("执行累计,2月前===>,merchantId={}, cabinetIds={}, preTwoMonthPower={}", merchantId, cabinetIds, preTwoMonthPower);
        
        // 上月数据
        MerchantPowerPeriodVO lastMonthPower = getLastMonthPower(tenantId, merchantId, cabinetIds);
        
        log.info("执行累计,上月===>,merchantId={}, cabinetIds={}, lastMonthPower={}", merchantId, cabinetIds, lastMonthPower);
        
        // 本月数据
        MerchantPowerPeriodVO thisMonthPower = getThisMonthPower(tenantId, merchantId, cabinetIds);
        
        log.info("执行累计,本月===>,merchantId={}, cabinetIds={}, thisMonthPower={}", merchantId, cabinetIds, thisMonthPower);
        
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
        
        log.info("执行累计结束===>，merchantId={}, powerVO={}", merchantId, powerVO);
        
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
            
            Double powerD = NumberConstant.ZERO_D;
            Double chargeD = NumberConstant.ZERO_D;
            if (Objects.nonNull(merchantPowerPeriodVO)) {
                if (Objects.nonNull(merchantPowerPeriodVO.getPower())) {
                    powerD = BigDecimal.valueOf(merchantPowerPeriodVO.getPower()).setScale(2, RoundingMode.HALF_UP).doubleValue();
                }
                if (Objects.nonNull(merchantPowerPeriodVO.getCharge())) {
                    chargeD = BigDecimal.valueOf(merchantPowerPeriodVO.getCharge()).setScale(2, RoundingMode.HALF_UP).doubleValue();
                }
            }
            
            // 电量
            MerchantProPowerLineDataVO power = new MerchantProPowerLineDataVO();
            power.setMonthDate(monthDate);
            power.setPower(powerD);
            powerList1.add(power);
            
            // 电费
            MerchantProPowerChargeLineDataVO charge = new MerchantProPowerChargeLineDataVO();
            charge.setMonthDate(monthDate);
            charge.setCharge(chargeD);
            chargeList1.add(charge);
        }
        
        if (hasLastMonth) {
            Double powerD = NumberConstant.ZERO_D;
            Double chargeD = NumberConstant.ZERO_D;
            if (Objects.nonNull(lastMonthPower)) {
                if (Objects.nonNull(lastMonthPower.getPower())) {
                    powerD = BigDecimal.valueOf(lastMonthPower.getPower()).setScale(2, RoundingMode.HALF_UP).doubleValue();
                }
                if (Objects.nonNull(lastMonthPower.getCharge())) {
                    chargeD = BigDecimal.valueOf(lastMonthPower.getCharge()).setScale(2, RoundingMode.HALF_UP).doubleValue();
                }
            }
            
            // 电量
            MerchantProPowerLineDataVO power = new MerchantProPowerLineDataVO();
            power.setMonthDate(lastMonthDate);
            power.setPower(powerD);
            powerList1.add(power);
            
            // 电费
            MerchantProPowerChargeLineDataVO charge = new MerchantProPowerChargeLineDataVO();
            charge.setMonthDate(lastMonthDate);
            charge.setCharge(chargeD);
            chargeList1.add(charge);
        }
        
        powerList1.addAll(powerList);
        chargeList1.addAll(chargeList);
        
        vo.setPowerList(powerList1);
        vo.setChargeList(chargeList1);
        
        log.info("商户电费-折线图, vo={}", vo);
        
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
        if (!monthDate.matches(DateUtils.GREP_YEAR_MONTH)) {
            return Collections.emptyList();
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
                Integer cabinetMerchantBindStatus = detailPro.getCabinetMerchantBindStatus();
                
                return getCabinetPowerDetail(startTime, endTime, eid, placeId, tenantId, monthDate, cabinetMerchantBindStatus);
                
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
                double todayPowerSum = todayPower.stream().filter(Objects::nonNull).mapToDouble(MerchantProLivePowerVO::getPower).sum();
                todayPowerSum = BigDecimal.valueOf(todayPowerSum).setScale(2, RoundingMode.HALF_UP).doubleValue();
                
                double todayChargeSum = todayPower.stream().filter(Objects::nonNull).mapToDouble(MerchantProLivePowerVO::getCharge).sum();
                todayChargeSum = BigDecimal.valueOf(todayChargeSum).setScale(2, RoundingMode.HALF_UP).doubleValue();
                
                // 本月电量
                List<MerchantProLivePowerVO> thisMonthPower = thisMonthPowerList.parallelStream().filter(e -> Objects.equals(e.getEid(), cabinetId)).collect(Collectors.toList());
                double thisMonthPowerSum = thisMonthPower.stream().filter(Objects::nonNull).mapToDouble(MerchantProLivePowerVO::getPower).sum();
                thisMonthPowerSum = BigDecimal.valueOf(thisMonthPowerSum).setScale(2, RoundingMode.HALF_UP).doubleValue();
                
                double thisMonthChargeSum = thisMonthPower.stream().filter(Objects::nonNull).mapToDouble(MerchantProLivePowerVO::getCharge).sum();
                thisMonthChargeSum = BigDecimal.valueOf(thisMonthChargeSum).setScale(2, RoundingMode.HALF_UP).doubleValue();
                
                // 上月电量
                List<MerchantProLivePowerVO> lastMonthPower = lastMonthPowerList.parallelStream().filter(e -> Objects.equals(e.getEid(), cabinetId)).collect(Collectors.toList());
                double lastMonthPowerSum = lastMonthPower.stream().filter(Objects::nonNull).mapToDouble(MerchantProLivePowerVO::getPower).sum();
                lastMonthPowerSum = BigDecimal.valueOf(lastMonthPowerSum).setScale(2, RoundingMode.HALF_UP).doubleValue();
                
                double lastMonthChargeSum = lastMonthPower.stream().filter(Objects::nonNull).mapToDouble(MerchantProLivePowerVO::getCharge).sum();
                lastMonthChargeSum = BigDecimal.valueOf(lastMonthChargeSum).setScale(2, RoundingMode.HALF_UP).doubleValue();
                
                // 本年电量
                double thisYearPower = thisMonthPowerSum + lastMonthPowerSum;
                double thisYearCharge = thisMonthChargeSum + lastMonthChargeSum;
                
                List<String> preTwoMonthList;
                if (monthList.size() > NumberConstant.TWO) {
                    List<String> subMonthList = monthList.subList(0, monthList.size() - NumberConstant.TWO);
                    preTwoMonthList = getPreTwoMonthList(subMonthList);
                    
                    MerchantPowerPeriodVO preTwoMonthPowerPeriod = merchantCabinetPowerMonthRecordProService.sumMonthPower(List.of(cabinetId), preTwoMonthList);
                    if (Objects.nonNull(preTwoMonthPowerPeriod)) {
                        if (Objects.nonNull(preTwoMonthPowerPeriod.getPower())) {
                            double preTwoMonthPower = BigDecimal.valueOf(preTwoMonthPowerPeriod.getPower()).setScale(2, RoundingMode.HALF_UP).doubleValue();
                            thisYearCharge += preTwoMonthPower;
                        }
                        
                        if (Objects.nonNull(preTwoMonthPowerPeriod.getCharge())) {
                            double preTwoMonthCharge = BigDecimal.valueOf(preTwoMonthPowerPeriod.getCharge()).setScale(2, RoundingMode.HALF_UP).doubleValue();
                            thisYearCharge += preTwoMonthCharge;
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
                
                log.info("柜机列表===>，electricityCabinet={}, merchantProCabinetPowerVO={}", electricityCabinet, merchantProCabinetPowerVO);
            }
        }
        
        if (CollectionUtils.isEmpty(cabinetPowerList)) {
            return Collections.emptyList();
        }
        
        // cabinetPowerList 根据time进行倒叙排序
        cabinetPowerList.sort((o1, o2) -> o2.getTime().compareTo(o1.getTime()));
        
        log.info("柜机列表===>，merchantId={}, cabinetPowerList={}", merchant.getId(), cabinetPowerList);
        
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
