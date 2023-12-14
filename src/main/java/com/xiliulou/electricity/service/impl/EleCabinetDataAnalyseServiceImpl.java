package com.xiliulou.electricity.service.impl;

import cn.hutool.core.util.StrUtil;
import com.xiliulou.core.thread.XllThreadPoolExecutorService;
import com.xiliulou.core.thread.XllThreadPoolExecutors;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.query.ElectricityCabinetQuery;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.DateUtils;
import com.xiliulou.electricity.vo.EleCabinetDataAnalyseVO;
import com.xiliulou.electricity.vo.EleCabinetOrderAnalyseVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
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
    @Autowired
    private ElectricityCabinetPowerService eleCabinetPowerService;
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


    @Override
    public List<EleCabinetDataAnalyseVO> selectOfflineByPage(ElectricityCabinetQuery cabinetQuery) {
        List<EleCabinetDataAnalyseVO> electricityCabinetList = eleCabinetService.selecteleCabinetVOByQuery(cabinetQuery);
        if (CollectionUtils.isEmpty(electricityCabinetList)) {
            return Collections.emptyList();
        }

        return buildEleCabinetDataAnalyseVOs(electricityCabinetList);
    }

    @Override
    public List<EleCabinetDataAnalyseVO> selectLockPage(ElectricityCabinetQuery cabinetQuery) {
        List<EleCabinetDataAnalyseVO> electricityCabinetList = eleCabinetService.selectLockCellByQuery(cabinetQuery);
        if (CollectionUtils.isEmpty(electricityCabinetList)) {
            return Collections.emptyList();
        }

        return buildEleCabinetDataAnalyseVOs(electricityCabinetList);
    }

    @Override
    public List<EleCabinetDataAnalyseVO> selectPowerPage(ElectricityCabinetQuery cabinetQuery) {
        List<EleCabinetDataAnalyseVO> electricityCabinetList = eleCabinetService.selectPowerPage(cabinetQuery);
        if (CollectionUtils.isEmpty(electricityCabinetList)) {
            return Collections.emptyList();
        }

        return buildEleCabinetDataAnalyseVOs(electricityCabinetList.stream().sorted(Comparator.comparing(EleCabinetDataAnalyseVO::getId)).collect(Collectors.toList()));
    }

    @Override
    public Integer selectOfflinePageCount(ElectricityCabinetQuery cabinetQuery) {
        return eleCabinetService.selectOfflinePageCount(cabinetQuery);
    }

    @Override
    public Integer selectLockPageCount(ElectricityCabinetQuery cabinetQuery) {
        return eleCabinetService.selectLockPageCount(cabinetQuery);
    }

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
        List<ElectricityCabinetOrder> electricityCabinetOrders = eleCabinetOrderService.selectMonthExchangeOrders(electricityCabinet.getId(), DateUtils.get30AgoStartTime(), System.currentTimeMillis(), electricityCabinet.getTenantId());
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
        List<ElectricityCabinetOrder> electricityCabinetOrders = eleCabinetOrderService.selectTodayExchangeOrder(electricityCabinet.getId(), DateUtils.getTodayStartTimeStamp(), DateUtils.getTodayEndTimeStamp(), electricityCabinet.getTenantId());
        if (CollectionUtils.isEmpty(electricityCabinetOrders)) {
            return result;
        }

        //今日换电数量
        result.setExchangeNumber(electricityCabinetOrders.size());

        //今日活跃度
        result.setPeopleNumber((int) electricityCabinetOrders.stream().map(ElectricityCabinetOrder::getUid).distinct().count());
        
        eleCabinetService.handleElectricityCabinetStatistic("1");
        return result;
    }

    private List<EleCabinetDataAnalyseVO> buildEleCabinetDataAnalyseVOs(List<EleCabinetDataAnalyseVO> electricityCabinetList) {
        CompletableFuture<Void> acquireBasicInfo = CompletableFuture.runAsync(() -> electricityCabinetList.forEach(item -> {

            ElectricityCabinetModel cabinetModel = eleCabinetModelService.queryByIdFromCache(item.getModelId());
            item.setModelName(Objects.nonNull(cabinetModel) ? cabinetModel.getName() : "");

            EleCabinetCoreData eleCabinetCoreData = eleCabinetCoreDataService.selectByEid(item.getId());
            item.setTemp(Objects.nonNull(eleCabinetCoreData) ? eleCabinetCoreData.getTemp() : 0);

            ElectricityCabinetServer eleCabinetServer = eleCabinetServerService.selectByEid(item.getId());
            item.setServerEndTime(Objects.nonNull(eleCabinetServer) ? eleCabinetServer.getServerEndTime() : System.currentTimeMillis());

            ElectricityCabinetPower eleCabinetPower = eleCabinetPowerService.selectLatestByEid(item.getId());
            item.setPowerConsumption(Objects.nonNull(eleCabinetPower) ? eleCabinetPower.getSumPower() : 0);

            Store store = storeService.queryByIdFromCache(item.getStoreId());
            item.setStoreName(Objects.nonNull(store) ? store.getName() : "");

            Franchisee franchisee = franchiseeService.queryByIdFromCache(Objects.nonNull(store) ? store.getFranchiseeId() : 0);
            item.setFranchiseeName(Objects.nonNull(franchisee) ? franchisee.getName() : "");

        }), DATA_ANALYSE_THREAD_POOL).exceptionally(e -> {
            log.error("ELE ERROR! acquire eleCabinet basic info fail", e);
            return null;
        });

        CompletableFuture<Void> acquireCellInfo = CompletableFuture.runAsync(() -> electricityCabinetList.forEach(item -> {

            Double fullyCharged = item.getFullyCharged();

            List<ElectricityCabinetBox> cabinetBoxList = electricityCabinetBoxService.queryAllBoxByElectricityCabinetId(item.getId());
            if (CollectionUtils.isEmpty(cabinetBoxList)) {
                return;
            }

            long exchangeableNumber = cabinetBoxList.stream().filter(e -> StringUtils.isNotBlank(e.getSn()) && !StrUtil.contains(e.getSn(), "UNKNOW") && eleCabinetService.isExchangeable(e, fullyCharged)).count();

            long fullBatteryNumber = cabinetBoxList.stream().filter(e -> StringUtils.isNotBlank(e.getSn()) && !StrUtil.contains(e.getSn(), "UNKNOW") && Objects.nonNull(e.getPower()) && Objects.equals(e.getPower().intValue(), 100)).count();

            long emptyCellNumber = cabinetBoxList.stream().filter(e -> eleCabinetService.isNoElectricityBattery(e) && Objects.equals(e.getUsableStatus(), ElectricityCabinetBox.ELECTRICITY_CABINET_BOX_USABLE)).count();

            long disableCellNumber = cabinetBoxList.stream().filter(e -> Objects.equals(e.getUsableStatus(), ElectricityCabinetBox.ELECTRICITY_CABINET_BOX_UN_USABLE)).count();

            long openFanNumber = cabinetBoxList.stream().filter(e -> !Objects.equals(e.getIsFan(), ElectricityCabinetBox.OPEN_FAN)).count();

            long chargeCellNumber = cabinetBoxList.stream().filter(e -> StringUtils.isNotBlank(e.getSn())).map(t -> electricityBatteryService.queryBySnFromDb(t.getSn())).filter(battery -> Objects.nonNull(battery) && (Objects.equals(battery.getChargeStatus(), ElectricityBattery.CHARGE_STATUS_STARTING) || Objects.equals(battery.getChargeStatus(), ElectricityBattery.CHARGE_STATUS_CHARGING))).count();

            item.setFullBatteryNumber((int) fullBatteryNumber);
            item.setChargeBatteryNumber((int) chargeCellNumber);
            item.setEmptyCellNumber((int) emptyCellNumber);
            item.setDisableCellNumber((int) disableCellNumber);
            item.setFanOpenNumber((int) openFanNumber);
            item.setExchangeBatteryNumber((int) exchangeableNumber);
        }), DATA_ANALYSE_THREAD_POOL).exceptionally(e -> {
            log.error("ELE ERROR! acquire eleCabinet cell info fail", e);
            return null;
        });

        CompletableFuture<Void> future = CompletableFuture.allOf(acquireBasicInfo, acquireCellInfo);

        try {
            future.get(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("ELE ERROR! acquire result fail", e);
        }

        return electricityCabinetList;
    }
}
