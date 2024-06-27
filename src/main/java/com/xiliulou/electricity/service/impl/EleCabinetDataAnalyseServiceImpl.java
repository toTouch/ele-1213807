package com.xiliulou.electricity.service.impl;

import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Maps;
import com.xiliulou.core.thread.XllThreadPoolExecutorService;
import com.xiliulou.core.thread.XllThreadPoolExecutors;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.EleCabinetConstant;
import com.xiliulou.electricity.constant.ElectricityCabinetDataAnalyseConstant;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.entity.EleCabinetCoreData;
import com.xiliulou.electricity.entity.ElePower;
import com.xiliulou.electricity.entity.ElectricityBattery;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.ElectricityCabinetBox;
import com.xiliulou.electricity.entity.ElectricityCabinetModel;
import com.xiliulou.electricity.entity.ElectricityCabinetOrder;
import com.xiliulou.electricity.entity.ElectricityCabinetServer;
import com.xiliulou.electricity.entity.ElectricityConfig;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.entity.Store;
import com.xiliulou.electricity.entity.merchant.MerchantArea;
import com.xiliulou.electricity.query.ElectricityCabinetQuery;
import com.xiliulou.electricity.request.merchant.MerchantAreaRequest;
import com.xiliulou.electricity.service.EleCabinetCoreDataService;
import com.xiliulou.electricity.service.EleCabinetDataAnalyseService;
import com.xiliulou.electricity.service.ElePowerService;
import com.xiliulou.electricity.service.EleWarnMsgService;
import com.xiliulou.electricity.service.ElectricityBatteryService;
import com.xiliulou.electricity.service.ElectricityCabinetBoxService;
import com.xiliulou.electricity.service.ElectricityCabinetModelService;
import com.xiliulou.electricity.service.ElectricityCabinetOrderService;
import com.xiliulou.electricity.service.ElectricityCabinetServerService;
import com.xiliulou.electricity.service.ElectricityCabinetService;
import com.xiliulou.electricity.service.ElectricityConfigService;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.StoreService;
import com.xiliulou.electricity.service.merchant.MerchantAreaService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.DateUtils;
import com.xiliulou.electricity.vo.EleCabinetDataAnalyseVO;
import com.xiliulou.electricity.vo.EleCabinetOrderAnalyseVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-04-18-9:12
 */
@Service("eleCabinetDataAnalyseService")
@Slf4j
public class EleCabinetDataAnalyseServiceImpl implements EleCabinetDataAnalyseService {
    
    private static final XllThreadPoolExecutorService DATA_ANALYSE_THREAD_POOL = XllThreadPoolExecutors.newFixedThreadPool("DATA-ANALYSE-THREAD-POOL", 4, "dataAnalyseThread:");
    
    @Autowired
    private ElectricityCabinetService eleCabinetService;
    
    @Autowired
    private ElectricityCabinetModelService eleCabinetModelService;
    
    @Autowired
    private ElectricityCabinetServerService eleCabinetServerService;
    
    @Autowired
    private EleCabinetCoreDataService eleCabinetCoreDataService;
    
    @Autowired
    private FranchiseeService franchiseeService;
    
    @Resource
    private ElePowerService elePowerService;
    
    @Autowired
    private ElectricityBatteryService electricityBatteryService;
    
    @Autowired
    private ElectricityCabinetBoxService electricityCabinetBoxService;
    
    @Autowired
    private ElectricityCabinetOrderService eleCabinetOrderService;
    
    @Autowired
    private StoreService storeService;
    
    @Autowired
    private EleWarnMsgService eleWarnMsgService;
    
    @Resource
    private MerchantAreaService merchantAreaService;
    

    @Resource
    private ElectricityConfigService electricityConfigService;
    
    
    @Override
    public List<EleCabinetDataAnalyseVO> selectOfflineByPage(ElectricityCabinetQuery cabinetQuery) {
        List<EleCabinetDataAnalyseVO> electricityCabinetList = eleCabinetService.selecteleCabinetVOByQuery(cabinetQuery);
        if (CollectionUtils.isEmpty(electricityCabinetList)) {
            return Collections.emptyList();
        }
        
        return buildEleCabinetDataAnalyseVOs(electricityCabinetList, cabinetQuery);
    }
    
