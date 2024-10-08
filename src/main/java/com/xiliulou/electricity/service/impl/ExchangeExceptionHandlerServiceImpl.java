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
import org.redisson.Redisson;
import org.redisson.api.RMapCache;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
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
    Redisson redisson;
    
    @Resource
    private ElectricityCabinetService electricityCabinetService;
    
    @Resource
    private ExchangeConfig exchangeConfig;
    
    
    @Override
    public void saveExchangeExceptionCell(String orderStatus, Integer eid, Integer oldCell, Integer newCell) {
        log.info("SaveExchangeExceptionCell Info! eid is {} , orderStatus is {}, oldCell is {}, newCell is {}", eid, orderStatus, oldCell, newCell);
        
        Long exceptionCellSaveTime = Objects.isNull(exchangeConfig.getExceptionCellSaveTime()) ? 1000 * 60 * 5L : exchangeConfig.getExceptionCellSaveTime();
        // 空仓失败
        if (Objects.equals(orderStatus, ElectricityCabinetOrder.INIT_OPEN_FAIL)) {
            RMapCache<Integer, Integer> mapCache = redisson.getMapCache(String.format(CacheConstant.EXCEPTION_EMPTY_EID_KEY, eid));
            mapCache.put(oldCell, 1, exceptionCellSaveTime, TimeUnit.MILLISECONDS);
        }
        // 满电仓失败
        if (Objects.equals(orderStatus, ElectricityCabinetOrder.COMPLETE_OPEN_FAIL)) {
            RMapCache<Integer, Integer> mapCache = redisson.getMapCache(String.format(CacheConstant.EXCEPTION_FULL_EID_KEY, eid));
            mapCache.put(newCell, 1, exceptionCellSaveTime, TimeUnit.MILLISECONDS);
        }
    }
    
    @Override
    public void saveRentReturnExceptionCell(String orderStatus, Integer eid, Integer cellNo) {
        log.info("SaveRentReturnExceptionCell Info! eid is {}, orderStatus is {}, cellNo is {}", eid, orderStatus, cellNo);
        
        Long exceptionCellSaveTime = Objects.isNull(exchangeConfig.getExceptionCellSaveTime()) ? 1000 * 60 * 5L : exchangeConfig.getExceptionCellSaveTime();
        // 空仓(退电)失败
        if (Objects.equals(orderStatus, RentBatteryOrder.RETURN_OPEN_FAIL)) {
            RMapCache<Integer, Integer> mapCache = redisson.getMapCache(String.format(CacheConstant.EXCEPTION_EMPTY_EID_KEY, eid));
            mapCache.put(cellNo, 1, exceptionCellSaveTime, TimeUnit.MILLISECONDS);
        }
        // 满电仓(租电)失败
        if (Objects.equals(orderStatus, RentBatteryOrder.RENT_OPEN_FAIL)) {
            RMapCache<Integer, Integer> mapCache = redisson.getMapCache(String.format(CacheConstant.EXCEPTION_FULL_EID_KEY, eid));
            mapCache.put(cellNo, 1, exceptionCellSaveTime, TimeUnit.MILLISECONDS);
        }
    }
    
    
    @Override
    @SuppressWarnings("all")
    public Pair<Boolean, List<ElectricityCabinetBox>> filterEmptyExceptionCell(Integer eid, List<ElectricityCabinetBox> emptyList) {
        // 获取柜机异常空仓
        RMapCache<Integer, Integer> mapCache = redisson.getMapCache(String.format(CacheConstant.EXCEPTION_EMPTY_EID_KEY, eid));
        if (mapCache.isEmpty()) {
            log.info("FilterEmptyExceptionCell Info! redisExceptionEmptyCell is null, eid is {}", eid);
            return Pair.of(false, emptyList);
        }
        
        List<Integer> exchangeEmptyCellList = CollUtil.newArrayList();
        mapCache.forEach((k, v) -> exchangeEmptyCellList.add(k));
        
        // 过滤掉异常空仓
        List<ElectricityCabinetBox> filterExchangeEmptyCellList = emptyList.stream().filter(item -> !exchangeEmptyCellList.contains(Integer.valueOf(item.getCellNo())))
                .collect(Collectors.toList());
        
        log.info("FilterEmptyExceptionCell Info! emptyList is {}, filterExchangeEmptyCellList is {}", JsonUtil.toJson(emptyList),
                CollUtil.isEmpty(filterExchangeEmptyCellList) ? "null" : JsonUtil.toJson(filterExchangeEmptyCellList));
        
        // 如果剩余空仓为空，则返回随机的异常空仓
        if (CollUtil.isEmpty(filterExchangeEmptyCellList)) {
            log.info("FilterEmptyExceptionCell Info! filterExchangeEmptyCellList is null, return exchangeEmptyCellList");
            return Pair.of(true, filterExchangeEmptyCellList);
        }
        
        // 否则返回剩余空仓
        return Pair.of(false, filterExchangeEmptyCellList);
    }
    
    
    @Override
    @SuppressWarnings("all")
    public Pair<Boolean, List<ElectricityCabinetBox>> filterFullExceptionCell(List<ElectricityCabinetBox> fullList) {
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
        RMapCache<Integer, Integer> fullMapCache = redisson.getMapCache(String.format(CacheConstant.EXCEPTION_FULL_EID_KEY, cabinet.getId()));
        if (fullMapCache.isEmpty()) {
            log.info("FilterFullExceptionCell Info! redisExceptionFullCell is null, eid is {}", cabinet.getId());
            return Pair.of(false, fullList);
        }
        
        List<Integer> exchangeFullCellList = CollUtil.newArrayList();
        fullMapCache.forEach((k, v) -> exchangeFullCellList.add(k));
        
        // 过滤掉异常满电仓
        List<ElectricityCabinetBox> filterExchangeFullCellList = fullList.stream().filter(item -> !exchangeFullCellList.contains(Integer.valueOf(item.getCellNo())))
                .collect(Collectors.toList());
        
        log.info("FilterFullExceptionCell Info! fullList is {}, exchangeFullCellList is {}", JsonUtil.toJson(fullList),
                CollUtil.isEmpty(exchangeFullCellList) ? "null" : JsonUtil.toJson(exchangeFullCellList));
        
        // 如果剩余满电仓为空，则返回随机的异常满电仓
        if (CollUtil.isEmpty(filterExchangeFullCellList)) {
            log.info("FilterFullExceptionCell Info! filterExchangeFullCellList is null, eid is {}", cabinet.getId());
            return Pair.of(true, filterExchangeFullCellList);
        }
        
        // 否则返回剩余满电仓
        return Pair.of(false, filterExchangeFullCellList);
    }
}
