package com.xiliulou.electricity.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.config.ExchangeConfig;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.ElectricityCabinetBox;
import com.xiliulou.electricity.entity.ElectricityCabinetOrder;
import com.xiliulou.electricity.entity.RentBatteryOrder;
import com.xiliulou.electricity.service.ElectricityCabinetService;
import com.xiliulou.electricity.service.ExchangeExceptionHandlerService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.redisson.api.RMapCache;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @ClassName: ExchangeExceptionHandlerServiceImpl
 * @description:
 * @author: renhang
 * @create: 2024-09-29 15:12
 */
@Service
@Slf4j
public class ExchangeExceptionHandlerServiceImpl implements ExchangeExceptionHandlerService {
    
    @Resource
    private RedissonClient redissonClient;
    
    @Resource
    private ElectricityCabinetService electricityCabinetService;
    
    @Resource
    private ExchangeConfig exchangeConfig;
    
    
    public static final Integer INVALID_KEY = -2;
    
    
    @Override
    public void saveExchangeExceptionCell(String orderStatus, Integer eid, Integer oldCell, Integer newCell) {
        log.info("SaveExchangeExceptionCell Info! eid is {} , orderStatus is {}, oldCell is {}, newCell is {}", eid, orderStatus, oldCell, newCell);
        
        try {
            Long exceptionCellSaveTime = Objects.isNull(exchangeConfig.getExceptionCellSaveTime()) ? 1000 * 60 * 5L : exchangeConfig.getExceptionCellSaveTime();
            // 空仓失败
            if (Objects.equals(orderStatus, ElectricityCabinetOrder.INIT_OPEN_FAIL)) {
                RMapCache<Integer, Integer> mapCache = redissonClient.getMapCache(String.format(CacheConstant.EXCEPTION_EMPTY_EID_KEY, eid));
                mapCache.put(oldCell, 1, exceptionCellSaveTime, TimeUnit.MILLISECONDS);
            }
            // 满电仓失败
            if (Objects.equals(orderStatus, ElectricityCabinetOrder.COMPLETE_OPEN_FAIL)) {
                RMapCache<Integer, Integer> mapCache = redissonClient.getMapCache(String.format(CacheConstant.EXCEPTION_FULL_EID_KEY, eid));
                mapCache.put(newCell, 1, exceptionCellSaveTime, TimeUnit.MILLISECONDS);
            }
            
        } catch (Exception e) {
            log.error("saveExchangeExceptionCell Error!", e);
        }
    }
    