    @Override
    public List<EleCabinetDataAnalyseVO> selectLockPage(ElectricityCabinetQuery cabinetQuery) {
        List<EleCabinetDataAnalyseVO> electricityCabinetList = eleCabinetService.selectLockCellByQuery(cabinetQuery);
        if (CollectionUtils.isEmpty(electricityCabinetList)) {
            return Collections.emptyList();
        }
        
        return buildEleCabinetDataAnalyseVOs(electricityCabinetList, cabinetQuery);
    }
    
    @Slave
    @Override
    public List<EleCabinetDataAnalyseVO> selectLowPowerPage(ElectricityCabinetQuery cabinetQuery) {
        ElectricityConfig electricityConfig = electricityConfigService.queryFromCacheByTenantId(cabinetQuery.getTenantId());
    
        if (Objects.nonNull(electricityConfig)) {
            // 统一配置
            if (Objects.equals(electricityConfig.getChargeRateType(), ElectricityConfig.CHARGE_RATE_TYPE_UNIFY)) {
                BigDecimal lowChargeRate = electricityConfig.getLowChargeRate();
                cabinetQuery.setLowChargeRate(Objects.isNull(lowChargeRate) ? NumberConstant.TWENTY_FIVE_D : lowChargeRate.doubleValue());
            } else {
                // 单个配置
                cabinetQuery.setBatteryCountType(EleCabinetConstant.BATTERY_COUNT_TYPE_LESS);
            }
        }
        
        return this.selectPowerPage(cabinetQuery);
    }
    
    @Slave
    @Override
    public Integer selectLowPowerPageCount(ElectricityCabinetQuery cabinetQuery) {
        ElectricityConfig electricityConfig = electricityConfigService.queryFromCacheByTenantId(cabinetQuery.getTenantId());
        
        if (Objects.nonNull(electricityConfig)) {
            // 统一配置
            if (Objects.equals(electricityConfig.getChargeRateType(), ElectricityConfig.CHARGE_RATE_TYPE_UNIFY)) {
                BigDecimal lowChargeRate = electricityConfig.getLowChargeRate();
                cabinetQuery.setLowChargeRate(Objects.isNull(lowChargeRate) ? NumberConstant.TWENTY_FIVE_D : lowChargeRate.doubleValue());
            } else {
                // 单个配置
                cabinetQuery.setBatteryCountType(EleCabinetConstant.BATTERY_COUNT_TYPE_LESS);
            }
        }
        
        return this.selectPowerPageCount(cabinetQuery);
    }
    
    @Slave
    @Override
    public List<EleCabinetDataAnalyseVO> selectFullPowerPage(ElectricityCabinetQuery cabinetQuery) {
        ElectricityConfig electricityConfig = electricityConfigService.queryFromCacheByTenantId(cabinetQuery.getTenantId());
    
        if (Objects.nonNull(electricityConfig)) {
            // 统一配置
            if (Objects.equals(electricityConfig.getChargeRateType(), ElectricityConfig.CHARGE_RATE_TYPE_UNIFY)) {
                BigDecimal fullChargeRate = electricityConfig.getFullChargeRate();
                cabinetQuery.setFullChargeRate(Objects.isNull(fullChargeRate) ? NumberConstant.SEVENTY_FIVE_D : fullChargeRate.doubleValue());
            } else {
                // 单个配置
                cabinetQuery.setBatteryCountType(EleCabinetConstant.BATTERY_COUNT_TYPE_MORE);
            }
        }
    
        return this.selectPowerPage(cabinetQuery);
    }
    
    @Slave
    @Override
    public Integer selectFullPowerPageCount(ElectricityCabinetQuery cabinetQuery) {
        ElectricityConfig electricityConfig = electricityConfigService.queryFromCacheByTenantId(cabinetQuery.getTenantId());
    
        if (Objects.nonNull(electricityConfig)) {
            // 统一配置
            if (Objects.equals(electricityConfig.getChargeRateType(), ElectricityConfig.CHARGE_RATE_TYPE_UNIFY)) {
                BigDecimal fullChargeRate = electricityConfig.getFullChargeRate();
                cabinetQuery.setFullChargeRate(Objects.isNull(fullChargeRate) ? NumberConstant.SEVENTY_FIVE_D : fullChargeRate.doubleValue());
            } else {
                // 单个配置
                cabinetQuery.setBatteryCountType(EleCabinetConstant.BATTERY_COUNT_TYPE_MORE);
            }
        }
        
        return this.selectPowerPageCount(cabinetQuery);
    }
    
