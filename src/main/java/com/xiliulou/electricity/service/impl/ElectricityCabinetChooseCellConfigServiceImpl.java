package com.xiliulou.electricity.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.entity.ElectricityCabinetBox;
import com.xiliulou.electricity.entity.ElectricityCabinetChooseCellConfig;
import com.xiliulou.electricity.entity.ElectricityCabinetModel;
import com.xiliulou.electricity.entity.ElectricityConfig;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.mapper.ElectricityCabinetChooseCellConfigMapper;
import com.xiliulou.electricity.service.ElectricityCabinetChooseCellConfigService;
import com.xiliulou.electricity.service.ElectricityCabinetModelService;
import com.xiliulou.electricity.service.ElectricityConfigService;
import com.xiliulou.electricity.service.UserInfoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * @ClassName: ElectricityCabinetChooseCellConfigServiceImpl
 * @description:
 * @author: renhang
 * @create: 2024-08-08 09:04
 */
@Service
@Slf4j
public class ElectricityCabinetChooseCellConfigServiceImpl implements ElectricityCabinetChooseCellConfigService {
    
    @Resource
    private ElectricityCabinetChooseCellConfigMapper electricityCabinetChooseCellConfigMapper;
    
    @Resource
    RedisService redisService;
    
    @Resource
    private ElectricityCabinetChooseCellConfigService chooseCellConfigService;
    
    @Resource
    private ElectricityCabinetModelService cabinetModelService;
    
    @Resource
    private ElectricityConfigService electricityConfigService;
    
    @Resource
    private UserInfoService userInfoService;
    
    @Override
    public ElectricityCabinetChooseCellConfig queryConfigByNumFromDB(Integer num) {
        return electricityCabinetChooseCellConfigMapper.selectConfigByNum(num);
    }
    
    @Override
    public ElectricityCabinetChooseCellConfig queryConfigByNumFromCache(Integer num) {
        if (Objects.isNull(num)) {
            return null;
        }
        //先查缓存
        ElectricityCabinetChooseCellConfig chooseCellConfig = redisService.getWithHash(CacheConstant.CACHE_ELECTRICITY_CABINET_CELL_CONFIG + num,
                ElectricityCabinetChooseCellConfig.class);
        if (Objects.nonNull(chooseCellConfig)) {
            return chooseCellConfig;
        }
        
        ElectricityCabinetChooseCellConfig chooseCellConfigFromDb = this.queryConfigByNumFromDB(num);
        if (Objects.isNull(chooseCellConfigFromDb)) {
            return null;
        }
        
        redisService.saveWithHash(CacheConstant.CACHE_ELECTRICITY_CABINET_MODEL + num, chooseCellConfigFromDb);
        return chooseCellConfigFromDb;
    }
    
    @Override
    public Pair<Boolean, String> comfortExchangeGetFullCell(Long uid, List<ElectricityCabinetBox> usableBoxes) {
        if (Objects.isNull(uid)) {
            log.warn("COMFORT EXCHANGE WARN! uid is null");
            return Pair.of(false, null);
        }
        
        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
        if (Objects.isNull(userInfo)) {
            log.warn("COMFORT EXCHANGE WARN! userInfo is null, uid is {}", uid);
            return Pair.of(false, null);
        }
        
        ElectricityConfig electricityConfig = electricityConfigService.queryFromCacheByTenantId(userInfo.getTenantId());
        if (Objects.isNull(electricityConfig) || Objects.equals(electricityConfig.getIsComfortExchange(), ElectricityConfig.NOT_COMFORT_EXCHANGE)) {
            log.info("COMFORT EXCHANGE INFO! comfortExchangeGetFullCell.electricityConfig is null, tenantId is {}", userInfo.getTenantId());
            return Pair.of(false, null);
        }
        log.info("COMFORT EXCHANGE INFO!comfortExchangeGetFullCell.electricityConfig is {}", JsonUtil.toJson(electricityConfig));
        
        // 是否可以满足优先换电标准的电池列表
        List<ElectricityCabinetBox> comfortExchangeBox = usableBoxes.stream()
                .filter(e -> Objects.nonNull(electricityConfig.getPriorityExchangeNorm()) && Double.compare(e.getPower(), electricityConfig.getPriorityExchangeNorm()) >= 0)
                .collect(Collectors.toList());
        
        if (CollUtil.isEmpty(comfortExchangeBox)) {
            log.info("COMFORT EXCHANGE INFO! comfortExchangeGetFullCell.comfortExchangeBox is empty,uid={}", userInfo.getUid());
            return Pair.of(false, null);
        }
        
        Integer electricityCabinetId = comfortExchangeBox.get(0).getElectricityCabinetId();
        ElectricityCabinetModel cabinetModel = cabinetModelService.queryByIdFromCache(electricityCabinetId);
        if (Objects.isNull(cabinetModel)) {
            log.warn("COMFORT EXCHANGE WARN! comfortExchangeGetFullCell.cabinetModel is null, eid is {}", electricityCabinetId);
            return Pair.of(false, null);
        }
        // 舒适换电
        ElectricityCabinetChooseCellConfig cellConfig = chooseCellConfigService.queryConfigByNumFromCache(cabinetModel.getNum());
        if (Objects.isNull(cellConfig)) {
            log.warn("COMFORT EXCHANGE WARN! comfortExchangeGetFullCell.cellConfig is null, eid is {}", cabinetModel.getNum());
            return Pair.of(false, null);
        }
        // 中间
        Pair<Boolean, String> middleCellBoxPair = getPositionCell(comfortExchangeBox, cellConfig.getMiddleCell());
        if (middleCellBoxPair.getLeft()) {
            log.info("COMFORT EXCHANGE INFO! comfortExchangeGetFullCell.satisfyMiddleCell, middleCell is {}", cellConfig.getMiddleCell());
            return middleCellBoxPair;
        }
        // 下面
        Pair<Boolean, String> belowCellBoxPair = getPositionCell(comfortExchangeBox, cellConfig.getBelowCell());
        if (belowCellBoxPair.getLeft()) {
            log.info("COMFORT EXCHANGE INFO! comfortExchangeGetFullCell.satisfyBelowCell, belowCell is {}", cellConfig.getBelowCell());
            return belowCellBoxPair;
        }
        
        log.info("COMFORT EXCHANGE INFO! comfortExchangeGetFullCell.randomGetCell");
        // 随机分配
        return Pair.of(true, comfortExchangeBox.get(ThreadLocalRandom.current().nextInt(comfortExchangeBox.size())).getCellNo());
    }
    
    private static Pair<Boolean, String> getPositionCell(List<ElectricityCabinetBox> comfortExchangeBox, String positionCell) {
        List<Integer> positionCellList = StrUtil.isNotBlank(positionCell) ? JsonUtil.fromJsonArray(positionCell, Integer.class) : new ArrayList<>();
        List<ElectricityCabinetBox> boxes = comfortExchangeBox.stream().filter(item -> positionCellList.contains(Integer.valueOf(item.getCellNo()))).collect(Collectors.toList());
        if (CollUtil.isEmpty(boxes)) {
            return Pair.of(false, null);
        }
        if (Objects.equals(boxes.size(), 1)) {
            return Pair.of(true, boxes.get(0).getCellNo());
        }
        return Pair.of(true, boxes.get(ThreadLocalRandom.current().nextInt(boxes.size())).getCellNo());
    }
}
