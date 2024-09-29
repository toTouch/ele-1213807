package com.xiliulou.electricity.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.xiliulou.core.json.JsonUtil;
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
    
    
    @Override
    public void saveExchangeExceptionCell(String orderStatus, Integer eid, Integer oldCell, Integer newCell) {
        log.info("SaveExchangeExceptionCell Info! orderStatus is {}, eid is {}, oldCell is {}, newCell is {}", orderStatus, eid, oldCell, newCell);
        // 空仓失败
        if (Objects.equals(orderStatus, ElectricityCabinetOrder.INIT_OPEN_FAIL)) {
            RMapCache<Integer, Integer> mapCache = redisson.getMapCache(String.format(CacheConstant.EXCEPTION_EMPTY_EID_KEY, eid));
            mapCache.put(oldCell, 1, 5, TimeUnit.MINUTES);
        }
        // 满电仓失败
        if (Objects.equals(orderStatus, ElectricityCabinetOrder.COMPLETE_OPEN_FAIL)) {
            RMapCache<Integer, Integer> mapCache = redisson.getMapCache(String.format(CacheConstant.EXCEPTION_FULL_EID_KEY, eid));
            mapCache.put(newCell, 1, 5, TimeUnit.MINUTES);
        }
    }
    
    @Override
    public void saveRentReturnExceptionCell(String orderStatus, Integer eid, Integer cellNo) {
        log.info("SaveRentReturnExceptionCell Info! orderStatus is {}, eid is {}, cellNo is {}", orderStatus, eid, cellNo);
        // 空仓(退电)失败
        if (Objects.equals(orderStatus, RentBatteryOrder.RETURN_OPEN_FAIL)) {
            RMapCache<Integer, Integer> mapCache = redisson.getMapCache(String.format(CacheConstant.EXCEPTION_EMPTY_EID_KEY, eid));
            mapCache.put(cellNo, 1, 5, TimeUnit.MINUTES);
        }
        // 满电仓(租电)失败
        if (Objects.equals(orderStatus, RentBatteryOrder.RENT_OPEN_FAIL)) {
            RMapCache<Integer, Integer> mapCache = redisson.getMapCache(String.format(CacheConstant.EXCEPTION_FULL_EID_KEY, eid));
            mapCache.put(cellNo, 1, 5, TimeUnit.MINUTES);
        }
    }
    
    
    @Override
    @SuppressWarnings("all")
    public Pair<Boolean, List<ElectricityCabinetBox>> filterEmptyExceptionCell(Integer eid, List<ElectricityCabinetBox> emptyList) {
        // 获取柜机异常空仓
        RMapCache<Integer, Integer> mapCache = redisson.getMapCache(String.format(CacheConstant.EXCEPTION_EMPTY_EID_KEY, eid));
        if (mapCache.isEmpty()) {
            log.info("Exchange Info! filterEmptyExceptionCell.mapCache is null, eid is {}", eid);
            return Pair.of(false, null);
        }
        
        List<Integer> exchangeEmptyCellList = CollUtil.newArrayList();
        mapCache.forEach((k, v) -> exchangeEmptyCellList.add(k));
        
        // 过滤掉异常空仓
        List<ElectricityCabinetBox> filterExchangeEmptyCellList = emptyList.stream().filter(item -> !exchangeEmptyCellList.contains(Integer.valueOf(item.getCellNo())))
                .collect(Collectors.toList());
        
        // 如果剩余空仓为空，则返回随机的异常空仓
        if (CollUtil.isEmpty(filterExchangeEmptyCellList)) {
            return Pair.of(true, filterExchangeEmptyCellList);
        }
        
        // 否则返回剩余空仓
        return Pair.of(false, filterExchangeEmptyCellList);
    }
    
    
    @Override
    @SuppressWarnings("all")
    public Pair<Boolean, List<ElectricityCabinetBox>> filterFullExceptionCell(List<ElectricityCabinetBox> fullList) {
        if (CollUtil.isEmpty(fullList)) {
            return Pair.of(false, null);
        }
        Integer electricityCabinetId = fullList.get(0).getElectricityCabinetId();
        ElectricityCabinet cabinet = electricityCabinetService.queryByIdFromCache(electricityCabinetId);
        if (Objects.isNull(cabinet)) {
            log.warn("Exchange WARN! FilterFullExceptionCell.cabinet is null, eid is {}", electricityCabinetId);
            return Pair.of(false, null);
        }
        
        // 获取柜机异常空仓
        RMapCache<Integer, Integer> fullMapCache = redisson.getMapCache(String.format(CacheConstant.EXCEPTION_FULL_EID_KEY, cabinet.getId()));
        if (fullMapCache.isEmpty()) {
            log.info("Exchange Info! mapCache is null, eid is {}", cabinet.getId());
            return Pair.of(false, null);
        }
        
        List<Integer> exchangeFullCellList = CollUtil.newArrayList();
        fullMapCache.forEach((k, v) -> exchangeFullCellList.add(k));
        
        // 过滤掉异常满电仓
        List<ElectricityCabinetBox> filterExchangeFullCellList = fullList.stream().filter(item -> !exchangeFullCellList.contains(Integer.valueOf(item.getCellNo())))
                .collect(Collectors.toList());
        
        log.info("Exchange Info! fullList is {}, exchangeFullCellList is {}", JsonUtil.toJson(fullList),
                CollUtil.isEmpty(exchangeFullCellList) ? "null" : JsonUtil.toJson(exchangeFullCellList));
        
        // 如果剩余满电仓为空，则返回随机的异常满电仓
        if (CollUtil.isEmpty(filterExchangeFullCellList)) {
            log.info("Exchange Info! filterExchangeFullCellList is null, eid is {}", cabinet.getId());
            return Pair.of(true, filterExchangeFullCellList);
        }
        
        // 否则返回剩余满电仓
        return Pair.of(false, filterExchangeFullCellList);
    }
}