    @Override
    public List<EleCabinetDataAnalyseVO> selectPowerPage(ElectricityCabinetQuery cabinetQuery) {
        List<EleCabinetDataAnalyseVO> electricityCabinetList = eleCabinetService.selectPowerPage(cabinetQuery);
        if (CollectionUtils.isEmpty(electricityCabinetList)) {
            return Collections.emptyList();
        }
        
        return buildEleCabinetDataAnalyseVOs(electricityCabinetList, cabinetQuery);
    }
    
    @Override
    public Integer selectOfflinePageCount(ElectricityCabinetQuery cabinetQuery) {
        return eleCabinetService.selectOfflinePageCount(cabinetQuery);
    }
    
    @Override
    public Integer selectLockPageCount(ElectricityCabinetQuery cabinetQuery) {
        return eleCabinetService.selectLockPageCount(cabinetQuery);
    }
    
    @Slave
    @Override
    public Integer selectPowerPageCount(ElectricityCabinetQuery cabinetQuery) {
        return eleCabinetService.selectPowerPageCount(cabinetQuery);
    }
    
    @Override
    public EleCabinetOrderAnalyseVO averageStatistics(Integer eid) {
        EleCabinetOrderAnalyseVO result = new EleCabinetOrderAnalyseVO();
        
        ElectricityCabinet electricityCabinet = eleCabinetService.queryByIdFromCache(eid);
        if (Objects.isNull(electricityCabinet) || !Objects.equals(electricityCabinet.getTenantId(), TenantContextHolder.getTenantId())) {
            return result;
        }
        
        //获取本月订单
        List<ElectricityCabinetOrder> electricityCabinetOrders = eleCabinetOrderService.selectMonthExchangeOrders(electricityCabinet.getId(), DateUtils.get30AgoStartTime(),
                System.currentTimeMillis(), electricityCabinet.getTenantId());
        if (CollectionUtils.isEmpty(electricityCabinetOrders)) {
            return result;
        }
        
        //日均换电次数
        result.setAverageExchangeNumber(BigDecimal.valueOf(electricityCabinetOrders.size()).divide(BigDecimal.valueOf(30), 2, RoundingMode.HALF_UP).doubleValue());
        
        //本月换电总人数
        long peopleNumber = electricityCabinetOrders.stream().map(ElectricityCabinetOrder::getUid).distinct().count();
        
        //日均活跃度
        result.setAveragePeopleNumber(BigDecimal.valueOf(peopleNumber).divide(BigDecimal.valueOf(30), 2, RoundingMode.HALF_UP).doubleValue());
        
        return result;
    }
    
    @Override
    public EleCabinetOrderAnalyseVO todayStatistics(Integer eid) {
        EleCabinetOrderAnalyseVO result = new EleCabinetOrderAnalyseVO();
        
        ElectricityCabinet electricityCabinet = eleCabinetService.queryByIdFromCache(eid);
        if (Objects.isNull(electricityCabinet) || !Objects.equals(electricityCabinet.getTenantId(), TenantContextHolder.getTenantId())) {
            return result;
        }
        
        //今日换电订单
        List<ElectricityCabinetOrder> electricityCabinetOrders = eleCabinetOrderService.selectTodayExchangeOrder(electricityCabinet.getId(), DateUtils.getTodayStartTimeStamp(),
                DateUtils.getTodayEndTimeStamp(), electricityCabinet.getTenantId());
        if (CollectionUtils.isEmpty(electricityCabinetOrders)) {
            return result;
        }
        
        //今日换电数量
        result.setExchangeNumber(electricityCabinetOrders.size());
        
        //今日活跃度
        result.setPeopleNumber((int) electricityCabinetOrders.stream().map(ElectricityCabinetOrder::getUid).distinct().count());
        
        return result;
    }
    