    @Override
    public void saveRentReturnExceptionCell(String orderStatus, Integer eid, Integer cellNo) {
        log.info("SaveRentReturnExceptionCell Info! eid is {}, orderStatus is {}, cellNo is {}", eid, orderStatus, cellNo);
        
        try {
            Long exceptionCellSaveTime = Objects.isNull(exchangeConfig.getExceptionCellSaveTime()) ? 1000 * 60 * 5L : exchangeConfig.getExceptionCellSaveTime();
            // 空仓(退电)失败
            if (Objects.equals(orderStatus, RentBatteryOrder.RETURN_OPEN_FAIL)) {
                RMapCache<Integer, Integer> mapCache = redissonClient.getMapCache(String.format(CacheConstant.EXCEPTION_EMPTY_EID_KEY, eid));
                mapCache.put(cellNo, 1, exceptionCellSaveTime, TimeUnit.MILLISECONDS);
            }
            // 满电仓(租电)失败
            if (Objects.equals(orderStatus, RentBatteryOrder.RENT_OPEN_FAIL)) {
                RMapCache<Integer, Integer> mapCache = redissonClient.getMapCache(String.format(CacheConstant.EXCEPTION_FULL_EID_KEY, eid));
                mapCache.put(cellNo, 1, exceptionCellSaveTime, TimeUnit.MILLISECONDS);
            }
            
        } catch (Exception e) {
            log.error("saveRentReturnExceptionCell Error!", e);
        }
    }
    
    
    @Override
    @SuppressWarnings("all")
    public Pair<Boolean, List<ElectricityCabinetBox>> filterEmptyExceptionCell(Integer eid, List<ElectricityCabinetBox> emptyList) {
        try {
            // 获取柜机异常空仓
            RMapCache<Integer, Integer> mapCache = redissonClient.getMapCache(String.format(CacheConstant.EXCEPTION_EMPTY_EID_KEY, eid));
            if (!isExpired(mapCache)) {
                log.info("FilterEmptyExceptionCell Info! redisExceptionEmptyCell is null, eid is {}", eid);
                return Pair.of(false, emptyList);
            }
            
            List<Integer> exceptionEmptyCellList = CollUtil.newArrayList();
            mapCache.forEach((k, v) -> exceptionEmptyCellList.add(k));
            
            // 过滤掉异常空仓
            List<ElectricityCabinetBox> filterExchangeEmptyCellList = emptyList.stream().filter(item -> !exceptionEmptyCellList.contains(Integer.valueOf(item.getCellNo())))
                    .collect(Collectors.toList());
            
            log.info("FilterEmptyExceptionCell Info! emptyList is {}, exceptionEmptyCellList is {}", JsonUtil.toJson(emptyList),
                    CollUtil.isEmpty(exceptionEmptyCellList) ? "null" : JsonUtil.toJson(exceptionEmptyCellList));
            
            // 如果剩余空仓为空，则返回随机的异常空仓
            if (CollUtil.isEmpty(filterExchangeEmptyCellList)) {
                log.info("FilterEmptyExceptionCell Info! filterExchangeEmptyCellList is null, return exchangeEmptyCellList");
                return Pair.of(true, filterExchangeEmptyCellList);
            }
            
            // 否则返回剩余空仓
            return Pair.of(false, filterExchangeEmptyCellList);
            
        } catch (Exception e) {
            log.error("FilterEmptyExceptionCell Error!", e);
        }
        return Pair.of(false, emptyList);
    }
    
    
    @Override
    @SuppressWarnings("all")
    public Pair<Boolean, List<ElectricityCabinetBox>> filterFullExceptionCell(List<ElectricityCabinetBox> fullList) {
        try {
            if (CollUtil.isEmpty(fullList)) {
                return Pair.of(false, fullList);
            }
            Integer electricityCabinetId = fullList.get(0).getElectricityCabinetId();
            ElectricityCabinet cabinet = electricityCabinetService.queryByIdFromCache(electricityCabinetId);
            if (Objects.isNull(cabinet)) {
                log.warn("FilterFullExceptionCell WARN! cabinet is null, eid is {}", electricityCabinetId);
                return Pair.of(false, fullList);
            }
            
            // 获取柜机异常空仓
            RMapCache<Integer, Integer> fullMapCache = redissonClient.getMapCache(String.format(CacheConstant.EXCEPTION_FULL_EID_KEY, cabinet.getId()));
            if (!isExpired(fullMapCache)) {
                log.info("FilterFullExceptionCell Info! redisExceptionFullCell is null, eid is {}", cabinet.getId());
                return Pair.of(false, fullList);
            }
            
            List<Integer> exceptionFullCellList = CollUtil.newArrayList();
            fullMapCache.forEach((k, v) -> exceptionFullCellList.add(k));
            
            // 过滤掉异常满电仓
            List<ElectricityCabinetBox> filterExchangeFullCellList = fullList.stream().filter(item -> !exceptionFullCellList.contains(Integer.valueOf(item.getCellNo())))
                    .collect(Collectors.toList());
            
            log.info("FilterFullExceptionCell Info! fullList is {}, exceptionEmptyCellList is {}", JsonUtil.toJson(fullList),
                    CollUtil.isEmpty(exceptionFullCellList) ? "null" : JsonUtil.toJson(exceptionFullCellList));
            
            // 如果剩余满电仓为空，则返回随机的异常满电仓
            if (CollUtil.isEmpty(filterExchangeFullCellList)) {
                log.info("FilterFullExceptionCell Info! filterExchangeFullCellList is null, eid is {}", cabinet.getId());
                return Pair.of(true, filterExchangeFullCellList);
            }
            
            // 否则返回剩余满电仓
            return Pair.of(false, filterExchangeFullCellList);
        } catch (Exception e) {
            
            log.error("FilterFullExceptionCell Error!", e);
        }
        return Pair.of(false, fullList);
        
    }
    
    
    private Boolean isExpired(RMapCache<Integer, Integer> mapCache) {
        AtomicInteger atomicInteger = new AtomicInteger();
        Set<Map.Entry<Integer, Integer>> entries = mapCache.entrySet();
        entries.forEach(e -> {
            if (!Objects.equals(e.getKey(), INVALID_KEY)) {
                atomicInteger.getAndAdd(1);
            }
        });
        
        return atomicInteger.get() > 0;
    }
}
