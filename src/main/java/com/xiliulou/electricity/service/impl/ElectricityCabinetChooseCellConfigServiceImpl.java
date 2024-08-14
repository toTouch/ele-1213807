package com.xiliulou.electricity.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.ElectricityCabinetBox;
import com.xiliulou.electricity.entity.ElectricityCabinetChooseCellConfig;
import com.xiliulou.electricity.entity.ElectricityCabinetModel;
import com.xiliulou.electricity.entity.ElectricityConfig;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.mapper.ElectricityCabinetChooseCellConfigMapper;
import com.xiliulou.electricity.service.ElectricityCabinetChooseCellConfigService;
import com.xiliulou.electricity.service.ElectricityCabinetModelService;
import com.xiliulou.electricity.service.ElectricityCabinetService;
import com.xiliulou.electricity.service.ElectricityConfigService;
import com.xiliulou.electricity.service.UserInfoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Comparator;
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
    
    @Resource
    private ElectricityCabinetService cabinetService;
    
    @Override
    @Slave
    public ElectricityCabinetChooseCellConfig queryConfigByNumFromDB(Integer num) {
        return electricityCabinetChooseCellConfigMapper.selectConfigByNum(num);
    }
    
    @Override
    public ElectricityCabinetChooseCellConfig queryConfigByNumFromCache(Integer num) {
        if (Objects.isNull(num)) {
            return null;
        }
        String cacheKey = CacheConstant.CACHE_ELECTRICITY_CABINET_CELL_CONFIG + num;
        //先查缓存
        ElectricityCabinetChooseCellConfig chooseCellConfig = redisService.getWithHash(cacheKey, ElectricityCabinetChooseCellConfig.class);
        if (Objects.nonNull(chooseCellConfig)) {
            return chooseCellConfig;
        }
        
        ElectricityCabinetChooseCellConfig chooseCellConfigFromDb = this.queryConfigByNumFromDB(num);
        if (Objects.isNull(chooseCellConfigFromDb)) {
            return null;
        }
        
        redisService.saveWithHash(cacheKey, chooseCellConfigFromDb);
        return chooseCellConfigFromDb;
    }
    
    @Override
    public Pair<Boolean, ElectricityCabinetBox> comfortExchangeGetFullCell(Long uid, List<ElectricityCabinetBox> usableBoxes) {
        log.info("COMFORT EXCHANGE GET FULL INFO! comfort exchange get full cell");
        try {
            return getComfortExchangeGetFullCell(uid, usableBoxes);
        } catch (Exception e) {
            log.error("comfortExchangeGetFullCell.error, continue norm getFullCell, uid is {}", uid, e);
            return Pair.of(false, null);
        }
    }
    
    private Pair<Boolean, ElectricityCabinetBox> getComfortExchangeGetFullCell(Long uid, List<ElectricityCabinetBox> usableBoxes) {
        if (Objects.isNull(uid)) {
            log.warn("COMFORT EXCHANGE GET FULL WARN! comfortExchangeGetFullCell.uid is null");
            return Pair.of(false, null);
        }
        
        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
        if (Objects.isNull(userInfo)) {
            log.warn("COMFORT EXCHANGE GET FULL WARN! comfortExchangeGetFullCell.userInfo is null, uid is {}", uid);
            return Pair.of(false, null);
        }
        
        if (CollUtil.isEmpty(usableBoxes)) {
            log.warn("COMFORT EXCHANGE GET FULL WARN! comfortExchangeGetFullCell.usableBoxes is null, uid is {}", uid);
            return Pair.of(false, null);
        }
        
        ElectricityConfig electricityConfig = electricityConfigService.queryFromCacheByTenantId(userInfo.getTenantId());
        if (Objects.isNull(electricityConfig) || Objects.equals(electricityConfig.getIsComfortExchange(), ElectricityConfig.NOT_COMFORT_EXCHANGE)) {
            log.info("COMFORT EXCHANGE GET FULL INFO! comfortExchangeGetFullCell.electricityConfig is null or ComfortExchange is close, tenantId is {}", userInfo.getTenantId());
            return Pair.of(false, null);
        }
        
        log.info("COMFORT EXCHANGE GET FULL INFO! comfortExchangeGetFullCell.electricityConfig.comfort is {}, priorityExchangeNorm is {}, usableBoxes is {}",
                electricityConfig.getIsComfortExchange(), electricityConfig.getPriorityExchangeNorm(),
                JsonUtil.toJson(usableBoxes.stream().map(ElectricityCabinetBox::getCellNo).collect(Collectors.toList())));
        
        // 是否可以满足优先换电标准的电池列表
        List<ElectricityCabinetBox> comfortExchangeBox = usableBoxes.stream()
                .filter(e -> Objects.nonNull(electricityConfig.getPriorityExchangeNorm()) && Double.compare(e.getPower(), electricityConfig.getPriorityExchangeNorm()) >= 0)
                .collect(Collectors.toList());
        
        if (CollUtil.isEmpty(comfortExchangeBox)) {
            log.info("COMFORT EXCHANGE GET FULL INFO! comfortExchangeGetFullCell.comfortExchangeBox is empty,uid={}", userInfo.getUid());
            return Pair.of(false, null);
        }
        
        Integer electricityCabinetId = comfortExchangeBox.get(0).getElectricityCabinetId();
        ElectricityCabinet cabinet = cabinetService.queryByIdFromCache(electricityCabinetId);
        if (Objects.isNull(cabinet)) {
            log.warn("COMFORT EXCHANGE GET FULL WARN! comfortExchangeGetFullCell.cabinet is null, eid is {}", electricityCabinetId);
            return Pair.of(false, null);
        }
        
        ElectricityCabinetModel cabinetModel = cabinetModelService.queryByIdFromCache(cabinet.getModelId());
        if (Objects.isNull(cabinetModel)) {
            log.warn("COMFORT EXCHANGE GET FULL WARN! comfortExchangeGetFullCell.cabinetModel is null, eid is {}, modelId is {}", electricityCabinetId, cabinet.getModelId());
            return Pair.of(false, null);
        }
        // 舒适换电
        ElectricityCabinetChooseCellConfig cellConfig = chooseCellConfigService.queryConfigByNumFromCache(cabinetModel.getNum());
        if (Objects.isNull(cellConfig)) {
            log.warn("COMFORT EXCHANGE GET FULL WARN! comfortExchangeGetFullCell.cellConfig is null, num is {}", cabinetModel.getNum());
            return Pair.of(false, null);
        }
        
        // 中间
        Pair<Boolean, ElectricityCabinetBox> middleCellBoxPair = getPositionFullCell(comfortExchangeBox, cellConfig.getMiddleCell());
        if (middleCellBoxPair.getLeft()) {
            log.info("COMFORT EXCHANGE GET FULL INFO! getConformationCell,num is {}; middleCell is {}, cell is {}", cabinetModel.getNum(), cellConfig.getMiddleCell(),
                    Objects.nonNull(middleCellBoxPair.getRight()) ? middleCellBoxPair.getRight().getCellNo() : "null");
            return middleCellBoxPair;
        }
        // 下面
        Pair<Boolean, ElectricityCabinetBox> belowCellBoxPair = getPositionFullCell(comfortExchangeBox, cellConfig.getBelowCell());
        if (belowCellBoxPair.getLeft()) {
            log.info("COMFORT EXCHANGE GET FULL INFO! getConformationCell,num is {}; belowCell is {}, cell is {}", cabinetModel.getNum(), cellConfig.getBelowCell(),
                    Objects.nonNull(belowCellBoxPair.getRight()) ? belowCellBoxPair.getRight().getCellNo() : "null");
            return belowCellBoxPair;
        }
        
        // 上面
        Pair<Boolean, ElectricityCabinetBox> topCellBoxPair = getPositionFullCell(comfortExchangeBox, cellConfig.getTopCell());
        if (topCellBoxPair.getLeft()) {
            log.info("COMFORT EXCHANGE GET FULL INFO! getConformationCell,num is {}; topCell is {}, cell is {}", cabinetModel.getNum(), cellConfig.getTopCell(),
                    Objects.nonNull(topCellBoxPair.getRight()) ? topCellBoxPair.getRight().getCellNo() : "null");
            return topCellBoxPair;
        }
        log.warn("COMFORT EXCHANGE GET FULL WARN! getConformationCell is null");
        return Pair.of(false, null);
    }
    
    
    private static Pair<Boolean, ElectricityCabinetBox> getPositionFullCell(List<ElectricityCabinetBox> comfortExchangeBox, String positionCell) {
        // 每个位置的数据反序列
        List<Integer> positionCellList = StrUtil.isNotBlank(positionCell) ? JsonUtil.fromJsonArray(positionCell, Integer.class) : new ArrayList<>();
        List<ElectricityCabinetBox> boxes = comfortExchangeBox.stream().filter(item -> positionCellList.contains(Integer.valueOf(item.getCellNo()))).collect(Collectors.toList());
        
        if (CollUtil.isEmpty(boxes)) {
            log.info("COMFORT EXCHANGE GET FULL INFO! box is null, positionCell is {}", positionCell);
            return Pair.of(false, null);
        }
        
        Double maxPower = boxes.get(0).getPower();
        boxes = boxes.stream().filter(item -> Objects.equals(item.getPower(), maxPower)).collect(Collectors.toList());
        if (boxes.size() == 1) {
            return Pair.of(true, boxes.get(0));
        }
        
        // 如果存在多个电量相同的格挡，取充电器电压最大
        ElectricityCabinetBox usableCabinetBox = boxes.stream().filter(item -> Objects.nonNull(item.getChargeV())).sorted(Comparator.comparing(ElectricityCabinetBox::getChargeV))
                .reduce((first, second) -> second).orElse(null);
        if (Objects.isNull(usableCabinetBox)) {
            log.warn("COMFORT EXCHANGE GET FULL WARN! nou found full battery,eid={}", boxes.get(0).getElectricityCabinetId());
            return Pair.of(false, null);
        }
        
        return Pair.of(true, usableCabinetBox);
    }
    
    
    @Override
    public Pair<Boolean, Integer> comfortExchangeGetEmptyCell(Long uid, List<ElectricityCabinetBox> emptyCellBoxList) {
        log.info("COMFORT EXCHANGE GET EMPTY INFO! comfort exchange get empty Cell");
        try {
            return getComfortExchangeGetEmptyCell(uid, emptyCellBoxList);
        } catch (Exception e) {
            log.error("comfortExchangeGetEmptyCell.error, continue norm getEmptyCell, uid is {}", uid, e);
            return Pair.of(false, null);
        }
    }
    
    private Pair<Boolean, Integer> getComfortExchangeGetEmptyCell(Long uid, List<ElectricityCabinetBox> emptyCellBoxList) {
        if (Objects.isNull(uid)) {
            log.warn("COMFORT EXCHANGE GET EMPTY WARN! comfortExchangeGetEmptyCell.uid is null");
            return Pair.of(false, null);
        }
        
        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
        if (Objects.isNull(userInfo)) {
            log.warn("COMFORT EXCHANGE GET EMPTY WARN! comfortExchangeGetEmptyCell.userInfo is null, uid is {}", uid);
            return Pair.of(false, null);
        }
        
        if (CollUtil.isEmpty(emptyCellBoxList)) {
            log.warn("COMFORT EXCHANGE GET EMPTY WARN! comfortExchangeGetEmptyCell.emptyCellBoxList is null, uid is {}", uid);
            return Pair.of(false, null);
        }
        
        ElectricityConfig electricityConfig = electricityConfigService.queryFromCacheByTenantId(userInfo.getTenantId());
        if (Objects.isNull(electricityConfig) || Objects.equals(electricityConfig.getIsComfortExchange(), ElectricityConfig.NOT_COMFORT_EXCHANGE)) {
            log.info("COMFORT EXCHANGE GET EMPTY INFO! comfortExchangeGetEmptyCell.electricityConfig is null or ComfortExchange is close, tenantId is {}", userInfo.getTenantId());
            return Pair.of(false, null);
        }
        
        Integer electricityCabinetId = emptyCellBoxList.get(0).getElectricityCabinetId();
        ElectricityCabinet cabinet = cabinetService.queryByIdFromCache(electricityCabinetId);
        if (Objects.isNull(cabinet)) {
            log.warn("COMFORT EXCHANGE GET EMPTY WARN! comfortExchangeGetEmptyCell.cabinet is null, eid is {}", electricityCabinetId);
            return Pair.of(false, null);
        }
        
        ElectricityCabinetModel cabinetModel = cabinetModelService.queryByIdFromCache(cabinet.getModelId());
        if (Objects.isNull(cabinetModel)) {
            log.warn("COMFORT EXCHANGE GET EMPTY WARN! comfortExchangeGetEmptyCell.cabinetModel is null, eid is {}, modelId is {}", electricityCabinetId, cabinet.getModelId());
            return Pair.of(false, null);
        }
        
        // 舒适换电
        ElectricityCabinetChooseCellConfig cellConfig = chooseCellConfigService.queryConfigByNumFromCache(cabinetModel.getNum());
        if (Objects.isNull(cellConfig)) {
            log.warn("COMFORT EXCHANGE GET EMPTY WARN! comfortExchangeGetEmptyCell.cellConfig is null, num is {}", cabinetModel.getNum());
            return Pair.of(false, null);
        }
        
        // 优先分配开门的格挡
        List<ElectricityCabinetBox> openDoorEmptyCellList = emptyCellBoxList.stream().filter(item -> Objects.equals(item.getIsLock(), ElectricityCabinetBox.OPEN_DOOR))
                .collect(Collectors.toList());
        if (Objects.equals(openDoorEmptyCellList.size(), 1)) {
            return Pair.of(true, Integer.valueOf(openDoorEmptyCellList.get(0).getCellNo()));
        }
        
        log.info("COMFORT EXCHANGE GET EMPTY INFO! start.conformWay, num is {}", cabinetModel.getNum());
        
        // 舒适匹配规则
        if (CollUtil.isNotEmpty(openDoorEmptyCellList)) {
            // 开门的格挡
            log.info("COMFORT EXCHANGE GET EMPTY INFO! allot open door empty cell, openDoorEmptyCellList is {}", JsonUtil.toJson(openDoorEmptyCellList));
            return Pair.of(true, Integer.valueOf(getConformationCell(openDoorEmptyCellList, cellConfig).getRight()));
        } else {
            return Pair.of(true, Integer.valueOf(getConformationCell(emptyCellBoxList, cellConfig).getRight()));
        }
    }
    
    /**
     * 根据配置选择合适的机柜单元
     *
     * @param cabinetBoxList 机柜列表，包含各个机柜单元的信息
     * @param cellConfig     机柜单元选择配置，包含待检查和选择的机柜单元标识
     * @return 返回一个包含选择结果和机柜单元编号的Pair对象 结果为true表示找到合适的机柜单元，false表示未找到
     */
    private static Pair<Boolean, String> getConformationCell(List<ElectricityCabinetBox> cabinetBoxList, ElectricityCabinetChooseCellConfig cellConfig) {
        // 中间
        Pair<Boolean, String> middleCellBoxPair = getPositionEmptyCell(cabinetBoxList, cellConfig.getMiddleCell());
        if (middleCellBoxPair.getLeft()) {
            log.info("COMFORT EXCHANGE GET EMPTY INFO! getConformationCell, middleCell is {} , cell is {}", cellConfig.getMiddleCell(), middleCellBoxPair.getRight());
            return middleCellBoxPair;
        }
        // 下面
        Pair<Boolean, String> belowCellBoxPair = getPositionEmptyCell(cabinetBoxList, cellConfig.getBelowCell());
        if (belowCellBoxPair.getLeft()) {
            log.info("COMFORT EXCHANGE GET EMPTY INFO! getConformationCell, belowCell is {} , cell is {}", cellConfig.getBelowCell(), belowCellBoxPair.getRight());
            return belowCellBoxPair;
        }
        
        log.info("COMFORT EXCHANGE GET EMPTY INFO! getConformationCell.randomGetCell");
        return Pair.of(true, cabinetBoxList.get(ThreadLocalRandom.current().nextInt(cabinetBoxList.size())).getCellNo());
    }
    
    
    private static Pair<Boolean, String> getPositionEmptyCell(List<ElectricityCabinetBox> comfortExchangeBox, String positionCell) {
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