    private List<EleCabinetDataAnalyseVO> buildEleCabinetDataAnalyseVOs(List<EleCabinetDataAnalyseVO> electricityCabinetList, ElectricityCabinetQuery cabinetQuery) {
        List<Integer> electricityCabinetIdList = electricityCabinetList.stream().map(EleCabinetDataAnalyseVO::getId).collect(Collectors.toList());
    
        //柜机核心板数据
        List<EleCabinetCoreData> eleCabinetCoreDataList=eleCabinetCoreDataService.listCabinetCoreDataByEids(electricityCabinetIdList);
        Map<Long, EleCabinetCoreData> eleCabinetCoreDataMap =Maps.newHashMap();
        if(CollectionUtils.isNotEmpty(eleCabinetCoreDataList)){
            eleCabinetCoreDataMap = eleCabinetCoreDataList.stream().collect(Collectors.toMap(EleCabinetCoreData::getElectricityCabinetId, Function.identity(), (k1, k2) -> k2));
        }
        
        //柜机服务时间
        List<ElectricityCabinetServer> eleCabinetServerList=eleCabinetServerService.listCabinetServerByEids(electricityCabinetIdList);
        Map<Integer, ElectricityCabinetServer> eleCabinetServerMap =Maps.newHashMap();
        if(CollectionUtils.isNotEmpty(eleCabinetServerList)){
            eleCabinetServerMap=eleCabinetServerList.stream().collect(Collectors.toMap(ElectricityCabinetServer::getElectricityCabinetId, Function.identity(), (k1, k2) -> k2));
        }
        
        //柜机电表读数
        List<ElePower> eleCabinetPowerList=elePowerService.listCabinetPowerByEids(electricityCabinetIdList);
        Map<Long, ElePower> eleCabinetPowerMap =Maps.newHashMap();
        if(CollectionUtils.isNotEmpty(eleCabinetPowerList)){
            eleCabinetPowerMap=eleCabinetPowerList.stream().collect(Collectors.toMap(ElePower::getEid, Function.identity(), (k1, k2) -> k2));
        }
    
        //充电中的电池
        List<ElectricityBattery> batteryList = electricityBatteryService.listBatteryByEid(electricityCabinetIdList);
        Map<Integer, Long> chargeBatteryMap = Maps.newHashMap();
        if (CollectionUtils.isNotEmpty(batteryList)) {
            chargeBatteryMap = batteryList.stream()
                    .filter(battery -> Objects.nonNull(battery.getElectricityCabinetId()) && (Objects.equals(battery.getChargeStatus(), ElectricityBattery.CHARGE_STATUS_STARTING)
                            || Objects.equals(battery.getChargeStatus(), ElectricityBattery.CHARGE_STATUS_CHARGING)))
                    .collect(Collectors.groupingBy(ElectricityBattery::getElectricityCabinetId, Collectors.counting()));
        }
    
        for (EleCabinetDataAnalyseVO item : electricityCabinetList) {
            //电柜温度
            EleCabinetCoreData eleCabinetCoreData =eleCabinetCoreDataMap.get(item.getId().longValue());
            if(Objects.nonNull(eleCabinetCoreData) && Objects.nonNull(eleCabinetCoreData.getTemp())){
                item.setTemp(eleCabinetCoreData.getTemp());
            }else{
                item.setTemp(0);
            }
            
            //电柜服务时间
            ElectricityCabinetServer eleCabinetServer =eleCabinetServerMap.get(item.getId());
            if(Objects.nonNull(eleCabinetServer) && Objects.nonNull(eleCabinetServer.getServerEndTime())){
                item.setServerEndTime(eleCabinetServer.getServerEndTime());
            }else{
                item.setServerEndTime(System.currentTimeMillis());
            }
            
            //电表读数
            ElePower elePower =eleCabinetPowerMap.get(item.getId().longValue());
            if(Objects.nonNull(elePower) && Objects.nonNull(elePower.getSumPower())){
                item.setPowerConsumption(elePower.getSumPower());
            }else{
                item.setPowerConsumption(0D);
            }
            
            //充电电池数
            Long chargeBatteryNumber = chargeBatteryMap.get(item.getId());
            if(Objects.nonNull(chargeBatteryNumber)){
                item.setChargeBatteryNumber(Math.toIntExact(chargeBatteryNumber));
            }else{
                item.setChargeBatteryNumber(NumberConstant.ZERO);
            }
        }
        
        CompletableFuture<Void> acquireBasicInfo = CompletableFuture.runAsync(() -> electricityCabinetList.forEach(item -> {
            ElectricityCabinetModel cabinetModel = eleCabinetModelService.queryByIdFromCache(item.getModelId());
            item.setModelName(Objects.nonNull(cabinetModel) ? cabinetModel.getName() : "");
            
            Store store = storeService.queryByIdFromCache(item.getStoreId());
            item.setStoreName(Objects.nonNull(store) ? store.getName() : "");
            
            Franchisee franchisee = franchiseeService.queryByIdFromCache(Objects.nonNull(store) ? store.getFranchiseeId() : 0);
            item.setFranchiseeName(Objects.nonNull(franchisee) ? franchisee.getName() : "");
            
        }), DATA_ANALYSE_THREAD_POOL).exceptionally(e -> {
            log.error("ELE ERROR! acquire eleCabinet basic info fail", e);
            return null;
        });
        
        //将格挡根据柜机id分组
        List<ElectricityCabinetBox> eleCabinetBoxList = electricityCabinetBoxService.listCabineBoxByEids(electricityCabinetIdList);
        Map<Integer, List<ElectricityCabinetBox>> cabinetBoxMap=Maps.newHashMap();
        if(CollectionUtils.isNotEmpty(eleCabinetBoxList)){
            cabinetBoxMap = eleCabinetBoxList.stream().collect(Collectors.groupingBy(ElectricityCabinetBox::getElectricityCabinetId));
        }
    
        Map<Integer, List<ElectricityCabinetBox>> finalCabinetBoxMap = cabinetBoxMap;
        CompletableFuture<Void> acquireCellInfo = CompletableFuture.runAsync(() -> electricityCabinetList.forEach(item -> {
            Double fullyCharged = item.getFullyCharged();
            
            List<ElectricityCabinetBox> cabinetBoxList = finalCabinetBoxMap.get(item.getId());
            if (CollectionUtils.isEmpty(cabinetBoxList)) {
                return;
            }
            
            long exchangeableNumber = cabinetBoxList.stream()
                    .filter(e -> StringUtils.isNotBlank(e.getSn()) && !StrUtil.contains(e.getSn(), "UNKNOW") && eleCabinetService.isExchangeable(e, fullyCharged)).count();
            
            long fullBatteryNumber = cabinetBoxList.stream()
                    .filter(e -> StringUtils.isNotBlank(e.getSn()) && !StrUtil.contains(e.getSn(), "UNKNOW") && Objects.nonNull(e.getPower()) && Objects.equals(
                            e.getPower().intValue(), 100)).count();
            
            long emptyCellNumber = cabinetBoxList.stream()
                    .filter(e -> eleCabinetService.isNoElectricityBattery(e) && Objects.equals(e.getUsableStatus(), ElectricityCabinetBox.ELECTRICITY_CABINET_BOX_USABLE)).count();
            
            long disableCellNumber = cabinetBoxList.stream().filter(e -> Objects.equals(e.getUsableStatus(), ElectricityCabinetBox.ELECTRICITY_CABINET_BOX_UN_USABLE)).count();
            
            long openFanNumber = cabinetBoxList.stream().filter(e -> !Objects.equals(e.getIsFan(), ElectricityCabinetBox.OPEN_FAN)).count();
            
            item.setFullBatteryNumber((int) fullBatteryNumber);
            item.setEmptyCellNumber((int) emptyCellNumber);
            item.setDisableCellNumber((int) disableCellNumber);
            item.setFanOpenNumber((int) openFanNumber);
            item.setExchangeBatteryNumber((int) exchangeableNumber);
        }), DATA_ANALYSE_THREAD_POOL).exceptionally(e -> {
            log.error("ELE ERROR! acquire eleCabinet cell info fail", e);
            return null;
        });
    
        // 查询区域
        List<Long> areaIdList = electricityCabinetList.stream().map(EleCabinetDataAnalyseVO::getAreaId).filter(Objects::nonNull).distinct().collect(Collectors.toList());
        MerchantAreaRequest areaQuery = MerchantAreaRequest.builder().idList(areaIdList).build();
        List<MerchantArea> merchantAreaList = merchantAreaService.queryList(areaQuery);
        Map<Long, String> areaNameMap = Maps.newHashMap();
        if (!CollectionUtils.isEmpty(merchantAreaList)) {
            areaNameMap = merchantAreaList.stream().collect(Collectors.toMap(MerchantArea::getId, MerchantArea::getName, (item1, item2) -> item2));
        }
    
        Map<Long, String> finalAreaNameMap = areaNameMap;
        CompletableFuture<Void> areaInfo = CompletableFuture.runAsync(() -> electricityCabinetList.forEach(item -> {
            if (finalAreaNameMap.containsKey(item.getAreaId())) {
                item.setAreaName(finalAreaNameMap.get(item.getAreaId()));
            }
        }), DATA_ANALYSE_THREAD_POOL).exceptionally(e -> {
            log.error("ELE ERROR! acquire eleCabinet cell info fail", e);
            return null;
        });

        CompletableFuture<Void> future = CompletableFuture.allOf(acquireBasicInfo, acquireCellInfo, areaInfo);

        try {
            future.get(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("ELE ERROR! acquire result fail", e);
        }
        
        // 排序
        if (Objects.equals(cabinetQuery.getOrderByAverageNumber(), ElectricityCabinetDataAnalyseConstant.ORDER_BY_AVERAGE_NUMBER_DESC)) {
            return electricityCabinetList.stream().sorted(Comparator.comparing(EleCabinetDataAnalyseVO::getAverageNumber).reversed()).collect(Collectors.toList());
        } else if (Objects.equals(cabinetQuery.getOrderByAverageNumber(), ElectricityCabinetDataAnalyseConstant.ORDER_BY_AVERAGE_NUMBER_ASC)) {
            return electricityCabinetList.stream().sorted(Comparator.comparing(EleCabinetDataAnalyseVO::getAverageNumber)).collect(Collectors.toList());
        } else if (Objects.equals(cabinetQuery.getOrderByAverageActivity(), ElectricityCabinetDataAnalyseConstant.ORDER_BY_AVERAGE_ACTIVITY_DESC)) {
            return electricityCabinetList.stream().sorted(Comparator.comparing(EleCabinetDataAnalyseVO::getAverageActivity).reversed()).collect(Collectors.toList());
        } else if (Objects.equals(cabinetQuery.getOrderByAverageActivity(), ElectricityCabinetDataAnalyseConstant.ORDER_BY_AVERAGE_ACTIVITY_ASC)) {
            return electricityCabinetList.stream().sorted(Comparator.comparing(EleCabinetDataAnalyseVO::getAverageActivity)).collect(Collectors.toList());
        } else if (Objects.equals(cabinetQuery.getOrderByTodayNumber(), ElectricityCabinetDataAnalyseConstant.ORDER_BY_TODAY_NUMBER_DESC)) {
            return electricityCabinetList.stream().sorted(Comparator.comparing(EleCabinetDataAnalyseVO::getTodayNumber).reversed()).collect(Collectors.toList());
        } else if (Objects.equals(cabinetQuery.getOrderByTodayNumber(), ElectricityCabinetDataAnalyseConstant.ORDER_BY_TODAY_NUMBER_ASC)) {
            return electricityCabinetList.stream().sorted(Comparator.comparing(EleCabinetDataAnalyseVO::getTodayNumber)).collect(Collectors.toList());
        } else if (Objects.equals(cabinetQuery.getOrderByTodayActivity(), ElectricityCabinetDataAnalyseConstant.ORDER_BY_TODAY_ACTIVITY_DESC)) {
            return electricityCabinetList.stream().sorted(Comparator.comparing(EleCabinetDataAnalyseVO::getTodayActivity).reversed()).collect(Collectors.toList());
        } else if (Objects.equals(cabinetQuery.getOrderByTodayActivity(), ElectricityCabinetDataAnalyseConstant.ORDER_BY_TODAY_ACTIVITY_ASC)) {
            return electricityCabinetList.stream().sorted(Comparator.comparing(EleCabinetDataAnalyseVO::getTodayActivity)).collect(Collectors.toList());
        } else {
            return electricityCabinetList.stream().sorted(Comparator.comparing(EleCabinetDataAnalyseVO::getAverageNumber).reversed()).collect(Collectors.toList());
        }
    }
}
