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
            log.warn("COMFORT EXCHANGE WARN! comfortExchangeGetFullCell.uid is null");
            return Pair.of(false, null);
        }
        
        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
        if (Objects.isNull(userInfo)) {
            log.warn("COMFORT EXCHANGE WARN! comfortExchangeGetFullCell.userInfo is null, uid is {}", uid);
            return Pair.of(false, null);
        }
        
        if (CollUtil.isEmpty(usableBoxes)) {
            log.warn("COMFORT EXCHANGE WARN! comfortExchangeGetFullCell.usableBoxes is null, uid is {}", uid);
            return Pair.of(false, null);
        }
        
        ElectricityConfig electricityConfig = electricityConfigService.queryFromCacheByTenantId(userInfo.getTenantId());
        if (Objects.isNull(electricityConfig) || Objects.equals(electricityConfig.getIsComfortExchange(), ElectricityConfig.NOT_COMFORT_EXCHANGE)) {
            log.info("COMFORT EXCHANGE INFO! comfortExchangeGetFullCell.electricityConfig is null, tenantId is {}", userInfo.getTenantId());
            return Pair.of(false, null);
        }
        log.info("COMFORT EXCHANGE INFO! comfortExchangeGetFullCell.electricityConfig is {}", JsonUtil.toJson(electricityConfig));
        
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
        
        return getConformationCell(comfortExchangeBox, cellConfig);
    }
    
    
    @Override
    public Pair<Boolean, Integer> comfortExchangeGetEmptyCell(Long uid, List<ElectricityCabinetBox> emptyCellBoxList) {
        if (Objects.isNull(uid)) {
            log.warn("COMFORT EXCHANGE WARN! comfortExchangeGetEmptyCell.uid is null");
            return Pair.of(false, null);
        }
        
        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
        if (Objects.isNull(userInfo)) {
            log.warn("COMFORT EXCHANGE WARN! comfortExchangeGetEmptyCell.userInfo is null, uid is {}", uid);
            return Pair.of(false, null);
        }
        
        if (CollUtil.isEmpty(emptyCellBoxList)) {
            log.warn("COMFORT EXCHANGE WARN! comfortExchangeGetEmptyCell.usableBoxes is null, uid is {}", uid);
            return Pair.of(false, null);
        }
        
        ElectricityConfig electricityConfig = electricityConfigService.queryFromCacheByTenantId(userInfo.getTenantId());
        if (Objects.isNull(electricityConfig) || Objects.equals(electricityConfig.getIsComfortExchange(), ElectricityConfig.NOT_COMFORT_EXCHANGE)) {
            log.info("COMFORT EXCHANGE INFO! comfortExchangeGetEmptyCell.electricityConfig is null, tenantId is {}", userInfo.getTenantId());
            return Pair.of(false, null);
        }
        log.info("COMFORT EXCHANGE INFO!comfortExchangeGetEmptyCell.electricityConfig is {}", JsonUtil.toJson(electricityConfig));
        
        Integer electricityCabinetId = emptyCellBoxList.get(0).getElectricityCabinetId();
        ElectricityCabinetModel cabinetModel = cabinetModelService.queryByIdFromCache(electricityCabinetId);
        if (Objects.isNull(cabinetModel)) {
            log.warn("COMFORT EXCHANGE WARN! comfortExchangeGetEmptyCell.cabinetModel is null, eid is {}", electricityCabinetId);
            return Pair.of(false, null);
        }
        // 舒适换电
        ElectricityCabinetChooseCellConfig cellConfig = chooseCellConfigService.queryConfigByNumFromCache(cabinetModel.getNum());
        if (Objects.isNull(cellConfig)) {
            log.warn("COMFORT EXCHANGE WARN! comfortExchangeGetEmptyCell.cellConfig is null, eid is {}", cabinetModel.getNum());
            return Pair.of(false, null);
        }
        
        // 优先分配开门的格挡
        List<ElectricityCabinetBox> openDoorEmptyCellList = emptyCellBoxList.stream().filter(item -> Objects.equals(item.getIsLock(), ElectricityCabinetBox.OPEN_DOOR))
                .collect(Collectors.toList());
        if (Objects.equals(openDoorEmptyCellList.size(), 1)) {
            return Pair.of(true, Integer.valueOf(openDoorEmptyCellList.get(0).getCellNo()));
        }
        // 舒适匹配规则
        if (CollUtil.isNotEmpty(openDoorEmptyCellList)) {
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
        Pair<Boolean, String> middleCellBoxPair = getPositionCell(cabinetBoxList, cellConfig.getMiddleCell());
        if (middleCellBoxPair.getLeft()) {
            log.info("COMFORT EXCHANGE INFO! getConformationCell, middleCell is {}", cellConfig.getMiddleCell());
            return middleCellBoxPair;
        }
        // 下面
        Pair<Boolean, String> belowCellBoxPair = getPositionCell(cabinetBoxList, cellConfig.getBelowCell());
        if (belowCellBoxPair.getLeft()) {
            log.info("COMFORT EXCHANGE INFO! getConformationCell, belowCell is {}", cellConfig.getBelowCell());
            return belowCellBoxPair;
        }
        
        log.info("COMFORT EXCHANGE INFO! getConformationCell.randomGetCell");
        return Pair.of(true, cabinetBoxList.get(ThreadLocalRandom.current().nextInt(cabinetBoxList.size())).getCellNo());
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
